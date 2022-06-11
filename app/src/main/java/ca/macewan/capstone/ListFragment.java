package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;

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
    private User user;
    private SearchView searchView;
    private CheckBox checkBox_Term, checkBox_Desc;

    public ListFragment(User user) {
        this.user = user;
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
        recyclerViewProject = (RecyclerView) getView().findViewById(R.id.recyclerView_Project);
        checkBox_Desc = getView().findViewById(R.id.checkBox_Desc);
        checkBox_Term = getView().findViewById(R.id.checkBox_Term);

        setUp();
    }

    public void setUp() {
        // This will display all the current projects in our database
        checkBox_Term.setVisibility(View.GONE);
        checkBox_Desc.setVisibility(View.GONE);

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
        MenuItem item = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                search(newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox_Term.setVisibility(View.VISIBLE);
                checkBox_Desc.setVisibility(View.VISIBLE);

                checkBoxSetUp();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                checkBox_Term.setVisibility(View.GONE);
                checkBox_Desc.setVisibility(View.GONE);
                recyclerAdapter.stopListening();

                Query query = FirebaseFirestore.getInstance().collection("Projects");
                FirestoreRecyclerOptions<Project> options = new FirestoreRecyclerOptions.Builder<Project>()
                        .setQuery(query, Project.class)
                        .build();
                recyclerAdapter = new RecyclerAdapter(options);
                recyclerViewProject.setAdapter(recyclerAdapter);
                recyclerAdapter.setOnProjectListener(ListFragment.this);
                recyclerViewProject.setItemAnimator(null);
                recyclerAdapter.startListening();
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void checkBoxSetUp() {
        checkBox_Desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox_Term.setEnabled(!checkBox_Desc.isChecked());
            }
        });

        checkBox_Term.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox_Desc.setEnabled(!checkBox_Term.isChecked());
            }
        });
    }

    // prefix search only
    private void search(String term) {
        Query query = null;
        if (checkBox_Term.isChecked()) {
            recyclerAdapter.stopListening();
            query = FirebaseFirestore.getInstance().collection("Projects")
                    .whereLessThanOrEqualTo("semester", term + '\uf8ff')
                    .whereGreaterThanOrEqualTo("semester", term);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot querySnapshot = task.getResult();
                    System.out.println(querySnapshot.size());
                }
            });
        }

            FirestoreRecyclerOptions<Project> newOptions = new FirestoreRecyclerOptions.Builder<Project>()
                    .setQuery(query, Project.class)
                    .build();
            recyclerAdapter = new RecyclerAdapter(newOptions);
            recyclerViewProject.setAdapter(recyclerAdapter);
            recyclerAdapter.setOnProjectListener(this);
            recyclerViewProject.setItemAnimator(null);
            recyclerAdapter.startListening();
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