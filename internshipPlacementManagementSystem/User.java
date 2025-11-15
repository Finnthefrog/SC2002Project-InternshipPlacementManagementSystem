package internshipPlacementManagementSystem;

import java.io.Serializable;

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

    public boolean login(String passwordAttempt) {
        return this.password.equals(passwordAttempt);
    }

    public void logout() {
        System.out.println("User " + this.name + " logged out.");
    }

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