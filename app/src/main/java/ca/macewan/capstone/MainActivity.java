package ca.macewan.capstone;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    User user;
    public HomeFragment homeFragment;
    public ListFragment listFragment;
    public SettingsFragment settingsFragment;
    Fragment selected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_home:
                        hideFragment(selected);
                        selected = homeFragment;
                        showFragment(selected);
                        break;
                    case R.id.ic_list:
                        hideFragment(selected);
                        selected = listFragment;
                        showFragment(selected);
                        break;
                    case R.id.ic_settings:
                        hideFragment(selected);
                        selected = settingsFragment;
                        showFragment(selected);
                        break;
                }
                return true;
            }
        });
    }

    private void setup(){
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore.getInstance()
        .collection("Users")
        .document(email)
        .get()
        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(User.class);
                        setupAllFragments();
                    }
                }
            }

            private void setupAllFragments() {
                homeFragment = new HomeFragment(user);
                listFragment = new ListFragment(user);
                settingsFragment = new SettingsFragment();
                selected = homeFragment;

                createFragment(homeFragment, "home");
                createFragment(listFragment, "list");
                createFragment(settingsFragment, "settings");
                showFragment(selected);
            }
        });
    }

    private void createFragment(Fragment fragment, String tag){
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_wrapper, fragment, tag)
                .hide(fragment)
                .commit();
    }
    private void showFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .show(fragment)
                .commit();
    }
    private void hideFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .hide(fragment)
                .commit();
    }
}
