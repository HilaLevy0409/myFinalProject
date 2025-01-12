package com.example.myfinalproject.Database;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public class UserDatabase {

    private FirebaseAuth mAuth;
    private FirebaseFirestore database;

    // Constructor for initializing FirebaseAuth and FirebaseFirestore
    public UserDatabase() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }

    // Add a new user to Firestore
    public void addUser(User user, AddUserCallback callback) {
        // Create user in Firebase Authentication first
        mAuth.createUserWithEmailAndPassword(user.getUserEmail(), user.getUserPass())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            DocumentReference userRef = database.collection("users").document(user.getUserEmail());
                            userRef.set(user)
                                    .addOnSuccessListener(aVoid -> callback.onUserAdd(user)) // On success, call callback
                                    .addOnFailureListener(e -> callback.onError(e.getMessage())); // On failure, call callback
                    } else {

                        System.err.println("Error creating user: " + Objects.requireNonNull(task.getException()).getMessage());
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // Get user details from Firestore
    public void getUser(final String userId, final UserCallback callback) {
        // Fetch user from Firestore using the user email as the document ID
        DocumentReference userRef = database.collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onUserReceived(user); // Callback with user data
                        } else {
                            callback.onError("User data is invalid.");
                        }
                    } else {
                        callback.onError("User not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onError("Database error: " + e.getMessage()));
    }
}
