package ca.macewan.capstone;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class Login extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
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
            Toast toast = Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Toast toast = Toast.makeText(getApplicationContext(), "Successful!", Toast.LENGTH_SHORT);
                        //toast.show();
                        firebaseFirestore = FirebaseFirestore.getInstance();
                        documentReference = firebaseFirestore.collection("Users").document(username);
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    Toast toast;
                                    if (documentSnapshot.exists()) {
                                        toast = Toast.makeText(getApplicationContext(), documentSnapshot.get("role").toString(), Toast.LENGTH_LONG);
                                    }
                                    else {
                                        toast = Toast.makeText(getApplicationContext(), "Please contact admin", Toast.LENGTH_LONG);
                                    }
                                    toast.show();
                                }
                            }
                        });
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });
        }
    }
}