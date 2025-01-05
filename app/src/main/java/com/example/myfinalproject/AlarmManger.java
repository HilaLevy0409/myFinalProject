package com.example.myfinalproject;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class AlarmManger extends AppCompatActivity {

    private EditText etEventTitle, etEventDescription;
    private Button btnAddEvent, btnSelectDate, btnSelectTime;


    private int year, month, day, hour, minute;
    private boolean dateSelected = false;
    private boolean timeSelected = false;

    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDescription = findViewById(R.id.etEventDescription);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        btnAddEvent.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CALENDAR},
                        PERMISSION_REQUEST_CODE);
            } else {
                addEventToCalendar();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            this.year = year;
            this.month = month;
            this.day = dayOfMonth;
            dateSelected = true;
            btnSelectDate.setText(String.format("%d-%d-%d", dayOfMonth, month + 1, year));
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            this.hour = hourOfDay;
            this.minute = minute;
            timeSelected = true;
            btnSelectTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, hour, minute, true);
        timePickerDialog.show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void addEventToCalendar() {
        // קבלת הכותרת והתיאור מהמשתמש
        String title = etEventTitle.getText().toString();
        String description = etEventDescription.getText().toString();

        // בדיקת תקינות הכותרת, תאריך ושעה
        if (title.isEmpty()) {
            Toast.makeText(this, "אנא הכניסו כותרת לאירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dateSelected || !timeSelected) {
            Toast.makeText(this, "אנא בחרו תאריך ושעה לאירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        // הגדרת זמני התחלה וסיום של האירוע
        Calendar startTime = Calendar.getInstance();
        startTime.set(year, month, day, hour, minute);

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, 1); // משך האירוע שעה אחת

        // הכנת הנתונים להוספה ליומן
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // שנה את ה-ID של הלוח שנה שלך במידת הצורך
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");

        try {
            cr.insert(CalendarContract.Events.CONTENT_URI, values);
            Toast.makeText(this, "אירוע נוסף ליומן", Toast.LENGTH_SHORT).show();

            // הוספת האירוע ליומן
            Uri eventUri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (eventUri != null) {
                long eventID = Long.parseLong(eventUri.getLastPathSegment());

                // הוספת תזכורת ליום לפני האירוע
                ContentValues reminderValues = new ContentValues();
                reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
                reminderValues.put(CalendarContract.Reminders.MINUTES, 24 * 60); // 24 שעות לפני האירוע
                reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

                // הגדרת התראה ב-AlarmManager ליום לפני האירוע
                Calendar alarmTime = (Calendar) startTime.clone();
                alarmTime.add(Calendar.DAY_OF_MONTH, -1); // הגדרת התראה ליום לפני האירוע

                Intent alarmIntent = new Intent(this, EventReminderReceiver.class);
                alarmIntent.putExtra("eventTitle", title);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);

                Toast.makeText(this, "אירוע, תזכורת והתראה נוספו ליומן", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("MainTag", e.getMessage());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addEventToCalendar();
            } else {
                Toast.makeText(this, "הרשאה נדחתה", Toast.LENGTH_SHORT
                ).show();
            }
        }
    }



}