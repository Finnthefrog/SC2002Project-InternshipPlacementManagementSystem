package internshipPlacementManagementSystem;
import java.io.*;
import java.util.*;

<<<<<<< HEAD
=======
/**
 * Method to manage and storethe global state for the InternshipPlacementManagementSystem.
 * SystemState serves as the central container for all registered users, students, staff,
 * company representatives, internship opportunities, applications, filter settings, and
 * withdrawal requests 
 * It also tracks and generates unique identifiers for core entities,
 * and provides methods to load or save the system's persistent state.
 * SystemState is serializable to support saving and restoring application state.
 */
>>>>>>> ziyanwork/origin
public class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int curOpportunityId = 001;
    public int curApplicationId = 001;
    public int curCompanyrepId = 001;
    public Map<String, User> users = new HashMap<>();
    public Map<String, Student> students = new HashMap<>();
    public Map<String, CompanyRepresentative> reps = new HashMap<>();
    public Map<String, CareerCenterStaff> staff = new HashMap<>();

<<<<<<< HEAD
    public Map<String, WithdrawalRequest> withdrawals = new HashMap<>();
    public List<InternshipOpportunity> internshipOpportunities = new ArrayList<>();
    public List<Application> applications = new ArrayList<>();
    public Map<String, FilterSettings> userFilters = new HashMap<>();
=======
    public List<InternshipOpportunity> internshipOpportunities = new ArrayList<>();
    public List<Application> applications = new ArrayList<>();
    public Map<String, FilterSettings> userFilters = new HashMap<>();
    public List<PasswordResetRequest> passwordResetRequests = new ArrayList<>();
>>>>>>> ziyanwork/origin
    
    
    public int nextIntSeq = 1;
    public int nextAppSeq = 1;
    public int nextWrSeq = 1;
<<<<<<< HEAD

=======
    /**
     * Method to load an existing SystemState from the specified file path or create a new one if none is present
     * @param path The file path to load the saved state from
     * @return The loaded SystemState instance or a new blank instance if loading fails
     */
>>>>>>> ziyanwork/origin
    public static SystemState loadOrCreate(String path) {
        File f = new File(path);
        if (!f.exists()) return new SystemState();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
<<<<<<< HEAD
            return (SystemState) ois.readObject();
=======
        	 SystemState loaded = (SystemState) ois.readObject();
             // Patch for new fields after deserialization for compatibility
             if (loaded.passwordResetRequests == null)
                 loaded.passwordResetRequests = new ArrayList<>();
             // (Add similar patches for other future fields, if any)
             return loaded;
>>>>>>> ziyanwork/origin
        } catch (Exception e) {
            System.out.println("[Warn] Failed to load state: " + e.getMessage());
            return new SystemState();
        }
    }
<<<<<<< HEAD

=======
    /**
     * Saves the current system state to the specified file path
     * @param path The file path to store the serialized system state
     */
>>>>>>> ziyanwork/origin
    public void save(String path) {
        File f = new File(path);
        f.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        } catch (Exception e) {
            System.out.println("[Warn] Failed to save state: " + e.getMessage());
        }
    }
<<<<<<< HEAD
    
    public void clearAllData() {
        users.clear();
        students.clear();
        reps.clear();
        staff.clear();
        withdrawals.clear();
        internshipOpportunities.clear();
        applications.clear();
        userFilters.clear();
        
        curOpportunityId = 1;
        curApplicationId = 1;
        curCompanyrepId = 1;
        nextIntSeq = 1;
        nextAppSeq = 1;
        nextWrSeq = 1;

        System.out.println("All data has been cleared.");
    }

=======
>>>>>>> ziyanwork/origin
    public int getCurOpID() {return this.curOpportunityId++;}
    public int getComapnyrepid() {return this.curCompanyrepId++;}
    public int getCurappid() {return this.curApplicationId++;}
    public String nextInternshipId() { return String.format("INT-%05d", nextIntSeq++); }
    public String nextApplicationId() { return String.format("APP-%05d", nextAppSeq++); }
<<<<<<< HEAD
    public String nextWithdrawalId() { return String.format("WR-%05d", nextWrSeq++); }
=======
   // public String nextWithdrawalId() { return String.format("WR-%05d", nextWrSeq++); }
>>>>>>> ziyanwork/origin
}

