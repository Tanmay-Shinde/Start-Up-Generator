package use_case.view_user_info.application_business_rules;

public class ViewUserInfoOutputData {

    private final String name;

    private final String fieldOfExpertise;

    private final double rating;

    private final String city;

    private final String email;

    private final String phoneNumber;

    public ViewUserInfoOutputData(String name, String fieldOfExpertise, double rating, String city, String email, String phoneNumber) {
        this.name = name;
        this.fieldOfExpertise = fieldOfExpertise;
        this.rating = rating;
        this.city = city;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFieldOfExpertise() {
        return fieldOfExpertise;
    }

    public String getCity() {
        return city;
    }
}
