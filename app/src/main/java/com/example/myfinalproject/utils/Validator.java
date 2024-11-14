package com.example.myfinalproject.utils;

public class Validator {
    public static String isValidPassword(String password) {
        if (password.length() < 8) {
            return "סיסמה צריכה להכיל לפחות 8 תווים";
        }
        boolean hasUppercase = false;
        boolean hasSpecialChar = false;
        String specialChars = "!@#$%^&*()_+\\-=[\\]{}|;:'\",<>/?";

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            }
            if (specialChars.indexOf(c) >= 0) {
                hasSpecialChar = true;
            }

        }
        return (hasUppercase && hasSpecialChar) ? "" : "סיסמה צריכה להכיל תו מיוחד ואות גדולה";
    }
    public static String isValidUsername(String username) {
        if(username.length() < 3) {
            return "שם משתמש חייב להכיל 3 תווים לפחות";
        }
        return "";
    }



}
