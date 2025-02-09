package com.example.myfinalproject.UserProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.myfinalproject.CallBacks.AddUserCallback;
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

//    public void updateUserData(User updatedUser) {
//        String userId = mAuth.getCurrentUser().getUid();
//
//        db.updateUser(userId, updatedUser, new UserCallback() {
//            @Override
//            public void onUserReceived(User user) {
//                view.onUpdateSuccess();
//            }
//
//            @Override
//            public void onError(String error) {
//                view.onUpdateError(error);
//            }
//        });
//    }

//    public void updateUserDetails(String userId, String newEmail, String newPhone, String newUsername) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        Map<String, Object> updatedUserData = new HashMap<>();
//        updatedUserData.put("userEmail", newEmail);
//        updatedUserData.put("phone", newPhone);
//        updatedUserData.put("userName", newUsername);
//
//        db.collection("Users").document(userId)
//                .update(updatedUserData)
//                .addOnSuccessListener(aVoid -> view.showSuccessMessage("פרטי המשתמש עודכנו בהצלחה!"))
//                .addOnFailureListener(e -> view.showError("שגיאה בעדכון הנתונים: " + e.getMessage()));
//    }


    public void submitClicked(User user) {
        db.addUser(user, new AddUserCallback() {
            @Override
            public void onUserAdd(User user) {

            }

            @Override
            public void onError(String error) {

            }
        });

    }
}