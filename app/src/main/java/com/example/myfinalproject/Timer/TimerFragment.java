package com.example.myfinalproject.Timer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.CallBacks.TimeCallback;
import com.example.myfinalproject.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class TimerFragment extends Fragment {

    private TextView tvTimerDisplay;
    private EditText etHours, etMinutes, etSeconds, etNotificationTime;
    private Button btnStart, btnStopContinue, btnReset;
    private SwitchMaterial switchNotification;
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
                    updateTimerDisplay(millisUntilFinished);
                }

                @Override
                public void onTimerFinish() {
                    updateTimerDisplay(0);
                    Toast.makeText(getContext(), "הטיימר הסתיים!", Toast.LENGTH_SHORT).show();
                    resetButtonStates();
                }
            });

            if (timerService.isTimerRunning()) {
                updateTimerDisplay(timerService.getTimeRemaining());
                updateButtonStates(true);
            } else if (timerService.getTimeRemaining() > 0) {
                updateTimerDisplay(timerService.getTimeRemaining());
                updateButtonStates(false);
                isTimerPaused = true;
                btnStopContinue.setText("המשך");
                btnStopContinue.setEnabled(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        tvTimerDisplay = view.findViewById(R.id.tvTimerDisplay);
        etHours = view.findViewById(R.id.etHoursInput);
        etMinutes = view.findViewById(R.id.etMinutesInput);
        etSeconds = view.findViewById(R.id.etSecondsInput);
        etNotificationTime = view.findViewById(R.id.etNotificationTimeInput);
        switchNotification = view.findViewById(R.id.switchNotification);

        btnStart = view.findViewById(R.id.btnStart);
        btnStopContinue = view.findViewById(R.id.btnStopContinue);
        btnReset = view.findViewById(R.id.btnReset);

        setupInputValidation(etMinutes);
        setupInputValidation(etSeconds);

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etNotificationTime.setEnabled(isChecked);
            if (isChecked && etNotificationTime.getText().toString().isEmpty()) {
                etNotificationTime.requestFocus();
            }
            if (isBound) {
                timerService.setNotificationsEnabled(isChecked);
            }
        });

        btnStopContinue.setOnClickListener(v -> toggleStopContinue());
        btnStart.setOnClickListener(v -> startTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        return view;
    }

    private void setupInputValidation(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // לא צריך לעשות כלום
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // לא צריך לעשות כלום
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    int value = Integer.parseInt(s.toString());
                    if (value > 59) {
                        editText.setText("59");
                        editText.setSelection(2);
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), CountdownTimerService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            timerService.removeTimerUpdateCallback();
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void toggleStopContinue() {
        if (isBound) {
            if (!isTimerPaused) {
                timerService.pauseTimer();
                btnStopContinue.setText("המשך");
                isTimerPaused = true;
            } else {
                if (timerService.getTimeRemaining() > 0) {
                    int notificationMinutes = 0;
                    if (switchNotification.isChecked()) {
                        String notificationText = etNotificationTime.getText().toString();
                        if (!notificationText.isEmpty()) {
                            notificationMinutes = Integer.parseInt(notificationText);
                        } else {
                            Toast.makeText(getContext(), "יש להזין זמן התראה", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        long totalMinutes = (timerService.getTimeRemaining() / 1000) / 60;

                        if (notificationMinutes >= totalMinutes) {
                            Toast.makeText(getContext(), "זמן ההתראה גדול מזמן הטיימר", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    timerService.startTimer(timerService.getTimeRemaining(), notificationMinutes);
                    timerService.setNotificationsEnabled(switchNotification.isChecked());
                    btnStopContinue.setText("עצור");
                    isTimerPaused = false;
                    btnStart.setEnabled(false);
                }
            }
        }
    }

    private void startTimer() {
        if (isBound) {
            try {
                int hours = parseInputField(etHours, 0);
                int minutes = parseInputField(etMinutes, 0);
                int seconds = parseInputField(etSeconds, 0);
                int notificationMinutes = 0;

                long totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

                if (totalTimeInMillis <= 0) {
                    Toast.makeText(getContext(), "יש להזין זמן גדול מאפס", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (switchNotification.isChecked()) {
                    String notificationText = etNotificationTime.getText().toString();
                    if (notificationText.isEmpty()) {
                        Toast.makeText(getContext(), "יש להזין זמן התראה", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    notificationMinutes = Integer.parseInt(notificationText);

                    long totalMinutes = (totalTimeInMillis / 1000) / 60;

                    if (notificationMinutes >= totalMinutes) {
                        Toast.makeText(getContext(), "זמן ההתראה גדול מזמן הטיימר", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Intent serviceIntent = new Intent(getActivity(), CountdownTimerService.class);
                serviceIntent.putExtra("TIME_MILLIS", totalTimeInMillis);
                serviceIntent.putExtra("NOTIFICATION_MINUTES", notificationMinutes);
                serviceIntent.putExtra("NOTIFICATIONS_ENABLED", switchNotification.isChecked());
                getActivity().startService(serviceIntent);

                timerService.startTimer(totalTimeInMillis, notificationMinutes);
                timerService.setNotificationsEnabled(switchNotification.isChecked());
                updateButtonStates(true);
                isTimerPaused = false;

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "נא להזין מספרים בלבד", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetTimer() {
        if (isBound) {
            timerService.resetTimer();
            updateTimerDisplay(0);

            etHours.setText("");
            etMinutes.setText("");
            etSeconds.setText("");
            etNotificationTime.setText("");

            resetButtonStates();
        }
    }

    private int parseInputField(EditText editText, int defaultValue) {
        String text = editText.getText().toString();
        if (text.isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(text);
    }

    private void updateTimerDisplay(long millisRemaining) {
        int hours = (int) (millisRemaining / (1000 * 60 * 60));
        int minutes = (int) (millisRemaining % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) (millisRemaining % (1000 * 60)) / 1000;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        tvTimerDisplay.setText(timeString);
    }

    private void updateButtonStates(boolean isRunning) {
        btnStart.setEnabled(!isRunning);
        btnStopContinue.setEnabled(isRunning);
        btnReset.setEnabled(true);

        if (isRunning) {
            btnStopContinue.setText("עצור");
        } else {
            btnStopContinue.setText("המשך");
        }
    }

    private void resetButtonStates() {
        btnStart.setEnabled(true);
        btnStopContinue.setEnabled(false);
        btnReset.setEnabled(false);
        isTimerPaused = false;
        btnStopContinue.setText("עצור");
    }
}