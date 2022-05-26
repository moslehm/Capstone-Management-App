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

public class ProjectInfoActivity extends AppCompatActivity {
    TextView title, creator, description, members, supervisors;
    Button button_Join;
    FirebaseFirestore db;
    String projectID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_info);
        setUp();
    }

    private void setUp() {
        title = findViewById(R.id.textView);
        creator = findViewById(R.id.textView6);
        supervisors = findViewById(R.id.textView8);
        description = findViewById(R.id.textView10);
        members = findViewById(R.id.textView12);
        button_Join = findViewById(R.id.button2);
        db = FirebaseFirestore.getInstance();

        projectID = getIntent().getExtras().getString("projectID");
        db.collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    title.setText(documentSnapshot.getString("name"));
                    description.setText(documentSnapshot.getString("description"));

                    // display members and handle a case where no one has joined yet
                    try {
                        SharedMethods.displayItems((ArrayList<DocumentReference>) documentSnapshot.get("members"), members);
                    } catch (NullPointerException ex) {
                        members.setText("");
                    }

                    String creatorEmail = documentSnapshot.getString("creator");
                    db.collection("Users")
                            .document(creatorEmail).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    String nameEmail = task.getResult().get("name") + " <" + creatorEmail + ">";
                                    creator.setText(nameEmail);
                                }
                            });

                    // display supervisors and handle a case where none supervisor selected
                    try {
                        SharedMethods.displayItems((List<DocumentReference>) documentSnapshot.get("supervisors"), supervisors);
                    } catch (NullPointerException ex) {
                        supervisors.setText("");
                    }

                    // disable Join button if current user is the one who created the project
                    if (creatorEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())) {
                        button_Join.setEnabled(false);
                    }
                    else {
                        button_Join.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                List<DocumentReference> memberRefList = (List<DocumentReference>) documentSnapshot.get("members");
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
                                                    db.collection("Users")
                                                            .document(FirebaseAuth
                                                                    .getInstance().getCurrentUser()
                                                                    .getEmail().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                String name = task.getResult().get("name").toString();
                                                                members.append("\n" + name + " <"
                                                                        + FirebaseAuth.getInstance().getCurrentUser().getEmail().toString() + ">");
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
    }
}
