package ca.macewan.capstone;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Project {
    private String name;
    private String description;
    private String creator;
    private List<DocumentReference> members;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCreator() {
        return creator;
    }

    public List<DocumentReference> getMembers() {
        return members;
    }
}
