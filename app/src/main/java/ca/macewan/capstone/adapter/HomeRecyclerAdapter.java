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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.macewan.capstone.R;

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerAdapter.ViewHolder> {
    List<DocumentReference> documentReferenceList;
    OnProjectListener onProjectListener;

    public HomeRecyclerAdapter(List<DocumentReference> documentReferenceList) {
        this.documentReferenceList = documentReferenceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        HomeRecyclerAdapter.ViewHolder viewHolder = new HomeRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        documentReferenceList.get(position).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    holder.textView_pTitle.setText(snapshot.getString("name"));
                    String semesterAndYear = snapshot.getString("semester") + " " + snapshot.getString("year");
                    holder.textView_pSemesterAndYear.setText(semesterAndYear);
                    ArrayList<String> tagsList = (ArrayList<String>) snapshot.get("tags");
                    if (tagsList != null) {
                        holder.textView_pTags.setText(android.text.TextUtils.join(", ", tagsList));
                    }

                    if (!snapshot.getBoolean("status"))
                        holder.imageView_status.setImageResource(R.drawable.ic_baseline_closed_red);
                    else
                        holder.imageView_status.setImageResource(R.drawable.ic_baseline_open_green);
                }
                holder.viewProgressBarBackground.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void setOnProjectListener(HomeRecyclerAdapter.OnProjectListener onProjectListener) {
        this.onProjectListener = onProjectListener;
    }

    @Override
    public int getItemCount() {
        return documentReferenceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView_pTitle, textView_pSemesterAndYear, textView_pTags;
        View viewProgressBarBackground;
        ProgressBar progressBar;
        ImageView imageView_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView_pTitle = itemView.findViewById(R.id.textView_pTitle);
            this.textView_pSemesterAndYear = itemView.findViewById(R.id.textView_pSemesterAndYear);
            this.textView_pTags = itemView.findViewById(R.id.textView_pTags);
            this.viewProgressBarBackground = itemView.findViewById(R.id.viewProgressBarBackground);
            this.imageView_status = itemView.findViewById(R.id.imageView_status);
            this.progressBar = itemView.findViewById(R.id.progressBar);
            textView_pTitle.setVisibility(View.VISIBLE);
            textView_pSemesterAndYear.setVisibility(View.VISIBLE);
            textView_pTags.setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.textView_pCreator).setVisibility(View.GONE);
            itemView.findViewById(R.id.imageView_pCreator).setVisibility(View.GONE);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String projectId = documentReferenceList.get(getBindingAdapterPosition()).getId();
            onProjectListener.onProjectClick(getBindingAdapterPosition(), projectId);
        }
    }

    public interface OnProjectListener {
        void onProjectClick(int position, String projectID);
    }

    public void updateList(List<DocumentReference> list){
        documentReferenceList = list;
        notifyDataSetChanged();
    }
}
