package com.example.myfinalproject.UserProfileFragment;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Repositories.UserRepository;
import com.example.myfinalproject.DataModels.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;


public class UserProfilePresenter {
    private UserProfileFragment view;
    private UserRepository database;
    private ListenerRegistration userListener;


    public UserProfilePresenter(UserProfileFragment view) {
        this.view = view;
        this.database = new UserRepository();
    }

    // טוען את נתוני המשתמש לפי המזהה השמור ב־SharedPreferences
    public void loadUserData() {
        SharedPreferences sp = view.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sp.getString("userId", "");
        if (userId.isEmpty()) {
            view.showError("משתמש לא מזוהה, אנא התחבר מחדש.");
            return;
        }

        // שליפת הנתונים מה־Repository
        database.getUserById(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                if (user != null)
                    view.displayUserData(user); // הצגת הנתונים בממשק המשתמש
                else
                    view.showError("לא נמצאו נתוני משתמש");
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }

    // מתנתק מהמשתמש הנוכחי
    public void logOut() {
        FirebaseAuth.getInstance().signOut(); // התנתקות מ־Firebase
        view.onLogOutSuccess(); // עדכון הממשק המשתמש
    }


    // מוחק את המשתמש ממסד הנתונים
    public void deleteUser() {
        SharedPreferences sp = view.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sp.getString("userId", "");
        if (userId.isEmpty()) {
            view.showError("משתמש לא מזוהה");
            return;
        }
        database.deleteUser(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                view.onDeleteSuccess();
            } // עדכון ממשק המשתמש לאחר המחיקה

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }


    // מתבצע כאשר המשתמש לוחץ על "שמור שינויים" – שולח את הנתונים המעודכנים ל־Firestore
    public void submitClicked(User updatedUser) {
        database.updateUser(updatedUser, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                view.displayUserData(user);
            } // הצגת הנתונים המעודכנים

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }


    // מתחיל האזנה בזמן אמת לעדכוני המשתמש ממסד הנתונים
    public void startUserRealtimeUpdates() {
        SharedPreferences sp = view.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sp.getString("userId", "");
        if (userId.isEmpty()) {
            view.showError("משתמש לא מזוהה, אנא התחבר מחדש.");
            return;
        }

        userListener = database.listenToUserById(userId, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                if (user != null) {
                    view.displayUserData(user);  // עדכון הנתונים בממשק המשתמש בזמן אמת
                }
            }

            @Override
            public void onError(String message) {
                view.showError(message);
            }
        });
    }
    // מפסיק את ההאזנה לעדכונים בזמן אמת
    public void stopUserRealtimeUpdates() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }

}