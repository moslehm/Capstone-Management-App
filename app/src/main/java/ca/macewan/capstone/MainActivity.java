package ca.macewan.capstone;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import ca.macewan.capstone.adapter.SharedMethods;

public class MainActivity extends AppCompatActivity implements ListFragment.OnListListener {
    private static final String TAG = "MainActivity";

    User user;
    public HomeFragment homeFragment;
    public Fragment listFragment;
    public SettingsFragment settingsFragment;
    Fragment selected;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_main);
        setup();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_home:
                        if (selected == homeFragment)
                            break;
                        SharedMethods.hideFragment(getSupportFragmentManager(), selected);
                        selected = homeFragment;
                        SharedMethods.showFragment(getSupportFragmentManager(), selected);
                        break;
                    case R.id.ic_list:
                        if (selected == listFragment)
                            break;
                        SharedMethods.hideFragment(getSupportFragmentManager(), selected);
                        selected = listFragment;
                        SharedMethods.showFragment(getSupportFragmentManager(), selected);
                        break;
                    case R.id.ic_settings:
                        if (selected == settingsFragment)
                            break;
                        SharedMethods.hideFragment(getSupportFragmentManager(), selected);
                        selected = settingsFragment;
                        SharedMethods.showFragment(getSupportFragmentManager(), selected);
                        break;
                }
                return true;
            }
        });
    }

    private void setup(){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        db.collection("Users")
            .document(email)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "User fields modified: " + documentSnapshot.getData());
                            user = documentSnapshot.toObject(User.class);
                            setupAllFragments();
                        }
                    }
                }
            });
    }

    private void setupAllFragments() {
        Bundle bundle = new Bundle();
        bundle.putString("email", user.email);
        homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);

        if (Objects.equals(user.role, "student")) {
            bundle = new Bundle();
            bundle.putString("email", user.email);
            bundle.putString("screenType", "list");
            bundle.putBoolean("isSupervisor", false);
            listFragment = new ListFragment();
            listFragment.setArguments(bundle);
            ((ListFragment) listFragment).setListener(this);
        } else if (Objects.equals(user.role, "professor")) {
            bundle = new Bundle();
            bundle.putString("email", user.email);
            listFragment = new ProfListFragment();
            listFragment.setArguments(bundle);
        }
        settingsFragment = new SettingsFragment();
        selected = homeFragment;

        SharedMethods.createFragment(getSupportFragmentManager(), R.id.fl_wrapper, homeFragment, "home");
        SharedMethods.createFragment(getSupportFragmentManager(), R.id.fl_wrapper, listFragment, "list");
        SharedMethods.createFragment(getSupportFragmentManager(), R.id.fl_wrapper, settingsFragment, "settings");
        SharedMethods.showFragment(getSupportFragmentManager(), selected);
    }

    @Override
    public void onListUpdate(String fragmentName, ListFragment.OnUpdateListener onUpdateListener) {
        ArrayList<String> projectIds = new ArrayList<String>();
        if (Objects.equals(fragmentName, "list")) {
            db.collection("Projects")
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
    }
}
