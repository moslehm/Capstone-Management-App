package ca.macewan.capstone;

import android.content.DialogInterface;
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

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import ca.macewan.capstone.adapter.SharedMethods;

public class HomeFragment extends Fragment implements ListFragment.OnListListener {
    private static final String TAG = "HomeFragment";
    private static final int OTHER = 0;
    private static final int HAS_ONE = 1;

    private String email;
    private ListFragment listFragment;
    private FirebaseFirestore db;
    private View view;
    private int prevSize;
    private ArrayList<String> projectIds;
    private int currentState;
    private boolean isVisible;
    private View progressBar;
    private View viewProgressBarBackground;
    private DocumentReference userRef;
    // Used if the user ever has a single project
    private View singleProjectView;
    private boolean isOwnerOfSingleProject;
    private View markAsCompleteButton;
    private SwipeRefreshLayout swipeRefreshSingleProject;
    private DocumentReference projectRef;

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

        progressBar = view.findViewById(R.id.progressBar);
        viewProgressBarBackground = view.findViewById(R.id.viewProgressBarBackground);
        email = getArguments().getString("email");
        userRef = db.collection("Users").document(email);
        prevSize = -1;
        // If the user has a single project
        singleProjectView = view.findViewById(R.id.singleProject);
        markAsCompleteButton = singleProjectView.findViewById(R.id.markAsCompleteButton);
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
        bundle.putString("emptyListText", "You have no projects\nCreate or join one in the list tab");
        listFragment = new ListFragment();
        listFragment.setArguments(bundle);
        listFragment.setListener(this);
        SharedMethods.createFragment(getChildFragmentManager(), R.id.listFrameLayout, listFragment, "homeList");

        return view;
    }

    private void refresh() {
        progressBar.setVisibility(View.VISIBLE);
        viewProgressBarBackground.setVisibility(View.VISIBLE);
        db.collection("Users")
            .document(email)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot snapshot = task.getResult();
                    Log.d(TAG, "User updated: " + snapshot.getData());
                    projectIds = (ArrayList<String>) task.getResult().get("projects");
                    if (projectIds == null) {
                        projectIds = new ArrayList<String>();
                    }
                    updateView();
                }
            });
    }

    private void updateView() {
        int size = projectIds.size();
        if (size == 1) {
            // User only has one project, no need for a list view
            currentState = HAS_ONE;
            projectRef = FirebaseFirestore.getInstance().collection("Projects").document(projectIds.get(0));
            if (prevSize != 1) {
                SharedMethods.hideFragment(getChildFragmentManager(), listFragment);
                refreshSingleProject(projectRef, new EventCompleteListener() {
                    @Override
                    public void onComplete() {
                        singleProjectView.setVisibility(View.VISIBLE);
                        swipeRefreshSingleProject.setEnabled(true);
                        swipeRefreshSingleProject.setVisibility(View.VISIBLE);
                        SharedMethods.setupProjectView(singleProjectView, projectRef, email, getActivity());
                        requireActivity().invalidateOptionsMenu();
                        progressBar.setVisibility(View.GONE);
                        viewProgressBarBackground.setVisibility(View.GONE);
                    }
                });
                markAsCompleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setMessage("Mark project as complete?")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Move project id to from projects to completed
                                        userRef.update("completed", FieldValue.arrayUnion(projectRef.getId()));
                                        userRef.update("projects", FieldValue.arrayRemove(projectRef.getId()));
                                        // Add project to the "Complete" collection and remove it from Projects
                                        projectRef.update("isComplete", true);
                                        // Remove supervisor and members in project
                                        db.collection("Users").whereArrayContains("projects", projectRef.getId())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        for (QueryDocumentSnapshot supervisor : task.getResult()) {
                                                            supervisor.getReference().update("projects", FieldValue.arrayRemove(projectRef.getId()));
                                                            supervisor.getReference().update("completed", FieldValue.arrayUnion(projectRef.getId()));
                                                        }
                                                    }
                                                });
                                        // Remove invites from professors
                                        db.collection("Users").whereArrayContains("invited", projectRef.getId())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        for (QueryDocumentSnapshot supervisor : task.getResult()) {
                                                            supervisor.getReference().update("invited", FieldValue.arrayRemove(projectRef.getId()));
                                                        }
                                                    }
                                                });
                                        refresh();
                                    }
                                })
                                .show();
                    }
                });
            } else {
                refreshSingleProject(projectRef, new EventCompleteListener() {
                    @Override
                    public void onComplete() {
                        SharedMethods.setupProjectView(singleProjectView, projectRef, email, getActivity());
                        progressBar.setVisibility(View.GONE);
                        viewProgressBarBackground.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            progressBar.setVisibility(View.GONE);
            viewProgressBarBackground.setVisibility(View.GONE);
            currentState = OTHER;
            if (prevSize - size != 0) {
                SharedMethods.showFragment(getChildFragmentManager(), listFragment);
//                textViewDefault.setVisibility(View.GONE);
                singleProjectView.setVisibility(View.GONE);
//                swipeRefreshRecycler.setEnabled(true);
//                swipeRefreshRecycler.setVisibility(View.VISIBLE);
                swipeRefreshSingleProject.setEnabled(false);
                swipeRefreshSingleProject.setVisibility(View.GONE);
                requireActivity().invalidateOptionsMenu();
            }
            if (prevSize != -1) {
                listFragment.refresh(projectIds, new EventCompleteListener() {
                    @Override
                    public void onComplete() {
                    }
                });
            }
        }
        prevSize = size;
    }

    private void refreshSingleProject(DocumentReference projectRef, EventCompleteListener eventCompleteListener) {
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
                                isOwnerOfSingleProject = Objects.equals(creatorEmail, email);
                                eventCompleteListener.onComplete();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflate menu
        inflater.inflate(R.menu.menu_information_proposal, menu);
        if (currentState == OTHER) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_quit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        } else if (currentState == HAS_ONE) {
            menu.findItem(R.id.action_edit).setVisible(true);
            if (isOwnerOfSingleProject) {
                menu.findItem(R.id.action_delete).setVisible(true);
                markAsCompleteButton.setVisibility(View.VISIBLE);
            }
            else
                menu.findItem(R.id.action_quit).setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (currentState == HAS_ONE) {
            int id = item.getItemId();
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
                            projectIds = new ArrayList<String>();
                        }
                        onUpdateListener.onUpdateComplete(projectIds);
                    }
                }
            });
    }
}