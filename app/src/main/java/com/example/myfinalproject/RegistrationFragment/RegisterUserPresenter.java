package com.example.myfinalproject.RegistrationFragment;

import android.util.Log;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.DataModels.User;
import com.example.myfinalproject.Repositories.UserRepository;

public class RegisterUserPresenter {
    private static final String TAG = "RegisterUserPresenter";
    private RegistrationFragment view;
    private UserRepository userDb;

    public RegisterUserPresenter(RegistrationFragment view) {
        this.view = view;
        this.userDb = new UserRepository();
    }

    public void submitClicked(User user, AddUserCallback callback) {
        Log.d(TAG, "Submitting user registration for: " + user.getUserName());
        userDb.addUser(user, callback);
    }

    public void submitClicked(User user) {
        submitClicked(user, new AddUserCallback() {
            @Override
            public void onUserAdd(User user) {
                Log.d(TAG, "User added: " + user.getId());
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error adding user: " + error);
            }
        });
    }
}