package com.example.myfinalproject.models;

public class User {
    public String email;
    public String username;
    public String password;
    public String profileImageUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String username, String password, String profileImageUrl) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }
}
