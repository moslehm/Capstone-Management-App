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
import com.google.firebase.messaging.FirebaseMessaging;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    User user;
    public HomeFragment homeFragment;
    public Fragment listFragment;
    public SettingsFragment settingsFragment;
    Fragment selected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult();
                        tokenThread(token);
                    }
                });

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
                Bundle bundle = new Bundle();
                bundle.putString("email", user.email);

                homeFragment = new HomeFragment();
                homeFragment.setArguments(bundle);
                if (Objects.equals(user.role, "student")) {
                    listFragment = new ListFragment();
                    listFragment.setArguments(bundle);
                } else if (Objects.equals(user.role, "professor")) {
                    listFragment = new ProfListFragment();
                }
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

    private void tokenThread(final String token) {
        Thread t = new Thread(() -> {
            try {
                sendToken(token);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    private void sendToken(String token) throws IOException {
        Socket socket = null;
        OutputStream output = null;
        socket = new Socket("34.168.78.99", 10000);
        System.out.println("Connected");
        output = socket.getOutputStream();
    }
}
