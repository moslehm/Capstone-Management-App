package ca.macewan.capstone;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

import ca.macewan.capstone.adapter.SharedMethods;

public class Project implements Parcelable {
    private Timestamp lastModified;
    private DocumentReference creator;
    private List<DocumentReference> supervisors;
    private List<DocumentReference> supervisorsPending;
    private List<DocumentReference> members;
    private DocumentReference projectRef;
    private String name;
    private String creatorString;
    private String semester;
    private String year;
    private List<String> supervisorsStringList;
    private List<String> supervisorsPendingStringList;
    private List<String> membersStringList;
    private String description;
    private List<String> tags;
    private List<String> imagePaths;
    private boolean isComplete;
    private int membersCounter;
    private int supervisorsCounter;
    private int supervisorsPendingCounter;

    public Project() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public Project(DocumentReference creator, String title, String description, String semester, String year) {
        lastModified = Timestamp.now();
        this.creator = creator;
        this.name = title;
        this.description = description;
        this.semester = semester;
        this.year = year;
        this.isComplete = false;
    }

    public Project(DocumentReference creator, String title, String description, String semester, String year, List<String> tags) {
        lastModified = Timestamp.now();
        this.creator = creator;
        this.name = title;
        this.description = description;
        this.semester = semester;
        this.year = year;
        this.isComplete = false;
        this.tags = tags;
    }

    // For parcelling
    public Project(String name, String creatorString, String semester, String year, List<String> supervisorsStringList,
                   List<String> supervisorsPendingStringList, List<String> membersStringList, String description, List<String> tags,
                   List<String> imagePaths, boolean isComplete) {
        this.name = name;
        this.creatorString = creatorString;
        this.semester = semester;
        this.year = year;
        this.supervisorsStringList = supervisorsStringList;
        this.supervisorsPendingStringList = supervisorsPendingStringList;
        this.membersStringList = membersStringList;
        this.description = description;
        this.tags = tags;
        this.imagePaths = imagePaths;
        this.isComplete = isComplete;
    }

    public void setCreator(DocumentReference creator) {
        this.creator = creator;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setYear(String year) {
        this.year = year;
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

    public void setMembers(List<DocumentReference> members) {
        this.members = members;
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

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
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

    public boolean getIsComplete() {
        return isComplete;
    }

    public List<DocumentReference> getSupervisorsPending() {
        return supervisorsPending;
    }

    public void setSupervisorsPending(List<DocumentReference> supervisorsPending) {
        this.supervisorsPending = supervisorsPending;
    }

    public void setSupervisors(List<DocumentReference> supervisors) {
        this.supervisors = supervisors;
    }

    @Exclude
    public DocumentReference getProjectRef() {
        return projectRef;
    }

    @Exclude
    public void setProjectRef(DocumentReference projectRef) {
        this.projectRef = projectRef;
    }

    @Exclude
    public String getCreatorString() {
        return creatorString;
    }

    private void setCreatorString(EventCompleteListener eventCompleteListener) {
        creator.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    creatorString = task.getResult().get("name").toString()
                            + " <" + task.getResult().get("email").toString() + ">";
                    setSupervisorsStringList(eventCompleteListener);
                }
            }
        });
    }

    private void setSupervisorsStringList(EventCompleteListener eventCompleteListener) {
        supervisorsStringList = new ArrayList<String>();
        if (SharedMethods.listIsEmpty(supervisors)) {
            setSupervisorsPendingStringList(eventCompleteListener);
            return;
        }
        int size = supervisors.size();
        supervisorsCounter = 0;
        for (DocumentReference supervisor : supervisors) {
            supervisor.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String info = task.getResult().get("name").toString()
                                    + " <" + task.getResult().get("email").toString() + ">";
                            supervisorsStringList.add(info);
                            if (supervisorsCounter == size - 1) {
                                setSupervisorsPendingStringList(eventCompleteListener);
                            }
                            supervisorsCounter++;
                        }
                    }
                });
        }
    }

    private void setSupervisorsPendingStringList(EventCompleteListener eventCompleteListener) {
        supervisorsPendingStringList = new ArrayList<String>();
        if (SharedMethods.listIsEmpty(supervisorsPending)) {
            setMembersStringList(eventCompleteListener);
            return;
        }
        int size = supervisorsPending.size();
        supervisorsPendingCounter = 0;
        for (DocumentReference supervisor : supervisorsPending) {
            supervisor.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String info = task.getResult().get("name").toString()
                                + " <" + task.getResult().get("email").toString() + ">";
                        supervisorsPendingStringList.add(info);
                        if (supervisorsPendingCounter == size - 1) {
                            setMembersStringList(eventCompleteListener);
                        }
                        supervisorsPendingCounter++;
                    }
                }
            });
        }
    }

    private void setMembersStringList(EventCompleteListener eventCompleteListener) {
        membersStringList = new ArrayList<String>();
        if (SharedMethods.listIsEmpty(members)) {
            eventCompleteListener.onComplete();
            return;
        }
        int size = members.size();
        membersCounter = 0;
        for (DocumentReference member : members) {
            member.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String info = task.getResult().get("name").toString()
                                + " <" + task.getResult().get("email").toString() + ">";
                        membersStringList.add(info);
                        if (membersCounter == size - 1) {
                            eventCompleteListener.onComplete();

                        }
                        membersCounter++;
                    }
                }
            });
        }
    }

    @Exclude
    public String getMembersString() {
        return String.join("\n", membersStringList);
    }

    @Exclude
    public List<String> getMembersStringList() {
        return membersStringList;
    }

    @Exclude
    public List<String> getSupervisorsStringList() {
        return supervisorsStringList;
    }

    @Exclude
    public List<String> getSupervisorsPendingStringList() {
        return supervisorsPendingStringList;
    }

    @Exclude
    public String getSupervisorsString() {
        return String.join("\n", supervisorsStringList);
    }

    @Exclude
    public void setAllStrings(EventCompleteListener eventCompleteListener) {
        setCreatorString(eventCompleteListener);
    }

    @Exclude
    public String getAllStrings() {
        return name + " " + description + " " + semester + " " + year + " " + getSupervisorsString();
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = Timestamp.now();
    }

    // Parcelling
    public Project(Parcel in){
        name = in.readString();
        creatorString = in.readString();
        semester = in.readString();
        year = in.readString();
        supervisorsStringList = in.readArrayList(String.class.getClassLoader());
        supervisorsPendingStringList = in.readArrayList(String.class.getClassLoader());
        membersStringList = in.readArrayList(String.class.getClassLoader());
        description = in.readString();
        tags = in.readArrayList(String.class.getClassLoader());
        imagePaths = in.readArrayList(String.class.getClassLoader());
        isComplete = in.readBoolean();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(creatorString);
        dest.writeString(semester);
        dest.writeString(year);
        dest.writeList(supervisorsStringList);
        dest.writeList(supervisorsPendingStringList);
        dest.writeList(membersStringList);
        dest.writeString(description);
        dest.writeList(tags);
        dest.writeList(imagePaths);
        dest.writeBoolean(isComplete);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        public Project[] newArray(int size) {
            return new Project[size];
        }
    };
}

