package internshipPlacementManagementSystem;
import java.io.*;
import java.util.*;

public class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int curOpportunityId = 001;
    public int curApplicationId = 001;
    public int curCompanyrepId = 001;
    public Map<String, User> users = new HashMap<>();
    public Map<String, Student> students = new HashMap<>();
    public Map<String, CompanyRepresentative> reps = new HashMap<>();
    public Map<String, CareerCenterStaff> staff = new HashMap<>();

    public Map<String, WithdrawalRequest> withdrawals = new HashMap<>();
    public List<InternshipOpportunity> internshipOpportunities = new ArrayList<>();
    public List<Application> applications = new ArrayList<>();
    public Map<String, FilterSettings> userFilters = new HashMap<>();
    
    
    public int nextIntSeq = 1;
    public int nextAppSeq = 1;
    public int nextWrSeq = 1;

    public static SystemState loadOrCreate(String path) {
        File f = new File(path);
        if (!f.exists()) return new SystemState();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (SystemState) ois.readObject();
        } catch (Exception e) {
            System.out.println("[Warn] Failed to load state: " + e.getMessage());
            return new SystemState();
        }
    }

    public void save(String path) {
        File f = new File(path);
        f.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        } catch (Exception e) {
            System.out.println("[Warn] Failed to save state: " + e.getMessage());
        }
    }
    
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

    public int getCurOpID() {return this.curOpportunityId++;}
    public int getComapnyrepid() {return this.curCompanyrepId++;}
    public int getCurappid() {return this.curApplicationId++;}
    public String nextInternshipId() { return String.format("INT-%05d", nextIntSeq++); }
    public String nextApplicationId() { return String.format("APP-%05d", nextAppSeq++); }
    public String nextWithdrawalId() { return String.format("WR-%05d", nextWrSeq++); }
}

