package ca.macewan.capstone;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import ca.macewan.capstone.adapter.SharedMethods;
import uk.co.onemandan.materialtextview.MaterialTextView;

public class ProjectInfoActivity extends AppCompatActivity {
    Button button_Join, button_Withdraw;
    FirebaseFirestore db;
    String projectID;
    DocumentReference projectRef;
//    User user;
    List<DocumentReference> memberRefList;
    String email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);
        db = FirebaseFirestore.getInstance();
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        setUp();
        refreshButtons();
    }

    private void setUp() {
        button_Join = findViewById(R.id.button_Join);
        button_Withdraw = findViewById(R.id.button_Withdraw);

        projectID = getIntent().getExtras().getString("projectID");
        projectRef = db.collection("Projects").document(projectID);
        View projectView = findViewById(R.id.projectLayout);
        SharedMethods.setupProjectView(projectView, projectRef, email, this);

        button_Join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prompt if user already joined the project
                if (memberRefList != null && memberRefList.contains(db.collection("Users")
                        .document(email))) {
                    new AlertDialog.Builder(ProjectInfoActivity.this)
                            .setMessage("You already joined this project")
                            .show();
                } else {
                    new AlertDialog.Builder(ProjectInfoActivity.this)
                            .setMessage("Join this project?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    projectRef.update("members",
                                            FieldValue.arrayUnion(db.collection("Users")
                                                    .document(email)));
                                    db.collection("Users").document(email)
                                            .update("projects",
                                                    FieldValue.arrayUnion(projectRef));
                                    System.out.println("Clicked yes on join");
                                    refreshButtons();
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

        button_Withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProjectInfoActivity.this)
                        .setMessage("Quit this project?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                projectRef.update("members",
                                        FieldValue.arrayRemove(db.collection("Users")
                                                .document(email)));
                                db.collection("Users").document(email)
                                        .update("projects",
                                                FieldValue.arrayRemove(projectRef));
                                System.out.println("Clicked yes on quit");
                                refreshButtons();
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
    }

    private void refreshButtons() {
        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    memberRefList = (ArrayList<DocumentReference>) documentSnapshot.get("members");
                    buttonJoinRefresh(documentSnapshot, memberRefList);
                    buttonQuitRefresh(memberRefList);
                }
            }
        });
    }

    private void buttonJoinRefresh(DocumentSnapshot documentSnapshot, List<DocumentReference> memberRefList) {
        DocumentReference creatorRef = documentSnapshot.getDocumentReference("creator");
        creatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String creatorEmail = task.getResult().getString("email");
                    if (documentSnapshot.getBoolean("status") == false)
                        button_Join.setEnabled(false);
                    // disable Join button if current user is the one who created the project or status is false
                    if (creatorEmail.equals(email)) {
                        button_Join.setEnabled(false);
                    }
                    else {
                        button_Join.setEnabled(true);
                    }
                }

            }
        });
    }

    private void buttonQuitRefresh(List<DocumentReference> memberRefList) {
        if (memberRefList == null) {
            button_Withdraw.setEnabled(false);
        }
        else if (!memberRefList.contains(db.collection("Users").document(email))) {
            button_Withdraw.setEnabled(false);
        }
        else {
            button_Withdraw.setEnabled(true);
        }
    }
}
