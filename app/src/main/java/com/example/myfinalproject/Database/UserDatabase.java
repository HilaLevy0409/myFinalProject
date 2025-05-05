package com.example.myfinalproject.Database;

import android.util.Log;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.CallBacks.UsersCallback;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDatabase {

    private FirebaseAuth mAuth;
    private FirebaseFirestore database;


    public UserDatabase() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }

    public void addUser(User user, AddUserCallback callback) {
        mAuth.createUserWithEmailAndPassword(user.getUserEmail(), user.getUserPass())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser.getUid();
                        user.setId(userId);

                        DocumentReference userRef = database.collection("users").document(userId);

                        userRef.set(user)
                                .addOnSuccessListener(aVoid -> {
                                    callback.onUserAdd(user);
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        System.err.println("Error creating user: " + Objects.requireNonNull(task.getException()).getMessage());
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    public void getUserById(String userId, UserCallback callback) {
        database.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            callback.onUserReceived(user);
                        } else {
                            callback.onError("שגיאה בטעינת נתוני משתמש");
                        }
                    } else {
                        callback.onError("לא נמצא משתמש");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }



    public void getUser(final String username, final UserCallback callback) {

        Query userQuery = database.collection("users").whereEqualTo("userName", username);

        userQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onUserReceived(user);
                        } else {
                            callback.onError("נתוני המשתמש אינם חוקיים");
                        }
                    } else {
                        callback.onError("לא נמצא משתמש");
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Database error: " + e.getMessage();
                    Log.e("FirestoreError", errorMessage);
                    callback.onError(errorMessage);
                });
    }


    public void updateUserPassword(String userEmail, String newPassword, UserCallback callback) {
        database.collection("users")
                .whereEqualTo("userEmail", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Update password
                            user.setUserPass(newPassword);

                            // Save updated user
                            database.collection("users").document(user.getId())
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> callback.onUserReceived(user))
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                        } else {
                            callback.onError("Invalid user data");
                        }
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }



    public void updateUser(User user, UserCallback callback) {
        database.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onUserReceived(user))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    public void deleteUser(String UserId, UserCallback callback) {
        database.collection("users").document(UserId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onUserReceived(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getAllUsers(UsersCallback callback) {
        database.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<User> users = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId());
                            users.add(user);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void decrementUserSummaryCount(String userId, UserCallback callback) {
        // Get a reference to the user document
        DocumentReference userRef = database.collection("users").document(userId);

        // Use a transaction to ensure atomic update
        database.runTransaction(transaction -> {
            DocumentSnapshot userSnapshot = transaction.get(userRef);
            User user = userSnapshot.toObject(User.class);

            if (user != null) {
                // Decrement the summary count (ensure it doesn't go below 0)
                int currentCount = user.getSumCount();
                int newCount = Math.max(0, currentCount - 1);
                transaction.update(userRef, "sumCount", newCount);
            }

            return null;
        }).addOnSuccessListener(result -> {
            // After successful update, retrieve the updated user
            getUserById(userId, callback);
        }).addOnFailureListener(e -> {
            callback.onError("Failed to update summary count: " + e.getMessage());
        });
    }



}







