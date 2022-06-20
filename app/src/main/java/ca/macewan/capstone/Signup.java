package ca.macewan.capstone;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Signup extends AppCompatActivity {
    private EditText emailField, passwordField, confirmPassword, nameField;
    private Button signup;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.signUpEmail);
        passwordField = findViewById(R.id.signUpPassword);
        confirmPassword = findViewById(R.id.signUpConfirmPassword);
        nameField = findViewById(R.id.signUpFullName);
        signup = findViewById(R.id.signUpButton);

//        firebaseAuth.signInAnonymously()
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            FirebaseUser user = firebaseAuth.getCurrentUser();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//    }

    private void signUp() {
        String password = passwordField.getText().toString();
        String name = nameField.getText().toString();
        String email = emailField.getText().toString();

        if (email.equals("")) {
            emailField.setError("This field can not be left blank");
        }

        if (name.equals ("")) {
            nameField.setError("This field can not be left blank");
        }

        if (password.equals("")) {
            passwordField.setError("This field can not be left blank");
        }

        if (confirmPassword.getText().toString().equals("")) {
            confirmPassword.setError("This field can not be left blank");
        }

        if (!password.equals(confirmPassword.getText().toString())) {
            confirmPassword.setError("Both passwords must match.");
        }

        if (!email.equals("")) {
            if (!email.endsWith("@mymacewan.ca") && !email.endsWith("@macewan.ca")) {
                emailField.setError("Must be a valid MacEwan email");
            }
        }

        if (emailField.getError() != null || passwordField.getError() != null || nameField.getError() != null || confirmPassword.getError() != null) {
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    firebaseFirestore = FirebaseFirestore.getInstance();
                    CollectionReference usersRef = firebaseFirestore.collection("Users");
                    User user = new User(email, name, email.endsWith("@macewan.ca") ? "professor" : "student");
                    usersRef.document(email).set(user);
                    finish();
                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        passwordField.setError("Password should be at least 6 characters");
                        Log.e(TAG, e.getMessage());
                    } catch(FirebaseAuthUserCollisionException e) {
                        emailField.setError("This email is already in use");
                        Log.e(TAG, e.getMessage());
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }
}
