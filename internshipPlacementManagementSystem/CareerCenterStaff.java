package internshipPlacementManagementSystem;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.time.LocalDate; 
import java.time.format.DateTimeParseException;
import java.io.*;

public class CareerCenterStaff extends User implements Serializable{
    
    private String staffDepartment;
    private FilterSettings filterSettings; 
    private String role;
    
    public CareerCenterStaff(String userID,String name,String email ,String role, String staffDepartment) {
    	super(userID, name, email); 
        this.staffDepartment = staffDepartment;
        this.role = role;
        this.filterSettings = new FilterSettings();
    }

    public void viewInternshipOpportunities(List<InternshipOpportunity> allInternships, Scanner scanner) {
        int page = 1;
        final int pageSize = 5;
        boolean keepViewing = true;
        List<InternshipOpportunity> filteredList = new ArrayList<>();

        while (keepViewing) {
            
            filteredList.clear(); 
            FilterSettings filters = this.filterSettings; 

            for (InternshipOpportunity internship : allInternships) {
                
            	if (internship.getStatus() != InternshipStatus.APPROVED) continue;
                
                if (!filters.majors.isEmpty() && !filters.majors.contains(internship.getPreferredMajor())) {
                    continue;
                }
                
                if (!filters.levels.isEmpty()) {
                    if (!filters.levels.contains(internship.getLevel())) {
                        continue;
                    }
                }
                
                if (!filters.companyName.isEmpty() && !filters.companyName.contains(internship.getCompanyName())) {
                    continue;
                }

                if (filters.startingFrom != null && internship.getApplicationOpeningDate().isBefore(filters.startingFrom)) {
                    continue;
                }
                
                if (filters.closingBefore != null && internship.getApplicationClosingDate().isAfter(filters.closingBefore)) {
                    continue;
                }

                filteredList.add(internship);
            } 

            int totalItems = filteredList.size();
            int totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
            if (page > totalPages) page = totalPages;
            if (page < 1) page = 1;
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalItems);

            System.out.println("\n--- Viewing All Internship Opportunities ---");
            System.out.println("Current Filters: " + filterSettings.describe());

            if (totalItems == 0) {
                System.out.println("\nNo internships match your current filters.");
            } else {
                System.out.printf("--- Page %d of %d (Showing %d-%d of %d results) ---\n", 
                                  page, totalPages, (totalItems == 0 ? 0 : startIndex + 1), endIndex, totalItems);
                List<InternshipOpportunity> pageItems = filteredList.subList(startIndex, endIndex);
                for (int i = 0; i < pageItems.size(); i++) {
                    InternshipOpportunity opp = pageItems.get(i);
                    System.out.printf("%d. | id: %s | %s (%s) | Description: %s | Level: %s | Major: %s | Slots: %d | Open: %s | Close: %s | Internship Status: %s\n",
                    		(startIndex + i + 1),
                    		opp.getOpportunityId(),
                    		opp.getTitle(),
                    		opp.getCompanyName(),
                    		opp.getDescription(),
                    		opp.getLevel(),
                    		opp.getPreferredMajor(),
                            opp.getTotalSlots()- opp.getConfirmedSlots(),
                            opp.getApplicationOpeningDate(), 
                            opp.getApplicationClosingDate(), 
                            opp.getStatus()); 
                }
            }
            
            System.out.println("\n--- Options ---");
            System.out.print("Enter (N)ext, (P)revious, (F)ilter, (C)lear, or (Q)uit: ");
            String choice = scanner.nextLine().toUpperCase();
            
            switch (choice) {
                case "N":
                    if (page < totalPages) page++;
                    else System.out.println("You are on the last page.");
                    break;
                case "P":
                    if (page > 1) page--;
                    else System.out.println("You are on the first page.");
                    break;
                case "F":
                    applyOpportunityFilters(scanner);
                    page = 1; 
                    break;
                case "C":
                    clearFilters();
                    page = 1; 
                    break;
                case "Q":
                    keepViewing = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } 
    }
    
    public FilterSettings getFilterSettings() {
        return this.filterSettings;
    }
    
    public void clearFilters() {
        this.filterSettings = new FilterSettings();
        System.out.println("All filters have been cleared.");
    }

    public void applyOpportunityFilters(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Apply Filters ---");
            System.out.println("Current Filters: " + filterSettings.describe());
            System.out.println("\nChoose filter to add/edit:");
            System.out.println("1. Filter by Internship Level");
            System.out.println("2. Filter by Major");
            System.out.println("3. Filter by Company Name");
            System.out.println("4. Set 'Opening From' Date");
            System.out.println("5. Set 'Closing Before' Date");
            System.out.println("6. Back");

            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1": applyLevelFilter(scanner); break;
                case "2": applyMajorFilter(scanner); break;
                case "3": applyCompanyFilter(scanner); break;
                case "4": applyDateFilter(scanner, true); break;
                case "5": applyDateFilter(scanner, false); break;
                case "6": back = true; break;
                default: System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void applyLevelFilter(Scanner scanner) {
        System.out.println("Add filter by Level (1: Basic, 2: Intermediate, 3: Advanced, 0: Clear Level Filter):");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1": filterSettings.levels.add(InternshipLevel.BASIC); System.out.println("Added 'Basic' to level filter."); break;
            case "2": filterSettings.levels.add(InternshipLevel.INTERMEDIATE); System.out.println("Added 'Intermediate' to level filter."); break;
            case "3": filterSettings.levels.add(InternshipLevel.ADVANCED); System.out.println("Added 'Advanced' to level filter."); break;
            case "0": filterSettings.levels.clear(); System.out.println("Cleared level filter."); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private void applyMajorFilter(Scanner scanner) {
        System.out.print("Enter Major to filter by (e.g., Computer Science) or '0' to clear: ");
        String major = scanner.nextLine().toUpperCase();
        if (major.equals("0")) {
            filterSettings.majors.clear();
            System.out.println("Cleared major filter.");
        } else {
            filterSettings.majors.add(major);
            System.out.println("Added '" + major + "' to major filter.");
        }
    }

    private void applyCompanyFilter(Scanner scanner) {
        System.out.print("Enter Company Name to filter by or '0' to clear: ");
        String company = scanner.nextLine();
        if (company.equals("0")) {
            filterSettings.companyName.clear();
            System.out.println("Cleared company filter.");
        } else {
            filterSettings.companyName.add(company);
            System.out.println("Added '" + company + "' to company filter.");
        }
    }

    private void applyDateFilter(Scanner scanner, boolean isStartDate) {
        String prompt = isStartDate ? "Enter 'Opening From' Date (YYYY-MM-DD) or '0' to clear:" 
                                    : "Enter 'Closing Before' Date (YYYY-MM-DD) or '0' to clear:";
        System.out.print(prompt + " ");
        String dateInput = scanner.nextLine();

        if (dateInput.equals("0")) {
            if (isStartDate) filterSettings.startingFrom = null;
            else filterSettings.closingBefore = null;
            System.out.println("Cleared date filter.");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateInput);
            if (isStartDate) {
                filterSettings.startingFrom = date;
                System.out.println("Set 'Opening From' date to " + date);
            } else {
                filterSettings.closingBefore = date;
                System.out.println("Set 'Closing Before' date to " + date);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
        }
    }
    
    public boolean approveCompanyRegistration(CompanyRepresentative representative) {
        if (!representative.getAccountStatus().equals("Pending")) {
            System.out.println("Error: Can only approve 'Pending' registrations.");
            return false;
        }
        representative.setAccountStatus("Approved");
        System.out.println("Company representative " + representative.getName() + " from " + representative.getCompanyName() + " has been APPROVED.");
        return true;
    }

    public boolean rejectCompanyRegistration(CompanyRepresentative representative) {
         if (!representative.getAccountStatus().equals("Pending")) {
            System.out.println("Error: Can only reject 'Pending' registrations.");
            return false;
        }
        representative.setAccountStatus("Rejected");
        System.out.println("Company representative " + representative.getName() + " from " + representative.getCompanyName() + " has been REJECTED.");
        return true;
    }

    public boolean approveInternship(InternshipOpportunity internship) {
        if (internship.getStatus() != InternshipStatus.PENDING) {
            System.out.println("Error: Can only approve 'Pending' internships.");
            return false;
        }
        internship.approve(this);
        System.out.println("Internship '" + internship.getTitle() + "' has been APPROVED.");
        return true;
    }

    public boolean rejectInternship(InternshipOpportunity internship,String reason) {
        if (internship.getStatus() != InternshipStatus.PENDING) {
            System.out.println("Error: Can only reject 'Pending' internships.");
            return false;
        }
        internship.reject(this,reason);
        System.out.println("Internship '" + internship.getTitle() + "' has been REJECTED.");
        return true;
    }
    
    public void managePendingInternships(List<InternshipOpportunity> allInternships, Scanner scanner) {
        boolean keepViewing = true;

        while (keepViewing) {
            List<InternshipOpportunity> pendingInternships = new ArrayList<>();
            for (InternshipOpportunity opp : allInternships) {
                if (opp.getStatus() == InternshipStatus.PENDING) {
                    pendingInternships.add(opp);
                }
            }

            System.out.println("\n--- Pending Internship Approvals ---");
            if (pendingInternships.isEmpty()) {
                System.out.println("No pending internships found.");
                return; 
            }

            for (int i = 0; i < pendingInternships.size(); i++) {
                InternshipOpportunity opp = pendingInternships.get(i);
                System.out.printf("%d. | id: %s | Visible: %s | %s (%s) | Level: %s | Major: %s | Slots: %d | Open: %s | Close: %s | Internship Status: %s\n",
                		(i + 1),
                		opp.getOpportunityId(),
                		opp.isVisible(),
                		opp.getTitle(),
                		opp.getCompanyName(),
                		opp.getLevel(),
                		opp.getPreferredMajor(),
                        opp.getTotalSlots() - opp.getConfirmedSlots(),
                        opp.getApplicationOpeningDate(), 
                        opp.getApplicationClosingDate(), 
                        opp.getStatus()); 
            
            }

            System.out.println("-----------------------------------");
            System.out.print("Enter Number to Manage, or (Q)uit: ");
            String choice = scanner.nextLine();

            try {
                int numericChoice = Integer.parseInt(choice);
                if (numericChoice > 0 && numericChoice <= pendingInternships.size()) {
                    InternshipOpportunity selectedOpp = pendingInternships.get(numericChoice - 1);
                    
                    System.out.println("\nSelected: " + selectedOpp.getTitle() + " (" + selectedOpp.getCompanyName() + ")");
                    System.out.print("Action: (A)pprove, (R)eject, or (C)ancel: ");
                    String action = scanner.nextLine().toUpperCase();
                    
                    switch (action) {
                        case "A":
                            approveInternship(selectedOpp);
                            break;
                        case "R":
                            System.out.print("Enter reason for rejection: ");
                            String reason = scanner.nextLine();
                            if (reason.isEmpty()) reason = "Does not meet guidelines.";
                            rejectInternship(selectedOpp, reason);
                            break;
                        default:
                            System.out.println("Action cancelled.");
                            break;
                    }
                } else {
                    System.out.println("Invalid number. Please select a number from the list.");
                }
            } catch (NumberFormatException e) {
                if (choice.equalsIgnoreCase("Q")) {
                    keepViewing = false;
                } else {
                    System.out.println("Invalid choice. Please enter a number or 'Q'.");
                }
            }
        } 
    }

    public void manageWithdrawalRequests(List<InternshipOpportunity> allInternships, Scanner scanner) {
        boolean keepViewing = true;

        while (keepViewing) {
            List<Application> pendingWithdrawals = new ArrayList<>();
            for (InternshipOpportunity opp : allInternships) {
                for (Application app : opp.getApplications()) {
                    if (app.getStatus() == ApplicationStatus.WITHDRAWL_PENDING) {
                        pendingWithdrawals.add(app);
                    }
                }
            }

            System.out.println("\n--- Pending Withdrawal Requests ---");
            if (pendingWithdrawals.isEmpty()) {
                System.out.println("No pending withdrawal requests found.");
                return; 
            }

            for (int i = 0; i < pendingWithdrawals.size(); i++) {
                Application app = pendingWithdrawals.get(i);
                System.out.printf("%d. | id: %s | Student: %s | Internship: %s (%s) | Reason: %s\n",
                    (i + 1),
                    app.getApplicationId(),
                    app.getApplicant().getName(),
                    app.getOpportunity().getTitle(),
                    app.getOpportunity().getCompanyName(),
                    app.getWithdrawalReason()
                );
            }

            System.out.println("-----------------------------------");
            System.out.print("Enter Number to Manage, or (Q)uit: ");
            String choice = scanner.nextLine();
            
            try {
                int numericChoice = Integer.parseInt(choice);
                if (numericChoice > 0 && numericChoice <= pendingWithdrawals.size()) {
                    Application selectedApp = pendingWithdrawals.get(numericChoice - 1);
                    
                    System.out.println("\nSelected: " + selectedApp.getApplicant().getName() + " for " + selectedApp.getOpportunity().getTitle());
                    System.out.print("Action: (A)pprove Withdrawal, (R)eject Withdrawal, or (C)ancel: ");
                    String action = scanner.nextLine().toUpperCase();
                    
                    switch (action) {
                        case "A":
                            approveWithdrawal(selectedApp);
                            break;
                        case "R":
                            rejectWithdrawal(selectedApp);
                            break;
                        default:
                            System.out.println("Action cancelled.");
                            break;
                    }
                } else {
                    System.out.println("Invalid number. Please select a number from the list.");
                }
            } catch (NumberFormatException e) {
                if (choice.equalsIgnoreCase("Q")) {
                    keepViewing = false;
                } else {
                    System.out.println("Invalid choice. Please enter a number or 'Q'.");
                }
            }
        } 
    }

    public boolean approveWithdrawal(Application application) {
        if (application.getStatus() != ApplicationStatus.WITHDRAWL_PENDING) {
            System.out.println("Error: Can only approve 'Withdrawal_Pending' applications.");
            return false;
        }
        
        InternshipStatus oldStatus = application.getOpportunity().getStatus();
        
        application.approveWithdrawal(this);
        System.out.println("Withdrawal request for " + application.getApplicant().getName() + " has been APPROVED.");
        
        if (oldStatus == InternshipStatus.FIllED) { 
            application.getOpportunity().approve(this); 
             System.out.println("Internship " + application.getOpportunity().getTitle() + " is no longer 'Filled' and is 'Approved' again.");
        }
        return true;
    }
    
    public boolean rejectWithdrawal(Application application) {
        if (application.getStatus() != ApplicationStatus.WITHDRAWL_PENDING) {
            System.out.println("Error: Can only reject 'Withdrawal_Pending' applications.");
            return false;
        }
        
        application.rejectWithdrawal(this); 
        System.out.println("Withdrawal request for " + application.getApplicant().getName() + " has been REJECTED.");
        return true;
    }

    public void generateInternshipReport(List<InternshipOpportunity> allInternships, Scanner scanner) {
        System.out.println("\n\n--- [ Internship Report ] ---");

        int basicCount = 0;
        int intermediateCount = 0;
        int advancedCount = 0;
        Map<String, Integer> majorCounts = new HashMap<>();
        Map<InternshipStatus, Integer> statusCounts = new HashMap<>();
        Map<String, Integer> companyCounts = new HashMap<>();
        for (InternshipOpportunity internship : allInternships) {

            switch (internship.getLevel()) {
                case BASIC: basicCount++; break;
                case INTERMEDIATE: intermediateCount++; break;
                case ADVANCED: advancedCount++; break;
            }

            String major = internship.getPreferredMajor();
            majorCounts.put(major, majorCounts.getOrDefault(major, 0) + 1);
            
            InternshipStatus status = internship.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            
            String companyName = internship.getCompanyName();
            companyCounts.put(companyName, companyCounts.getOrDefault(companyName,0)+1);
        } 
        
        System.out.println("---------------------------------");
        System.out.println("--- Summary of All Internships ---");
        System.out.println("Total Opportunities: " + allInternships.size());
        
        System.out.println("\nBy Level:");
        System.out.println("  Basic: " + basicCount);
        System.out.println("  Intermediate: " + intermediateCount);
        System.out.println("  Advanced: " + advancedCount);
        
        System.out.println("\nBy Status:");
        if (allInternships.isEmpty()) {
            System.out.println("  No internships in the system.");
        } else {
            for (InternshipStatus status : InternshipStatus.values()) {
                System.out.println("  " + status + ": " + statusCounts.getOrDefault(status, 0));
            }
        }

        System.out.println("\nBy Major:");
        if (majorCounts.isEmpty() && allInternships.isEmpty()) {
            System.out.println("  No internships in the system.");
        } else if (majorCounts.isEmpty()) {
            System.out.println("  No internships found.");
        } else {
            for (Map.Entry<String, Integer> entry : majorCounts.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        System.out.println("\nBy Company:");
        if (companyCounts.isEmpty()) {
        	System.out.println("No internships in the system.");
        } else {
            for (Map.Entry<String, Integer> entry : companyCounts.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
        }
        
        System.out.println("--- [ End of Report ] ---\n");
        System.out.println("Press Enter to return to the menu...");
        scanner.nextLine(); 
    }	

    
}