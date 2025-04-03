package com.example.myfinalproject.Timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myfinalproject.MainActivity;
import com.example.myfinalproject.R;

public class CountdownTimerService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "TimerServiceChannel";

    private final IBinder binder = new LocalBinder();
    private CountDownTimer countDownTimer;
    private long timeRemaining = 0;
    private boolean isTimerRunning = false;
    private int notificationMinutes = 0;
    private boolean notificationSent = false;
    private boolean notificationsEnabled = true;

    private TimerUpdateListener timerUpdateListener;

    public interface TimerUpdateListener {
        void onTimerUpdate(long millisUntilFinished);
        void onTimerFinish();
    }

    public class LocalBinder extends Binder {
        CountdownTimerService getService() {
            return CountdownTimerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            long timeMillis = intent.getLongExtra("TIME_MILLIS", 0);
            notificationMinutes = intent.getIntExtra("NOTIFICATION_MINUTES", 0);
            notificationsEnabled = intent.getBooleanExtra("NOTIFICATIONS_ENABLED", true);

            if (timeMillis > 0) {
                startTimer(timeMillis, notificationMinutes);
            }
        }

        startForeground(NOTIFICATION_ID, createNotification("טיימר פעיל"));

        return START_STICKY;
    }

    public void startTimer(long millisInFuture, int notificationMinutes) {
        if (millisInFuture <= 0) return;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        notificationSent = false;
        timeRemaining = millisInFuture;
        isTimerRunning = true;
        this.notificationMinutes = notificationMinutes;

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                if (timerUpdateListener != null) {
                    timerUpdateListener.onTimerUpdate(millisUntilFinished);
                }

                if (notificationsEnabled && !notificationSent && notificationMinutes > 0 &&
                        millisUntilFinished <= notificationMinutes * 60 * 1000) {
                    sendNotification("התראת טיימר",
                            "נשארו " + notificationMinutes + " דקות לסיום הטיימר!");
                    notificationSent = true;
                }

                updateNotification(formatTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                isTimerRunning = false;
                if (timerUpdateListener != null) {
                    timerUpdateListener.onTimerFinish();
                }

                if (notificationsEnabled) {
                    sendNotification("הטיימר הסתיים!", "סיימת את זמן הלמידה שלך!");
                }

                stopForeground(true);
                stopSelf();
            }
        };

        countDownTimer.start();
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            updateNotification("הטיימר הושהה - " + formatTime(timeRemaining));
        }
    }

    public void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            timeRemaining = 0;
            stopForeground(true);
            stopSelf();
        }
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }

    public boolean areNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setTimerUpdateListener(TimerUpdateListener listener) {
        this.timerUpdateListener = listener;
    }

    public void removeTimerUpdateListener() {
        this.timerUpdateListener = null;
    }

    private String formatTime(long millis) {
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) (millis % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) (millis % (1000 * 60)) / 1000;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "טיימר למידה",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createNotification(String title) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("סכמו אותי!")
                .setSmallIcon(R.drawable.newlogo)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification(String timeString) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("טיימר פעיל - " + timeString)
                .setContentText("סכמו אותי!")
                .setSmallIcon(R.drawable.newlogo)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void sendNotification(String title, String content) {
        if (!notificationsEnabled) {
            return;
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.newlogo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(2, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}