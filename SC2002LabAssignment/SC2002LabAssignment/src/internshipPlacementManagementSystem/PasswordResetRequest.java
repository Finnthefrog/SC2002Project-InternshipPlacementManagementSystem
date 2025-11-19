package internshipPlacementManagementSystem;
import java.io.Serializable;
/**
 * to prompt the CareerCenter staff to reset a forgotten password
 */
public class PasswordResetRequest implements Serializable {
    private String email;
    private boolean handled;
/**
 * Constructor for PasswordResetRequest
 * @param email Email account of the requester
 */
    public PasswordResetRequest(String email) {
        this.email = email;
        this.handled = false;
    }
    
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
public boolean isHandled() {
	return handled;
}
public void setHandled(boolean handled) {
	this.handled = handled;
}

    
}