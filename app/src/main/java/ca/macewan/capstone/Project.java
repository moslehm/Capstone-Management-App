package ca.macewan.capstone;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class Project {
    private DocumentReference creator;
    private String name;
    private String description;
    private String semester;
    private String year;
    private List<DocumentReference> supervisors;
    private List<String> tags;
    private List<String> imagePaths;
    private List<DocumentReference> members;

    public Project() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Project(DocumentReference creator, String title, String description, String semester, String year, List<DocumentReference> supervisors) {
        this.creator = creator;
        this.name = title;
        this.description = description;
        this.semester = semester;
        this.year = year;
        this.supervisors = supervisors;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DocumentReference getCreator() {
        return creator;
    }

    public List<DocumentReference> getMembers() {
        return members;
    }

    public List<DocumentReference> getSupervisors() {
        return supervisors;
    }

    public String getSemester() {
        return semester;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getYear() {
        return year;
    }
}
