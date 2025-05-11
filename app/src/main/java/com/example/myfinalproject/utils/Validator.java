package com.example.myfinalproject.Utils;

public class Validator {
    public static String isValidUsername(String username) {
        if (username.length() < 3) {
            return "שם משתמש חייב להכיל לפחות 3 תווים";
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
        if (email == null || email.trim().isEmpty()) {
            return "נא להזין אימייל";
        }

        if (!email.contains("@")) {
            return "אימייל חייב לכלול את התו '@'";
        }
        return "";
    }



    public static String isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "נא להזין מספר טלפון";
        }

        if (!phone.startsWith("05")) {
            return "מספר טלפון חייב להתחיל ב-05";
        }
        if (phone.length() < 10) {
            return "מספר טלפון חייב להכיל 10 ספרות";
        }

        return "";
    }


    public static String isValidBirthDate(String birthDate) {
        if (birthDate == null) {
            return "נא להזין תאריך לידה";
        }
        return "";
    }

    public static String isValidImageProfile(String imageProfile) {
        if (imageProfile == null) {
            return "נא להעלות תמונת פרופיל";
        }
        return "";
    }
}
