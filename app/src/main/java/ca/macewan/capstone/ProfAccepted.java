package ca.macewan.capstone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

import ca.macewan.capstone.adapter.RecyclerAdapter;
import ca.macewan.capstone.adapter.RecyclerAdapterV2;

public class ProfAccepted extends Fragment implements RecyclerAdapter.OnProjectListener {
    private RecyclerView recyclerView_Accepted;
    private FirebaseFirestore db;
    private RecyclerAdapter recyclerAdapter;
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.frag_accepted, container, false);
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_accepted, container, false);
        recyclerView_Accepted = view.findViewById(R.id.recyclerView_Accepted);
        db = FirebaseFirestore.getInstance();
        setUp();
        return view;
    }

    private void setUp() {
        Query query = db.collection("Projects").whereArrayContains("supervisors",
                db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString()));
        FirestoreRecyclerOptions<Project> options = new FirestoreRecyclerOptions.Builder<Project>()
                .setQuery(query, Project.class)
                .build();
        recyclerAdapter = new RecyclerAdapter(options);
        recyclerView_Accepted.setAdapter(recyclerAdapter);
        recyclerView_Accepted.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapter.setOnProjectListener(this);
        recyclerView_Accepted.setItemAnimator(null);
    }

    @Override
    public void onProjectClick(int position, String projectID) {
        Intent intent = new Intent(getContext(), ProjectInfoActivityProf.class);
        intent.putExtra("projectID", projectID);
        startActivity(intent);
    }


    @Override
    public void onStart() {
        super.onStart();
        recyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerAdapter.stopListening();
    }
}

