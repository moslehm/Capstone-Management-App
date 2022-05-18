package ca.macewan.capstone;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {
    String role = "";
    String name = "";
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
        role = getIntent().getExtras().getString("role");
        name = getIntent().getExtras().getString("name");
        homeFragment = new HomeFragment(role, name);
        listFragment = new ListFragment();
        settingsFragment = new SettingsFragment();
        selected = homeFragment;

        createFragment(homeFragment);
        createFragment(listFragment);
        createFragment(settingsFragment);
        showFragment(selected);
    }

    private void createFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_wrapper, fragment)
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
