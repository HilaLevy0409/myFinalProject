package com.example.myfinalproject.RegistrationFragment;

import android.util.Log;
import android.widget.Toast;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.Database.UserDatabase;

import java.util.Objects;

public class RegisterUserPresenter {

    RegistrationFragment view;
    UserDatabase userDb;

    public RegisterUserPresenter(RegistrationFragment view) {
        this.view = view;
        this.userDb = new UserDatabase();
    }

    public void submitClicked(User user) {
        userDb.addUser(user, new AddUserCallback() {
            @Override
            public void onUserAdd(User user) {
                Toast.makeText(view.getContext(), "המשתמש " + user.getUserName() + " התווסף בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {

            }
        });

    }


}

