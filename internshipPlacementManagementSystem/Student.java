package internshipPlacementManagementSystem;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet; 
import internshipPlacementManagementSystem.InternshipLevel; 
import internshipPlacementManagementSystem.ApplicationStatus; 
/**
 * Student User applying to and being placed into Internships
 * This class can Apply to internships, monitor their applications and their status, and if needed withdraw
 * applications they have previously made
 */

public class Student extends User implements Serializable {
	
    private int yearOfStudy; 
    private String major;    
    private List<Application> appliedInternships; 
    private FilterSettings filterSettings;
    /**
     * Constructor for the student class 
     * @param userID Student Identification Number for this user
     * @param name Name of the student 
     * @param email Email of the student for login 
     * @param yearOfStudy Year cohort of the student
     * @param major Degree of study 
     */
    public Student(String userID, String name,String email, int yearOfStudy, String major) {
        super(userID, name, email); 
        this.yearOfStudy = yearOfStudy;
        this.major = major;
        this.appliedInternships = new ArrayList<>();
        this.filterSettings = new FilterSettings();
    }
    /**
     * Method to handle UI for printing a list of all internship listings in the system 
     * Can choose to filter for Required experience level, desired major, Company or Application timeframe
     * @param allInternships This is the List of all internships taken stream
     */
    public void viewInternshipOpportunities(List<InternshipOpportunity> allInternships, Scanner scanner, SystemState state) {
        int page = 1;
        final int pageSize = 5;
        boolean keepViewing = true;
        List<InternshipOpportunity> filteredList = new ArrayList<>();

        while (keepViewing) {
            filteredList.clear(); 
            FilterSettings filters = this.filterSettings; 

            for (InternshipOpportunity internship : allInternships) {
            	if (!internship.canApply()) {
                    continue;}
                if (this.yearOfStudy <= 2 && internship.getLevel() != InternshipLevel.BASIC) continue;
                if (filters.majors.isEmpty()) {
                    if (!internship.getPreferredMajor().equalsIgnoreCase(this.major)) continue;
                } else {
                    if (!filters.majors.contains(internship.getPreferredMajor())) continue;
                }
                if (!filters.levels.isEmpty()) {
                    if (!filters.levels.contains(internship.getLevel())) continue;
                }
                if (!filters.companyName.isEmpty() && !filters.companyName.contains(internship.getCompanyName())) continue;
                if (filters.startingFrom != null && internship.getApplicationOpeningDate().isBefore(filters.startingFrom)) continue;
                if (filters.closingBefore != null && internship.getApplicationClosingDate().isAfter(filters.closingBefore)) continue;
                
                filteredList.add(internship);
            } 

            int totalItems = filteredList.size();
            int totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
            if (page > totalPages) page = totalPages;
            if (page < 1) page = 1;
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalItems);

            System.out.println("\n--- Viewing Available Internships for " + getName() + " ---");
            System.out.println("Current Filters: " + filterSettings.describe());

            if (totalItems == 0) {
                System.out.println("\nNo internships match your current profile and filters.");
            } else {
                System.out.printf("--- Page %d of %d (Showing %d-%d of %d results) ---\n", 
                                  page, totalPages, (totalItems == 0 ? 0 : startIndex + 1), endIndex, totalItems);
                List<InternshipOpportunity> pageItems = filteredList.subList(startIndex, endIndex);
                for (int i = 0; i < pageItems.size(); i++) {
                    InternshipOpportunity opp = pageItems.get(i);
                    System.out.printf("%d. | id: %s | %s (%s) | Level: %s | Major: %s | Slots: %d | Open: %s | Close: %s | App Status: %s\n",
                    		(startIndex + i + 1),
                    		opp.getOpportunityId(),
                    		opp.getTitle(),
                    		opp.getCompanyName(),
                    		opp.getLevel(),
                    		opp.getPreferredMajor(),
                            opp.getTotalSlots(),
                            opp.getApplicationOpeningDate(), 
                            opp.getApplicationClosingDate(), 
                            opp.getStatus()); 
    
                }
            }
            
            System.out.println("\n--- Options ---");
            System.out.print("Enter Number to Apply, (N)ext, (P)revious, (F)ilter, (C)lear, or (Q)uit: ");
            String choice = scanner.nextLine();
            
            try {
                int numericChoice = Integer.parseInt(choice);
                if (numericChoice > 0 && numericChoice <= totalItems) {
                    InternshipOpportunity selectedOpp = filteredList.get(numericChoice - 1);
                    boolean success = this.applyForInternship(selectedOpp,state);
                    if (success) {
                        System.out.println("Application successful. Returning to menu.");
                        keepViewing = false; 
                    } else {
                        System.out.println("Application failed. Please check messages and try again.");
                    }
                } else {
                    System.out.println("Invalid number. Please enter a number from the list.");
                }
            } catch (NumberFormatException e) {
                switch (choice.toUpperCase()) {
                    case "N": if (page < totalPages) page++; else System.out.println("You are on the last page."); break;
                    case "P": if (page > 1) page--; else System.out.println("You are on the first page."); break;
                    case "F": applyOpportunityFilters(scanner); page = 1; break;
                    case "C": clearFilters(); page = 1; break;
                    case "Q": keepViewing = false; break;
                    default: System.out.println("Invalid option. Please try again.");
                }
            } 
        } 
    }
/**
 * Method to allow the student to create an internship for application
 * Student applies to an opportunity made a companyrep, which is then 
 * @param internship  
 * @param state
 * @return
 */
    public boolean applyForInternship(InternshipOpportunity internship, SystemState state) { 
        for (Application app : appliedInternships) {
            if (app.getOpportunity().equals(internship)) {
                System.out.println("Application failed: You have already applied for " + internship.getTitle());
                return false;
            }
        }
        
        List <Application> pendingApplication = new ArrayList<>();
        for (Application app : appliedInternships) {
            if ((app.getStatus() == ApplicationStatus.PENDING)|| (app.getStatus() == ApplicationStatus.WITHDRAWL_PENDING)) {
            	pendingApplication.add(app);
            }
        }
        
        if (pendingApplication.size() >= 3) {
            System.out.println("Application failed: You already have 3 pending applications.");
            return false;
        }
        
        if (this.yearOfStudy <= 2 && internship.getLevel() != InternshipLevel.BASIC) {
            System.out.println("Application failed: Year 1 & 2 students can only apply for 'Basic' level internships.");
            return false;
        }
        
        Application newApp = new Application(this, internship);
        newApp.setApplicationId(state.getCurappid());
        
        boolean addedToOpportunity = internship.addApplication(newApp);
        
        if (addedToOpportunity) {
            appliedInternships.add(newApp);
            System.out.println("Successfully applied for " + internship.getTitle() + ".");
            return true;
        } else {
            System.out.println("Application failed: The internship is full or the application period is closed.");
            return false;
        }
    }
    
    public void viewAppliedInternships(Scanner scanner) {
        boolean keepViewing = true;

        while (keepViewing) {
            System.out.println("\n--- Your Applied Internships ---");

            if (appliedInternships.isEmpty()) {
                System.out.println("You have not applied for any internships.");
                return; 
            }

            for (int i = 0; i < appliedInternships.size(); i++) {
                Application app = appliedInternships.get(i);
                System.out.printf("%d. | id: %s | %s (%s) | Level: %s | Major: %s | Slots: %d | Open: %s | Close: %s | App Status: %s\n",
                        (i + 1),
                        app.getApplicationId(),
                        app.getOpportunity().getTitle(),
                        app.getOpportunity().getCompanyName(),
                        app.getOpportunity().getLevel(),
                        app.getOpportunity().getPreferredMajor(),
                        app.getOpportunity().getTotalSlots(), // <-- ADDED
                        app.getOpportunity().getApplicationOpeningDate(), // <-- ADDED
                        app.getOpportunity().getApplicationClosingDate(), // <-- ADDED
                        app.getStatus()); 
                // --- END OF MODIFICATION ---
            }

            System.out.println("-----------------------------------");
            System.out.print("Enter Number to Manage, or (Q)uit: ");
            String choice = scanner.nextLine();

            try {
                int numericChoice = Integer.parseInt(choice);

                if (numericChoice > 0 && numericChoice <= appliedInternships.size()) {
                    Application selectedApp = appliedInternships.get(numericChoice - 1);
                    ApplicationStatus status = selectedApp.getStatus();
                    
                    System.out.println("\nSelected: " + selectedApp.getOpportunity().getTitle() + " | Status: " + status);
                    
                    switch (status) {
                        case SUCCESSFUL: 
                            System.out.print("Action: (A)ccept Offer, (W)ithdraw Application, or (C)ancel: ");
                            String action = scanner.nextLine().toUpperCase();
                            if (action.equals("A")) {
                                String reason = "Accepted another offer";     
                                selectedApp.acceptPlacement();
                                System.out.println("Congratulations! You have accepted the offer for " + selectedApp.getOpportunity().getTitle());
                                for (Application app : appliedInternships) {
                                    boolean isPending = (app.getStatus() == ApplicationStatus.PENDING);
                                    boolean isSuccessful = (app.getStatus() == ApplicationStatus.SUCCESSFUL);

                                    if (app != selectedApp && (isPending || isSuccessful)) {
                                        app.requestWithdrawal(reason);
                                        System.out.println("Withdrawn application for " + app.getOpportunity().getTitle());
                                    }
                                }
                                keepViewing = false; 
                            } else if (action.equals("W")) {
                                System.out.print("Enter reason for withdrawing this successful offer: ");
                                String reason = scanner.nextLine();
                                if (reason.isEmpty()) reason = "Withdrawing offer";
                                
                                requestWithdrawal(selectedApp, reason);
                                keepViewing = false; 
                            }
                            break;
                            
                        case PENDING: 
                            System.out.println("This is a pending application.");
                            System.out.print("Action: (W)ithdraw Application, or (C)ancel: ");
                            String withdrawAction = scanner.nextLine().toUpperCase();
                            
                            if (withdrawAction.equals("W")) {
                                System.out.print("Enter reason for withdrawal: ");
                                String reason = scanner.nextLine();
                                if (reason.isEmpty()) reason = "No longer interested"; 
                                
                                requestWithdrawal(selectedApp, reason);
                                keepViewing = false; 
                            }
                            break;
                        
                        case WITHDRAWL_PENDING:
                            System.out.println("This application is already pending withdrawal approval. No further actions available.");
                            break;
                            
                        case WITHDRAWN:
                        case UNSUCCESSFUL:
                        default:
                            System.out.println("No actions are available for this application status.");
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
/**
 * Method for Student to confirm Placement at internship once CompanyRep has approved
 * withdraws student from other internships that have been applied to 
 * @param successfulApplication The Internship Application that is being confirmed
 * @param reason Reason for Withdrawal of all other applied to internships
 * @return True if Successful application and withdrawal from other internships  
 */
    public boolean acceptInternship(Application successfulApplication,String reason) {
        System.out.println("--- Accepting Internship Offer ---");
        
        if (successfulApplication.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("Error: Can only accept 'Successful' applications.");
            return false;
        }
        
        successfulApplication.acceptPlacement();
        System.out.println("Congratulations! You have accepted the offer for " + successfulApplication.getOpportunity().getTitle());
        
        for (Application app : appliedInternships) {
            boolean isPending = (app.getStatus() == ApplicationStatus.PENDING);
            boolean isSuccessful = (app.getStatus() == ApplicationStatus.SUCCESSFUL);

            if (app != successfulApplication && (isPending || isSuccessful)) {
                app.requestWithdrawal(reason);
                System.out.println("Withdrawn application for " + app.getOpportunity().getTitle());
            }
        }
        return true;
    }
/**
 * Method to submit a withdrawal-application to careerCenter staff
 * A reason must be given for CareerCentre staff to overview and approve the request 
 * @param application The ID of the appliation to be withdrawn
 * @param reason Reason for withdrawal
 * @return True if request submitted
 */
    public boolean requestWithdrawal(Application application, String reason) {
        System.out.println("Submitting withdrawal request for (" + application.getStatus() + ") " + application.getOpportunity().getTitle() + "...");
        application.requestWithdrawal(reason);
        return true;
    }
    
    public FilterSettings getFilterSettings() {
        return this.filterSettings;
    }
    /**
     * Remove filters currently in place on viewing
     */
    public void clearFilters() {
        this.filterSettings = new FilterSettings();
        System.out.println("All filters have been cleared.");
    }
    /**
     * Method to handle the filtering of {@link viewInternshipOpportunities} by 
     * experience level, desired major, Company or Application timeframe 
     */
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
            System.out.println("6. Back to Opportunities");
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
/**
 * Method to handle settings for filtering by experience Level
 */
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
    /**
     * Method to handle settings for filtering by Major
     */
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
    /**
     * Method to handle settings for filtering by Company
     */
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
    /**
     * Method to handle settings for filtering by timeframe
     */
    private void applyDateFilter(Scanner scanner, boolean isStartDate) {
        String prompt = isStartDate ? "Enter 'Opening From' Date (YYYY-MM-DD) or '0' to clear:" : "Enter 'Closing Before' Date (YYYY-MM-DD) or '0' to clear:";
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
    
    public int getyearOfStudy() { return this.yearOfStudy; }
    public String getMajor() { return this.major; }
    public List<Application> getappliedInternships() { return this.appliedInternships; }
}
package internshipPlacementManagementSystem;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet; 
import internshipPlacementManagementSystem.InternshipLevel; 
import internshipPlacementManagementSystem.ApplicationStatus; 

public class Student extends User implements Serializable {
	
    private int yearOfStudy; 
    private String major;    
    private List<Application> appliedInternships; 
    private FilterSettings filterSettings;
    
    public Student(String userID, String name,String email, int yearOfStudy, String major) {
        super(userID, name, email); 
        this.yearOfStudy = yearOfStudy;
        this.major = major;
        this.appliedInternships = new ArrayList<>();
        this.filterSettings = new FilterSettings();
    }

    public void viewInternshipOpportunities(List<InternshipOpportunity> allInternships, Scanner scanner, SystemState state) {
        int page = 1;
        final int pageSize = 5;
        boolean keepViewing = true;
        List<InternshipOpportunity> filteredList = new ArrayList<>();

        while (keepViewing) {
            filteredList.clear(); 
            FilterSettings filters = this.filterSettings; 

            for (InternshipOpportunity internship : allInternships) {
            	if (!internship.canApply()) {
                    continue;}
                if (this.yearOfStudy <= 2 && internship.getLevel() != InternshipLevel.BASIC) continue;
                if (filters.majors.isEmpty()) {
                    if (!internship.getPreferredMajor().equalsIgnoreCase(this.major)) continue;
                } else {
                    if (!filters.majors.contains(internship.getPreferredMajor())) continue;
                }
                if (!filters.levels.isEmpty()) {
                    if (!filters.levels.contains(internship.getLevel())) continue;
                }
                if (!filters.companyName.isEmpty() && !filters.companyName.contains(internship.getCompanyName())) continue;
                if (filters.startingFrom != null && internship.getApplicationOpeningDate().isBefore(filters.startingFrom)) continue;
                if (filters.closingBefore != null && internship.getApplicationClosingDate().isAfter(filters.closingBefore)) continue;
                
                filteredList.add(internship);
            } 

            int totalItems = filteredList.size();
            int totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
            if (page > totalPages) page = totalPages;
            if (page < 1) page = 1;
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalItems);

            System.out.println("\n--- Viewing Available Internships for " + getName() + " ---");
            System.out.println("Current Filters: " + filterSettings.describe());

            if (totalItems == 0) {
                System.out.println("\nNo internships match your current profile and filters.");
            } else {
                System.out.printf("--- Page %d of %d (Showing %d-%d of %d results) ---\n", 
                                  page, totalPages, (totalItems == 0 ? 0 : startIndex + 1), endIndex, totalItems);
                List<InternshipOpportunity> pageItems = filteredList.subList(startIndex, endIndex);
                for (int i = 0; i < pageItems.size(); i++) {
                    InternshipOpportunity opp = pageItems.get(i);
                    System.out.printf("%d. | id: %s | %s (%s) | Level: %s | Major: %s | Slots: %d | Open: %s | Close: %s | App Status: %s\n",
                    		(startIndex + i + 1),
                    		opp.getOpportunityId(),
                    		opp.getTitle(),
                    		opp.getCompanyName(),
                    		opp.getLevel(),
                    		opp.getPreferredMajor(),
                            opp.getTotalSlots(),
                            opp.getApplicationOpeningDate(), 
                            opp.getApplicationClosingDate(), 
                            opp.getStatus()); 
    
                }
            }
            
            System.out.println("\n--- Options ---");
            System.out.print("Enter Number to Apply, (N)ext, (P)revious, (F)ilter, (C)lear, or (Q)uit: ");
            String choice = scanner.nextLine();
            
            try {
                int numericChoice = Integer.parseInt(choice);
                if (numericChoice > 0 && numericChoice <= totalItems) {
                    InternshipOpportunity selectedOpp = filteredList.get(numericChoice - 1);
                    boolean success = this.applyForInternship(selectedOpp,state);
                    if (success) {
                        System.out.println("Application successful. Returning to menu.");
                        keepViewing = false; 
                    } else {
                        System.out.println("Application failed. Please check messages and try again.");
                    }
                } else {
                    System.out.println("Invalid number. Please enter a number from the list.");
                }
            } catch (NumberFormatException e) {
                switch (choice.toUpperCase()) {
                    case "N": if (page < totalPages) page++; else System.out.println("You are on the last page."); break;
                    case "P": if (page > 1) page--; else System.out.println("You are on the first page."); break;
                    case "F": applyOpportunityFilters(scanner); page = 1; break;
                    case "C": clearFilters(); page = 1; break;
                    case "Q": keepViewing = false; break;
                    default: System.out.println("Invalid option. Please try again.");
                }
            } 
        } 
    }

    public boolean applyForInternship(InternshipOpportunity internship, SystemState state) { 
        for (Application app : appliedInternships) {
            if (app.getOpportunity().equals(internship)) {
                System.out.println("Application failed: You have already applied for " + internship.getTitle());
                return false;
            }
        }
        
        List <Application> pendingApplication = new ArrayList<>();
        for (Application app : appliedInternships) {
            if ((app.getStatus() == ApplicationStatus.PENDING)|| (app.getStatus() == ApplicationStatus.WITHDRAWL_PENDING)) {
            	pendingApplication.add(app);
            }
        }
        
        if (pendingApplication.size() >= 3) {
            System.out.println("Application failed: You already have 3 pending applications.");
            return false;
        }
        
        if (this.yearOfStudy <= 2 && internship.getLevel() != InternshipLevel.BASIC) {
            System.out.println("Application failed: Year 1 & 2 students can only apply for 'Basic' level internships.");
            return false;
        }
        
        Application newApp = new Application(this, internship);
        newApp.setApplicationId(state.getCurappid());
        
        boolean addedToOpportunity = internship.addApplication(newApp);
        
        if (addedToOpportunity) {
            appliedInternships.add(newApp);
            System.out.println("Successfully applied for " + internship.getTitle() + ".");
            return true;
        } else {
            System.out.println("Application failed: The internship is full or the application period is closed.");
            return false;
        }
    }
    
    public void viewAppliedInternships(Scanner scanner) {
        boolean keepViewing = true;

        while (keepViewing) {
            System.out.println("\n--- Your Applied Internships ---");

            if (appliedInternships.isEmpty()) {
                System.out.println("You have not applied for any internships.");
                return; 
            }

            for (int i = 0; i < appliedInternships.size(); i++) {
                Application app = appliedInternships.get(i);
                System.out.printf("%d. | id: %s | %s (%s) | Level: %s | Major: %s | Slots: %d | Open: %s | Close: %s | App Status: %s\n",
                        (i + 1),
                        app.getApplicationId(),
                        app.getOpportunity().getTitle(),
                        app.getOpportunity().getCompanyName(),
                        app.getOpportunity().getLevel(),
                        app.getOpportunity().getPreferredMajor(),
                        app.getOpportunity().getTotalSlots(), // <-- ADDED
                        app.getOpportunity().getApplicationOpeningDate(), // <-- ADDED
                        app.getOpportunity().getApplicationClosingDate(), // <-- ADDED
                        app.getStatus()); 
                // --- END OF MODIFICATION ---
            }

            System.out.println("-----------------------------------");
            System.out.print("Enter Number to Manage, or (Q)uit: ");
            String choice = scanner.nextLine();

            try {
                int numericChoice = Integer.parseInt(choice);

                if (numericChoice > 0 && numericChoice <= appliedInternships.size()) {
                    Application selectedApp = appliedInternships.get(numericChoice - 1);
                    ApplicationStatus status = selectedApp.getStatus();
                    
                    System.out.println("\nSelected: " + selectedApp.getOpportunity().getTitle() + " | Status: " + status);
                    
                    switch (status) {
                        case SUCCESSFUL: 
                            System.out.print("Action: (A)ccept Offer, (W)ithdraw Application, or (C)ancel: ");
                            String action = scanner.nextLine().toUpperCase();
                            if (action.equals("A")) {
                                String reason = "Accepted another offer";     
                                selectedApp.acceptPlacement();
                                System.out.println("Congratulations! You have accepted the offer for " + selectedApp.getOpportunity().getTitle());
                                for (Application app : appliedInternships) {
                                    boolean isPending = (app.getStatus() == ApplicationStatus.PENDING);
                                    boolean isSuccessful = (app.getStatus() == ApplicationStatus.SUCCESSFUL);

                                    if (app != selectedApp && (isPending || isSuccessful)) {
                                        app.requestWithdrawal(reason);
                                        System.out.println("Withdrawn application for " + app.getOpportunity().getTitle());
                                    }
                                }
                                keepViewing = false; 
                            } else if (action.equals("W")) {
                                System.out.print("Enter reason for withdrawing this successful offer: ");
                                String reason = scanner.nextLine();
                                if (reason.isEmpty()) reason = "Withdrawing offer";
                                
                                requestWithdrawal(selectedApp, reason);
                                keepViewing = false; 
                            }
                            break;
                            
                        case PENDING: 
                            System.out.println("This is a pending application.");
                            System.out.print("Action: (W)ithdraw Application, or (C)ancel: ");
                            String withdrawAction = scanner.nextLine().toUpperCase();
                            
                            if (withdrawAction.equals("W")) {
                                System.out.print("Enter reason for withdrawal: ");
                                String reason = scanner.nextLine();
                                if (reason.isEmpty()) reason = "No longer interested"; 
                                
                                requestWithdrawal(selectedApp, reason);
                                keepViewing = false; 
                            }
                            break;
                        
                        case WITHDRAWL_PENDING:
                            System.out.println("This application is already pending withdrawal approval. No further actions available.");
                            break;
                            
                        case WITHDRAWN:
                        case UNSUCCESSFUL:
                        default:
                            System.out.println("No actions are available for this application status.");
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

    public boolean acceptInternship(Application successfulApplication,String reason) {
        System.out.println("--- Accepting Internship Offer ---");
        
        if (successfulApplication.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("Error: Can only accept 'Successful' applications.");
            return false;
        }
        
        successfulApplication.acceptPlacement();
        System.out.println("Congratulations! You have accepted the offer for " + successfulApplication.getOpportunity().getTitle());
        
        for (Application app : appliedInternships) {
            boolean isPending = (app.getStatus() == ApplicationStatus.PENDING);
            boolean isSuccessful = (app.getStatus() == ApplicationStatus.SUCCESSFUL);

            if (app != successfulApplication && (isPending || isSuccessful)) {
                app.requestWithdrawal(reason);
                System.out.println("Withdrawn application for " + app.getOpportunity().getTitle());
            }
        }
        return true;
    }

    public boolean requestWithdrawal(Application application, String reason) {
        System.out.println("Submitting withdrawal request for (" + application.getStatus() + ") " + application.getOpportunity().getTitle() + "...");
        application.requestWithdrawal(reason);
        return true;
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
            System.out.println("6. Back to Opportunities");
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
        String prompt = isStartDate ? "Enter 'Opening From' Date (YYYY-MM-DD) or '0' to clear:" : "Enter 'Closing Before' Date (YYYY-MM-DD) or '0' to clear:";
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
    
    public int getyearOfStudy() { return this.yearOfStudy; }
    public String getMajor() { return this.major; }
    public List<Application> getappliedInternships() { return this.appliedInternships; }
}