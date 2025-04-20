package com.example.myfinalproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Database.UserDatabase;
import com.example.myfinalproject.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordResetHandler {
    private static final String TAG = "PasswordResetHandler";
    private static final String PREFS_NAME = "PasswordResetPrefs";
    private static final String LAST_LOGIN_TIME = "LastLoginTime";

    private final Context context;
    private final UserDatabase userDatabase;
    private FirebaseAuth.AuthStateListener authStateListener;

    public PasswordResetHandler(Context context) {
        this.context = context;
        this.userDatabase = new UserDatabase();
        setupAuthStateListener();
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                String email = user.getEmail();

                // Check if this user has a pending password reset
                if (email != null && hasPendingReset(email)) {
                    // Get the new token to refresh metadata
                    user.getIdToken(true)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // We don't have direct access to password change timestamp
                                    // Let's use the last account update time and compare with
                                    // our saved last login time
                                    long lastSignInTime = user.getMetadata().getLastSignInTimestamp();
                                    long lastAccountUpdateTime = user.getMetadata().getCreationTimestamp();

                                    // For Firebase, if the account was updated after creation,
                                    // this likely means a password change or other account update

                                    // Get our stored last login time
                                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                    long storedLastLoginTime = prefs.getLong(LAST_LOGIN_TIME + email, 0);

                                    Log.d(TAG, "Last sign-in: " + lastSignInTime);
                                    Log.d(TAG, "Last stored login: " + storedLastLoginTime);

                                    // If this is a different login session
                                    // and we had a pending reset, update the password
                                    if (storedLastLoginTime != lastSignInTime) {
                                        // Update Firestore
                                        Log.d(TAG, "New login after password reset, updating in Firestore");

                                        // We can't know the new password, so we'll set it to a placeholder
                                        // and the user will need to use Firebase Authentication
                                        userDatabase.updateUserPassword(email, "RESET_WITH_FIREBASE_AUTH", new UserCallback() {
                                            @Override
                                            public void onUserReceived(User user) {
                                                Log.d(TAG, "Firestore password updated for " + email);
                                                clearPendingReset(email);

                                                // Store the new login time
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putLong(LAST_LOGIN_TIME + email, lastSignInTime);
                                                editor.apply();
                                            }

                                            @Override
                                            public void onError(String error) {
                                                Log.e(TAG, "Failed to update Firestore password: " + error);
                                            }
                                        });
                                    }
                                }
                            });
                } else {
                    // Store last login time for comparison on next login
                    SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(LAST_LOGIN_TIME + email, user.getMetadata().getLastSignInTimestamp());
                    editor.apply();
                }
            }
        };
    }

    public void startListening() {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    public void stopListening() {
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    private boolean hasPendingReset(String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(email, false);
    }

    private void clearPendingReset(String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(email);
        editor.apply();
    }
}