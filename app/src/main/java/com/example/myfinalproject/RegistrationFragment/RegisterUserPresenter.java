package com.example.myfinalproject.RegistrationFragment;

import android.widget.Toast;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.Database.UserDatabase;


public class RegisterUserPresenter {

    RegistrationFragment view;
    UserDatabase userDb;

    User user;
    String id;


    public RegisterUserPresenter(RegistrationFragment view) {
        this.view = view;
        this.userDb = new UserDatabase();

        this.id = id;

    }



            public void submitClicked(User user) {
        userDb.addUser(user, new AddUserCallback() {
            @Override
            public void onUserAdd(User user) {
                Toast.makeText(view.getContext(), "המשתמש " + user.getUserName() + " התווסף בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(view.getContext(), "אירעה שגיאה בהרשמה: " + error, Toast.LENGTH_SHORT).show();

            }
        });

    }


}

