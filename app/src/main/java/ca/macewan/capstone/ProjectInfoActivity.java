package ca.macewan.capstone;


import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProjectInfoActivity extends AppCompatActivity {
    TextView title, creator, description, members;
    Spinner spinner;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_info);
        setUp();
    }

    private void setUp() {
        title = findViewById(R.id.textView);
        creator = findViewById(R.id.textView6);
        spinner = findViewById(R.id.spinner);
        description = findViewById(R.id.textView10);
        members = findViewById(R.id.textView12);

        String projectID = getIntent().getExtras().getString("projectID");
        FirebaseFirestore.getInstance().collection("Projects").document(projectID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    FirebaseFirestore.getInstance().collection("Users")
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


    }

}
