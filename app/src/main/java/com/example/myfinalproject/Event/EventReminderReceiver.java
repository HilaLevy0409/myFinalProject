package com.example.myfinalproject.Event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.example.myfinalproject.R;

public class EventReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "event_reminder_channel";
    private static final String TAG = "EventReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String eventTitle = intent.getStringExtra("eventTitle");
            if (eventTitle == null) {
                eventTitle = "אירוע מתוזמן";
                Log.e(TAG, "Event title was null");
            }

            createNotificationChannel(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setContentTitle("תזכורת לאירוע")
                    .setContentText("תזכורת: " + eventTitle)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                Log.d(TAG, "Notification sent for event: " + eventTitle);
            } else {
                Log.e(TAG, "NotificationManager was null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying notification", e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Reminder Channel";
            String description = "Channel for event reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}