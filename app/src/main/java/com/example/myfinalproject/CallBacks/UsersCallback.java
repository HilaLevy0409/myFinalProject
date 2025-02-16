package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.Models.User;

import java.util.List;


public interface UsersCallback {

    public void onSuccess(List<User> users);
    public void onError(String message);

}
