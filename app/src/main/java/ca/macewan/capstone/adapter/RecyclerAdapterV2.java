package ca.macewan.capstone.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ca.macewan.capstone.EventCompleteListener;
import ca.macewan.capstone.Project;
import ca.macewan.capstone.R;

public class RecyclerAdapterV2 extends RecyclerView.Adapter<RecyclerAdapterV2.ViewHolder> {
    private final FirebaseFirestore db;
    private int addCounter;
    private int modifiedCounter;
    LinkedHashMap<String, Project> projects;
    OnProjectListener onProjectListener;
    private LinkedHashMap<String, Project> projectsToDisplay;
    private String searchTerm;
    private List<String> currentProjectIds;
    private Timestamp lastRefresh;


    public RecyclerAdapterV2(LinkedHashMap<String, Project> projects) {
        db = FirebaseFirestore.getInstance();
        this.projects = projects;
        lastRefresh = Timestamp.now();
        projectsToDisplay = new LinkedHashMap<String, Project>();
    }

    public RecyclerAdapterV2(List<String> projectIds, EventCompleteListener eventCompleteListener) {
        db = FirebaseFirestore.getInstance();
        projects = new LinkedHashMap<String, Project>();
        projectsToDisplay = new LinkedHashMap<String, Project>();
        currentProjectIds = new ArrayList<String>();
        searchTerm = "";
        lastRefresh = Timestamp.now();
        updateList(projectIds, eventCompleteListener);
    }

    private void addProject(String projectId, EventCompleteListener eventCompleteListener) {
        DocumentReference projectRef = db.collection("Projects").document(projectId);
        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Project project = task.getResult().toObject(Project.class);
                    assert project != null;
                    project.setProjectRef(task.getResult().getReference());
                    project.setAllStrings(new EventCompleteListener() {
                        @Override
                        public void onComplete() {
                            projects.put(projectId, project);
                            notifyItemInserted(new ArrayList<String>(projects.keySet()).indexOf(projectId));
                            eventCompleteListener.onComplete();
                        }
                    });
                }
            }
        });
    }

    public void search(String term) {
        searchTerm = term.toLowerCase(Locale.ROOT);
        Project project;
        String projectString;
        if (searchTerm.length() == 0) {
            projectsToDisplay = new LinkedHashMap<String, Project>(projects);
        } else {
            projectsToDisplay.clear();
            for (Map.Entry<String, Project> entry : projects.entrySet()) {
                project = entry.getValue();
                projectString = project.getAllStrings().toLowerCase(Locale.ROOT);
                if (projectString.contains(searchTerm)) {
                    projectsToDisplay.put(entry.getKey(), entry.getValue());
                }
            }
        }

        //update recyclerview
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        RecyclerAdapterV2.ViewHolder viewHolder = new RecyclerAdapterV2.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String key = new ArrayList<String>(projectsToDisplay.keySet()).get(position);
        Project project = projectsToDisplay.get(key);
        holder.textView_pTitle.setText(project.getName());
        holder.textView_pCreator.setText(project.getCreatorString());

        if (!project.getStatus())
            holder.materialCardViewProject.setCardBackgroundColor(Color.parseColor("#fdaaaa"));
        else
            holder.materialCardViewProject.setCardBackgroundColor(Color.parseColor("#77DD77"));

        holder.viewProgressBarBackground.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);
    }

    public static String getHashMapKeyFromIndex(HashMap hashMap, int index) {
        String key = null;
        HashMap<String, Object> hs = hashMap;
        int pos = 0;
        for (Map.Entry<String, Object> entry : hs.entrySet()) {
            if (index == pos) {
                key = entry.getKey();
            }
            pos++;
        }
        return key;
    }

    public void setOnProjectListener(RecyclerAdapterV2.OnProjectListener onProjectListener) {
        this.onProjectListener = onProjectListener;
    }

    @Override
    public int getItemCount() {
        return projectsToDisplay.size();
    }

    public void updateList(List<String> newProjectIds, EventCompleteListener eventCompleteListener) {
        List<String> added = new ArrayList<String>(newProjectIds);
        List<String> removed = new ArrayList<String>(currentProjectIds);
        List<String> old = new ArrayList<String>(currentProjectIds);
        added.removeAll(currentProjectIds);
        removed.removeAll(newProjectIds);
        old.removeAll(removed);
        int addedSize = added.size();

        EventCompleteListener addingEvent = new EventCompleteListener() {
            @Override
            public void onComplete() {
                // This will run after all the new documents have been added
                for (String projectId : removed) {
                    int index = new ArrayList<String>(projects.keySet()).indexOf(projectId);
                    projects.remove(projectId);
                    notifyItemRemoved(index);
                }

                EventCompleteListener updateModifiedEvent = new EventCompleteListener() {
                    @Override
                    public void onComplete() {
                        currentProjectIds = new ArrayList<String>(newProjectIds);
                        lastRefresh = Timestamp.now();
                        search(searchTerm);
                        eventCompleteListener.onComplete();
                    }
                };

                updateModified(old, updateModifiedEvent);
            }
        };

        if (addedSize == 0) {
            addingEvent.onComplete();
        }
        addCounter = 0;
        for (String projectId : added) {
            addProject(projectId, new EventCompleteListener() {
                @Override
                public void onComplete() {
                    if (addCounter == addedSize - 1) {
                        addingEvent.onComplete();
                    }
                    addCounter++;
                }
            });
        }
    }

    private void updateModified(List<String> projectIds, EventCompleteListener updateModifiedEvent) {
        int oldSize = projectIds.size();
        if (oldSize == 0) {
            updateModifiedEvent.onComplete();
        }
        modifiedCounter = 0;
        for (String projectId : projectIds) {
            updateProject(projectId, new EventCompleteListener() {
                @Override
                public void onComplete() {
                    if (modifiedCounter == oldSize - 1) {
                        updateModifiedEvent.onComplete();
                    }
                    modifiedCounter++;
                }
            });
        }
    }

    private void updateProject(String projectId, EventCompleteListener eventCompleteListener) {
        DocumentReference projectRef = db.collection("Projects").document(projectId);
        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Timestamp lastModified = task.getResult().getTimestamp("lastModified");
                    if (lastModified.getSeconds() > lastRefresh.getSeconds()) {
                        Project project = task.getResult().toObject(Project.class);
                        assert project != null;
                        project.setProjectRef(task.getResult().getReference());
                        project.setAllStrings(new EventCompleteListener() {
                            @Override
                            public void onComplete() {
                                projects.put(projectId, project);
                                notifyItemChanged(new ArrayList<String>(projects.keySet()).indexOf(projectId));
                                eventCompleteListener.onComplete();
                            }
                        });
                    }
                    eventCompleteListener.onComplete();
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView_pCreator, textView_pTitle;
        View viewProgressBarBackground;
        ProgressBar progressBar;
        MaterialCardView materialCardViewProject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView_pCreator = itemView.findViewById(R.id.textView_pCreator);
            this.textView_pTitle = itemView.findViewById(R.id.textView_pTitle);
            this.viewProgressBarBackground = itemView.findViewById(R.id.viewProgressBarBackground);
            this.progressBar = itemView.findViewById(R.id.progressBar);
            materialCardViewProject = itemView.findViewById(R.id.materialCardView_Project);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String key = getHashMapKeyFromIndex(projectsToDisplay, getBindingAdapterPosition());
            onProjectListener.onProjectClick(getBindingAdapterPosition(),
                    projectsToDisplay.get(key).getProjectRef().getId(), projectsToDisplay.get(key));
        }
    }

    public interface OnProjectListener {
        void onProjectClick(int position, String projectID, Project project);
    }

//    public void updateList(LinkedHashMap<String, Project> projects) {
//        this.projects = projects;
//        notifyDataSetChanged();
//    }
//
//    public void updateItem(String key, LinkedHashMap<String, Project> projects) {
//        this.projects = projects;
//        int index = new ArrayList<String>(projects.keySet()).indexOf(key);
//        notifyItemChanged(index);
//    }
//
//    public void addItem(String key, LinkedHashMap<String, Project> projects) {
//        this.projects = projects;
//        int index = new ArrayList<String>(projects.keySet()).indexOf(key);
//        notifyItemInserted(index);
//    }
//
//    public void removeItem(String key, LinkedHashMap<String, Project> projects) {
//        this.projects = projects;
//        int index = new ArrayList<String>(projects.keySet()).indexOf(key);
//        notifyItemRemoved(index);
//    }
}
