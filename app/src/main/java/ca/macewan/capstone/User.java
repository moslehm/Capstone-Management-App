package ca.macewan.capstone;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class User implements Serializable {
    public String email;
    public String name;
    public String role;
    public String phone;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String name, String role) {
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public User(String email, String name, String role, String phone) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.phone = phone;
    }
}
