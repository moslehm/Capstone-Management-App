package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.Objects;

import ca.macewan.capstone.adapter.HomeRecyclerAdapter;
import ca.macewan.capstone.adapter.SharedMethods;

public class HomeFragment extends Fragment implements ListFragment.OnListListener {
    private static final String TAG = "HomeFragment";
    private static final int HAS_NONE = 0;
    private static final int HAS_ONE = 1;
    private static final int HAS_MORE_THAN_ONE = 2;

    private TextView textViewDefault;
    private View singleProjectView;
    private String email;
    private ListFragment listFragment;
    private FirebaseFirestore db;
    private View view;
    private int prevSize;
    private SwipeRefreshLayout swipeRefreshRecycler;
    private SwipeRefreshLayout swipeRefreshSingleProject;
    private ArrayList<String> projectIds;
    private int currentState;
    private boolean isVisible;
    private boolean isOwnerOfSingleProject;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        textViewDefault = (TextView) view.findViewById(R.id.textViewDefault);
        singleProjectView = view.findViewById(R.id.singleProject);
        prevSize = -1;
        email = getArguments().getString("email");

        swipeRefreshRecycler = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshRecycler);
        swipeRefreshRecycler.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshRecycler.setRefreshing(false);
            }
        });
        swipeRefreshSingleProject = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshSingleProject);
        swipeRefreshSingleProject.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                swipeRefreshSingleProject.setRefreshing(false);
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("screenType", "homeList");
        bundle.putBoolean("isSupervisor", false);
        listFragment = new ListFragment();
        listFragment.setArguments(bundle);
        listFragment.setListener(this);
        SharedMethods.createFragment(getChildFragmentManager(), R.id.listFrameLayout, listFragment, "homeList");

        return view;
    }

    private void refresh() {
        db.collection("Users")
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot snapshot = task.getResult();
                        Log.d(TAG, "User fields modified: " + snapshot.getData());
                        projectIds = (ArrayList<String>) task.getResult().get("projects");
                        updateView();
                    }
                });
    }

    private void updateView() {
        if (projectIds == null) {
            currentState = HAS_NONE;
            // User has no projects, make sure default TextView visible
            textViewDefault.setVisibility(View.VISIBLE);
            SharedMethods.hideFragment(getChildFragmentManager(), listFragment);
            singleProjectView.setVisibility(View.GONE);
            requireActivity().invalidateOptionsMenu();
            return;
        }
        int size = projectIds.size();
        if (prevSize != 0 && size == 0) {
            currentState = HAS_NONE;
            // User has no projects, make sure default TextView visible
            textViewDefault.setVisibility(View.VISIBLE);
            SharedMethods.hideFragment(getChildFragmentManager(), listFragment);
            singleProjectView.setVisibility(View.GONE);
            requireActivity().invalidateOptionsMenu();
        }
        else if (size == 1) {
            currentState = HAS_ONE;
            DocumentReference projectRef = FirebaseFirestore.getInstance().collection("Projects").document(projectIds.get(0));
            // User only has one project, no need for a list view
            if (prevSize != 1) {
                singleProjectView.setVisibility(View.VISIBLE);
                textViewDefault.setVisibility(View.GONE);
                SharedMethods.hideFragment(getChildFragmentManager(), listFragment);
                swipeRefreshSingleProject.setEnabled(true);
                swipeRefreshSingleProject.setVisibility(View.VISIBLE);
                swipeRefreshRecycler.setEnabled(false);
                swipeRefreshRecycler.setVisibility(View.GONE);
                requireActivity().invalidateOptionsMenu();
            }
            projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentReference creatorRef = task.getResult().getDocumentReference("creator");
                        creatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    String creatorEmail = task.getResult().getString("email");
                                    if (Objects.equals(creatorEmail, email))
                                        isOwnerOfSingleProject = true;
                                    else {
                                        isOwnerOfSingleProject = false;
                                    }
                                }
                            }
                        });
                    }
                }
            });
            SharedMethods.setupProjectView(singleProjectView, projectRef, email, getActivity());
        }
        else if (size > 1) {
            currentState = HAS_MORE_THAN_ONE;
            if (prevSize < 2) {
                SharedMethods.showFragment(getChildFragmentManager(), listFragment);
                textViewDefault.setVisibility(View.GONE);
                singleProjectView.setVisibility(View.GONE);
                swipeRefreshRecycler.setEnabled(true);
                swipeRefreshRecycler.setVisibility(View.VISIBLE);
                swipeRefreshSingleProject.setEnabled(false);
                swipeRefreshSingleProject.setVisibility(View.GONE);
                requireActivity().invalidateOptionsMenu();
            }
        }
        prevSize = size;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflate menu
        inflater.inflate(R.menu.menu_information_proposal, menu);
        if (currentState == HAS_NONE || currentState == HAS_MORE_THAN_ONE) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_quit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        } else if (currentState == HAS_ONE) {
            menu.findItem(R.id.action_edit).setVisible(true);
            if (isOwnerOfSingleProject)
                menu.findItem(R.id.action_delete).setVisible(true);
            else
                menu.findItem(R.id.action_quit).setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentState == HAS_ONE) {
            int id = item.getItemId();
            DocumentReference projectRef = FirebaseFirestore.getInstance().collection("Projects").document(projectIds.get(0));
            DocumentReference userRef = db.collection("Users").document(email);
            if (id == R.id.action_edit) {
                Intent intent = new Intent(getContext(), ProposalEditActivity.class);
                intent.putExtra("projectID", projectRef.getId());
                intent.putExtra("email", email);
                startActivity(intent);
            } else if (id == R.id.action_quit) {
                SharedMethods.quitProject(userRef, projectRef, getContext(), new EventCompleteListener() {
                    @Override
                    public void onComplete() {
                        refresh();
                    }
                });
            } else if (id == R.id.action_delete) {
                SharedMethods.deleteProject(userRef, projectRef, getContext(), new EventCompleteListener() {
                    @Override
                    public void onComplete() {
                        refresh();
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        isVisible = !hidden;
        if (isVisible) {
            refresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible) {
            refresh();
        }
    }


    @Override
    public void onListUpdate(String fragmentName, ListFragment.OnUpdateListener onUpdateListener) {
        db.collection("Users")
                .document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            projectIds = (ArrayList<String>) task.getResult().get("projects");
                            if (projectIds == null) {
                                onUpdateListener.onUpdateComplete(new ArrayList<String>());
                            } else {
                                onUpdateListener.onUpdateComplete(projectIds);
                            }
                        }
                    }
                });
    }
}