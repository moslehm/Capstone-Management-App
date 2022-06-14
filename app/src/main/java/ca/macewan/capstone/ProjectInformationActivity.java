package ca.macewan.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ca.macewan.capstone.adapter.SharedMethods;

public class ProjectInformationActivity extends AppCompatActivity {
    Button button_Edit, button_Delete;
    FirebaseFirestore db;
    String projectID;
    DocumentReference projectRef;
    List<DocumentReference> memberRefList;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_information);
        db = FirebaseFirestore.getInstance();
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        setUp();
//        refreshButtons();
    }

    private void setUp() {
        button_Edit = findViewById(R.id.button_Edit);
        button_Delete = findViewById(R.id.button_Delete);

        projectID = getIntent().getExtras().getString("projectID");
        projectRef = db.collection("Projects").document(projectID);
        View projectView = findViewById(R.id.projectLayout);
        SharedMethods.setupProjectView(projectView, projectRef, email, this);

        button_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProjectInformationActivity.this)
                        .setMessage("Delete this project?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // TODO: Delete project
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        });

        button_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Launch edit activity
            }
        });
    }

//    private void refreshButtons() {
//        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                DocumentSnapshot documentSnapshot = task.getResult();
//                if (documentSnapshot.exists()) {
//                    memberRefList = (ArrayList<DocumentReference>) documentSnapshot.get("members");
//                    buttonQuitRefresh(memberRefList);
//                }
//            }
//        });
//    }

}