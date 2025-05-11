package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.DataModels.User;

public interface UserCallback {
    void onUserReceived(User user);
    void onError(String error);
}