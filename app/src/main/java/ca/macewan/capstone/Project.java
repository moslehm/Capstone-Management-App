package ca.macewan.capstone;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Project {
    private DocumentReference creator;
    private String name;
    private String description;
    private String semester;
    private String year;
    private List<DocumentReference> supervisors;
    private List<DocumentReference> supervisorsPending;
    private List<String> tags;
    private List<String> imagePaths;
    private List<DocumentReference> members;
    private boolean status;

    public Project() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Project(DocumentReference creator, String title, String description, String semester, String year) {
        this.creator = creator;
        this.name = title;
        this.description = description;
        this.semester = semester;
        this.year = year;
        this.status = true;
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

    public boolean getStatus() {
        return status;
    }

    public List<DocumentReference> getSupervisorsPending() {
        return supervisorsPending;
    }

    public void setSupervisorsPending(List<DocumentReference> supervisorsPending) {
        this.supervisorsPending = supervisorsPending;
    }
}
