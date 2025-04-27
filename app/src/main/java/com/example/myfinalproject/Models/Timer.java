package com.example.myfinalproject.Models;

public class Timer{
    private long timeMillis;
    private int notificationMinutes;
    private boolean notificationsEnabled;
    private boolean isRunning;
    private boolean isPaused;
    private long timeRemaining;

    public Timer() {
        this.timeMillis = 0;
        this.notificationMinutes = 0;
        this.notificationsEnabled = true;
        this.isRunning = false;
        this.isPaused = false;
        this.timeRemaining = 0;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public int getNotificationMinutes() {
        return notificationMinutes;
    }

    public void setNotificationMinutes(int notificationMinutes) {
        this.notificationMinutes = notificationMinutes;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public String formatTime(long millis) {
        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) (millis % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) (millis % (1000 * 60)) / 1000;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public boolean isValidNotificationTime(int notificationMinutes) {
        long totalMinutes = (timeMillis / 1000) / 60;
        return notificationMinutes < totalMinutes;
    }
}