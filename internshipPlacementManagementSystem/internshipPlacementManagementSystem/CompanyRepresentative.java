package internshipPlacementManagementSystem;

import java.util.List;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.time.format.DateTimeParseException;
import internshipPlacementManagementSystem.InternshipLevel;
import internshipPlacementManagementSystem.ApplicationStatus; 

public class CompanyRepresentative extends User implements Serializable {
	
    private static int nextId = 1001;
    private String companyName;
    private String department;
    private String position;
    private String accountStatus; 
    private List<InternshipOpportunity> createdOpportunities; 
    private FilterSettings filterSettings; 

    public CompanyRepresentative( String name,String email,  
            String companyName, String department, String position) {
			super("", name, email); 
			this.companyName = companyName;
			this.department = department;
			this.position = position;
			this.accountStatus = "Pending";
			this.createdOpportunities = new ArrayList<>(); 
			this.filterSettings = new FilterSettings();
	}

    public InternshipOpportunity createInternship(String title, String description, InternshipLevel level,
            String preferredMajor, LocalDate openingDate, LocalDate closingDate,
            CompanyRepresentative companyRep, int totalSlots, SystemState state) {
        System.out.println("--- Creating New Internship ---");
        if (this.createdOpportunities != null && this.createdOpportunities.size() >= 5) {
            System.out.println("Error: You have reached the maximum limit of 5 created opportunities.");
            return null; 
        }

        while (totalSlots > 10) {
            System.out.println("Error: Maximum of 10 slots allowed.");
            System.out.print("Please re-enter the number of slots (<= 10): ");

            try {
                Scanner sc = new Scanner(System.in);
                totalSlots = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                totalSlots = 999; 
            }
        }
        
        InternshipOpportunity newInternship = new InternshipOpportunity(title, description, level, preferredMajor, openingDate,closingDate, this,totalSlots);
        newInternship.setOpportunityId(state.getCurOpID());
        this.createdOpportunities.add(newInternship);
        System.out.println("Successfully created internship: " + title + ". It is now 'Pending' staff approval.");
        return newInternship;
    }

    public void viewCreatedInternships(Scanner scanner) {
        boolean keepViewing = true;
        
        while (keepViewing) {
            System.out.println("\n--- Your Created Internships ---");
            if (createdOpportunities.isEmpty()) {
                System.out.println("You have not created any internships.");
                return;
            }

            for (int i = 0; i < createdOpportunities.size(); i++) {
                InternshipOpportunity opp = createdOpportunities.get(i);
                System.out.printf("%d. | id: %s | Visible: %s | %s (%s) | Level: %s | Major: %s | Slots: %d | Open: %s | Close: %s | App Status: %s\n",
                		(i + 1),
                		opp.getOpportunityId(),
                		opp.isVisible(),
                		opp.getTitle(),
                		opp.getCompanyName(),
                		opp.getLevel(),
                		opp.getPreferredMajor(),
                        opp.getTotalSlots(),
                        opp.getApplicationOpeningDate(), 
                        opp.getApplicationClosingDate(), 
                        opp.getStatus()); 
            }

            System.out.println("-----------------------------------");
            System.out.print("Enter Number to Manage Visibility, or (Q)uit: ");
            String choice = scanner.nextLine();

            try {
                int numericChoice = Integer.parseInt(choice);

                if (numericChoice > 0 && numericChoice <= createdOpportunities.size()) {
                    InternshipOpportunity selectedOpp = createdOpportunities.get(numericChoice - 1);
                    
                    System.out.println("\nSelected: " + selectedOpp.getTitle());
                    System.out.println("Current Status: " + selectedOpp.getStatus() + " | Current Visibility: " + selectedOpp.isVisible());

                    System.out.print("Action: (O)pen (Visible=true), (C)lose (Visible=false), or (B)ack: ");
                    String action = scanner.nextLine().toUpperCase();

                    switch (action) {
                        case "O":
                            toggleInternshipVisibility(selectedOpp, true);
                            break;
                        case "C":
                            toggleInternshipVisibility(selectedOpp, false);
                            break;
                        case "B":
                        default:
                            System.out.println("Returning to list...");
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

    public boolean toggleInternshipVisibility(InternshipOpportunity internship, boolean isVisible) {
        if (!internship.getCompanyRepresentative().equals(this)) {
            System.out.println("Error: You can only edit your own internships.");
            return false;
        }
        
        if (internship.getStatus() != InternshipStatus.APPROVED) {
            System.out.println("Error: Can only toggle visibility for 'Approved' internships. This one is '" + internship.getStatus() + "'.");
            return false;
        }
        
        internship.setVisibility(this,isVisible);
        System.out.println(internship.getTitle() + " visibility set to: " + isVisible);
        return true;
    }
    
    public void manageAllApplications(Scanner scanner) {
        boolean keepViewing = true;

        while (keepViewing) {
            List<Application> pendingApplications = new ArrayList<>();
            for (InternshipOpportunity opp : createdOpportunities) {
                for (Application app : opp.getApplications()) {
                    if (app.getStatus() == ApplicationStatus.PENDING) {
                        pendingApplications.add(app);
                        
                    }
                }
            }
            
            System.out.println("\n--- All Pending Applications (" + this.companyName + ") ---");
            if (pendingApplications.isEmpty()) {
                System.out.println("No pending applications found.");
                return; 
            }

            for (int i = 0; i < pendingApplications.size(); i++) {
                Application app = pendingApplications.get(i);
                System.out.printf("%d. | id: %s | %s (%s) | Student: %s | Status: %s\n",
                    (i + 1),
                    app.getApplicationId(),
                    app.getOpportunity().getTitle(), 
                    app.getOpportunity().getCompanyName(),
                    app.getApplicant().getName(),
                    app.getStatus()
                );
            }

            System.out.println("-----------------------------------");
            System.out.print("Enter Number to Manage Application, or (Q)uit: ");
            String choice = scanner.nextLine();

            try {
                int numericChoice = Integer.parseInt(choice);
                if (numericChoice > 0 && numericChoice <= pendingApplications.size()) {
                    Application selectedApp = pendingApplications.get(numericChoice - 1);
                    
                    System.out.println("\nSelected: " + selectedApp.getApplicant().getName() + " for " + selectedApp.getOpportunity().getTitle());

                    System.out.print("Action: (A)pprove, (R)eject, or (C)ancel: ");
                    String action = scanner.nextLine().toUpperCase();
                    
                    switch (action) {
                        case "A":
                            approveApplication(selectedApp);
                            break;
                        case "R":
                            System.out.print("Enter reason for rejection: ");
                            String reason = scanner.nextLine();
                            if (reason.isEmpty()) reason = "Not a suitable fit at this time.";
                            rejectApplication(selectedApp, reason);
                            break;
                        default:
                            System.out.println("Returning to application list...");
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
    
    public void viewApplications(InternshipOpportunity internship, Scanner scanner) {
        boolean keepViewing = true;

        while(keepViewing) {
            System.out.println("\n--- Viewing Applications for " + internship.getTitle() + " ---");
            List<Application> apps = internship.getApplications();
            
            if (apps.isEmpty()) {
                System.out.println("No applications found.");
                return;
            }

            for (int i = 0; i < apps.size(); i++) {
                Application app = apps.get(i);
                System.out.printf("%d. Student: %s | Major: %s | Status: %s\n",
                    (i + 1),
                    app.getApplicant().getName(),
                    ((Student)app.getApplicant()).getMajor(),
                    app.getStatus()
                );
            }

            System.out.println("-----------------------------------");
            System.out.print("Enter Number to Manage Application, or (Q)uit: ");
            String choice = scanner.nextLine();

            try {
                int numericChoice = Integer.parseInt(choice);
                if (numericChoice > 0 && numericChoice <= apps.size()) {
                    Application selectedApp = apps.get(numericChoice - 1);
                    ApplicationStatus status = selectedApp.getStatus();
                    
                    System.out.println("\nSelected: " + selectedApp.getApplicant().getName() + " | Status: " + status);

                    if (status == ApplicationStatus.PENDING) {
                        System.out.print("Action: (A)pprove, (R)eject, or (C)ancel: ");
                        String action = scanner.nextLine().toUpperCase();
                        
                        switch (action) {
                            case "A":
                                approveApplication(selectedApp);
                                break;
                            case "R":
                                System.out.print("Enter reason for rejection: ");
                                String reason = scanner.nextLine();
                                if (reason.isEmpty()) reason = "Not a suitable fit at this time.";
                                rejectApplication(selectedApp, reason);
                                break;
                            default:
                                System.out.println("Returning to application list...");
                                break;
                        }
                    } else {
                        System.out.println("No actions are available for an application with status '" + status + "'.");
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

    public boolean approveApplication(Application application) {
        if (application.getStatus() != ApplicationStatus.PENDING) {
            System.out.println("Error: Can only approve 'Pending' applications.");
            return false;
        }
        application.approveApplication(this);
        System.out.println("Application for " + application.getApplicant().getName() + " set to 'Successful'.");
        return true;
    }

    public boolean rejectApplication(Application application,String reason) {
        if (application.getStatus() != ApplicationStatus.PENDING) {
            System.out.println("Error: Can only reject 'Pending' applications.");
            return false;
        }
        application.rejectApplication(this,reason);
        System.out.println("Application for " + application.getApplicant().getName() + " set to 'Unsuccessful'.");
        return true;
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

    public String getAccountStatus() { return this.accountStatus; }
    public String getCompanyName() { return this.companyName; }
    public String getDepartment() { return this.department; }
    public String getPosition() { return this.position; }
    public void setAccountStatus(String status) { this.accountStatus = status; }
}