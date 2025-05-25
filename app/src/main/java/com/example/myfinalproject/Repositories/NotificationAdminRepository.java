package com.example.myfinalproject.Repositories;

import com.example.myfinalproject.DataModels.NotificationAdmin;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class NotificationAdminRepository {
    private static NotificationAdminRepository instance; // משתנה סטטי עבור תבנית להגבלת יצירת המופע של המחלקה למופע יחיד
    private final CollectionReference notifications; // קישור לאוסף "notifications" ב-Firestore

    // בנאי שמאתחל את הרפרנס לאוסף הדיווחים במסד הנתונים
    public NotificationAdminRepository() {
        notifications = FirebaseFirestore.getInstance().collection("notifications");
    }

    // מימוש תבנית שמחזיר מופע יחיד של הרפוזיטורי
    public static NotificationAdminRepository getInstance() {
        if (instance == null) {
            instance = new NotificationAdminRepository();
        }
        return instance;
    }

    // הוספת דיווח למסד הנתונים
    public Task<Void> addNotification(NotificationAdmin notification) {
        return notifications.add(notification) // הוספה למסד הנתונים
                .continueWith(task -> {
                    // אם ההוספה הצליחה, שומרים את ה-ID של המסמך במודל
                    if (task.isSuccessful() && task.getResult() != null) {
                        notification.setId(task.getResult().getId());
                    }
                    return null;
                });
    }

    // שליפת כל הדיווחים ממויינים לפי הדיווח החדש ביותר לישן ביותר
    public Query getAllNotifications() {
        return notifications.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    // מחיקת דיווח לפי מזהה
    public Task<Void> deleteNotification(String notificationId) {
        return notifications.document(notificationId).delete();
    }
}
