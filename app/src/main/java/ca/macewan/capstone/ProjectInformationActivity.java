package ca.macewan.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.macewan.capstone.adapter.SharedMethods;

public class ProjectInformationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    DocumentReference projectRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_information);
        db = FirebaseFirestore.getInstance();
        String projectPath = getIntent().getExtras().getString("projectPath");
        projectRef = db.document(projectPath);

        setupView();
    }

    private void setupView() {
        View projectView = findViewById(R.id.projectLayout);
        SharedMethods.setupProjectView(projectView, projectRef, this);
    }
}