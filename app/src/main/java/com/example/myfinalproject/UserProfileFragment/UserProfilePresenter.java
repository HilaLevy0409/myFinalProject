package com.example.myfinalproject.UserProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Database.UserDatabase;
import com.example.myfinalproject.Models.User;
import com.google.firebase.auth.FirebaseAuth;

public class UserProfilePresenter {
    private UserProfileFragment view;
    private UserDatabase db;
    private FirebaseAuth mAuth;

    public UserProfilePresenter(UserProfileFragment view) {
        this.view = view;
        this.db = new UserDatabase();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void loadUserData() {
        SharedPreferences sharedPreferences = view.requireContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");

        if (userId.isEmpty()) {
            view.showError("משתמש לא נמצא");
            return;
        }

        db.getUserById(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                view.displayUserData(user);
            }

            @Override
            public void onError(String error) {
                view.showError(error);
            }
        });
    }

    public void deleteUser() {
        String userId = mAuth.getCurrentUser().getUid();
        db.deleteUser(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                mAuth.getCurrentUser().delete()
                        .addOnSuccessListener(aVoid -> view.onDeleteSuccess())
                        .addOnFailureListener(e -> view.showError(e.getMessage()));
            }

            @Override
            public void onError(String error) {
                view.showError(error);
            }
        });
    }

    public void logOut() {
        mAuth.signOut();
        view.onLogOutSuccess();
    }
}