package internshipPlacementManagementSystem;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main class for the Internship Placement Management System
 * Provides CLI interface for all user types
 */
/**
 * @author finnt
 *
 */
public class InternshipPlacementManagementSystem {
    
    private Map<String, Student> students;
    private Map<String, CompanyRepresentative> companyRepresentatives;
    private Map<String, CareerCenterStaff> careerCenterStaff;
    private List<InternshipOpportunity> internshipOpportunities;
    private List<Application> applications;
    private Scanner scanner;
    private User currentUser;
    private Map<String, List<InternshipOpportunity>> userFilters;
    
    // TXT File paths for data initialization, We can use CSV also but I am less familiar, my java File IO is lacking
    private static final String STUDENT_DATA_FILE = "students.txt";
    private static final String STAFF_DATA_FILE = "staff.txt";
    
    public InternshipPlacementManagementSystem() {
        this.students = new HashMap<>();
        this.companyRepresentatives = new HashMap<>();
        this.careerCenterStaff = new HashMap<>();
        this.internshipOpportunities = new ArrayList<>();
        this.applications = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.userFilters = new HashMap<>();
        
        // Initialize data from files
        initializeData();
    }
    
    /**
     * Main entry point of the system
     */
    public static void main(String[] args) {
        InternshipPlacementManagementSystem system = new InternshipPlacementManagementSystem();
        system.run();
    }
    
    /**
     * Main system loop
     */
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
    
    /**
     * Display main menu for non-authenticated users
     */
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
                System.out.println("Thank you for using the system. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    /**
     * Display user-specific menu based on user type
     */
    private void showUserMenu() {
        System.out.println("\n=== Welcome, " + currentUser.getName() + " ===");
        
        if (currentUser instanceof Student) {
            showStudentMenu();
        } else if (currentUser instanceof CompanyRepresentative) {
            showCompanyRepresentativeMenu();
        } else if (currentUser instanceof CareerCenterStaff) {
            showCareerCenterStaffMenu();
        }
    }
    
    /**
     * Handle user login
     */
    private void handleLogin() {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine().trim();
        
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        
        User user = authenticateUser(userId, password);
        
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + user.getName());
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }
    
    /**
     * Authenticate user credentials
     */
    private User authenticateUser(String userId, String password) {
        // Check students
        if (students.containsKey(userId)) {
            Student student = students.get(userId);
            if (student.getPassword().equals(password)) {
                return student;
            }
        }
        
        // Check company representatives (only if approved)
        if (companyRepresentatives.containsKey(userId)) {
            CompanyRepresentative companyRep = companyRepresentatives.get(userId);
            if (companyRep.getPassword().equals(password) && companyRep.isApproved()) {
                return companyRep;
            }
        }
        
        // Check career center staff
        if (careerCenterStaff.containsKey(userId)) {
            CareerCenterStaff staff = careerCenterStaff.get(userId);
            if (staff.getPassword().equals(password)) {
                return staff;
            }
        }
        
        return null;
    }
    
    // ==================== STUDENT MENU ====================
    
    private void showStudentMenu() {
        System.out.println("\n=== STUDENT MENU ===");
        System.out.println("1. View Available Internship Opportunities");
        System.out.println("2. Apply for Internship");
        System.out.println("3. View My Applications");
        System.out.println("4. Accept Internship Placement");
        System.out.println("5. Request Application Withdrawal");
        System.out.println("6. Change Password");
        System.out.println("7. Logout");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        Student student = (Student) currentUser;
        
        switch (choice) {
            case 1:
                viewAvailableOpportunities(student);
                break;
            case 2:
                applyForInternship(student);
                break;
            case 3:
                viewMyApplications(student);
                break;
            case 4:
                acceptInternshipPlacement(student);
                break;
            case 5:
                requestApplicationWithdrawal(student);
                break;
            case 6:
                changePassword();
                break;
            case 7:
                logout();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    private void viewAvailableOpportunities(Student student) {
        List<InternshipOpportunity> availableOpportunities = internshipOpportunities.stream()
                .filter(opp -> opp.isVisibleToStudent(student) && opp.canApply())
                .collect(Collectors.toList());
        
        if (availableOpportunities.isEmpty()) {
            System.out.println("No internship opportunities are currently available for you.");
            return;
        }
        
        // Apply any saved filters
        availableOpportunities = applyFilters(availableOpportunities, student.getUserId());
        
        System.out.println("\n=== Available Internship Opportunities ===");
        for (int i = 0; i < availableOpportunities.size(); i++) {
            System.out.println((i + 1) + ". " + availableOpportunities.get(i).toString());
        }
        
        System.out.println("\nOptions:");
        System.out.println("1. View Details");
        System.out.println("2. Apply Filters");
        System.out.println("3. Clear Filters");
        System.out.println("4. Back to Menu");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                viewOpportunityDetails(availableOpportunities);
                break;
            case 2:
                applyOpportunityFilters(student.getUserId());
                break;
            case 3:
                clearFilters(student.getUserId());
                break;
            case 4:
                return;
        }
    }
    
    private void applyForInternship(Student student) {
        // Check if student already has 3 applications
        long activeApplications = applications.stream()
                .filter(app -> app.getApplicant().equals(student))
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING || 
                              app.getStatus() == ApplicationStatus.SUCCESSFUL)
                .count();
        
        if (activeApplications >= 3) {
            System.out.println("You have reached the maximum limit of 3 active applications.");
            return;
        }
        
        // Check if student has already accepted a placement
        boolean hasAcceptedPlacement = applications.stream()
                .filter(app -> app.getApplicant().equals(student))
                .anyMatch(app -> app.isPlacementAccepted());
        
        if (hasAcceptedPlacement) {
            System.out.println("You have already accepted an internship placement.");
            return;
        }
        
        List<InternshipOpportunity> availableOpportunities = internshipOpportunities.stream()
                .filter(opp -> opp.isVisibleToStudent(student) && opp.canApply())
                .collect(Collectors.toList());
        
        if (availableOpportunities.isEmpty()) {
            System.out.println("No internship opportunities are available for application.");
            return;
        }
        
        System.out.println("\n=== Available Opportunities for Application ===");
        for (int i = 0; i < availableOpportunities.size(); i++) {
            System.out.println((i + 1) + ". " + availableOpportunities.get(i).toString());
        }
        
        System.out.print("Select an opportunity to apply for (or 0 to cancel): ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= availableOpportunities.size()) {
            InternshipOpportunity selectedOpportunity = availableOpportunities.get(choice - 1);
            
            // Create and submit application
            Application application = new Application(student, selectedOpportunity);
            applications.add(application);
            selectedOpportunity.addApplication(application);
            
            System.out.println("Application submitted successfully!");
            System.out.println("Application ID: " + application.getApplicationId());
        }
    }
    
    // ==================== COMPANY REPRESENTATIVE MENU ====================
    
    private void showCompanyRepresentativeMenu() {
        System.out.println("\n=== COMPANY REPRESENTATIVE MENU ===");
        System.out.println("1. Create Internship Opportunity");
        System.out.println("2. View My Opportunities");
        System.out.println("3. Edit Opportunity");
        System.out.println("4. Toggle Opportunity Visibility");
        System.out.println("5. View Applications");
        System.out.println("6. Review Applications");
        System.out.println("7. Change Password");
        System.out.println("8. Logout");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        CompanyRepresentative companyRep = (CompanyRepresentative) currentUser;
        
        switch (choice) {
            case 1:
                createInternshipOpportunity(companyRep);
                break;
            case 2:
                viewMyOpportunities(companyRep);
                break;
            case 3:
                editOpportunity(companyRep);
                break;
            case 4:
                toggleOpportunityVisibility(companyRep);
                break;
            case 5:
                viewOpportunityApplications(companyRep);
                break;
            case 6:
                reviewApplications(companyRep);
                break;
            case 7:
                changePassword();
                break;
            case 8:
                logout();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    private void createInternshipOpportunity(CompanyRepresentative companyRep) {
        // Check if company rep already has 5 opportunities
        long existingOpportunities = internshipOpportunities.stream()
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
        
        // Select internship level
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
        
        System.out.print("Enter preferred major (e.g., CSC, EEE, MAE): ");
        String preferredMajor = scanner.nextLine().toUpperCase();
        
        LocalDate openingDate = getDateInput("Enter application opening date (YYYY-MM-DD): ");
        LocalDate closingDate = getDateInput("Enter application closing date (YYYY-MM-DD): ");
        
        if (closingDate.isBefore(openingDate)) {
            System.out.println("Closing date cannot be before opening date.");
            return;
        }
        
        System.out.print("Enter number of slots (max 10): ");
        int slots = Math.min(getIntInput(), 10);
        
        InternshipOpportunity opportunity = new InternshipOpportunity(
            title, description, level, preferredMajor, 
            openingDate, closingDate, companyRep, slots
        );
        
        internshipOpportunities.add(opportunity);
        
        System.out.println("Internship opportunity created successfully!");
        System.out.println("Opportunity ID: " + opportunity.getOpportunityId());
        System.out.println("Status: PENDING (awaiting Career Center approval)");
    }
    
    // ==================== CAREER CENTER STAFF MENU ====================
    
    private void showCareerCenterStaffMenu() {
        System.out.println("\n=== CAREER CENTER STAFF MENU ===");
        System.out.println("1. Approve/Reject Company Representatives");
        System.out.println("2. Approve/Reject Internship Opportunities");
        System.out.println("3. View All Opportunities");
        System.out.println("4. Generate Reports");
        System.out.println("5. Handle Withdrawal Requests");
        System.out.println("6. Change Password");
        System.out.println("7. Logout");
        System.out.print("Select an option: ");
        
        int choice = getIntInput();
        CareerCenterStaff staff = (CareerCenterStaff) currentUser;
        
        switch (choice) {
            case 1:
                manageCompanyRepresentatives(staff);
                break;
            case 2:
                manageInternshipOpportunities(staff);
                break;
            case 3:
                viewAllOpportunities();
                break;
            case 4:
                generateReports();
                break;
            case 5:
                handleWithdrawalRequests(staff);
                break;
            case 6:
                changePassword();
                break;
            case 7:
                logout();
                break;
            default:
                System.out.println("Invalid option. Please try again.");
        }
    }
    
    private void manageCompanyRepresentatives(CareerCenterStaff staff) {
        List<CompanyRepresentative> pendingReps = companyRepresentatives.values().stream()
                .filter(rep -> !rep.isApproved() && rep.getStatus() == UserStatus.PENDING)
                .collect(Collectors.toList());
        
        if (pendingReps.isEmpty()) {
            System.out.println("No pending company representative registrations.");
            return;
        }
        
        System.out.println("\n=== Pending Company Representative Registrations ===");
        for (int i = 0; i < pendingReps.size(); i++) {
            CompanyRepresentative rep = pendingReps.get(i);
            System.out.println((i + 1) + ". " + rep.getName() + 
                             " (" + rep.getUserId() + ") - " + rep.getCompanyName());
        }
        
        System.out.print("Select a representative to review (or 0 to cancel): ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= pendingReps.size()) {
            CompanyRepresentative selectedRep = pendingReps.get(choice - 1);
            
            System.out.println("\n=== Representative Details ===");
            System.out.println("Name: " + selectedRep.getName());
            System.out.println("Email: " + selectedRep.getUserId());
            System.out.println("Company: " + selectedRep.getCompanyName());
            System.out.println("Department: " + selectedRep.getDepartment());
            System.out.println("Position: " + selectedRep.getPosition());
            
            System.out.println("\n1. Approve");
            System.out.println("2. Reject");
            System.out.print("Choose action: ");
            
            int action = getIntInput();
            switch (action) {
                case 1:
                    selectedRep.approve();
                    System.out.println("Company representative approved successfully!");
                    break;
                case 2:
                    selectedRep.reject();
                    System.out.println("Company representative rejected.");
                    break;
            }
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void handleCompanyRepRegistration() {
        System.out.println("\n=== Company Representative Registration ===");
        
        System.out.print("Enter your company email address: ");
        String email = scanner.nextLine().trim();
        
        if (!isValidEmail(email)) {
            System.out.println("Invalid email format.");
            return;
        }
        
        if (companyRepresentatives.containsKey(email)) {
            System.out.println("A representative with this email already exists.");
            return;
        }
        
        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter company name: ");
        String companyName = scanner.nextLine();
        
        System.out.print("Enter department: ");
        String department = scanner.nextLine();
        
        System.out.print("Enter your position: ");
        String position = scanner.nextLine();
        
        CompanyRepresentative companyRep = new CompanyRepresentative(
            email, name, "password", companyName, department, position
        );
        
        companyRepresentatives.put(email, companyRep);
        
        System.out.println("Registration submitted successfully!");
        System.out.println("Your account is pending approval from Career Center Staff.");
        System.out.println("You will be able to login once your account is approved.");
    }
    
    private void initializeData() {
        // Load students from file
        loadStudentsFromFile();
        // Load career center staff from file
        loadStaffFromFile();
        
        System.out.println("Data initialization complete.");
        System.out.println("Loaded " + students.size() + " students.");
        System.out.println("Loaded " + careerCenterStaff.size() + " staff members.");
    }
    
    private void loadStudentsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(STUDENT_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String userId = parts[0].trim();
                    String name = parts[1].trim();
                    int yearOfStudy = Integer.parseInt(parts[2].trim());
                    String major = parts[3].trim();
                    
                    Student student = new Student(userId, name, "password", yearOfStudy, major);
                    students.put(userId, student);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load student data file. Starting with empty student list.");
            // Add some sample students for testing
            addSampleStudents();
        }
    }
    /** File I/O will require further revision */
    private void loadStaffFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(STAFF_DATA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String userId = parts[0].trim();
                    String name = parts[1].trim();
                    String department = parts[2].trim();
                    
                    CareerCenterStaff staff = new CareerCenterStaff(userId, name, "password", department);
                    careerCenterStaff.put(userId, staff);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load staff data file. Starting with empty staff list.");
            // Add some sample staff for testing
            addSampleStaff();
        }
    }
    // Test methods
    private void addSampleStudents() {
        students.put("U1234567A", new Student("U1234567A", "John Doe", "password", 3, "CSC"));
        students.put("U2345678B", new Student("U2345678B", "Jane Smith", "password", 2, "EEE"));
        students.put("U3456789C", new Student("U3456789C", "Bob Johnson", "password", 4, "MAE"));
    }
    
    private void addSampleStaff() {
        careerCenterStaff.put("staff001", new CareerCenterStaff("staff001", "Admin User", "password", "Career Services"));
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
    
    private void changePassword() {
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
        
        currentUser.setPassword(newPassword);
        System.out.println("Password changed successfully!");
    }
    
    private void logout() {
        System.out.println("Logging out...");
        currentUser = null;
    }
    
    // Need to link this with given filters
    private List<InternshipOpportunity> applyFilters(List<InternshipOpportunity> opportunities, String userId) {
        // Implementation for applying saved filters
        return opportunities; // Placeholder
    }
    
    private void applyOpportunityFilters(String userId) {
        // Implementation for setting filters
        System.out.println("Filter functionality - to be implemented");
    }
    
    private void clearFilters(String userId) {
        userFilters.remove(userId);
        System.out.println("Filters cleared.");
    }
    
    private void viewOpportunityDetails(List<InternshipOpportunity> opportunities) {
        System.out.print("Enter opportunity number to view details: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= opportunities.size()) {
            System.out.println(opportunities.get(choice - 1).getDetailedInfo());
        }
    }
    
    // Stuff i'll do after seeing personclasses 
    private void viewMyApplications(Student student) { /* Implementation */ }
    private void acceptInternshipPlacement(Student student) { /* Implementation */ }
    private void requestApplicationWithdrawal(Student student) { /* Implementation */ }
    private void viewMyOpportunities(CompanyRepresentative companyRep) { /* Implementation */ }
    private void editOpportunity(CompanyRepresentative companyRep) { /* Implementation */ }
    private void toggleOpportunityVisibility(CompanyRepresentative companyRep) { /* Implementation */ }
    private void viewOpportunityApplications(CompanyRepresentative companyRep) { /* Implementation */ }
    //private void reviewApplications(CompanyRepresentative companyRep) { /* Implementation */ }
    private void manageInternshipOpportunities(CareerCenterStaff staff) { /* Implementation */ }
    private void viewAllOpportunities() { /* Link w sysstate */ } 
    private void handleWithdrawalRequests(CareerCenterStaff staff) { /* Implementation */ }
}