package internshipPlacementManagementSystem;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an internship opportunity created by a company representative
 * that students can apply for.
 */
public class InternshipOpportunity implements Serializable{
    private static int nextOpportunityId = 2000;
    
    private String opportunityId;
    private String title;
    private String description;
    private InternshipLevel level;
    private String preferredMajor;
    private LocalDate applicationOpeningDate;
    private LocalDate applicationClosingDate;
    private InternshipStatus status;
    private String companyName;
    private CompanyRepresentative companyRepresentative;
    private int totalSlots;
    private int confirmedSlots;
    private boolean isVisible;
    private LocalDate creationDate;
    private String rejectionReason;
    private List<Application> applications;
    
    /**
     * Constructor for creating a new internship Listing
     */
    public InternshipOpportunity(String title, String description, InternshipLevel level,
                               String preferredMajor, LocalDate openingDate, LocalDate closingDate,
                               CompanyRepresentative companyRep, int totalSlots) {
        
        this.opportunityId = "";
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.applicationOpeningDate = openingDate;
        this.applicationClosingDate = closingDate;
        this.status = InternshipStatus.PENDING;
        this.companyName = companyRep.getCompanyName();
        this.companyRepresentative = companyRep;
        this.totalSlots = Math.min(totalSlots, 10); // Max 10 slots as per requirements
        this.confirmedSlots = 0;
        this.isVisible = true; // Default to visible
        this.creationDate = LocalDate.now();
        this.applications = new ArrayList<>();
    }
    
    /**
     * CareerCenter Staff approves the pending opportunity
     */
    public boolean approve(CareerCenterStaff staff) {
        if (this.status == InternshipStatus.PENDING) {
            this.status = InternshipStatus.APPROVED;
            return true;
        }
        return false;
    }
    
    /**
     * CareerCenter Staff rejects the opportunity
     */
    public boolean reject(CareerCenterStaff staff, String reason) {
        if (this.status == InternshipStatus.PENDING) {
            this.status = InternshipStatus.REJECTED;
            this.rejectionReason = reason;
            return true;
        }
        return false;
    }
    
    /**
     * Method to toggle visibility of the opportunity
     */
    public void toggleVisibility(CompanyRepresentative companyRep) {
        if (this.companyRepresentative.equals(companyRep)) {
            this.isVisible = !this.isVisible;
        }
    }
    
    
     /** Set visibility status */
    public void setVisibility(CompanyRepresentative companyRep, boolean visible) {
        if (this.companyRepresentative.equals(companyRep)) {
            this.isVisible = visible;
        }
    }
    
    /**
     * Method to Check if students can apply for this opportunity
     */
    public boolean canApply() {
        LocalDate today = LocalDate.now();
        return status == InternshipStatus.APPROVED && 
               status != InternshipStatus.FIllED &&
               !today.isBefore(applicationOpeningDate) &&
               !today.isAfter(applicationClosingDate) &&
               isVisible &&
               confirmedSlots < totalSlots;
    }
    
    /**
     * Method to check if opportunity is visible to students based on their profile
     */
    public boolean isVisibleToStudent(Student student) {
        if (!isVisible || status != InternshipStatus.APPROVED) {
            return false;
        }
        
        // Check major requirement
        if (!student.getMajor().equals(preferredMajor)) {
            return false;
        }
        
        // Check level eligibility based on year of study
        int studentYear = student.getyearOfStudy();
        if (studentYear <= 2 && level != InternshipLevel.BASIC) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Method to an application to this opportunity
     */
    public boolean addApplication(Application application) {
        if (canApply()) {
            applications.add(application);
            return true;
        }
        return false;
    }
    
    /**
     * Method to remove an application from this opportunity
     */
    public boolean removeApplication(Application application) {
        return applications.remove(application);
    }
    
    /**
     * Confirm a placement (when student accepts)
     */
    public void confirmPlacement() {
        confirmedSlots++;
        if (confirmedSlots >= totalSlots) {
            this.status = InternshipStatus.FIllED;
        }
    }
    
    /**
     * Cancel a placement (when student withdraws after acceptance)
     */
    public void cancelPlacement() {
        if (confirmedSlots > 0) {
            confirmedSlots--;
            if (this.status == InternshipStatus.FIllED) {
                this.status = InternshipStatus.APPROVED; // Reopen for applications
            }
        }
    }
    
    /**
     * Get applications filtered by status
     */
    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applications.stream()
                .filter(app -> app.getStatus() == status)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get number of available slots at this position
     */
    public int getAvailableSlots() {
        return totalSlots - confirmedSlots;
    }
    
    /**
     * Check if application period is active
     */
    public boolean isApplicationPeriodActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(applicationOpeningDate) && !today.isAfter(applicationClosingDate);
    }
    
    /**
     * Update InternshipOpportunity listing details (only before approval)
     */
    public boolean updateDetails(String title, String description, InternshipLevel level,
                               String preferredMajor, LocalDate openingDate, LocalDate closingDate,
                               int totalSlots, CompanyRepresentative companyRep) {
        
        if (!this.companyRepresentative.equals(companyRep) || 
            this.status != InternshipStatus.PENDING) {
            return false;
        }
        
        this.title = title;
        this.description = description;
        this.level = level;
        this.preferredMajor = preferredMajor;
        this.applicationOpeningDate = openingDate;
        this.applicationClosingDate = closingDate;
        this.totalSlots = Math.min(totalSlots, 10);
        return true;
    }
    
    // Getters and Setters
    public void setOpportunityId(int OpportunityId) {this.opportunityId = "INTERN" + OpportunityId;}
    
    public String getOpportunityId() { return opportunityId; }
    
    public String getTitle() { return title; }
    
    public String getDescription() { return description; }
    
    public InternshipLevel getLevel() { return level; }
    
    public String getPreferredMajor() { return preferredMajor; }
    
    public LocalDate getApplicationOpeningDate() { return applicationOpeningDate; }
    
    public LocalDate getApplicationClosingDate() { return applicationClosingDate; }
    
    public InternshipStatus getStatus() { return status; }
    
    public String getCompanyName() { return companyName; }
    
    public CompanyRepresentative getCompanyRepresentative() { return companyRepresentative; }
    
    public int getTotalSlots() { return totalSlots; }
    
    public int getConfirmedSlots() { return confirmedSlots; }
    
    public boolean isVisible() { return isVisible; }
    
    public LocalDate getCreationDate() { return creationDate; }
    
    public String getRejectionReason() { return rejectionReason; }
    
    public List<Application> getApplications() { return new ArrayList<>(applications); }
    
    @Override
    public String toString() {
        return String.format("ID: %d | %s | Company: %s | Level: %s | Major: %s | " +
                           "Slots: %d/%d | Status: %s | Visible: %s | Apply: %s to %s",
                           opportunityId, title, companyName, level, preferredMajor,
                           confirmedSlots, totalSlots, status, isVisible,
                           applicationOpeningDate, applicationClosingDate);
    }
    
    /**
     * Print detailed information of the Internship Listing for display
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Internship Opportunity Details ===\n");
        sb.append("ID: ").append(opportunityId).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Company: ").append(companyName).append("\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Level: ").append(level).append("\n");
        sb.append("Preferred Major: ").append(preferredMajor).append("\n");
        sb.append("Application Period: ").append(applicationOpeningDate)
          .append(" to ").append(applicationClosingDate).append("\n");
        sb.append("Available Slots: ").append(getAvailableSlots())
          .append("/").append(totalSlots).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Visibility: ").append(isVisible ? "Visible" : "Hidden").append("\n");
        sb.append("Total Applications: ").append(applications.size()).append("\n");
        
        return sb.toString();
    }
}