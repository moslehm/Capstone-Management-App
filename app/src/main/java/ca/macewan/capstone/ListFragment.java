package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import ca.macewan.capstone.adapter.RecyclerAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements RecyclerAdapter.OnProjectListener {
    private TextView textViewRole, textViewName;
    private RecyclerView recyclerViewProject;
    private RecyclerAdapter recyclerAdapter;
    private String role;
    private String name;

    public ListFragment(String givenRole, String givenName) {
        // Required empty public constructor
        role = givenRole;
        name = givenName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textViewRole = (TextView) getView().findViewById(R.id.textView_Role);
        textViewName = (TextView) getView().findViewById(R.id.textView_Name);
        recyclerViewProject = (RecyclerView) getView().findViewById(R.id.recyclerView_Project);
        setUp();
    }

    public void setUp() {
//        textViewRole = (TextView) findViewById(R.id.textView_Role);
//        textViewName = (TextView) findViewById(R.id.textView_Name);
//        recyclerViewProject = (RecyclerView) findViewById(R.id.recyclerView_Project);
        textViewName.setText(name);
        textViewRole.setText(role);

        // This will display all the current projects in our database
        Query query = FirebaseFirestore.getInstance().collection("Projects");
        FirestoreRecyclerOptions<Project> options = new FirestoreRecyclerOptions.Builder<Project>()
                .setQuery(query, Project.class)
                .build();
        recyclerAdapter = new RecyclerAdapter(options);
        recyclerViewProject.setAdapter(recyclerAdapter);
        recyclerViewProject.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerAdapter.setOnProjectListener(this);
        recyclerViewProject.setItemAnimator(null);
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

    @Override
    public void onProjectClick(int position, String projectID) {
        Intent intent = new Intent(getContext(), ProjectInfoActivity.class);
        intent.putExtra("projectID", projectID);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflate menu
        inflater.inflate(R.menu.menu_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent menuIntent = new Intent(getActivity(), ProposalCreationActivity.class);
            startActivity(menuIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}