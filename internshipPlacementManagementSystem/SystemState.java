package internshipPlacementManagementSystem;
import java.io.*;
import java.util.*;

/**
 * Method to manage and storethe global state for the InternshipPlacementManagementSystem.
 * SystemState serves as the central container for all registered users, students, staff,
 * company representatives, internship opportunities, applications, filter settings, and
 * withdrawal requests 
 * It also tracks and generates unique identifiers for core entities,
 * and provides methods to load or save the system's persistent state.
 * SystemState is serializable to support saving and restoring application state.
 */
public class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int curOpportunityId = 001;
    public int curApplicationId = 001;
    public int curCompanyrepId = 001;
    public Map<String, User> users = new HashMap<>();
    public Map<String, Student> students = new HashMap<>();
    public Map<String, CompanyRepresentative> reps = new HashMap<>();
    public Map<String, CareerCenterStaff> staff = new HashMap<>();

    public List<InternshipOpportunity> internshipOpportunities = new ArrayList<>();
    public List<Application> applications = new ArrayList<>();
    public Map<String, FilterSettings> userFilters = new HashMap<>();
    public List<PasswordResetRequest> passwordResetRequests = new ArrayList<>();
    
    
    public int nextIntSeq = 1;
    public int nextAppSeq = 1;
    public int nextWrSeq = 1;
    /**
     * Method to load an existing SystemState from the specified file path or create a new one if none is present
     * @param path The file path to load the saved state from
     * @return The loaded SystemState instance or a new blank instance if loading fails
     */
    public static SystemState loadOrCreate(String path) {
        File f = new File(path);
        if (!f.exists()) return new SystemState();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
        	 SystemState loaded = (SystemState) ois.readObject();
             // Patch for new fields after deserialization for compatibility
             if (loaded.passwordResetRequests == null)
                 loaded.passwordResetRequests = new ArrayList<>();
             // (Add similar patches for other future fields, if any)
             return loaded;
        } catch (Exception e) {
            System.out.println("[Warn] Failed to load state: " + e.getMessage());
            return new SystemState();
        }
    }
    /**
     * Saves the current system state to the specified file path
     * @param path The file path to store the serialized system state
     */
    public void save(String path) {
        File f = new File(path);
        f.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        } catch (Exception e) {
            System.out.println("[Warn] Failed to save state: " + e.getMessage());
        }
    }
    public int getCurOpID() {return this.curOpportunityId++;}
    public int getComapnyrepid() {return this.curCompanyrepId++;}
    public int getCurappid() {return this.curApplicationId++;}
    public String nextInternshipId() { return String.format("INT-%05d", nextIntSeq++); }
    public String nextApplicationId() { return String.format("APP-%05d", nextAppSeq++); }
   // public String nextWithdrawalId() { return String.format("WR-%05d", nextWrSeq++); }
}

