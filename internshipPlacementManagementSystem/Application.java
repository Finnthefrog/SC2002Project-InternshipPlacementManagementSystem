package internshipPlacementManagementSystem;
//package internshipPlacementManagementSystem;
import java.io.*;
import java.time.LocalDate;

<<<<<<< HEAD

=======
/**
 * Application class created by Student applying to an Internship opportunity
 * Must be approved by CompanyRep and confirmed by Student to allow successful Internship allocation
 * Can be withdrawn with CareerCentre staff approval at any time
 */
>>>>>>> ziyanwork/origin
public class Application implements Serializable{
    private static int nextApplicationId = 1000;
    
    private String applicationId;
    private Student applicant;
    private InternshipOpportunity opportunity;
    private ApplicationStatus status;
    private LocalDate applicationDate;
    private LocalDate statusUpdateDate;
    private String rejectionReason;
    private String withdrawalReason;
    private boolean placementAccepted;
    
    /**
     * Constructor for creating a new application
     */
    public Application(Student applicant, InternshipOpportunity opportunity) {
        this.applicationId = "";
        this.applicant = applicant;
        this.opportunity = opportunity;
        this.status = ApplicationStatus.PENDING;
        this.applicationDate = LocalDate.now();
        this.statusUpdateDate = LocalDate.now();
        this.placementAccepted = false;
    }
    
    /**
     * Company representative approves the application
     */
    public boolean approveApplication(CompanyRepresentative companyRep) {
        // Verify the company rep is authorized for this opportunity
        if (!opportunity.getCompanyRepresentative().equals(companyRep)) {
            return false;
        }
        
        if (this.status == ApplicationStatus.PENDING) {
            this.status = ApplicationStatus.SUCCESSFUL;
            this.statusUpdateDate = LocalDate.now();
            return true;
        }
        return false;
    }
    
    /**
     * Company representative rejects the application
     */
    public boolean rejectApplication(CompanyRepresentative companyRep, String reason) {
        // Verify the company rep is authorized for this opportunity
        if (!opportunity.getCompanyRepresentative().equals(companyRep)) {
            return false;
        }
        
        if (this.status == ApplicationStatus.PENDING) {
            this.status = ApplicationStatus.UNSUCCESSFUL;
            this.rejectionReason = reason;
            this.statusUpdateDate = LocalDate.now();
            return true;
        }
        return false;
    }
    
    /**
     * Student accepts the internship placement (only if approved)
     */
    public boolean acceptPlacement() {
        if (this.status == ApplicationStatus.SUCCESSFUL && !this.placementAccepted) {
            this.placementAccepted = true;
            this.statusUpdateDate = LocalDate.now();
            // Update opportunity slot count	
            opportunity.confirmPlacement();
            return true;
        }
        return false;
    }
    
    /**
     * Request withdrawal of application
     */
    public boolean requestWithdrawal(String reason) {
        if (this.status == ApplicationStatus.PENDING || 
            (this.status == ApplicationStatus.SUCCESSFUL)) {
            this.withdrawalReason = reason;
            this.status = ApplicationStatus.WITHDRAWL_PENDING;
            System.out.println("Successfully Submitting");
            // This would trigger a request to Career Center Staff for approval
            return true;
        } else { System.out.println("You Are Not Allowed To Withdraw This Application");}
        return false;
    }
    
    /**
     * Career Center Staff approves withdrawal
     */
    public void approveWithdrawal(CareerCenterStaff staff) {
        this.status = ApplicationStatus.WITHDRAWN;
        this.statusUpdateDate = LocalDate.now();
        if (this.placementAccepted) {
            // Free up the slot in the opportunity
            opportunity.cancelPlacement();
        }
    }
    
    public void rejectWithdrawal(CareerCenterStaff staff) {
        this.status = ApplicationStatus.REJECT_WITHDRAWN;
        this.statusUpdateDate = LocalDate.now();
    }
    /**
     * Check if student is eligible for this internship level
     */
    public boolean isEligibleForLevel() {
        int studentYear = applicant.getyearOfStudy();
        InternshipLevel opportunityLevel = opportunity.getLevel();
        
        // Year 1-2 can only apply for Basic level
        if (studentYear <= 2 && opportunityLevel != InternshipLevel.BASIC) {
            return false;
        }
        // Year 3+ can apply for any level
        return true;
    }
    
    /**
     * Check if student's major matches opportunity preference
     */
    public boolean matchesMajorRequirement() {
        return applicant.getMajor().equals(opportunity.getPreferredMajor());
    }
    
    // Getters and Setters
    public void setApplicationId(int applicationId) {this.applicationId = "APP" + applicationId;}
    
    public String getApplicationId() { return applicationId; }
    
    public Student getApplicant() { return applicant; }
    
    public InternshipOpportunity getOpportunity() { return opportunity; }
    
    public ApplicationStatus getStatus() { return status; }
    
    public LocalDate getApplicationDate() { return applicationDate; }
    
    public LocalDate getStatusUpdateDate() { return statusUpdateDate; }
    
    public String getRejectionReason() { return rejectionReason; }
    
    public String getWithdrawalReason() { return withdrawalReason; }
    
    public boolean isPlacementAccepted() { return placementAccepted; }
    
    /**
     * Get company information from the associated opportunity
     */
    public String getCompanyName() {
        return opportunity.getCompanyName();
    }
    
    /**
     * Get internship level from the associated opportunity
     */
    public InternshipLevel getInternshipLevel() {
        return opportunity.getLevel();
    }
    
    @Override
    public String toString() {
        return String.format("Application ID: %d | Student: %s | Company: %s | " +
                           "Position: %s | Level: %s | Status: %s | Applied: %s",
                           applicationId, applicant.getName(), getCompanyName(),
                           opportunity.getTitle(), opportunity.getLevel(),
                           status, applicationDate);
    }
}
