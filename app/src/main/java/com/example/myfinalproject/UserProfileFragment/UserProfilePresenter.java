package com.example.myfinalproject.UserProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Repositories.UserRepository;
import com.example.myfinalproject.DataModels.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;


public class UserProfilePresenter {
    private UserProfileFragment view;
    private UserRepository database;
    private ListenerRegistration userListener;


    public UserProfilePresenter(UserProfileFragment view) {
        this.view = view;
        this.database = new UserRepository();
    }

    public void loadUserData() {
        SharedPreferences sp = view.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sp.getString("userId", "");
        if (userId.isEmpty()) {
            view.showError("משתמש לא מזוהה, אנא התחבר מחדש.");
            return;
        }
        database.getUserById(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                if (user != null)
                    view.displayUserData(user);
                else
                    view.showError("לא נמצאו נתוני משתמש");
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        view.onLogOutSuccess();
    }

    public void deleteUser() {
        SharedPreferences sp = view.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sp.getString("userId", "");
        if (userId.isEmpty()) {
            view.showError("משתמש לא מזוהה");
            return;
        }
        database.deleteUser(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                view.onDeleteSuccess();
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    public void submitClicked(User updatedUser) {
        database.updateUser(updatedUser, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                view.displayUserData(user);
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }


    public void startUserRealtimeUpdates() {
        SharedPreferences sp = view.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sp.getString("userId", "");
        if (userId.isEmpty()) {
            view.showError("משתמש לא מזוהה, אנא התחבר מחדש.");
            return;
        }

        userListener = database.listenToUserById(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                if (user != null) {
                    view.displayUserData(user);
                }
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    public void stopUserRealtimeUpdates() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

}