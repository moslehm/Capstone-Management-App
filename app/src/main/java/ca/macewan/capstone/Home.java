package ca.macewan.capstone;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import ca.macewan.capstone.adapter.RecyclerAdapter;

public class Home extends AppCompatActivity {
    private TextView textViewRole, textViewName;
    private RecyclerView recyclerViewProject;
    private RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        setUp();
    }

    private void setUp() {
        String role = getIntent().getExtras().getString("role");
        String name = getIntent().getExtras().getString("name");
        textViewRole = (TextView) findViewById(R.id.textView_Role);
        textViewName = (TextView) findViewById(R.id.textView_Name);
        recyclerViewProject = (RecyclerView) findViewById(R.id.recyclerView_Project);
        textViewName.setText(name);
        textViewRole.setText(role);

        // This will display all the current projects in our database
        Query query = FirebaseFirestore.getInstance().collection("Projects");
        FirestoreRecyclerOptions<Project> options = new FirestoreRecyclerOptions.Builder<Project>()
                .setQuery(query, Project.class)
                .build();
        recyclerAdapter = new RecyclerAdapter(options);
        recyclerViewProject.setAdapter(recyclerAdapter);
        recyclerViewProject.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        recyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        recyclerAdapter.stopListening();
    }
}
