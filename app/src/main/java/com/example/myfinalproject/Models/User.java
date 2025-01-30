package com.example.myfinalproject.Models;

public class User {
    private String userName;
    private String userPass;
    private String userEmail;
    private String phone;
    private String imageProfile;
    private int badPoints;
    private int sumCount;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public String getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(String imageProfile) {
        this.imageProfile = imageProfile;
    }





    public User() {}

    public User(String userName, String userPass, String userEmail, String phone) {
        this.userName = userName;
        this.userPass = userPass;
        this.userEmail = userEmail;
        this.phone = phone;

    }

    public String getUserEmail() {

        return userEmail;
    }

    public void setUserEmail(String userEmail) {

        this.userEmail = userEmail;
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(String userName) {

        this.userName = userName;
    }

    public String getUserPass() {

        return userPass;
    }

    public void setUserPass(String userPass) {

        this.userPass = userPass;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    public int getBadPoints() {
        return badPoints;
    }

    public void setBadPoints(int badPoints) {
        this.badPoints = badPoints;
    }

    public int getSumCount() {
        return sumCount;
    }

    public void setSumCount(int sumCount) {
        this.sumCount = sumCount;
    }



    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", userPass='" + userPass + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPhone='" + phone + '\'' +
                '}';
    }


}
