package ca.macewan.capstone;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.macewan.capstone.adapter.SharedMethods;
import uk.co.onemandan.materialtextview.MaterialTextView;

public class ProjectInfoActivityProf extends AppCompatActivity {
    Button button_Accept, button_Decline;
    FirebaseFirestore db;
    String projectID;
    MaterialTextView materialTextView_Creator, materialTextView_Title, materialTextView_Supervisor,
            materialTextView_Members, materialTextView_Description;
    CheckBox checkBox_Status;
    String currentUserID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info_prof);
        setUp();
    }

    private void setUp() {
        button_Accept = findViewById(R.id.button_Accept);
        button_Decline = findViewById(R.id.button_Decline);
        materialTextView_Creator = findViewById(R.id.textView_CreatorProf);
        materialTextView_Title = findViewById(R.id.textiew_TitleProf);
        materialTextView_Members = findViewById(R.id.textView_MembersProf);
        materialTextView_Supervisor = findViewById(R.id.textView_SupervisorProf);
        materialTextView_Description = findViewById(R.id.textView_DescriptionProf);
        checkBox_Status = findViewById(R.id.checkBox_StatusProf);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();

        db = FirebaseFirestore.getInstance();
        projectID = getIntent().getExtras().getString("projectID");

        db.collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    materialTextView_Title.setContentText(documentSnapshot.getString("name"), null);
                    materialTextView_Title.setLabelText("Title");
                    materialTextView_Description.setContentText(documentSnapshot.getString("description"), null);
                    materialTextView_Creator.setLabelText("Creator");
                    materialTextView_Description.setLabelText("Description");
                    materialTextView_Members.setLabelText("Member(s)");
                    materialTextView_Supervisor.setLabelText("Supervisor(s)");

                    List<DocumentReference> supervisorList = (ArrayList<DocumentReference>) documentSnapshot.get("supervisors");
                    List<DocumentReference> memberRefList = (ArrayList<DocumentReference>) documentSnapshot.get("members");

                    DocumentReference creatorRef = documentSnapshot.getDocumentReference("creator");
                    creatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String creatorName = task.getResult().getString("name");
                                String creatorEmail = task.getResult().getString("email");
                                String creatorInfo = creatorName + " <" + creatorEmail + ">";
                                materialTextView_Creator.setContentText(creatorInfo, null);
                            }
                        }
                    });

                    boolean status = documentSnapshot.getBoolean("status");
                    checkBoxStatusSetup(status);
                    buttonAcceptSetUp(status, supervisorList);
                    buttonDeclineSetup(status, supervisorList);

                    // display members and handle a case where no one has joined yet
                    try {
                        SharedMethods.displayItems(memberRefList, materialTextView_Members);
                    } catch (NullPointerException ex) {
                        materialTextView_Members.setContentText("", null);
                    }

                    // display supervisors and handle a case where none supervisor selected
                    try {
                        SharedMethods.displayItems((List<DocumentReference>) documentSnapshot.get("supervisors"), materialTextView_Supervisor);
                    } catch (NullPointerException ex) {
                        materialTextView_Supervisor.setContentText("", null);
                    }

                }
            }
        });
    }

    private void buttonDeclineSetup(boolean status, List<DocumentReference> supervisorList) {
        // won't be able to decline invitation after project has closed
        if (!status)
            button_Decline.setEnabled(false);
        // already accepted the invitation
        if (supervisorList.contains(db.collection("Users").document(currentUserID))) {
            button_Decline.setEnabled(false);
        }

        // disable the button for prof that was not invited to supervise
        db.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentReference> invitedProjList = (List<DocumentReference>) task.getResult().get("invited");
                    if (!invitedProjList.contains(db.document("Projects/" + projectID))) {
                        button_Decline.setEnabled(false);
                    }
                }
            }
        });

        button_Decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(ProjectInfoActivityProf.this)
                        .setMessage("Decline the request?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // remove the invitation from array
                                db.collection("Users").document(currentUserID).update("invited",
                                        FieldValue.arrayRemove(db.collection("Projects")
                                                .document(projectID)));
                                recreate();
                            }
                        })
                        .show();
            }
        });


    }

    private void buttonAcceptSetUp(boolean status, List<DocumentReference> supervisorList) {
        // won't be able to accept invitation after project has closed
        if (!status)
            button_Accept.setEnabled(false);
        // already accepted the invitation
        if (supervisorList.contains(db.collection("Users").document(currentUserID))) {
            button_Accept.setEnabled(false);
        }
        // disable the button for prof that was not invited to supervisor
        db.collection("Users").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentReference> invitedProjList = (List<DocumentReference>) task.getResult().get("invited");
                    if (!invitedProjList.contains(db.document("Projects/" + projectID))) {
                        button_Accept.setEnabled(false);
                    }
                }
            }
        });

        button_Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(ProjectInfoActivityProf.this)
                        .setMessage("Agree to supervise?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // add prof to supervisor
                                db.collection("Projects").document(projectID).update("supervisors",
                                        FieldValue.arrayUnion(db.collection("Users")
                                                .document(currentUserID)));
                                // add request to accepted array
                                db.collection("Users").document(currentUserID).update("accepted",
                                        FieldValue.arrayUnion(db.collection("Projects")
                                                .document(projectID)));
                                // remove request from invited array
                                db.collection("Users").document(currentUserID).update("invited",
                                        FieldValue.arrayRemove(db.collection("Projects")
                                                .document(projectID)));
                                recreate();
                            }
                        })
                        .show();
            }
        });
    }

    private void checkBoxStatusSetup(boolean status) {
        checkBox_Status.setEnabled(false);
        if (status) {
            checkBox_Status.setChecked(true);
        }
        else {
            checkBox_Status.setChecked(false);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        super.onBackPressed();
        finish();
    }
}
