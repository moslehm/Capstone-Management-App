package ca.macewan.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CompletedProjectsActivity extends AppCompatActivity implements ListFragment.OnListListener {
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_projects);

        // Get action bar and show back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Completed Projects");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("screenType", "completedList");
        bundle.putBoolean("isSupervisor", false);
        bundle.putString("emptyListText", "There are no completed projects to display.");
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);
        listFragment.setListener(this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.constraintLayout, listFragment, "completedList")
                .commit();
    }

    @Override
    public void onListUpdate(String fragmentName, ListFragment.OnUpdateListener onUpdateListener) {
        ArrayList<String> projectIds = new ArrayList<String>();
        FirebaseFirestore.getInstance()
                .collection("Projects")
                .whereEqualTo("isComplete", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                projectIds.add(document.getId());
                            }
                            onUpdateListener.onUpdateComplete(projectIds);
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}