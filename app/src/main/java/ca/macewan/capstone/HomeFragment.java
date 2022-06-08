package ca.macewan.capstone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

import ca.macewan.capstone.adapter.RecyclerAdapterV2;
import ca.macewan.capstone.adapter.SharedMethods;

public class HomeFragment extends Fragment {
    private TextView textViewDefault;
    private View singleProjectView;
    private User user;
    private RecyclerView recyclerView_Accepted;
    private FirebaseFirestore db;
    private RecyclerAdapterV2 recyclerAdapterV2;
    ActivityResultLauncher<Intent> activityResultLauncher;
    private View view;

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
        recyclerView_Accepted = (RecyclerView) view.findViewById(R.id.recyclerView_Accepted);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    refresh();
                }

            }
        });
        recyclerAdapterV2 = new RecyclerAdapterV2(user.projects);
        recyclerView_Accepted.setAdapter(recyclerAdapterV2);
        recyclerView_Accepted.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapterV2.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
            @Override
            public void onProjectClick(int position, String projectID) {
                Intent intent = new Intent(getContext(), ProjectInfoActivityProf.class);
                intent.putExtra("projectID", projectID);
                activityResultLauncher.launch(intent);
            }
        });
//        db.collection("Users")
//                .document(user.email)
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            recyclerAdapterV2 = new RecyclerAdapterV2(user.projects);
//                            recyclerView_Accepted.setAdapter(recyclerAdapterV2);
//                            recyclerView_Accepted.setLayoutManager(new LinearLayoutManager(getActivity()));
//                            recyclerAdapterV2.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
//                                @Override
//                                public void onProjectClick(int position, String projectID) {
//                                    Intent intent = new Intent(getContext(), ProjectInfoActivityProf.class);
//                                    intent.putExtra("projectID", projectID);
//                                    activityResultLauncher.launch(intent);
//                                }
//                            });
//                        }
//                    }
//                });

        return view;
    }


//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.frag_accepted, container, false);
//        textViewDefault = (TextView) view.findViewById(R.id.textViewDefault);
//        singleProjectView = view.findViewById(R.id.singleProject);
//        recyclerView_Accepted = (RecyclerView) view.findViewById(R.id.recyclerView_Accepted);
//
//        db = FirebaseFirestore.getInstance();
//
//        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    refresh();
//                }
//
//            }
//        });
//        db.collection("Users")
//                .document(user.email)
//                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            recyclerAdapterV2 = new RecyclerAdapterV2((List<DocumentReference>) task.getResult().get("projects"));
//                            recyclerView_Accepted.setAdapter(recyclerAdapterV2);
//                            recyclerView_Accepted.setLayoutManager(new LinearLayoutManager(getActivity()));
//                            recyclerAdapterV2.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
//                                @Override
//                                public void onProjectClick(int position, String projectID) {
//                                    Intent intent = new Intent(getContext(), ProjectInfoActivityProf.class);
//                                    intent.putExtra("projectID", projectID);
//                                    activityResultLauncher.launch(intent);
//                                }
//                            });
//                        }
//                    }
//                });
//
//        return view;
//    }

    private void refresh() {
        db.collection("Users")
                .document(user.email)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (snapshot != null && snapshot.exists()) {
                            user = snapshot.toObject(User.class);
                            updateView();
                        }
                    }
                });
    }

    private void updateView() {
        final List<DocumentReference> projects = user.projects;
        int size = projects.size();
        if (size == 0) {
            // User has no projects, make sure default TextView visible
            textViewDefault.setVisibility(View.VISIBLE);
            recyclerView_Accepted.setVisibility(View.GONE);
            singleProjectView.setVisibility(View.GONE);
        }
        else if (size == 1) {
            // User only has one project, no need for a list view
            singleProjectView.setVisibility(View.VISIBLE);
            textViewDefault.setVisibility(View.GONE);
            recyclerView_Accepted.setVisibility(View.GONE);
            SharedMethods.setupProjectView(singleProjectView, projects.get(0), user.email, getActivity());
        }
        else {
            recyclerView_Accepted.setVisibility(View.VISIBLE);
            textViewDefault.setVisibility(View.GONE);
            singleProjectView.setVisibility(View.GONE);
            recyclerAdapterV2 = new RecyclerAdapterV2(user.projects);
            recyclerView_Accepted.setAdapter(recyclerAdapterV2);
            recyclerAdapterV2.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
                @Override
                public void onProjectClick(int position, String projectPath) {
                    Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
                    intent.putExtra("projectPath", projectPath);
                    activityResultLauncher.launch(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}