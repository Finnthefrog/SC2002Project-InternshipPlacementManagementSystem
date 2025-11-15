package internshipPlacementManagementSystem;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class InternshipPlacementManagementSystem {
    
	private SystemState state;
    private List<Application> applications;
    private Scanner scanner;
    private User currentUser;
    
    private static final String STUDENT_DATA_FILE = "students.txt";
    private static final String STAFF_DATA_FILE = "staffs.txt";
    private static final String STATE_FILE_PATH = "data/system_state.dat";
    
    public InternshipPlacementManagementSystem() {
    	this.state = SystemState.loadOrCreate(STATE_FILE_PATH);
        this.applications = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        
        if (state.users.isEmpty() && state.students.isEmpty() && state.staff.isEmpty()) {
            System.out.println("No dat file found. Initializing from .txt files...");
            initializeData(); 
            System.out.println("Initialization complete. Saving initial state...");
            state.save(STATE_FILE_PATH); 
        } else {
            System.out.println("Loaded data from " + STATE_FILE_PATH);
        }
    }
    
    public static void main(String[] args) {
        InternshipPlacementManagementSystem system = new InternshipPlacementManagementSystem();
        system.run();
    }
    
    public void run() {
        System.out.println("=== Welcome to Internship Placement Management System ===");
        
        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else {
                showUserMenu();
            }
        }
    }
    
    private void showMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("1. Login");
        System.out.println("2. Register Company Representative");
        System.out.println("3. Exit");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                handleCompanyRepRegistration();
                break;
            case 3:
            	state.save(STATE_FILE_PATH);
                System.out.println("Thank you for using the system. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    private void showUserMenu() {
        System.out.println("\n=== Welcome, " + currentUser.getName() + " ===");
        
        if (currentUser instanceof Student) {
            showStudentMenu((Student) currentUser);
        } else if (currentUser instanceof CompanyRepresentative) {
            showCompanyRepresentativeMenu((CompanyRepresentative) currentUser);
        } else if (currentUser instanceof CareerCenterStaff) {
            showCareerCenterStaffMenu((CareerCenterStaff) currentUser);
        }
    }
    
    private void handleLogin() {
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().strip();
        
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        User user = authenticateUser(email, password);
        
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + user.getName());
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }
    
    private User authenticateUser(String email, String password) {
        if (state.students.containsKey(email)) {
            Student student = state.students.get(email);
            //System.out.println(student.getEmail()); #DEBUG
            if (student.getPassword().equals(password)) {
                return student;
            }
        }
        
        if (state.reps.containsKey(email)) {
            CompanyRepresentative companyRep = state.reps.get(email);
            if (companyRep.getPassword().equals(password) && companyRep.getAccountStatus().equals("Approved")) {
                return companyRep;
            }
        }
        
        if (state.staff.containsKey(email)) {
            CareerCenterStaff staff = state.staff.get(email);
            if (staff.getPassword().equals(password)) {
                return staff;
            }
        }
        
        return null;
    }
    
    private void showStudentMenu(Student student) {
        System.out.println("\n=== STUDENT MENU ===");
        System.out.println("1. View & Apply Available Internship Opportunities");
        System.out.println("2. View & Accepet or Withdrawl My Applications");
        System.out.println("3. Change Password");
        System.out.println("4. Logout");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        
        switch (choice) {
	        case 1:
	            student.viewInternshipOpportunities(state.internshipOpportunities, scanner, this.state); 
	            break;
            case 2:
            	student.viewAppliedInternships(scanner);
                break;
            case 3:
            	changePassword(student); 
                break;
            case 4:
            	student.logout();
            	this.currentUser = null;
            	return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    private void showCompanyRepresentativeMenu(CompanyRepresentative companyRep) {
        System.out.println("\n=== COMPANY REPRESENTATIVE MENU ===");
        System.out.println("1. Create Internship Opportunity");
        System.out.println("2. View & Edit My Opportunities");
        System.out.println("3. View & Manage Applications");
        System.out.println("4. View All Internship Opportunities");
        System.out.println("5. Change Password");
        System.out.println("6. Logout");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                createInternshipOpportunity(companyRep);
                break;
            case 2:
                companyRep.viewCreatedInternships(scanner);
                break;
            case 3:
            	companyRep.manageAllApplications(scanner);
                break;
            case 4:
            	companyRep.viewInternshipOpportunities(state.internshipOpportunities,scanner);
                break;
            case 5:
            	changePassword(companyRep);
                break;
            case 6:
            	companyRep.logout();
            	this.currentUser = null;
            	return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    private void createInternshipOpportunity(CompanyRepresentative companyRep) {
        long existingOpportunities = state.internshipOpportunities.stream()
                .filter(opp -> opp.getCompanyRepresentative().equals(companyRep))
                .count();
        
        if (existingOpportunities >= 5) {
            System.out.println("You have reached the maximum limit of 5 internship opportunities.");
            return;
        }
        
        System.out.println("\n=== Create New Internship Opportunity ===");
        
        System.out.print("Enter internship title: ");
        String title = scanner.nextLine();
        
        System.out.print("Enter description: ");
        String description = scanner.nextLine();
        
        System.out.println("Select internship level:");
        System.out.println("1. BASIC");
        System.out.println("2. INTERMEDIATE");
        System.out.println("3. ADVANCED");
        int levelChoice = getIntInput();
        
        InternshipLevel level;
        switch (levelChoice) {
            case 1: level = InternshipLevel.BASIC; break;
            case 2: level = InternshipLevel.INTERMEDIATE; break;
            case 3: level = InternshipLevel.ADVANCED; break;
            default: 
                System.out.println("Invalid choice. Defaulting to BASIC.");
                level = InternshipLevel.BASIC;
        }
        
        System.out.print("Enter preferred major : ");
        String preferredMajor = scanner.nextLine().toUpperCase();
        
        LocalDate openingDate = getDateInput("Enter application opening date (YYYY-MM-DD): ");
        LocalDate closingDate;
        while (true) {
	        closingDate = getDateInput("Enter application closing date (YYYY-MM-DD): ");
	        if (closingDate.isBefore(openingDate)) {
	            System.out.println("Closing date cannot be before opening date.");
	            continue;}
	        break;
        }
        
        System.out.print("Enter number of slots (max 10): ");
        int slots = Math.min(getIntInput(), 10);
        
        InternshipOpportunity newInternship;
        newInternship = companyRep.createInternship(title, description, level, preferredMajor, 
        	    openingDate, closingDate, companyRep, slots, this.state);
        state.internshipOpportunities.add(newInternship);
        System.out.println("Internship opportunity created ;successfully!");
        System.out.println("Opportunity ID: " + newInternship.getOpportunityId());
        System.out.println("Status: PENDING (awaiting Career Center approval)");
    }
    
    private void showCareerCenterStaffMenu(CareerCenterStaff staff) {
        System.out.println("\n=== CAREER CENTER STAFF MENU ===");
        System.out.println("1. Manage Company Representatives");
        System.out.println("2. Manage Pending Internships");
        System.out.println("3. View All Opportunities");
        System.out.println("4. Generate Internship Report"); // <-- MODIFIED
        System.out.println("5. Handle Withdrawal Requests");
        System.out.println("6. Change Password");
        System.out.println("7. Logout");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
    
        switch (choice) {
            case 1:
                manageCompanyRepresentatives(staff);
                break;
            case 2:
            	staff.managePendingInternships(state.internshipOpportunities,scanner);
                break;
            case 3:
            	staff.viewInternshipOpportunities(state.internshipOpportunities,scanner);
                break;
            case 4:
                staff.generateInternshipReport(state.internshipOpportunities, scanner); // <-- MODIFIED
                break;
            case 5:
                staff.manageWithdrawalRequests(state.internshipOpportunities, scanner);
                break;
            case 6:
            	changePassword(staff);
                break;
            case 7:
            	staff.logout();
            	this.currentUser = null;
            	return;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    private void manageCompanyRepresentatives(CareerCenterStaff staff) {
        List<CompanyRepresentative> pendingReps = state.reps.values().stream()
                .filter(rep -> !rep.getAccountStatus().equals("Approve") && rep.getAccountStatus().equals("Pending"))
                .collect(Collectors.toList());
        
        if (pendingReps.isEmpty()) {
            System.out.println("No pending company representative registrations.");
            return;
        }
        
        System.out.println("\n=== Pending Company Representative Registrations ===");
        for (int i = 0; i < pendingReps.size(); i++) {
            CompanyRepresentative rep = pendingReps.get(i);
            System.out.println((i + 1) + ". " + rep.getName() + 
                             " (" + rep.getUserID() + ") - " + rep.getCompanyName());
        }
        
        System.out.print("Select a representative to review (or 0 to cancel): ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= pendingReps.size()) {
            CompanyRepresentative selectedRep = pendingReps.get(choice - 1);
            
            System.out.println("\n=== Representative Details ===");
            System.out.println("Name: " + selectedRep.getName());
            System.out.println("Email: " + selectedRep.getEmail());
            System.out.println("Company: " + selectedRep.getCompanyName());
            System.out.println("Department: " + selectedRep.getDepartment());
            System.out.println("Position: " + selectedRep.getPosition());
            
            System.out.println("\n1. Approve");
            System.out.println("2. Reject");
            System.out.print("Choose action: ");
            
            int action = getIntInput();
            switch (action) {
                case 1:
                    staff.approveCompanyRegistration(selectedRep);
                    System.out.println("Company representative approved successfully!");
                    break;
                case 2:
                	staff.rejectCompanyRegistration(selectedRep);
                    System.out.println("Company representative rejected.");
                    break;
            }
        }
    }
    
    private void handleCompanyRepRegistration() {
        System.out.println("\n=== Company Representative Registration ===");
        
        String email;
        
        while (true) {
            System.out.print("Enter your company email address (or enter '3' to exit): ");
            email = scanner.nextLine().trim();

            if (email.equals("3")) {
                return; 
            }

            if (!isValidEmail(email)) {
                System.out.println("Invalid email format. Please try again.");
                continue; 
            }
            if (state.reps.containsKey(email) || state.users.containsKey(email)) {
                System.out.println("A representative with this email already exists. Please try again.");
                continue; 
            }
            break; 
        }
        String name;
        while (true) {
            System.out.print("Enter your full name: ");
            name = scanner.nextLine().trim(); 

            if (name.isEmpty()) { 
                System.out.println("Name cannot be empty. Please try again.");
            } else {
                break; 
            }
        }
        
        String companyName;
        while (true) {
        	System.out.print("Enter company name: ");
            companyName = scanner.nextLine(); 

            if (companyName.isEmpty()) { 
                System.out.println("Name cannot be empty. Please try again.");
            } else {
                break; 
            }
        }
        
        String department;
        while (true) {
        	System.out.print("Enter department: ");
            department = scanner.nextLine(); 

            if (department.isEmpty()) { 
                System.out.println("Name cannot be empty. Please try again.");
            } else {
                break; 
            }
        }

        String position;
        while (true) {
        	System.out.print("Enter your position: ");
            position = scanner.nextLine();

            if (position.isEmpty()) { 
                System.out.println("Name cannot be empty. Please try again.");
            } else {
                break; 
            }
        }
        CompanyRepresentative rep = new CompanyRepresentative(name,email, companyName, department, position);
        rep.setCompanyreid(state.getComapnyrepid());
        state.reps.put(email, rep);
        
        System.out.println("Registration submitted successfully!");
        System.out.println("Your account is pending approval from Career Center Staff.");
        System.out.println("You will be able to login once your account is approved.");
    }
    
    private void addSampleStudents() {
        state.students.put("U2345678B", new Student("U2345678B", "Jane Smith", "1234@gmail.com", 2, "EEE"));
        state.students.put("1234", new Student("U3456789C", "Bob Johnson", "12345@gmail.com", 4, "MAE"));
        state.students.put("12345", new Student("U3456789C", "Bob Johnson", "12345@gmail.com", 4, "MAE"));
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
    
    private int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    private LocalDate getDateInput(String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine();
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }
    
    private void changePassword(User user) {
        System.out.print("Enter current password: ");
        String currentPassword = scanner.nextLine();
        
        if (!currentUser.getPassword().equals(currentPassword)) {
            System.out.println("Current password is incorrect.");
            return;
        }
        
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();
        
        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }
        user.changePassword(currentPassword,confirmPassword);
        System.out.println("Password changed successfully!");
    }
    
    private void initializeData() {
        loadStudentsFromFile();
        loadStaffsFromFile();
        System.out.println("Data loaded. " + (state.students.size()+ state.staff.size())+ " students in the system.");
    }
    
    private void loadStudentsFromFile() {
        InputStream is = getClass().getResourceAsStream(STUDENT_DATA_FILE);

        if (is == null) {
            System.out.println("Could not find " + STUDENT_DATA_FILE + ". Starting with empty student list.");
            addSampleStudents();
            return; 
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String userId = parts[0].trim();
                    String name = parts[1].trim();
                    String major = parts[2].trim();
                    int yearOfStudy = Integer.parseInt(parts[3].trim());
                    String email = parts[4].trim();
                 
                    Student student = new Student(userId, name, email, yearOfStudy, major);
                    state.students.put(email, student);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + STUDENT_DATA_FILE + ". Starting with empty student list.");
            addSampleStudents();
        } catch (NumberFormatException e) {
            System.out.println("Error parsing number in " + STUDENT_DATA_FILE + ". Starting with empty student list.");
            addSampleStudents();
        }
    }
    
    private void loadStaffsFromFile() {
        InputStream is = getClass().getResourceAsStream(STAFF_DATA_FILE);

        if (is == null) {
            System.out.println("Could not find " + STAFF_DATA_FILE + ". Starting with empty student list.");
            addSampleStudents();
            return; 
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String userId = parts[0].trim();
                    String name = parts[1].trim();
                    String role = parts[2].trim();
                    String department = parts[3].trim();
                    String email = parts[4].trim();
                 
                    CareerCenterStaff staff = new CareerCenterStaff(userId, name, email, role, department);
                    state.staff.put(email, staff);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading " + STUDENT_DATA_FILE + ". Starting with empty student list.");
            addSampleStudents();
        } catch (NumberFormatException e) {
            System.out.println("Error parsing number in " + STUDENT_DATA_FILE + ". Starting with empty student list.");
            addSampleStudents();
        }
    }
}