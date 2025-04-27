package com.example.myfinalproject.Database;

import com.example.myfinalproject.Models.NotificationAdmin;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class NotificationAdminDatabase {
    private static NotificationAdminDatabase instance;
    private final CollectionReference notifications;

    public NotificationAdminDatabase() {
        notifications = FirebaseFirestore.getInstance().collection("notifications");
    }

    public static NotificationAdminDatabase getInstance() {
        if (instance == null) {
            instance = new NotificationAdminDatabase();
        }
        return instance;
    }

    public Task<Void> addNotification(NotificationAdmin notification) {
        return notifications.add(notification)
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        notification.setId(task.getResult().getId());
                    }
                    return null;
                });
    }

    public Query getAllNotifications() {
        return notifications.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getNotificationsByType(String type) {
        return notifications
                .whereEqualTo("type", type)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }


    public Task<Void> deleteNotification(String notificationId) {
        return notifications.document(notificationId).delete();
    }
}
