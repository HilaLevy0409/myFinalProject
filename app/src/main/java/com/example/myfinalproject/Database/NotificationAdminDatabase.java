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
    private final CollectionReference notificationsRef;

    public NotificationAdminDatabase() {
        notificationsRef = FirebaseFirestore.getInstance().collection("notifications");
    }

    public static NotificationAdminDatabase getInstance() {
        if (instance == null) {
            instance = new NotificationAdminDatabase();
        }
        return instance;
    }

    public Task<Void> addNotification(NotificationAdmin notification) {
        return notificationsRef.add(notification)
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        notification.setId(task.getResult().getId());
                    }
                    return null;
                });
    }

    public Query getAllNotifications() {
        return notificationsRef.orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Query getNotificationsByType(String type) {
        return notificationsRef
                .whereEqualTo("type", type)
                .orderBy("timestamp", Query.Direction.DESCENDING);
    }

    public Task<Void> markAsRead(String notificationId) {
        return notificationsRef.document(notificationId).update("read", true);
    }

    public Task<Void> deleteNotification(String notificationId) {
        return notificationsRef.document(notificationId).delete();
    }
}
