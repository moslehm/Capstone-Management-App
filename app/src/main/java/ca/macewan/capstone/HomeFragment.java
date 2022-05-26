package ca.macewan.capstone;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import ca.macewan.capstone.adapter.RecyclerAdapter;

public class HomeFragment extends Fragment {
    private TextView textViewRole, textViewName;
    private RecyclerView recyclerViewProject;
    private RecyclerAdapter recyclerAdapter;
    private String role;
    private String name;

    // Required empty public constructor
    public HomeFragment(String givenRole, String givenName) {
        role = givenRole;
        name = givenName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // This one was missing in your earlier code
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        textViewRole = (TextView) getView().findViewById(R.id.textView_Role);
//        textViewName = (TextView) getView().findViewById(R.id.textView_Name);
//        recyclerViewProject = (RecyclerView) getView().findViewById(R.id.recyclerView_Project);
//        setUp();
//    }
//
//    public void setUp() {
////        textViewRole = (TextView) findViewById(R.id.textView_Role);
////        textViewName = (TextView) findViewById(R.id.textView_Name);
////        recyclerViewProject = (RecyclerView) findViewById(R.id.recyclerView_Project);
//        textViewName.setText(name);
//        textViewRole.setText(role);
//
//        // This will display all the current projects in our database
//        Query query = FirebaseFirestore.getInstance().collection("Projects");
//        FirestoreRecyclerOptions<Project> options = new FirestoreRecyclerOptions.Builder<Project>()
//                .setQuery(query, Project.class)
//                .build();
//        recyclerAdapter = new RecyclerAdapter(options);
//        recyclerViewProject.setAdapter(recyclerAdapter);
//        recyclerViewProject.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerAdapter.setOnProjectListener(this);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        recyclerAdapter.startListening();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        recyclerAdapter.stopListening();
//    }
//
//    @Override
//    public void onProjectClick(int position, String projectID) {
//        System.out.println(projectID);
//    }
}