package com.example.myfinalproject.Event;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myfinalproject.R;

import java.util.Calendar;



public class AlarmManagerFragment extends Fragment {

    private EditText etEventTitle, etEventDescription;
    private Button btnAddEvent, btnSelectDate, btnSelectTime, btnSelectDuration, btnSelectReminderDate, btnSelectReminderTime;


    private int year, month, day, hour, minute;
    private boolean dateSelected = false;
    private boolean timeSelected = false;

    private int durationHour, durationMinute;
    private boolean durationSelected = false;

    private int reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute;
    private boolean reminderDateSelected = false;
    private boolean reminderTimeSelected = false;

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "AlarmManagerFragment";

    public AlarmManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm_manager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDescription = view.findViewById(R.id.etEventDescription);
        btnAddEvent = view.findViewById(R.id.btnAddEvent);
        btnSelectDate = view.findViewById(R.id.btnSelectDate);
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        btnSelectDuration = view.findViewById(R.id.btnSelectEventDuration);
        btnSelectReminderDate = view.findViewById(R.id.btnSelectReminderDate);
        btnSelectReminderTime = view.findViewById(R.id.btnSelectReminderTime);

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
        btnSelectTime.setOnClickListener(v -> showTimePickerDialog());

        btnSelectDuration.setOnClickListener(v -> showDurationPickerDialog());

        btnSelectReminderDate.setOnClickListener(v -> showReminderDatePickerDialog());
        btnSelectReminderTime.setOnClickListener(v -> showReminderTimePickerDialog());

        btnAddEvent.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
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

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            this.hour = hourOfDay;
            this.minute = minute;
            timeSelected = true;
            btnSelectTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showDurationPickerDialog() {
        TimePickerDialog durationPickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            this.durationHour = hourOfDay;
            this.durationMinute = minute;
            durationSelected = true;
            btnSelectDuration.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, 1, 0, true);
        durationPickerDialog.setTitle("בחירת את משך הזמן של האירוע");
        durationPickerDialog.show();
    }

    private void showReminderDatePickerDialog() {
        int startYear = dateSelected ? year : Calendar.getInstance().get(Calendar.YEAR);
        int startMonth = dateSelected ? month : Calendar.getInstance().get(Calendar.MONTH);
        int startDay = dateSelected ? day : Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        DatePickerDialog reminderDateDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            reminderYear = year;
            reminderMonth = month;
            reminderDay = dayOfMonth;
            reminderDateSelected = true;
            btnSelectReminderDate.setText(String.format("%d-%d-%d", dayOfMonth, month + 1, year));
        }, startYear, startMonth, startDay);
        reminderDateDialog.show();
    }

    private void showReminderTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        int startMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog reminderTimeDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            reminderHour = hourOfDay;
            reminderMinute = minute;
            reminderTimeSelected = true;
            btnSelectReminderTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, startHour, startMinute, true);
        reminderTimeDialog.show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void addEventToCalendar() {
        String title = etEventTitle.getText().toString();
        String description = etEventDescription.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "אנא הכניסו כותרת לאירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dateSelected || !timeSelected) {
            Toast.makeText(getContext(), "אנא בחרו תאריך ושעה לאירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!durationSelected) {
            Toast.makeText(getContext(), "אנא בחרו משך זמן לאירוע", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar startTime = Calendar.getInstance();
        startTime.set(year, month, day, hour, minute);

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR_OF_DAY, durationHour);
        endTime.add(Calendar.MINUTE, durationMinute);

        ContentResolver cr = requireContext().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.DTSTART, startTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "UTC");

        try {
            Uri eventUri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            if (eventUri != null) {
                long eventID = Long.parseLong(eventUri.getLastPathSegment());

                Calendar reminderTime;

                if (reminderDateSelected && reminderTimeSelected) {
                    reminderTime = Calendar.getInstance();
                    reminderTime.set(reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);
                } else {
                    reminderTime = (Calendar) startTime.clone();
                    reminderTime.add(Calendar.DAY_OF_MONTH, -1);
                }

                long reminderMillis = startTime.getTimeInMillis() - reminderTime.getTimeInMillis();
                int reminderMinutes = (int) (reminderMillis / (60 * 1000));

                if (reminderMinutes < 0) {
                    Toast.makeText(getContext(), "תזכורת חייבת להיות לפני האירוע", Toast.LENGTH_SHORT).show();
                    return;
                }

                ContentValues reminderValues = new ContentValues();
                reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
                reminderValues.put(CalendarContract.Reminders.MINUTES, reminderMinutes);
                reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
                cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);

                Intent alarmIntent = new Intent(getContext(), EventReminderReceiver.class);
                alarmIntent.putExtra("eventTitle", title);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getContext(),
                        (int) eventID,
                        alarmIntent,
                        PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);

                Toast.makeText(getContext(), "אירוע נוסף ליומן עם תזכורת", Toast.LENGTH_SHORT).show();
                clearInputFields();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error adding event to calendar", e);
        }
    }

    private void clearInputFields() {
        etEventTitle.setText("");
        etEventDescription.setText("");
        btnSelectDate.setText("בחירת תאריך");
        btnSelectTime.setText("בחירת שעה");
        btnSelectDuration.setText("בחירת משך זמן האירוע");
        btnSelectReminderDate.setText("בחירת תאריך לתזכורת");
        btnSelectReminderTime.setText("בחירת שעה לתזכורת");

        dateSelected = false;
        timeSelected = false;
        durationSelected = false;
        reminderDateSelected = false;
        reminderTimeSelected = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addEventToCalendar();
            } else {
                Toast.makeText(getContext(), "הרשאה נדחתה", Toast.LENGTH_SHORT).show();
            }
        }
    }
}