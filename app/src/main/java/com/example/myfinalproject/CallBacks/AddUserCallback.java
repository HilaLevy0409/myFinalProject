package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.Models.User;

public interface AddUserCallback {
    void onUserAdd(User user);
    void onError(String error);
}