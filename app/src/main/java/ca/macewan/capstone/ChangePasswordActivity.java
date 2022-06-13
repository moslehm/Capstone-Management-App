package ca.macewan.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    Button changePasswordButton;
    EditText currentPassword, newPassword, newPasswordConfirm;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        currentPassword = findViewById(R.id.pwChangeCurrent);
        newPassword = findViewById(R.id.pwChangeNew);
        newPasswordConfirm = findViewById(R.id.pwChangeNewConfirm);
        changePasswordButton = findViewById(R.id.pwChangeConfirm);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (newPassword.getText().toString().equals(newPasswordConfirm.getText().toString()))
                {
                    changePassword();
                } else {
                    newPassword.setError("New passwords must match!");
                    newPasswordConfirm.setError("New passwords must match!");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void changePassword() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential creds = EmailAuthProvider.getCredential(user.getEmail(), currentPassword.getText().toString());
        user.reauthenticate(creds)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Password successfully changed", Toast.LENGTH_SHORT);
                                                finish();
                                            } else {
                                                newPassword.setError("Password is not strong enough, it must be at least 6 characters");
                                                newPassword.requestFocus();
                                            }
                                        }
                                    });
                        } else {
                            currentPassword.setError("Password incorrect");
                            currentPassword.requestFocus();
                        }
                    }
                });
    }
}