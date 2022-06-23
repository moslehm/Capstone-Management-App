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
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private OnListListener onListListener;
    private String screenType;
    private boolean isSupervisor;
    private ProgressBar progressBar;
    private TextView textViewEmpty;

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
        screenType = getArguments().getString("screenType");
        isSupervisor = getArguments().getBoolean("isSupervisor");
        String emptyListText = getArguments().getString("emptyListText");

        textViewEmpty = (TextView) getView().findViewById(R.id.textViewEmpty);
        textViewEmpty.setText(emptyListText);

        progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewProject = (RecyclerView) getView().findViewById(R.id.recyclerView_Project);
        db = FirebaseFirestore.getInstance();

        onListListener.onListUpdate(screenType, new OnUpdateListener() {
            @Override
            public void onUpdateComplete(ArrayList<String> projectIds) {
                if (projectIds.size() != 0) {
                    textViewEmpty.setVisibility(View.GONE);
                    createAdapter(projectIds); // Hides progress bar when its done setting up
                } else {
                    textViewEmpty.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Request update for projectIds
                onListListener.onListUpdate(screenType, new OnUpdateListener() {
                    @Override
                    public void onUpdateComplete(ArrayList<String> projectIds) {
                        refresh(projectIds, new EventCompleteListener() {
                            @Override
                            public void onComplete() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                });
            }
        });
        settingUp = false;
    }

    private void createAdapter(ArrayList<String> projectIds) {
        recyclerAdapter = new RecyclerAdapterV2(projectIds, screenType, new EventCompleteListener() {
            @Override
            public void onComplete() {
                recyclerViewProject.setAdapter(recyclerAdapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                recyclerViewProject.setLayoutManager(linearLayoutManager);
                recyclerViewProject.setItemAnimator(null);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewProject.getContext(),
                        linearLayoutManager.getOrientation());
                recyclerViewProject.addItemDecoration(dividerItemDecoration);
                recyclerAdapter.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
                    @Override
                    public void onProjectClick(int position, String projectID, Project project) {
                        Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
                        intent.putExtra("projectID", projectID);
                        intent.putExtra("project", project);
                        intent.putExtra("isSupervisor", isSupervisor);
                        startActivity(intent);
                    }
                });
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void refresh(ArrayList<String> projectIds, EventCompleteListener eventCompleteListener) {
        this.projectIds = projectIds;
        if (recyclerAdapter == null) {
            if (projectIds.size() != 0) {
                textViewEmpty.setVisibility(View.GONE);
                createAdapter(projectIds);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
            eventCompleteListener.onComplete();
            return;
        }
        // projectIds update is complete

        recyclerAdapter.updateList(projectIds, new EventCompleteListener() {
            @Override
            public void onComplete() {
                progressBar.setVisibility(View.GONE);
                textViewEmpty.setVisibility(View.GONE);
                eventCompleteListener.onComplete();
            }
        });
        if (projectIds.size() == 0) {
            textViewEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProjectClick(int position, String projectID, Project project) {
        Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
        intent.putExtra("projectID", projectID);
        intent.putExtra("project", project);
        intent.putExtra("isSupervisor", isSupervisor);
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (screenType != null) {
            //inflate menu
            inflater.inflate(R.menu.menu_options, menu);
            MenuItem item = menu.findItem(R.id.app_bar_search);
            if (Objects.equals(screenType, "homeList") || Objects.equals(screenType, "completedList")) {
                menu.findItem(R.id.action_add).setVisible(false);
            }
            searchView = (SearchView) item.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (recyclerAdapter != null) {
                        recyclerAdapter.search(query);
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (recyclerAdapter != null) {
                        recyclerAdapter.search(newText);
                    }
                    return false;
                }
            });
            super.onCreateOptionsMenu(menu, inflater);
        }
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
            onListListener.onListUpdate(screenType, new OnUpdateListener() {
                @Override
                public void onUpdateComplete(ArrayList<String> projectIds) {
                    refresh(projectIds, new EventCompleteListener() {
                        @Override
                        public void onComplete() {
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (recyclerAdapter != null && hidden) {
            recyclerAdapter.search("");
        }
        if (!hidden){
            onListListener.onListUpdate(screenType, new OnUpdateListener() {
                @Override
                public void onUpdateComplete(ArrayList<String> projectIds) {
                    refresh(projectIds, new EventCompleteListener() {
                        @Override
                        public void onComplete() {
                        }
                    });
                }
            });
        }
    }

    public void setListener(OnListListener onListListener) {
        this.onListListener = onListListener;
    }

    public interface OnListListener {
        void onListUpdate(String fragmentName, OnUpdateListener onUpdateListener);
    }

    public interface OnUpdateListener {
        void onUpdateComplete(ArrayList<String> projectIds);
    }
}