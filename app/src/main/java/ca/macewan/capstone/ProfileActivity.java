package ca.macewan.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.remote.Stream;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    ImageView profilePicture;
    EditText editName, editEmail, editPhone;
    TextView changePassword, role, profAvail, changePhoto;
    ChipGroup profAvailChips;
    Menu menu;
    DocumentReference user;
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        role = findViewById(R.id.profileRole);
        changePhoto = findViewById(R.id.profileEditPhoto);
        editName = findViewById(R.id.profileName);
        editEmail = findViewById(R.id.profileEmail);
        editPhone = findViewById(R.id.profilePhone);

        profAvail = findViewById(R.id.profileProfAvail);
        profAvailChips = findViewById(R.id.profileProfAvailChips);

        changePassword = findViewById(R.id.profilePassword);
        profilePicture = findViewById(R.id.profilePicture);
        profilePicture.setImageResource(R.drawable.ic_baseline_person_24);

        firebaseAuth = FirebaseAuth.getInstance();

        user = FirebaseFirestore.getInstance().collection("Users").document(firebaseAuth.getCurrentUser().getEmail());
        user.get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);

                        role.setText(user.role);

                        if (user.picture != null) {
                            try {
                                URL imageUrl = new URL(user.picture);
                                Bitmap bitmap = BitmapFactory.decodeStream(imageUrl.openStream());
                                profilePicture.setImageBitmap(bitmap);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        editName.setText(user.name);
                        editEmail.setText(user.email);
                        editPhone.setText(user.phone);

                        if (user.role.equals("professor")) {
                            profAvail.setVisibility(View.VISIBLE);
                            profAvailChips.setVisibility(View.VISIBLE);
                        }
                    } else {
                        finish();
                    }
                }
            });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto();
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
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_edit:
                    if (!editName.isEnabled()) {
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

    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select profile picture"), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), data.getData());
                        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                        profilePicture.setImageBitmap(bitmap);
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference imageRef = storageRef.child("user_images/" + firebaseAuth.getCurrentUser().getEmail() + "/" + data.getData().getLastPathSegment());
                        imageRef.putFile(data.getData())
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Toast.makeText(getApplicationContext(), "Profile picture uploaded", Toast.LENGTH_SHORT).show();
                                        user.update("picture", taskSnapshot.getMetadata().getReference().getDownloadUrl());
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}