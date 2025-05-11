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

import com.example.myfinalproject.CallBacks.TimeCallback;
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

    private TimeCallback timerUpdateCallback;

    public class LocalBinder extends Binder {
        public CountdownTimerService getService() {

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

        startForeground(NOTIFICATION_ID, createForegroundNotification("טיימר פעיל"));

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

        if (notificationsEnabled) {
            sendStartNotification(millisInFuture);
        }

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;

                if (timerUpdateCallback != null) {
                    timerUpdateCallback.onTimerUpdate(millisUntilFinished);
                }

                if (notificationsEnabled && !notificationSent && notificationMinutes > 0) {
                    if (millisUntilFinished <= notificationMinutes * 60 * 1000 &&
                            millisUntilFinished > (notificationMinutes * 60 * 1000 - 1000)) {
                        sendWarningNotification(notificationMinutes);
                        notificationSent = true;
                    }
                }
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                isTimerRunning = false;

                if (timerUpdateCallback != null) {
                    timerUpdateCallback.onTimerFinish();
                }

                if (notificationsEnabled) {
                    sendFinishNotification();
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

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.notify(NOTIFICATION_ID, createForegroundNotification("טיימר מושהה"));
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


    public void setTimerUpdateCallback(TimeCallback callback) {
        this.timerUpdateCallback = callback;
    }

    public void removeTimerUpdateCallback() {
        this.timerUpdateCallback = null;
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
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("התראות עבור טיימר הלמידה");
            serviceChannel.enableLights(true);
            serviceChannel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createForegroundNotification(String title) {
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
                .setOngoing(true)
                .setSilent(true)
                .build();
    }

    private void sendStartNotification(long totalMillis) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("טיימר הופעל")
                .setContentText("זמן הטיימר: " + formatTime(totalMillis))
                .setSmallIcon(R.drawable.newlogo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        notificationManager.notify(2, notification);
    }

    private void sendWarningNotification(int minutesRemaining) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("התראת טיימר")
                .setContentText("נשארו " + minutesRemaining + " דקות לסיום הטיימר!")
                .setSmallIcon(R.drawable.newlogo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        notificationManager.notify(3, notification);
    }

    private void sendFinishNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("הטיימר הסתיים!")
                .setContentText("סיימת את זמן הלמידה שלך!")
                .setSmallIcon(R.drawable.newlogo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        notificationManager.notify(4, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}