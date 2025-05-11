package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.DataModels.User;

public interface AddUserCallback {
    void onUserAdd(User user);
    void onError(String error);
}