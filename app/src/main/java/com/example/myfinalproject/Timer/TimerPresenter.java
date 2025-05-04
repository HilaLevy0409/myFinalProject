package com.example.myfinalproject.Timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.EditText;

import com.example.myfinalproject.CallBacks.TimeCallback;

public class TimerPresenter {
    private TimerFragment view;
    private Context context;
    private CountdownTimerService timerService;
    private boolean isBound = false;
    private boolean isTimerPaused = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CountdownTimerService.LocalBinder binder = (CountdownTimerService.LocalBinder) service;
            timerService = binder.getService();
            isBound = true;

            timerService.setTimerUpdateCallback(new TimeCallback() {
                @Override
                public void onTimerUpdate(long millisUntilFinished) {
                    if (view != null) {
                        view.updateTimerDisplay(millisUntilFinished);
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

            if (timerService.isTimerRunning()) {
                if (view != null) {
                    view.updateTimerDisplay(timerService.getTimeRemaining());
                    view.updateButtonStates(true);
                }
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
        }
    };

    public TimerPresenter(TimerFragment view) {
        this.view = view;
        this.context = view.getContext();
    }

    public void bindService() {
        Intent intent = new Intent(context, CountdownTimerService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        if (isBound) {
            timerService.removeTimerUpdateCallback();
            context.unbindService(serviceConnection);
            isBound = false;
        }
    }

    public void onNotificationSwitchChanged(boolean isChecked) {
        if (isBound) {
            timerService.setNotificationsEnabled(isChecked);
        }
    }

    public void toggleStopContinue() {
        if (isBound) {
            if (!isTimerPaused) {
                timerService.pauseTimer();
                view.setStopContinueButtonText("המשך");
                isTimerPaused = true;
            } else {
                if (timerService.getTimeRemaining() > 0) {
                    int notificationMinutes = 0;
                    if (view.isNotificationEnabled()) {
                        String notificationText = view.getNotificationTime();
                        if (!notificationText.isEmpty()) {
                            notificationMinutes = Integer.parseInt(notificationText);
                        } else {
                            view.showMessage("יש להזין זמן התראה");
                            return;
                        }

                        long totalMinutes = (timerService.getTimeRemaining() / 1000) / 60;

                        if (notificationMinutes >= totalMinutes) {
                            view.showMessage("זמן ההתראה גדול מזמן הטיימר");
                            return;
                        }
                    }

                    timerService.startTimer(timerService.getTimeRemaining(), notificationMinutes);
                    timerService.setNotificationsEnabled(view.isNotificationEnabled());
                    view.setStopContinueButtonText("עצור");
                    isTimerPaused = false;
                    view.setStartButtonEnabled(false);
                }
            }
        }
    }

    public void startTimer() {
        if (isBound) {
            try {
                int hours = view.getHours();
                int minutes = view.getMinutes();
                int seconds = view.getSeconds();
                int notificationMinutes = 0;

                long totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

                if (totalTimeInMillis <= 0) {
                    view.showMessage("יש להזין זמן גדול מאפס");
                    return;
                }

                if (view.isNotificationEnabled()) {
                    String notificationText = view.getNotificationTime();
                    if (notificationText.isEmpty()) {
                        view.showMessage("יש להזין זמן התראה");
                        return;
                    }
                    notificationMinutes = Integer.parseInt(notificationText);

                    long totalMinutes = (totalTimeInMillis / 1000) / 60;

                    if (notificationMinutes >= totalMinutes) {
                        view.showMessage("זמן ההתראה גדול מזמן הטיימר");
                        return;
                    }
                }

                Intent serviceIntent = new Intent(context, CountdownTimerService.class);
                serviceIntent.putExtra("TIME_MILLIS", totalTimeInMillis);
                serviceIntent.putExtra("NOTIFICATION_MINUTES", notificationMinutes);
                serviceIntent.putExtra("NOTIFICATIONS_ENABLED", view.isNotificationEnabled());
                context.startService(serviceIntent);

                timerService.startTimer(totalTimeInMillis, notificationMinutes);
                timerService.setNotificationsEnabled(view.isNotificationEnabled());
                view.updateButtonStates(true);
                isTimerPaused = false;

            } catch (NumberFormatException e) {
                view.showMessage("נא להזין מספרים בלבד");
            }
        }
    }

    public void resetTimer() {
        if (isBound) {
            timerService.resetTimer();
            view.updateTimerDisplay(0);
            view.clearInputFields();
            view.resetButtonStates();
        }
    }

    public boolean isTimerPaused() {
        return isTimerPaused;
    }
}