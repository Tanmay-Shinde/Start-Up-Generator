package use_case.display_comment.application_business_rules;

import data_access.DisplayCommentDataAccessInterface;
import entity.Comment;
import entity.Post;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayCommentInteractor implements DisplayCommentInputBoundary {
    final DisplayCommentDataAccessInterface displayCommentDataAccessObject;
    final DisplayCommentOutputBoundary displayCommentPresenter;

    public DisplayCommentInteractor(DisplayCommentDataAccessInterface displayCommentDataAccessObject, DisplayCommentOutputBoundary displayCommentOutputBoundary) {
        this.displayCommentDataAccessObject = displayCommentDataAccessObject;
        this.displayCommentPresenter = displayCommentOutputBoundary;
    }

    public void execute(ObjectId id, int config) {
        Post post;
        List<Comment> comments;

        if (config == 0) {
            comments = new ArrayList<>();
            comments.add(displayCommentDataAccessObject.getCommentByCommentID(id));
        } else {
            comments = displayCommentDataAccessObject.getCommentsByParentPostID(id);
        }

        DisplayCommentOutputData outputData = new DisplayCommentOutputData(comments.size());

        if (!comments.isEmpty()) {
            post = displayCommentDataAccessObject.getPostByPostID(comments.get(0).getParentPostId());
            for (Comment comment : comments) {
                outputData.getComments().put(comment.getId(), processComment(post, comment));
            }
            displayCommentPresenter.prepareSuccessView(outputData);
        } else {
            displayCommentPresenter.prepareFailView("No comments found");
        }
    }

    private Map<String, Object> processComment(Post post, Comment comment) {
        Map<String, Object> processedComment = new HashMap<>(8, 1);
        processedComment.put("id", comment.getId());
        processedComment.put("parentPostId", comment.getParentPostId());
        processedComment.put("parentId", comment.getParentId());
        processedComment.put("authorId", comment.getAuthorId());
        processedComment.put("username", displayCommentDataAccessObject.getUserById(comment.getAuthorId()).getUsername());
        processedComment.put("body", comment.getBody());
        processedComment.put("qualifications", comment.getQualifications());

        boolean commentAuthorIsPostAuthor = post.getAuthorID().equals(comment.getAuthorId());
        boolean commentAuthorIsCollaborator = post.getCollaboratorIDs().contains(comment.getAuthorId());
        boolean loggedInUserIsPostAuthor = post.getAuthorID().equals(displayCommentDataAccessObject.getLoggedInUserId());
        boolean loggedInUserIsCollaborator = post.getCollaboratorIDs().contains(displayCommentDataAccessObject.getLoggedInUserId());
        boolean loggedInUserIsCommentAuthor = comment.getAuthorId().equals(displayCommentDataAccessObject.getLoggedInUserId());

        processedComment.put("comment_author_is_post_author", commentAuthorIsPostAuthor);
        processedComment.put("logged_in_user_is_comment_author", loggedInUserIsCommentAuthor);
        processedComment.put("show_more_info_button", (commentAuthorIsPostAuthor || commentAuthorIsCollaborator) && (loggedInUserIsPostAuthor || loggedInUserIsCollaborator));

        return processedComment;
    }
}
