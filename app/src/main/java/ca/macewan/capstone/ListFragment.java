package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.macewan.capstone.adapter.RecyclerAdapter;
import ca.macewan.capstone.adapter.RecyclerAdapterV2;
import ca.macewan.capstone.adapter.SharedMethods;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {
    private String email;
    private User user;
    private RecyclerView recyclerView_Project;
    private FirebaseFirestore db;
    private RecyclerAdapterV2 RecyclerAdapterV2;
    private View view;
    private List<DocumentReference> projectList;

    public ListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);
        db = FirebaseFirestore.getInstance();

        recyclerView_Project = (RecyclerView) view.findViewById(R.id.recyclerView_Project);
        projectList = new ArrayList<DocumentReference>();

        email = getArguments().getString("email");
        db.collection("Projects")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    System.out.println("New: " + dc.getDocument().getData());
                                    projectList.add(dc.getDocument().getReference());
                                    break;
                                case REMOVED:
                                    System.out.println("Removed: " + dc.getDocument().getData());
                                    projectList.remove(dc.getDocument().getReference());
                                    break;
                            }
                        }
                        updateView();
                    }
                });

        return view;
    }

    private void updateView() {
        View textViewEmpty = view.findViewById(R.id.textViewEmpty);
        if (projectList.size() == 0) {
            // No projects exist in DB
            textViewEmpty.setVisibility(View.VISIBLE);
            return;
        }
        textViewEmpty.setVisibility(View.GONE);
        RecyclerAdapterV2 = new RecyclerAdapterV2(projectList);
        recyclerView_Project.setAdapter(RecyclerAdapterV2);
        recyclerView_Project.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerAdapterV2.setOnProjectListener(new RecyclerAdapterV2.OnProjectListener() {
            @Override
            public void onProjectClick(int position, String projectPath) {
                Intent intent = new Intent(getContext(), ProjectInformationActivity.class);
                intent.putExtra("projectPath", projectPath);
                startActivity(intent);
            }
        });
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