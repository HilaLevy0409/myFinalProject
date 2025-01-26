package com.example.myfinalproject.Utils;

public class Validator {
    public static String isValidUsername(String username) {
        if (username.length() < 3) {
            return "שם משתמש חייב להכיל לפחות 3 תווים ";
        }
        return "";
    }

    public static String isValidPassword(String password) {
        if (password.length() < 8) {
            return "סיסמה צריכה להכיל לפחות 8 תווים";
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasSpecialChar = false;
        boolean hasNumber = false;
        String specialChars = "!@#$%^&*()_+\\-=[\\]{}|;:'\",<>/?";

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            }
            if (Character.isLowerCase(c)) {
                hasLowercase = true;
            }
            if (specialChars.indexOf(c) >= 0) {
                hasSpecialChar = true;
            }
            if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }

        return (hasUppercase && hasSpecialChar && hasNumber && hasLowercase) ? "" : "סיסמה צריכה להכיל תו מיוחד, אות גדולה, אות קטנה וספרה";
    }

    public static String isValidEmail(String email) {
        if (!email.contains("@")) {
            return "אימייל חייב לכלול את התו '@'";
        }
        return "";
    }
}
