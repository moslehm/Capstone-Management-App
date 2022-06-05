package ca.macewan.capstone;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.ArrayList;

import ca.macewan.capstone.adapter.SharedMethods;
import uk.co.onemandan.materialtextview.MaterialTextView;

public class ProjectInfoActivity extends AppCompatActivity {
//    TextView title, creator, description, members, supervisors;
    Button button_Join;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String projectID;
    MaterialTextView materialTextView_Creator, materialTextView_Title, materialTextView_Supervisor,
            materialTextView_Members, materialTextView_Descriptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);
        setUp();
    }

    private void setUp() {
        button_Join = findViewById(R.id.button_Join);
        materialTextView_Creator = findViewById(R.id.textView_Creator);
        materialTextView_Title = findViewById(R.id.textiew_Title);
        materialTextView_Members = findViewById(R.id.textView_Members);
        materialTextView_Supervisor = findViewById(R.id.textView_Supervisor);
        materialTextView_Descriptions = findViewById(R.id.textView_Description);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        projectID = getIntent().getExtras().getString("projectID");
        db.collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    materialTextView_Title.setContentText(documentSnapshot.getString("name"), null);
                    materialTextView_Title.setLabelText("Title");
                    materialTextView_Descriptions.setContentText(documentSnapshot.getString("description"), null);
                    materialTextView_Creator.setLabelText("Creator");
                    materialTextView_Descriptions.setLabelText("Description");
                    materialTextView_Members.setLabelText("Member(s)");
                    materialTextView_Supervisor.setLabelText("Supervisor(s)");
                    button_Join.setText("Join");

                    // display members and handle a case where no one has joined yet
                    try {
                        SharedMethods.displayItems(documentSnapshot.get("members"), materialTextView_Members);
                    } catch (NullPointerException ex) {
                        materialTextView_Members.setContentText("", null);
                    }

                    DocumentReference creatorRef = documentSnapshot.getDocumentReference("creator");
                    creatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String creatorName = task.getResult().getString("name");
                                String creatorEmail = task.getResult().getString("email");
                                String creatorInfo = creatorName + " <" + creatorEmail + ">";
                                materialTextView_Creator.setContentText(creatorInfo, null);
                                // disable Join button if current user is the one who created the project
                                if (creatorEmail.equals(mAuth.getCurrentUser().getEmail().toString())) {
                                    button_Join.setEnabled(false);
                                }
                                else {
                                    button_Join.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            List<DocumentReference> memberRefList = (List<DocumentReference>) documentSnapshot.get("members");
                                            // prompt if user already joined the project
                                            if (memberRefList.contains(db.collection("Users")
                                                    .document(mAuth.getCurrentUser().getEmail().toString()))) {
                                                new AlertDialog.Builder(ProjectInfoActivity.this)
                                                        .setMessage("You already joined this project")
                                                        .show();
                                            } else {
                                                new AlertDialog.Builder(ProjectInfoActivity.this)
                                                        .setMessage("Join this project?")
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                db.collection("Projects").document(projectID)
                                                                        .update("members",
                                                                                FieldValue.arrayUnion(db.collection("Users")
                                                                                        .document(mAuth.getCurrentUser().getEmail())));
                                                                db.collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        DocumentReference creator =  db.collection("Users").document(mAuth.getCurrentUser().getEmail());
                                                                        DocumentReference projectRef = task.getResult().getReference();
                                                                        creator.update("projects", FieldValue.arrayUnion(projectRef));
                                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                                        if (documentSnapshot.exists()) {
                                                                            try {
                                                                                SharedMethods.displayItems(documentSnapshot.get("members"), materialTextView_Members);
                                                                            } catch (NullPointerException ex) {
                                                                                materialTextView_Members.setContentText("", null);
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                            }
                                                        })
                                                        .show();
                                            }
                                        }
                                    });
                                }
                            }

                        }
                    });

//                    String creatorEmail = documentSnapshot.getString("creator");
//                    db.collection("Users")
//                            .document(creatorEmail).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    String nameEmail = task.getResult().get("name") + " <" + creatorEmail + ">";
//                                    materialTextView_Creator.setContentText(nameEmail, null);
//                                    // disable Join button if current user is the one who created the project
//
//                                }
//                            });

                    // display supervisors and handle a case where none supervisor selected
                    try {
                        SharedMethods.displayItems(documentSnapshot.get("supervisors"), materialTextView_Supervisor);
                    } catch (NullPointerException ex) {
                        materialTextView_Supervisor.setContentText("", null);
//                        supervisors.setText("");
                    }


                }
            }
        });
    }
}
