package ca.macewan.capstone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import ca.macewan.capstone.Project;
import ca.macewan.capstone.R;

public class RecyclerAdapter extends FirestoreRecyclerAdapter<Project, RecyclerAdapter.ProjectViewHolder> {
    private OnProjectListener onProjectListener;

    public RecyclerAdapter(@NonNull FirestoreRecyclerOptions<Project> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project model) {
        holder.textViewProjectCreator.setText(model.getCreator());
        holder.textViewProjectName.setText(model.getName());
        for (DocumentReference d : model.getMembers()) {
            d.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    String name = task.getResult().get("name").toString();
                    String email = task.getResult().get("email").toString();
                    holder.textViewProjectMembers.append(name + " <" + email + ">\n");
                }
            });
        }
        holder.textViewProjectDesc.setText(model.getDescription());
    }

    public void setOnProjectListener(OnProjectListener onProjectListener) {
        this.onProjectListener = onProjectListener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(view);
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewProjectName;
        TextView textViewProjectDesc;
        TextView textViewProjectCreator;
        TextView textViewProjectMembers;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProjectName = itemView.findViewById(R.id.textView_pName);
            textViewProjectCreator = itemView.findViewById(R.id.textView_pCreator);
            textViewProjectMembers = itemView.findViewById(R.id.textView_pMembers);
            textViewProjectDesc = itemView.findViewById(R.id.textView_pDescription);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onProjectListener.onProjectClick(getBindingAdapterPosition(), getSnapshots().getSnapshot(getBindingAdapterPosition()).getId());
        }
    }

    public interface OnProjectListener {
        void onProjectClick(int position, String projectID);
    }
}