package com.example.myfinalproject.Utils;

public class Validator {
    public static String isValidPassword(String password) {
        if (password.length() < 8) {
            return "סיסמה צריכה להכיל לפחות 8 תווים";
        }
        boolean hasUppercase = false;
        boolean hasSpecialChar = false;
        boolean hasNumber = false;
        String specialChars = "!@#$%^&*()_+\\-=[\\]{}|;:'\",<>/?";

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            }
            if (specialChars.indexOf(c) >= 0) {
                hasSpecialChar = true;
            }
            if(Character.isDigit(c)) {
                hasNumber = true;
            }

        }
        return (hasUppercase && hasSpecialChar && hasNumber) ? password : "סיסמה צריכה להכיל תו מיוחד, אות גדולה וספרה";
    }
    public static String isValidUsername(String username) {
        if(username.length() < 3) {
            return "שם משתמש חייב להכיל לפחות 3 תווים ";
        }
        return username;
    }



}
