package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.DataModels.User;

import java.util.List;


public interface UsersCallback {

     void onSuccess(List<User> users);
     void onError(String message);

}
