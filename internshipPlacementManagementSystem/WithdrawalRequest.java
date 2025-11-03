package internshipPlacementManagementSystem;
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
	public String getApplicationId {return ApplicationId;}
	public WithdrawalStatus getStatus() {return status;}
	public void setStatus(WithdrawalStatus status) {this.status = status;}
	public boolean isAfterConfirmation() {return afterConfirmation;}
	public LocalDateTime getCreatedAt() {return createdAt;}
}
