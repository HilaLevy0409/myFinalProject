package com.example.myfinalproject;

public class Admin {
    private static boolean isAdminLoggedIn = false; // משתנה סטטי שמייצג האם המנהל מחובר כרגע
    private static long loginTime = 0;// משתנה שמייצג את זמן ההתחברות האחרון (במילישניות)

    /**
     * פונקציה שמבצעת התחברות של המנהל
     * משנה את isAdminLoggedIn ל-true
     * ושומרת את זמן ההתחברות הנוכחי
     */
    public static void login() {
        isAdminLoggedIn = true;
        loginTime = System.currentTimeMillis();
    }

    /**
     * פונקציה שמבצעת התנתקות של המנהל
     * מאפסת את isAdminLoggedIn ל-false
     * ומאפסת את loginTime ל-0
     */
    public static void logout() {
        isAdminLoggedIn = false;
        loginTime = 0;
    }

    /**
     * פונקציה שבודקת האם המנהל מחובר כרגע
     * מחזירה true אם כן, false אם לא
     */
    public static boolean isAdminLoggedIn() {
        return isAdminLoggedIn;
    }

    /**
     * פונקציה שבודקת האם הזמן ההתחברות של ההנהלה פג תוקף
     *  נחשבת שפג תוקפה אם:
     * - המנהל מחובר
     * - עברו יותר מ-5 דקות (5 * 60 * 1000 מילישניות) מאז ההתחברות
     */
    public static boolean isSessionExpired() {
        long now = System.currentTimeMillis();
        return isAdminLoggedIn && (now - loginTime > 5 * 60 * 1000);
    }


    /*
     * ----------------------------------------------------------
     * הבדל בין הפונקציה login לבין הפונקציה isAdminLoggedIn:
     *
     * login():
     * - משנה את מצב המערכת (מחברת את המנהל)
     * - מעדכנת את זמן ההתחברות
     * - לא מחזירה ערך (void)
     *
     * isAdminLoggedIn():
     * - רק בודקת האם המנהל מחובר
     * - לא משנה כלום במערכת
     * - מחזירה true או false
     * ----------------------------------------------------------
     */
}