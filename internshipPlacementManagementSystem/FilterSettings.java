package internshipPlacementManagementSystem;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
<<<<<<< HEAD

=======
/**
 * Class to store the filter settings for Internship opportunity Viewing
 */
>>>>>>> ziyanwork/origin
public class FilterSettings implements Serializable{
	private static final long serialVersionUID = 1L;
	public Set<InternshipLevel> levels = new HashSet<>();
	public Set<String> majors = new HashSet<>();
	public LocalDate startingFrom = null;
	public LocalDate closingBefore = null;
	public Set<InternshipStatus> statuses = new HashSet<>();
	public Set<String> companyName = new HashSet<>();
<<<<<<< HEAD
	
=======
	/**
	 * Print Method to show current filters applied 
	 */
>>>>>>> ziyanwork/origin
	public String describe() {
		return String.format(
			"Company Name = %s | Statuses=%s | Majors=%s | Levels=%s | Opening Date=%s | Closing Before=%s",
			companyName.isEmpty()? "All" : companyName,
			statuses.isEmpty()? "All" : statuses,
			majors.isEmpty()? "All" : majors,
			levels.isEmpty()? "All": levels,
			startingFrom == null? "-" : startingFrom,
			closingBefore == null? "-"	: closingBefore	
		);
				
	}
}
