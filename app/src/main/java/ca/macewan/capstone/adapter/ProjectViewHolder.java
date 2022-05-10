package ca.macewan.capstone.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ca.macewan.capstone.R;

public class ProjectViewHolder extends RecyclerView.ViewHolder {
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
    }
}
