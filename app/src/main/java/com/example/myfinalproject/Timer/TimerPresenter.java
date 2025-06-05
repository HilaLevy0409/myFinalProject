package com.example.myfinalproject.Timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.myfinalproject.CallBacks.TimeCallback;

public class TimerPresenter {
    private TimerFragment view;
    private Context context;
    private CountdownTimerService timerService;
    private boolean isBound = false;
    private boolean isTimerPaused = false;

    // אובייקט ServiceConnection לקישור בין הפרגמנט לשירות
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // בעת חיבור לשירות - קבלת השירות
            CountdownTimerService.LocalBinder binder = (CountdownTimerService.LocalBinder) service;
            timerService = binder.getService();
            isBound = true;

            // קביעת Callback לעדכון זמן ומתי מסתיים
            timerService.setTimerUpdateCallback(new TimeCallback() {
                @Override
                public void onTimerUpdate(long millisUntilFinished) {
                    if (view != null) {
                        view.updateTimerDisplay(millisUntilFinished);  // עדכון התצוגה
                    }
                }

                @Override
                public void onTimerFinish() {
                    if (view != null) {
                        view.updateTimerDisplay(0);
                        view.showMessage("הטיימר הסתיים!");
                        view.resetButtonStates();
                    }
                }
            });

            // במידה שהטיימר כבר רץ – עדכון מצב הכפתורים והתצוגה
            if (timerService.isTimerRunning()) {
                if (view != null) {
                    view.updateTimerDisplay(timerService.getTimeRemaining());
                    view.updateButtonStates(true);
                }
                // אם הטיימר לא רץ אך יש זמן שמור – מציג את הזמן כהשהייה
            } else if (timerService.getTimeRemaining() > 0) {
                if (view != null) {
                    view.updateTimerDisplay(timerService.getTimeRemaining());
                    view.updateButtonStates(false);
                    isTimerPaused = true;
                    view.setStopContinueButtonText("המשך");
                    view.setStopContinueButtonEnabled(true);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        } // מתריעה למערכת שאין חיבור לשירות, כדי שלא תנסה להמשיך לתקשר איתו.
    };

    public TimerPresenter(TimerFragment view) {
        this.view = view;
        this.context = view.getContext();
    }

    // קישור לשירות הטיימר
    public void bindService() {
        Intent intent = new Intent(context, CountdownTimerService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // ניתוק מהשירות
    public void unbindService() {
        if (isBound) {
            timerService.removeTimerUpdateCallback(); // ביטול הקשר עם Callback
            context.unbindService(serviceConnection);
            isBound = false;
        }
    }

    // שינוי מצב ההתראה בשירות
    public void onNotificationSwitchChanged(boolean isChecked) {
        if (isBound) {
            timerService.setNotificationsEnabled(isChecked);
        }
    }

    // פעולה שמחליפה בין מצב עצירה להמשך טיימר
    public void toggleStopContinue() {
        if (isBound) {
            if (!isTimerPaused) {
                // עצירה: השהיית הטיימר
                timerService.pauseTimer();
                view.setStopContinueButtonText("המשך");
                isTimerPaused = true;
            } else {
                // המשך: בדיקות ולחזור להריץ
                if (timerService.getTimeRemaining() > 0) {
                    int notificationMinutes = 0;
                    if (view.isNotificationEnabled()) {
                        String notificationText = view.getNotificationTime();
                        // בדיקת תקינות זמן ההתראה
                        if (!notificationText.isEmpty()) {
                            notificationMinutes = Integer.parseInt(notificationText);
                        } else {
                            view.showMessage("יש להזין זמן התראה");
                            return;
                        }

                        long totalSeconds = timerService.getTimeRemaining() / 1000;
                        long notificationSeconds = notificationMinutes * 60;

                        if (notificationSeconds >= totalSeconds) {
                            view.showMessage("זמן ההתראה גדול מזמן הטיימר");
                            return;
                        }
                    }

                    // הפעלת הטיימר מחדש
                    timerService.startTimer(timerService.getTimeRemaining(), notificationMinutes);
                    timerService.setNotificationsEnabled(view.isNotificationEnabled());
                    view.setStopContinueButtonText("עצור");
                    isTimerPaused = false;
                    view.setStartButtonEnabled(false);
                }
            }
        }
    }

    // התחלת הטיימר מהתחלה
    public void startTimer() {
        if (isBound) {
            try {
                // קבלת ערכים מהתצוגה
                int hours = view.getHours();
                int minutes = view.getMinutes();
                int seconds = view.getSeconds();
                int notificationMinutes = 0;

                long totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

                // בדיקה אם הוזן זמן כלשהו
                if (totalTimeInMillis <= 0) {
                    view.showMessage("יש להזין זמן גדול מאפס");
                    return;
                }
                // בדיקת זמן ההתראה
                if (view.isNotificationEnabled()) {
                    String notificationText = view.getNotificationTime();
                    if (notificationText.isEmpty()) {
                        view.showMessage("יש להזין זמן התראה");
                        return;
                    }
                    notificationMinutes = Integer.parseInt(notificationText);

                    long totalSeconds = (totalTimeInMillis / 1000);
                    long notificationSeconds = notificationMinutes * 60;

                    if (notificationSeconds >= totalSeconds) {
                        view.showMessage("זמן ההתראה גדול מזמן הטיימר");
                        return;
                    }
                }

                // הפעלת השירות עם הפרמטרים הנדרשים
                Intent serviceIntent = new Intent(context, CountdownTimerService.class);
                serviceIntent.putExtra("TIME_MILLIS", totalTimeInMillis);
                serviceIntent.putExtra("NOTIFICATION_MINUTES", notificationMinutes);
                serviceIntent.putExtra("NOTIFICATIONS_ENABLED", view.isNotificationEnabled());
                context.startService(serviceIntent);

                // הפעלת הטיימר דרך השירות
                timerService.startTimer(totalTimeInMillis, notificationMinutes);
                timerService.setNotificationsEnabled(view.isNotificationEnabled());
                view.updateButtonStates(true);
                isTimerPaused = false;

            } catch (NumberFormatException e) {
                view.showMessage("נא להזין מספרים בלבד");
            }
        }
    }

    // איפוס הטיימר והפרגמנט
    public void resetTimer() {
        if (isBound) {
            timerService.resetTimer();            // איפוס הטיימר בשירות
            view.updateTimerDisplay(0); // הצגת זמן אפס
            view.clearInputFields();             // ניקוי שדות
            view.resetButtonStates();            // איפוס הכפתורים
        }
    }
}