package ca.macewan.capstone;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogIn, buttonSignUp;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private TextView textView_SignUp;
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
//        buttonSignUp = findViewById(R.id.button_SignUp);
        checkBoxRemember = findViewById(R.id.checkBox_Remember);
        loginPrefs = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPrefs.edit();

        rememberLogin = loginPrefs.getBoolean("rememberLogin", false);
        if (rememberLogin == true) {
            editTextUsername.setText(loginPrefs.getString("username", ""));
            editTextPassword.setText(loginPrefs.getString("password", ""));
            checkBoxRemember.setChecked(true);
            if (getIntent().getExtras() == null) {
                logIn();
            }
        }

        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });
//        buttonSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                signUp();
//            }
//        });

        SpannableString ss = new SpannableString("Don't have an account? Sign up.");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
//                startActivity(new Intent(MyActivity.this, NextActivity.class));
                signUp();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(getResources().getColor(R.color.maroon, null));
            }
        };
        ss.setSpan(clickableSpan, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.textView_SignUp);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
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
                                    if (documentSnapshot.exists()) {
                                        if (checkBoxRemember.isChecked()) {
                                            loginPrefsEditor.putBoolean("rememberLogin", true);
                                            loginPrefsEditor.putString("username", username);
                                            loginPrefsEditor.putString("password", password);
                                        } else {
                                            loginPrefsEditor.clear();
                                        }
                                        loginPrefsEditor.apply();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        // Login success but no role was assigned
                                        new MaterialAlertDialogBuilder(Login.this)
                                                .setMessage("No profile was created for this user.")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .show();
                                    }
                                }
                            }
                        });
                    } else {
                        new MaterialAlertDialogBuilder(Login.this)
                                .setMessage("Authentication failed!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();

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