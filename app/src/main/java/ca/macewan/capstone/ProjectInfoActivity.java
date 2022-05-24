package ca.macewan.capstone;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProjectInfoActivity extends AppCompatActivity {
    TextView title, creator, description, members;
    Spinner spinner_ProfList;
    Button button_Join;
    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_info);
        setUp();
    }

    private void setUp() {
        title = findViewById(R.id.textView);
        creator = findViewById(R.id.textView6);
        spinner_ProfList = findViewById(R.id.spinner);
        description = findViewById(R.id.textView10);
        members = findViewById(R.id.textView12);
        button_Join = findViewById(R.id.button2);
        db = FirebaseFirestore.getInstance();

        String projectID = getIntent().getExtras().getString("projectID");
        db.collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    title.setText(documentSnapshot.getString("name"));
                    description.setText(documentSnapshot.getString("description"));
                    for (DocumentReference documentReference : (List<DocumentReference>) documentSnapshot.get("members")) {
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                String name = task.getResult().get("name").toString();
                                String email = task.getResult().get("email").toString();
                                members.append(name + " <" + email + ">\n");
                            }
                        });
                    }
                    String creatorEmail = documentSnapshot.getString("creator");
                    db.collection("Users")
                            .document(documentSnapshot.getString("creator")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    String nameEmail = task.getResult().get("name") + " <" + creatorEmail +">";
                                    creator.setText(nameEmail);
                                }
                            });
                }
            }
        });

        List<String> professorList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, professorList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_ProfList.setAdapter(adapter);
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        if (queryDocumentSnapshot.get("role").toString().equals("professor")) {
                            String professorInfo = queryDocumentSnapshot.get("name") + " <" + queryDocumentSnapshot.get("email") + ">";
                            professorList.add(professorInfo);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        db.collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().get("creator").toString().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())) {
                        button_Join.setEnabled(false);
                    }
                    else {
                        button_Join.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new AlertDialog.Builder(ProjectInfoActivity.this)
                                        .setMessage("Join this project?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                db.collection("Projects").document(projectID)
                                                        .update("members",
                                                                FieldValue.arrayUnion(db.collection("Users")
                                                                        .document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString())));
                                                db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            String name = task.getResult().get("name").toString();
                                                            members.append(name + " <" + FirebaseAuth.getInstance().getCurrentUser().getEmail().toString() + ">\n");
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
                        });
                    }
                }
            }
        });
    }

}
