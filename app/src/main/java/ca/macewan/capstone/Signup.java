package ca.macewan.capstone;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Signup extends AppCompatActivity {
    private EditText username, password, email;
    private Button signup;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        firebaseAuth = FirebaseAuth.getInstance();
        username = findViewById(R.id.signUpUsername);
        password = findViewById(R.id.signUpPassword);
        email = findViewById(R.id.signUpEmail);
        signup = findViewById(R.id.signUpButton);

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
        Toast.makeText(getApplicationContext(), "Signing up.", Toast.LENGTH_SHORT).show();
    }
}
