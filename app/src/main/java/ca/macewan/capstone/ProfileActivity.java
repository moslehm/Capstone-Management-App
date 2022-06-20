package ca.macewan.capstone;

import static android.text.InputType.TYPE_NULL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    ImageView profile;
    EditText editName, editEmail, editPhone;
    TextView changePassword, role, profAvail;
    ChipGroup profAvailChips;
    Menu menu;
    DocumentReference user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        role = findViewById(R.id.profileRole);
        editName = findViewById(R.id.profileName);
        editEmail = findViewById(R.id.profileEmail);
        editPhone = findViewById(R.id.profilePhone);

        profAvail = findViewById(R.id.profileProfAvail);
        profAvailChips = findViewById(R.id.profileProfAvailChips);

        changePassword = findViewById(R.id.profilePassword);
        ImageView img= findViewById(R.id.imageView);
        img.setImageResource(R.drawable.ic_baseline_person_24);

        user = FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        user.get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);

                        role.setText(user.role);
                        editName.setText(user.name);
                        editEmail.setText(user.email);
                        editPhone.setText(user.phone);

                        if (user.role.equals("professor")) {
                            profAvail.setVisibility(View.VISIBLE);
                            profAvailChips.setVisibility(View.VISIBLE);
                        }
                    } else {
                        return;
                    }
                }
            });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_options, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_edit:
                    if (editName.isEnabled() == false) {
                        Toast.makeText(this, "Editing...", Toast.LENGTH_LONG).show();
                        editName.setEnabled(true);
                        editEmail.setEnabled(false);
                        editPhone.setEnabled(true);
                        item.setIcon(R.drawable.ic_baseline_save_24);
                    } else {
                        Toast.makeText(this, "Saving...", Toast.LENGTH_LONG).show();

                        User updatedUser = new User(editEmail.getText().toString(),
                                editName.getText().toString(),
                                role.getText().toString(),
                                editPhone.getText().toString());

                        user.set(updatedUser);
                        editName.setEnabled(false);
                        editEmail.setEnabled(false);
                        editPhone.setEnabled(false);
                        item.setIcon(R.drawable.ic_baseline_edit_24);
                    }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changePassword() {
        Intent intent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
        startActivity(intent);
    }
}