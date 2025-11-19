/* gang wth is this doing in here we are not using it this what is going on in the user subtypes
 * EMPHASIS DELETE THIS 
 * sorry ziyan but we have leo enum implementation the code is nice anyway
 * 
*/
package internshipPlacementManagementSystem;
import java.io.Serializable;
/**
 * Application for the withdrawal of an internship Application
 * Submitted by a student for CareerCenter approval, with a reason given to aide review
 */
import java.time.LocalDateTime;
public class WithdrawalRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	private final String id;
	private final String applicationId;
	private WithdrawalStatus status;
	private final boolean afterConfirmation;
	private final LocalDateTime createdAt;
	/**
	 * Constructor for WithdrawalRequest
	 * @param id the New ID of this request
	 * @param applicationId The ID of the Application being withdrawn from 
	 * @param afterConfirmation Boolean to store whether or not the Student has accepted the offer yet
	 */
	public WithdrawalRequest(String id, String applicationId, boolean afterConfirmation) {
		this.id = id;
		this.applicationId = applicationId;
		this.afterConfirmation = afterConfirmation;
		this.status = WithdrawalStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}
	
	public String getId() {return id;}
	public String getApplicationId() {return applicationId;}
	public WithdrawalStatus getStatus() {return status;}
	public void setStatus(WithdrawalStatus status) {this.status = status;}
	public boolean isAfterConfirmation() {return afterConfirmation;}
	public LocalDateTime getCreatedAt() {return createdAt;}
}package internshipPlacementManagementSystem;
import java.io.Serializable;
import java.time.LocalDateTime;
public class WithdrawalRequest implements Serializable{
	private static final long serialVersionUID = 1L;
	private final String id;
	private final String applicationId;
	private WithdrawalStatus status;
	private final boolean afterConfirmation;
	private final LocalDateTime createdAt;
	
	public WithdrawalRequest(String id, String applicationId, boolean afterConfirmation) {
		this.id = id;
		this.applicationId = applicationId;
		this.afterConfirmation = afterConfirmation;
		this.status = WithdrawalStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}
	
	public String getId() {return id;}
	public String getApplicationId() {return applicationId;}
	public WithdrawalStatus getStatus() {return status;}
	public void setStatus(WithdrawalStatus status) {this.status = status;}
	public boolean isAfterConfirmation() {return afterConfirmation;}
	public LocalDateTime getCreatedAt() {return createdAt;}
}
