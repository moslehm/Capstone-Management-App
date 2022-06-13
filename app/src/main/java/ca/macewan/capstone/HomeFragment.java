package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import org.w3c.dom.Document;

import java.util.List;
import java.util.Objects;

import ca.macewan.capstone.adapter.HomeRecyclerAdapter;
import ca.macewan.capstone.adapter.SharedMethods;

public class HomeFragment extends Fragment {
    private TextView textViewDefault;
    private View singleProjectView;
    private String email;
    private User user;
    private RecyclerView recyclerView_Accepted;
    private FirebaseFirestore db;
    private HomeRecyclerAdapter homeRecyclerAdapter;
    private View view;
    private int prevSize;
    private Menu menu;
    private boolean fragmentVisible;
    private boolean updateNeeded;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        recyclerView_Accepted = (RecyclerView) view.findViewById(R.id.recyclerView_Accepted);
        prevSize = -1;
        fragmentVisible = true;

        email = getArguments().getString("email");
        db.collection("Users")
                .document(email)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (snapshot != null && snapshot.exists()) {
                            System.out.println("Firebase user updated");
                            user = snapshot.toObject(User.class);
                            if (fragmentVisible) {
                                updateView();
                            } else {
                                if (user.projects.size() > 1) {
                                    if (prevSize <= 1)
                                        updateNeeded = true;
                                    else
                                        updateList();
                                } else
                                    updateNeeded = true;
                            }
                        }
                    }
                });

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateView();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    private void updateView() {
        final List<DocumentReference> projects = user.projects;
        if (projects == null) {
            // User has no projects, make sure default TextView visible
            textViewDefault.setVisibility(View.VISIBLE);
            recyclerView_Accepted.setVisibility(View.GONE);
            singleProjectView.setVisibility(View.GONE);
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_edit).setVisible(false);
            return;
        }
        int size = projects.size();
        if (prevSize != 0 && size == 0) {
            // User has no projects, make sure default TextView visible
            textViewDefault.setVisibility(View.VISIBLE);
            recyclerView_Accepted.setVisibility(View.GONE);
            singleProjectView.setVisibility(View.GONE);
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_edit).setVisible(false);
        }
        else if (size == 1) {
            // User only has one project, no need for a list view
            if (prevSize != 1) {
                singleProjectView.setVisibility(View.VISIBLE);
                textViewDefault.setVisibility(View.GONE);
                recyclerView_Accepted.setVisibility(View.GONE);
                SharedMethods.setupProjectView(singleProjectView, projects.get(0), email, getActivity());
            }
            enableMenuButtons(projects.get(0));
        }
        else if (size > 1) {
            if (prevSize < 2) {
                recyclerView_Accepted.setVisibility(View.VISIBLE);
                textViewDefault.setVisibility(View.GONE);
                singleProjectView.setVisibility(View.GONE);
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_edit).setVisible(false);
            }
            updateList();
        }
        prevSize = size;
    }

    private void updateList() {
        homeRecyclerAdapter = new HomeRecyclerAdapter(user.projects);
        recyclerView_Accepted.setAdapter(homeRecyclerAdapter);
        recyclerView_Accepted.setLayoutManager(new LinearLayoutManager(getActivity()));
        homeRecyclerAdapter.setOnProjectListener(new HomeRecyclerAdapter.OnProjectListener() {
            @Override
            public void onProjectClick(int position, String projectPath) {
                Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
                intent.putExtra("projectPath", projectPath);
                startActivity(intent);
            }
        });
    }

    private void enableMenuButtons(DocumentReference project) {
        project.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                    menu.findItem(R.id.action_delete).setVisible(true);
                                else {
                                    menu.findItem(R.id.action_quit).setVisible(true);
                                }
                                menu.findItem(R.id.action_edit).setVisible(true);
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        this.menu = menu;
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_quit).setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        if (user != null) {
            if (updateNeeded) {
                System.out.println("updating view");
                updateView();
                updateNeeded = false;
            } else if (user.projects.size() == 1 && prevSize == 1)
                enableMenuButtons(user.projects.get(0));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        DocumentReference projectRef = user.projects.get(0);
        DocumentReference userRef = db.collection("Users").document(email);
        if (id == R.id.action_edit) {
            Intent intent = new Intent(getContext(), ProposalEditActivity.class);
            intent.putExtra("projectPath", projectRef.getPath());
            intent.putExtra("email", email);
            startActivity(intent);
        } else if (id == R.id.action_quit) {
            SharedMethods.quitProject(userRef, projectRef, getContext(), new EventCompleteListener() {
                @Override
                public void onComplete() {
                    updateView();
                }
            });
        } else if (id == R.id.action_delete) {
            SharedMethods.deleteProject(userRef, projectRef, getContext(), new EventCompleteListener() {
                @Override
                public void onComplete() {
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        fragmentVisible = !hidden;
    }
}