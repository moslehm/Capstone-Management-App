package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.macewan.capstone.adapter.RecyclerAdapter;
import ca.macewan.capstone.adapter.RecyclerAdapterV2;

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
    private FirebaseFirestore db;

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
        setUp();
    }

    public void setUp() {
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
//                checkBox_Term.setVisibility(View.VISIBLE);
//                checkBox_Desc.setVisibility(View.VISIBLE);

//                checkBoxSetUp();
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
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


    // not ideal for large dataset
    private void search(String term) {
        db = FirebaseFirestore.getInstance();
        db.collection("Projects").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentReference> documentReferenceList = new ArrayList<>();
                    ArrayList<DocumentSnapshot> documentSnapshotList = new ArrayList<>();
                    QuerySnapshot querySnapshot = task.getResult();
                    for (DocumentSnapshot ds : querySnapshot) {
                        documentSnapshotList.add(ds);
                    }
                    for (DocumentSnapshot ds : documentSnapshotList) {
                        String temp = null;
                        List<DocumentReference> profList = (List<DocumentReference>) ds.get("supervisors");
                        if (profList != null) {
                            for (DocumentReference prof : profList) {
                                prof.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().getString("name").contains(term) &&
                                                    !documentReferenceList.contains(ds.getReference())) {
                                                documentReferenceList.add(ds.getReference());
                                                RecyclerAdapterV2 recyclerAdapterV2 = new RecyclerAdapterV2(documentReferenceList);
                                                recyclerViewProject.setAdapter(recyclerAdapterV2);
                                                recyclerAdapterV2.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
                                                    @Override
                                                    public void onProjectClick(int position, String projectID) {
                                                        Intent intent = new Intent(getContext(), ProjectInfoActivity.class);
                                                        intent.putExtra("projectID", projectID);
                                                        startActivity(intent);
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                            }
                        }

                        List<String> tagList = (List<String>) ds.get("tags");
                        if (tagList != null) {
                            for (String tag : tagList) {
                                if (tag.contains(term) && !documentReferenceList.contains(ds.getReference()))
                                    documentReferenceList.add(ds.getReference());
                            }
                        }

                        temp += ds.getString("semester") + " ";
                        temp += ds.getString("year") + " ";
                        temp += ds.getString("description") + " ";
                        temp += ds.getString("name") + " ";

                        if (temp.contains(term) && !documentReferenceList.contains(ds.getReference())) {
                            documentReferenceList.add(ds.getReference());
                        }
                    }
                    recyclerAdapter.stopListening();
                    RecyclerAdapterV2 recyclerAdapterV2 = new RecyclerAdapterV2(documentReferenceList);
                    recyclerViewProject.setAdapter(recyclerAdapterV2);
                    recyclerAdapterV2.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
                        @Override
                        public void onProjectClick(int position, String projectID) {
                            Intent intent = new Intent(getContext(), ProjectInfoActivity.class);
                            intent.putExtra("projectID", projectID);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
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