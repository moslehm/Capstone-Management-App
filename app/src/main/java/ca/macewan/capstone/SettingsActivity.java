package ca.macewan.capstone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.MenuItem;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.CheckBox;

import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // below line is used to check if
        // frame layout is empty or not.
        if (findViewById(R.id.idFrameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // below line is to inflate our fragment.
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.idFrameLayout, new PreferencesFragment())
                    .commit();

            System.out.print("Inflated fragment.");
        }
    }

    public static class PreferencesFragment extends PreferenceFragmentCompat {
        CheckBoxPreference notifsEnabled, notifsChange, notifsJoin, notifsSupervisorAccept;
        SharedPreferences prefs;

        Preference.OnPreferenceChangeListener notifListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                System.out.print("Listening to preference change");
                System.out.print(newValue);
                if (newValue == Boolean.FALSE) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(preference.getKey());
                } else {
                    FirebaseMessaging.getInstance().subscribeToTopic(preference.getKey());
                }
                return true;
            }
        };

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            notifsEnabled = findPreference("notifsEnabled");
            notifsChange = findPreference("projectChange");
            notifsJoin = findPreference("projectJoin");
            notifsSupervisorAccept = findPreference("supervisorJoin");
            prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

            notifsEnabled.setOnPreferenceChangeListener(notifListener);
            notifsChange.setOnPreferenceChangeListener(notifListener);
            notifsJoin.setOnPreferenceChangeListener(notifListener);
            notifsSupervisorAccept.setOnPreferenceChangeListener(notifListener);


            System.out.print(prefs.toString());
        }
    }
}