package internshipPlacementManagementSystem;

import java.io.Serializable;
/**
 * General Superclass for all Users in the Internship Placement system
 * handles features common to all users such as login/logout and password handling 
 */
public class User implements Serializable{
    private String userID; 
    private String name;
    private String password;
    private String email; 

    public User(String userID, String name, String email) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = "password"; 
    }
/**
 * Method to handle login and password verification 
 * @param passwordAttempt the input password
 * @return true if the password matches the user object
 */
    public boolean login(String passwordAttempt) {
        return this.password.equals(passwordAttempt);
    }
    /**
     * Method to log Current user out of the system
     */
    public void logout() {
        System.out.println("User " + this.name + " logged out.");
    }
/**
 * Method to change User password on request 
 * @param oldPassword The original password to verify changing is permitted
 * @param newPassword The new password to be set
 * @return true if the password is successfully changed
 */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (this.password.equals(oldPassword)) {
            this.password = newPassword;
            System.out.println("Password for " + this.name + " changed successfully.");
            return true;
        } else {
            System.out.println("Incorrect old password.");
            return false;
        }
    }
    
    public void setCompanyreid(int Companyrepid) {this.userID = "CR" + Companyrepid;}
    public String getUserID() { return this.userID; }
    public String getName() { return this.name; }
    public String getEmail() { return this.email; }
    public String getPassword() { return this.password; }
}