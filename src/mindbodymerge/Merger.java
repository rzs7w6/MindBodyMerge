package mindbodymerge;

import java.io.File;

/**
 *
 * @author Jonathon Lantsberger and Robert Spalding
 */
public class Merger {
    
    private final File purchaseData;
    private File studentData;
    private String semesterCode;
    
    public Merger(File purchaseData, File studentData, String semesterCode){
        this.purchaseData = purchaseData;
        this.studentData = studentData;
        this.semesterCode = semesterCode;
    }
}
