package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import ca.macewan.capstone.adapter.SharedMethods;

public class HomeFragment extends Fragment {
    private TextView textViewDefault;
    private View singleProjectView;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout linearLayoutProjectList;
    private User user;
    private FirebaseFirestore db;
    private View view;

    // Required empty public constructor
    public HomeFragment(User user) {
        this.user = user;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        textViewDefault = (TextView) view.findViewById(R.id.textViewDefault);
        singleProjectView = view.findViewById(R.id.singleProject);
        linearLayoutProjectList = (LinearLayout) view.findViewById(R.id.linearLayoutProjectList);

        db.collection("Users")
            .document(user.email)
            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (snapshot != null && snapshot.exists()) {
                        user = snapshot.toObject(User.class);
                    }
                }
            });

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshView();
            }
        });

        refreshView();
        return view;
    }


    private void refreshView() {
        // Remove all children from linear layout to add them again later if needed
        linearLayoutProjectList.removeAllViews();

        final List<DocumentReference> projects = user.projects;
        int size = projects.size();
        if (size == 0) {
            // User has no projects, make sure default TextView visible
            textViewDefault.setVisibility(View.VISIBLE);
            linearLayoutProjectList.setVisibility(View.GONE);
            singleProjectView.setVisibility(View.GONE);
        }
        else if (size == 1) {
            // User only has one project, no need for a list view
            singleProjectView.setVisibility(View.VISIBLE);
            textViewDefault.setVisibility(View.GONE);
            linearLayoutProjectList.setVisibility(View.GONE);
            SharedMethods.setupProjectView(singleProjectView, projects.get(0), getActivity());
        }
        else {
            // User has more than one, loop through projects
            linearLayoutProjectList.setVisibility(View.GONE);
            textViewDefault.setVisibility(View.GONE);
            singleProjectView.setVisibility(View.GONE);
            for (DocumentReference project : projects) {
                View projectItemView = LayoutInflater.from(getActivity()).inflate(R.layout.project_item, linearLayoutProjectList, false);
                TextView textViewProjectName = projectItemView.findViewById(R.id.textView_pTitle);
                TextView textViewProjectSem = projectItemView.findViewById(R.id.textView_pSemesterAndYear);
                TextView textViewProjectTags = projectItemView.findViewById(R.id.textView_pTags);
                textViewProjectName.setVisibility(View.VISIBLE);
                textViewProjectSem.setVisibility(View.VISIBLE);
                textViewProjectTags.setVisibility(View.VISIBLE);
                projectItemView.findViewById(R.id.textView_pCreator).setVisibility(View.GONE);
                projectItemView.findViewById(R.id.imageView_pCreator).setVisibility(View.GONE);
                project.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            textViewProjectName.setText(documentSnapshot.getString("name"));
                            String semesterAndYear = documentSnapshot.getString("semester") + " " + documentSnapshot.getString("year");
                            textViewProjectSem.setText(semesterAndYear);
                            ArrayList<String> tagsList = (ArrayList<String>) documentSnapshot.get("tags");
                            if (tagsList != null) {
                                textViewProjectTags.setText(android.text.TextUtils.join(", ", tagsList));
                            }
                        }
                    }
                });
                projectItemView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
                        intent.putExtra("projectPath", project.getPath());
                        startActivity(intent);
                    }
                });
                linearLayoutProjectList.addView(projectItemView);
            }
            linearLayoutProjectList.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(false);
    }
}