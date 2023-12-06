package app;

import api.GenerativeAIAPI;
import api.MistralCodegenAIAPI;
import data_access.GenerateIdeaDataAccessInterface;
import data_access.IdeaDataFileDataAccessObject;
import data_access.MongoDBDataAccessObject;
import data_access.MongoDBDataAccessObjectBuilder;
import entity.ConcreteIdeaFactory;
import entity.IdeaFactory;
import view.*;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public class Main {
    static final JFrame application = new JFrame("Startup Generator");
    protected static SignupViewModel signupViewModel;
    protected static LoginViewModel loginViewModel;
    protected static GenerateIdeaViewModel generateIdeaViewModel;
    protected static CreatePostViewModel createPostViewModel;
    protected static HomePageViewModel homePageViewModel;

    public static void main(String[] args) {
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();

        JPanel views = new JPanel(cardLayout);
        application.add(views);

        ViewManagerModel viewManagerModel = new ViewManagerModel();
        ViewManager viewManager = new ViewManager(views, cardLayout, viewManagerModel);
        viewManagerModel.addPropertyChangeListener(Main::propertyChange);

        signupViewModel = new SignupViewModel();
        loginViewModel = new LoginViewModel();
        generateIdeaViewModel = new GenerateIdeaViewModel();
        homePageViewModel = new HomePageViewModel();
        createPostViewModel = new CreatePostViewModel();

        MongoDBDataAccessObject mongoDBDataAccessObject;
        try {
            if (args != null && args.length == 5) {
                mongoDBDataAccessObject = new MongoDBDataAccessObjectBuilder()
                        .setDatabaseConnectionPath(args[0])
                        .setDatabaseName(args[1])
                        .setUsersCollectionName(args[2])
                        .setCommentsCollectionName(args[3])
                        .setPostsCollectionName(args[4])
                        .build();
            }
            else {
                mongoDBDataAccessObject = new MongoDBDataAccessObjectBuilder().setStandadParameters().build();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            throw new RuntimeException(e);
        }

        SignupView signupView = SignupUseCaseFactory.create(viewManagerModel, signupViewModel, loginViewModel, mongoDBDataAccessObject);
        views.add(signupView, signupView.viewName);

        LoginView loginView = LoginUseCaseFactory.create(viewManagerModel, signupViewModel, loginViewModel, mongoDBDataAccessObject);
        views.add(loginView, loginView.viewName);

        CreatePostViewModel createPostViewModel = new CreatePostViewModel();
        CreatePostView createPostView = CreatePostUseCaseFactory.create(viewManagerModel,createPostViewModel,mongoDBDataAccessObject);

        GenerativeAIAPI generativeAIAPI = new MistralCodegenAIAPI();
        GenerateIdeaDataAccessInterface generateIdeaDataAccessObject = null;
        IdeaFactory ideaFactory = new ConcreteIdeaFactory();
        try
        {
            generateIdeaDataAccessObject = new IdeaDataFileDataAccessObject("src/main/java/data_access/ideas.csv",ideaFactory);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        GenerateIdeaView generateIdeaView = GenerateIdeaUseCaseFactory.create(viewManagerModel,generateIdeaViewModel,createPostViewModel,generateIdeaDataAccessObject,generativeAIAPI,homePageViewModel,createPostView);
        views.add(generateIdeaView,generateIdeaView.viewName);

        PostAndCommentsViewModel postAndCommentsViewModel = new PostAndCommentsViewModel();
        CreateCommentUseCaseBuilder createCommentUseCaseBuilder = new CreateCommentUseCaseBuilder(postAndCommentsViewModel, mongoDBDataAccessObject);
        PostAndCommentsView postAndCommentsView = DisplayPostUseCaseFactory.create(postAndCommentsViewModel, viewManagerModel, mongoDBDataAccessObject, createCommentUseCaseBuilder);
        viewManager.setupDisplayComments(postAndCommentsViewModel, postAndCommentsView);
        NewWindow newPostAndCommentsWindow = new NewWindow(true, postAndCommentsView.viewName);
        NewWindow newCreateCommentWindow = new NewWindow(false, "Reply");
        NewWindow newCreatePostWindow = new NewWindow(false, "Post");
        viewManager.setupNewWindows(newPostAndCommentsWindow, newCreateCommentWindow, newCreatePostWindow);

        SearchPostViewModel searchPostViewModel = new SearchPostViewModel();
        SearchPostView searchPostView = SearchPostUseCaseFactory.create(viewManagerModel, searchPostViewModel, homePageViewModel, mongoDBDataAccessObject, createPostView);

        views.add(searchPostView, searchPostView.viewName);

        application.pack();
        application.setLocationRelativeTo(null);
        application.setVisible(true);
    }

    public static void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("resize")) {
            if (((String) evt.getNewValue()).equals("main")) {
                application.pack();
            }
        }
    }
}
