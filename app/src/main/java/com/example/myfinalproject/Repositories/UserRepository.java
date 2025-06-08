package com.example.myfinalproject.Repositories;

import android.util.Log;

import com.example.myfinalproject.CallBacks.AddUserCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.CallBacks.UsersCallback;
import com.example.myfinalproject.DataModels.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

import com.google.firebase.firestore.ListenerRegistration;


public class UserRepository {

    private FirebaseAuth mAuth;
    private FirebaseFirestore database;

    public UserRepository() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
    }

    // פונקציה ליצירת משתמש חדש באימות וב-DB
    public void addUser(User user, AddUserCallback callback) {
        // יצירת משתמש עם אימייל וסיסמה באימות של Firebase
        mAuth.createUserWithEmailAndPassword(user.getUserEmail(), user.getUserPass())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // אם נוצר משתמש בהצלחה, שומרים את המזהה שלו
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser.getUid();
                        user.setId(userId);

                        // הפניה למסמך של המשתמש במסד הנתונים
                        DocumentReference userRef = database.collection("users").document(userId);

                        // שמירת פרטי המשתמש במסד הנתונים
                        userRef.set(user)
                                .addOnSuccessListener(aVoid -> {
                                    callback.onUserAdd(user);
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        // טיפול במקרה של שגיאה ביצירת המשתמש
                        System.err.println("Error creating user: " + Objects.requireNonNull(task.getException()).getMessage());
                        callback.onError(task.getException().getMessage());
                    }
                });
    }

    // שליפת משתמש לפי מזהה (id)
    public void getUserById(String userId, UserCallback callback) {
        database.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            callback.onUserReceived(user);
                        } else {
                            callback.onError("שגיאה בטעינת נתוני משתמש");
                        }
                    } else {
                        callback.onError("לא נמצא משתמש");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // שליפת משתמש לפי שם משתמש (userName)
    public void getUser(final String username, final UserCallback callback) {
        // ביצוע שאילתה על אוסף המשתמשים לפי שם משתמש
        Query userQuery = database.collection("users").whereEqualTo("userName", username);

        userQuery.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // קבלת המסמך הראשון מהתוצאה
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            callback.onUserReceived(user);
                        } else {
                            callback.onError("נתוני המשתמש אינם חוקיים");
                        }
                    } else {
                        callback.onError("לא נמצא משתמש");
                    }
                })
                .addOnFailureListener(e -> {
                    String errorMessage = "Database error: " + e.getMessage();
                    Log.e("FirestoreError", errorMessage);
                    callback.onError(errorMessage);
                });
    }


    // עדכון פרטי משתמש במסד הנתונים
    public void updateUser(User user, UserCallback callback) {
        database.collection("users").document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onUserReceived(user))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // מחיקת משתמש לפי מזהה
    public void deleteUser(String UserId, UserCallback callback) {
        database.collection("users").document(UserId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onUserReceived(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // שליפת כל המשתמשים
    public void getAllUsers(UsersCallback callback) {
        database.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<User> users = new ArrayList<>();
                    // המרת כל מסמך לאובייקט User
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId());
                            users.add(user);
                        }
                    }
                    // החזרת הרשימה המלאה ב-callback
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // האזנה לשינויים בזמן אמת לפי מזהה
    public ListenerRegistration listenToUserById(String userId, UserCallback callback) {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);
        // הפעלת listener שמאזין לשינויים במסמך
        return userRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                callback.onError("שגיאה בקבלת נתונים בזמן אמת: " + error.getMessage());
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                callback.onUserReceived(user);
            }
        });
    }
}