package ca.macewan.capstone.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;

import ca.macewan.capstone.Project;
import ca.macewan.capstone.R;

public class RecyclerAdapter extends FirestoreRecyclerAdapter<Project, RecyclerAdapter.ProjectViewHolder> {
    private OnProjectListener onProjectListener;

    public RecyclerAdapter(@NonNull FirestoreRecyclerOptions<Project> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProjectViewHolder holder, int position, @NonNull Project model) {
        model.getCreator().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String creator = task.getResult().get("name") + " <" + task.getResult().get("email") + ">";
                    holder.textViewProjectCreator.setText(creator);

                    if (!task.getResult().getBoolean("status"))
                        holder.imageView_status.setImageResource(R.drawable.ic_baseline_closed_red);
                    else
                        holder.imageView_status.setImageResource(R.drawable.ic_baseline_open_green);
                }
            }
        });
        holder.textViewProjectName.setText(model.getName());
        String term = model.getSemester() + " " + model.getYear();
        holder.textView_pTerm.setText(term);

        for (String tag : model.getTags())
            holder.textView_Tags.append(tag + " ");

        holder.viewProgressBarBackground.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);
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
        TextView textViewProjectName, textView_pTerm, textView_Tags;
//        TextView textViewProjectDesc;
        TextView textViewProjectCreator;
//        TextView textViewProjectMembers;
        View viewProgressBarBackground;
        ProgressBar progressBar;
        ImageView imageView_status;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProjectName = itemView.findViewById(R.id.textView_pTitle);
            textViewProjectCreator = itemView.findViewById(R.id.textView_pCreator);
            imageView_status = itemView.findViewById(R.id.imageView_status);
            viewProgressBarBackground = itemView.findViewById(R.id.viewProgressBarBackground);
            progressBar = itemView.findViewById(R.id.progressBar);
            textView_pTerm = itemView.findViewById(R.id.textView_pSemesterAndYear);
            textView_Tags = itemView.findViewById(R.id.textView_pTags);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onProjectListener.onProjectClick(getBindingAdapterPosition(),
                    getSnapshots().getSnapshot(getBindingAdapterPosition()).getId());
        }
    }

    public interface OnProjectListener {
        void onProjectClick(int position, String projectID);
    }
}
