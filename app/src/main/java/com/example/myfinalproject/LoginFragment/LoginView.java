package com.example.myfinalproject.LoginFragment;

import com.example.myfinalproject.Models.User;

public interface LoginView {
    void showLoginSuccess(User user);
    void showLoginFailure(String error);
}
