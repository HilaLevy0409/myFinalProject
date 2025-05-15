package com.example.myfinalproject.Repositories;

import com.example.myfinalproject.DataModels.NotificationAdmin;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class NotificationAdminRepository {
    private static NotificationAdminRepository instance;
    private final CollectionReference notifications;

    public NotificationAdminRepository() {
        notifications = FirebaseFirestore.getInstance().collection("notifications");

    }

    public static NotificationAdminRepository getInstance() {
        if (instance == null) {
            instance = new NotificationAdminRepository();
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




    public Task<Void> deleteNotification(String notificationId) {
        return notifications.document(notificationId).delete();
    }
}
