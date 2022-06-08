package ca.macewan.capstone;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Signup extends AppCompatActivity {
    private TextView error;
    private EditText emailField, passwordField, confirmPassword, nameField;
    private Button signup;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String errorString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        firebaseAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.signUpEmail);
        passwordField = findViewById(R.id.signUpPassword);
        confirmPassword = findViewById(R.id.signUpConfirmPassword);
        signup = findViewById(R.id.signUpButton);
        error = findViewById(R.id.errorText);

        firebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void signUp() {
        String password = passwordField.getText().toString();
        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        errorString = "";

        if (email.equals("")) {
            errorString += "Email address cannot be empty.\n";
        }

        if (name.equals ("")) {
            errorString += "Full name cannot be empty.\n";
        }

        if (password.equals("")) {
            errorString += "Password cannot be empty.\n";
        }

        if (!password.equals(confirmPassword.getText().toString())) {
            errorString += "Both passwords must match.\n";
        }

        if (!email.equals("")) {
            if (!email.endsWith("@mymacewan.ca") && !email.endsWith("@macewan.ca")) {
                errorString += "Use a valid MacEwan email.\n";
            }
        }

        if (!errorString.isEmpty()) {
            error.setText(errorString);
            return;
        }

        firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firebaseFirestore.collection("Users");
        User user = new User(email, name, email.endsWith("@macewan.ca") ? "professor" : "student");
        usersRef.document(email).set(user);
        firebaseAuth.createUserWithEmailAndPassword(email, password);
        finish();
    }

    private void updateString(String string) {
        errorString += string;
    }

}
