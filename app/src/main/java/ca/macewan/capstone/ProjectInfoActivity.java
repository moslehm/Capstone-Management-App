package ca.macewan.capstone;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import ca.macewan.capstone.adapter.SharedMethods;
import uk.co.onemandan.materialtextview.MaterialTextView;

public class ProjectInfoActivity extends AppCompatActivity {
//    TextView title, creator, description, members, supervisors;
    Button button_Join, button_Withdraw;
    FirebaseFirestore db;
    String projectID;
    MaterialTextView materialTextView_Creator, materialTextView_Title, materialTextView_Supervisor,
            materialTextView_Members, materialTextView_Descriptions;
    CheckBox checkBox_Status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);
        setUp();
    }

    private void setUp() {
        button_Join = findViewById(R.id.button_Join);
        button_Withdraw = findViewById(R.id.button_Withdraw);
        materialTextView_Creator = findViewById(R.id.textView_Creator);
        materialTextView_Title = findViewById(R.id.textiew_Title);
        materialTextView_Members = findViewById(R.id.textView_Members);
        materialTextView_Supervisor = findViewById(R.id.textView_Supervisor);
        materialTextView_Descriptions = findViewById(R.id.textView_Description);
        checkBox_Status = findViewById(R.id.checkBox_Status);
        
        db = FirebaseFirestore.getInstance();

        projectID = getIntent().getExtras().getString("projectID");
        db.collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    materialTextView_Title.setContentText(documentSnapshot.getString("name"), null);
                    materialTextView_Title.setLabelText("Title");
                    materialTextView_Descriptions.setContentText(documentSnapshot.getString("description"), null);
                    materialTextView_Creator.setLabelText("Creator");
                    materialTextView_Descriptions.setLabelText("Description");
                    materialTextView_Members.setLabelText("Member(s)");
                    materialTextView_Supervisor.setLabelText("Supervisor(s)");
                    button_Join.setText("Join");


                    List<DocumentReference> memberRefList = (ArrayList<DocumentReference>) documentSnapshot.get("members");
                    if (memberRefList == null) {
                        Map<String, Object> docData = new HashMap<>();
                        List<DocumentReference> memberList = new ArrayList<>();
                        docData.put("members", memberList);
                        db.collection("Projects").document(projectID).set(docData, SetOptions.merge());
                    }


                    boolean status = documentSnapshot.getBoolean("status");
                    checkBoxStatusSetup(status);
                    buttonJoinSetUp(documentSnapshot, memberRefList, status);
                    buttonQuitSetup(memberRefList);

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

    private void buttonJoinSetUp(DocumentSnapshot documentSnapshot, List<DocumentReference> memberRefList, boolean status) {
        DocumentReference creatorRef = documentSnapshot.getDocumentReference("creator");
        creatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String creatorName = task.getResult().getString("name");
                    String creatorEmail = task.getResult().getString("email");
                    String creatorInfo = creatorName + " <" + creatorEmail + ">";
                    materialTextView_Creator.setContentText(creatorInfo, null);
                    if (status == false)
                        button_Join.setEnabled(false);
                    // disable Join button if current user is the one who created the project or status is false
                    if (creatorEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())) {
                        button_Join.setEnabled(false);

                        // only creator can change the status
                        checkBox_Status.setEnabled(true);
                        checkBox_Status.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (checkBox_Status.isChecked()) {
                                    db.collection("Projects").document(projectID)
                                            .update("status", true);
                                }

                                else {
                                    db.collection("Projects").document(projectID)
                                            .update("status", false);
                                }
                            }
                        });
                    }
                    else {
                        button_Join.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // prompt if user already joined the project
                                if (memberRefList.contains(db.collection("Users")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString()))) {
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
                                                                            .document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())));
                                                    recreate();
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
    }

    private void buttonQuitSetup(List<DocumentReference> memberRefList) {
        // withdraw from previously joined project
        if (!memberRefList.contains(db.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString()))) {
            button_Withdraw.setEnabled(false);
        }
        else {
            button_Withdraw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(ProjectInfoActivity.this)
                            .setMessage("Quit this project?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    db.collection("Projects").document(projectID)
                                            .update("members",
                                                    FieldValue.arrayRemove(db.collection("Users")
                                                            .document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())));
                                    recreate();
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
}
