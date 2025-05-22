package com.example.myfinalproject;

public class Admin {
    private static boolean isAdminLoggedIn = false;
    private static long loginTime = 0;

    public static void login() {
        isAdminLoggedIn = true;
        loginTime = System.currentTimeMillis();
    }

    public static void logout() {
        isAdminLoggedIn = false;
        loginTime = 0;
    }

    public static boolean isAdminLoggedIn() {
        return isAdminLoggedIn;
    }

    public static boolean isSessionExpired() {
        long now = System.currentTimeMillis();
        return isAdminLoggedIn && (now - loginTime > 5 * 60 * 1000);
    }
}