package com.example.myfinalproject.LoginFragment;

import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Database.UserDatabase;
import com.example.myfinalproject.Models.User;

public class LoginUserPresenter {

    private final LoginView view;
    private final UserDatabase userDb;

    public LoginUserPresenter(LoginView view) {
        this.view = view;
        this.userDb = new UserDatabase();
    }

    public void loginUser(String username, String password) {
        userDb.getUser(username, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                if (user.getUserPass().equals(password)) {
                    view.showLoginSuccess(user);
                } else {
                    view.showLoginFailure("Invalid password.");
                }
            }

            @Override
            public void onError(String error) {
                view.showLoginFailure(error);
            }
        });
    }


}
