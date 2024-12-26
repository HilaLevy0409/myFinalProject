package com.example.myfinalproject.Database;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

public class UserDatabase {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    // Constructor for initializing FirebaseAuth and FirebaseDatabase
    public UserDatabase() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    public void addUser(final User user, AddUserCallback callback) {
        mAuth.createUserWithEmailAndPassword(user.getUserEmail(), user.getUserPass())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            DatabaseReference myRef = database.getReference("users/" + firebaseUser.getUid());
                            myRef.setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        callback.onUserAdd(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        callback.onError(e.getMessage());
                                    });
                        }
                    } else {
                        // Handle failure to create user
                        System.err.println("Error creating user: " + task.getException().getMessage());
                    }
                });
    }
    public void getUser(final String userId, final UserCallback callback) {
        DatabaseReference myRef = database.getReference("users/" + userId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        callback.onUserReceived(user);
                    } else {
                        callback.onError("User data is invalid.");
                    }
                } else {
                    callback.onError("User not found.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Database error: " + databaseError.getMessage());
            }
        });
    }
}
