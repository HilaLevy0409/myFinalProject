package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.DataModels.User;

public interface LoginCallback {
    void showLoginSuccess(User user);
    void showLoginFailure(String error);
}
