package ca.macewan.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    private TextInputEditText editTextUsername, editTextPassword;
    private Button buttonLogIn;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        firebaseAuth = FirebaseAuth.getInstance();
        editTextUsername = findViewById(R.id.editText_Username);
        editTextPassword = findViewById(R.id.editText_Password);
        buttonLogIn = findViewById(R.id.button_LogIn);
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });
    }

    private void logIn() {
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

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
                                        Intent intent = null;
                                        String role = documentSnapshot.get("role").toString();
                                        String name = documentSnapshot.get("name").toString();
                                        if (role.equals("student")) {
                                            intent = new Intent(getApplicationContext(), MainActivity.class); 
                                        }
                                        else if (role.equals("professor")) {
                                            intent = new Intent(getApplicationContext(), ProfMain.class);
                                        }
                                        intent.putExtra("role", role);
                                        intent.putExtra("name", name);
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
}