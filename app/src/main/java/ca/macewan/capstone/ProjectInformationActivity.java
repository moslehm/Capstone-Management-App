package ca.macewan.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;

import ca.macewan.capstone.adapter.SharedMethods;

public class ProjectInformationActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String projectPath;
    DocumentReference projectRef;
    List<DocumentReference> memberRefList;
    String email;
    private Menu menu;
    private DocumentReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_information);
        db = FirebaseFirestore.getInstance();
        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        setUp();
    }

    private void setUp() {
        projectPath = getIntent().getExtras().getString("projectPath");
        projectRef = db.document(projectPath);
        userRef = db.collection("Users").document(email);
        View projectView = findViewById(R.id.projectLayout);
        SharedMethods.setupProjectView(projectView, projectRef, email, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_information_proposal, menu);
        this.menu = menu;
        updateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    private void updateOptionsMenu() {
        db.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User user = task.getResult().toObject(User.class);
                            refreshButtons(user);
                        }
                    }
                });
    }

    private void refreshButtons(User user) {
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_quit).setVisible(false);
        menu.findItem(R.id.action_join).setVisible(false);

        boolean userJoined = false;
        if (user.projects != null) {
            for (DocumentReference project : user.projects) {
                if (project.getId().equals(projectRef.getId())) {
                    userJoined = true;
                    break;
                }
            }
            if (!userJoined) {
                menu.findItem(R.id.action_join).setVisible(true);
                return;
            }
        }

        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    task.getResult()
                            .getDocumentReference("creator")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        String creatorEmail = task.getResult().getString("email");
                                        if (Objects.equals(creatorEmail, email))
                                            menu.findItem(R.id.action_delete).setVisible(true);
                                        else
                                            menu.findItem(R.id.action_quit).setVisible(true);
                                        menu.findItem(R.id.action_edit).setVisible(true);
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(getApplicationContext(), ProposalEditActivity.class);
            intent.putExtra("projectPath", projectPath);
            intent.putExtra("email", email);
            startActivity(intent);
        } else if (id == R.id.action_quit) {
            SharedMethods.quitProject(userRef, projectRef, ProjectInformationActivity.this, new EventCompleteListener() {
                @Override
                public void onComplete() {
                    updateOptionsMenu();
                }
            });
        } else if (id == R.id.action_delete) {
            SharedMethods.deleteProject(userRef, projectRef, ProjectInformationActivity.this, new EventCompleteListener() {
                @Override
                public void onComplete() {
                    finish();
                }
            });
        } else if (id == R.id.action_join) {
            SharedMethods.joinProject(userRef, projectRef, ProjectInformationActivity.this, new EventCompleteListener() {
                @Override
                public void onComplete() {
                    updateOptionsMenu();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
}