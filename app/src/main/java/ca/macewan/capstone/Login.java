package ca.macewan.capstone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import kotlinx.coroutines.channels.Send;

public class Login extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogIn, buttonSignUp;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private CheckBox checkBoxRemember;
    private SharedPreferences loginPrefs;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean rememberLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        firebaseAuth = FirebaseAuth.getInstance();
        editTextUsername = findViewById(R.id.editText_Username);
        editTextPassword = findViewById(R.id.editText_Password);
        buttonLogIn = findViewById(R.id.button_LogIn);
        buttonSignUp = findViewById(R.id.button_SignUp);
        checkBoxRemember = findViewById(R.id.checkBox_Remember);
        loginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPrefs.edit();

        rememberLogin = loginPrefs.getBoolean("rememberLogin", false);
        if (rememberLogin == true) {
            editTextUsername.setText(loginPrefs.getString("username", ""));
            editTextPassword.setText(loginPrefs.getString("password", ""));
            checkBoxRemember.setChecked(true);
        }

        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    private void logIn() {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        editTextUsername.clearFocus();
        editTextPassword.clearFocus();

        if (username.equals("") || password.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Invalid username or password", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            firebaseAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        firebaseFirestore = FirebaseFirestore.getInstance();
                        documentReference = firebaseFirestore.collection("Users")
                                .document(username);
                        documentReference.get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    Toast toast = null;
                                    if (documentSnapshot.exists()) {
//                                        User user = documentSnapshot.toObject(User.class);
                                        if (checkBoxRemember.isChecked()) {
                                            loginPrefsEditor.putBoolean("rememberLogin", true);
                                            loginPrefsEditor.putString("username", username);
                                            loginPrefsEditor.putString("password", password);
                                            loginPrefsEditor.apply();
                                        } else {
                                            loginPrefsEditor.clear();
                                            loginPrefsEditor.apply();
                                        }
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                        intent.putExtra("user", user);
                                        startActivity(intent);
                                    }
                                    else {
                                        // Login success but no role was assigned
                                        toast = Toast.makeText(getApplicationContext(),
                                                "Please contact admin for adding role",
                                                Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                }
                            }
                        });
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Failed!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }
    }

    private void signUp() {
        Intent intent = new Intent(getApplicationContext(), Signup.class);

        startActivity(intent);
    }
}