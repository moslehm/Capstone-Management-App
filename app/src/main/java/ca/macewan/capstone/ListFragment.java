package ca.macewan.capstone;

import android.app.Activity;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.macewan.capstone.adapter.RecyclerAdapterV2;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment implements RecyclerAdapterV2.OnProjectListener {
    private static final String TAG = "ListFragment";

    private RecyclerView recyclerViewProject;
    private RecyclerAdapterV2 recyclerAdapter;
    private SearchView searchView;
    private CheckBox checkBox_Term, checkBox_Desc;
    private FirebaseFirestore db;
    private List<String> projectIds;
    private boolean settingUp;

    public ListFragment() {
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
        settingUp = true;
    }

    public void setUp() {
        recyclerViewProject = (RecyclerView) getView().findViewById(R.id.recyclerView_Project);
        db = FirebaseFirestore.getInstance();
        projectIds = new ArrayList<String>();

        update(new EventCompleteListener() {
                   @Override
                   public void onComplete() {
                       createAdapter();
                   }
               });

        SwipeRefreshLayout swipeRefreshSingleProject = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshSingleProject.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update(new EventCompleteListener() {
                    @Override
                    public void onComplete() {
                        // Completed ID string list update
                        recyclerAdapter.updateList(projectIds, new EventCompleteListener() {
                            @Override
                            public void onComplete() {
                                // Completed updating Project objects in the adapter
                                swipeRefreshSingleProject.setRefreshing(false);
                            }
                        });
                    }
                });
            }
        });
        settingUp = false;
    }

    private void createAdapter() {
        recyclerAdapter = new RecyclerAdapterV2(projectIds, new EventCompleteListener() {
            @Override
            public void onComplete() {
                recyclerViewProject.setAdapter(recyclerAdapter);
                recyclerViewProject.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerAdapter.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
                    @Override
                    public void onProjectClick(int position, String projectID, Project project) {
                        Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
                        intent.putExtra("projectID", projectID);
                        intent.putExtra("project", project);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    void update(EventCompleteListener eventCompleteListener) {
        projectIds.clear();
        db.collection("Projects")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                projectIds.add(document.getId());
                            }
                            eventCompleteListener.onComplete();
                        }
                    }
                });
    }

    @Override
    public void onProjectClick(int position, String projectID, Project project) {
        Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
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
                recyclerAdapter.search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerAdapter.search(newText);
                return false;
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        if (settingUp) {
            setUp();
        } else {
            update(new EventCompleteListener() {
                @Override
                public void onComplete() {
                    recyclerAdapter.updateList(projectIds, new EventCompleteListener() {
                        @Override
                        public void onComplete() {
                        }
                    });
                }
            });
        }
    }
}