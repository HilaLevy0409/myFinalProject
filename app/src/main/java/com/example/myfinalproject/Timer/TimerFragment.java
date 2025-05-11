package com.example.myfinalproject.Timer;

import android.os.Bundle;
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

import com.example.myfinalproject.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class TimerFragment extends Fragment {

    private TextView tvTimerDisplay;
    private EditText etHours, etMinutes, etSeconds, etNotificationTime;
    private Button btnStart, btnStopContinue, btnReset;
    private SwitchMaterial switchNotification;
    private TimerPresenter presenter;

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

        presenter = new TimerPresenter(this);

        inputValidation(etMinutes);
        inputValidation(etSeconds);

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etNotificationTime.setEnabled(isChecked);
            if (isChecked && etNotificationTime.getText().toString().isEmpty()) {
                etNotificationTime.requestFocus();
            }
            presenter.onNotificationSwitchChanged(isChecked);
        });

        btnStopContinue.setOnClickListener(v -> presenter.toggleStopContinue());
        btnStart.setOnClickListener(v -> presenter.startTimer());
        btnReset.setOnClickListener(v -> presenter.resetTimer());


        return view;
    }


    private void inputValidation(final EditText editText) {
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
        presenter.bindService();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.unbindService();
    }

    public void updateTimerDisplay(long millisRemaining) {
        int hours = (int) (millisRemaining / (1000 * 60 * 60));
        int minutes = (int) (millisRemaining % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) (millisRemaining % (1000 * 60)) / 1000;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        tvTimerDisplay.setText(timeString);
    }

    public void updateButtonStates(boolean isRunning) {
        btnStart.setEnabled(!isRunning);
        btnStopContinue.setEnabled(isRunning);
        btnReset.setEnabled(true);

        if (isRunning) {
            btnStopContinue.setText("עצור");
        } else {
            btnStopContinue.setText("המשך");
        }
    }

    public void resetButtonStates() {
        btnStart.setEnabled(true);
        btnStopContinue.setEnabled(false);
        btnReset.setEnabled(false);
        btnStopContinue.setText("עצור");
    }

    public void setStopContinueButtonText(String text) {
        btnStopContinue.setText(text);
    }

    public void setStopContinueButtonEnabled(boolean enabled) {
        btnStopContinue.setEnabled(enabled);
    }

    public void setStartButtonEnabled(boolean enabled) {
        btnStart.setEnabled(enabled);
    }

    public void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void clearInputFields() {
        etHours.setText("");
        etMinutes.setText("");
        etSeconds.setText("");
        etNotificationTime.setText("");
    }

    public int getHours() {
        return parseInputField(etHours, 0);
    }

    public int getMinutes() {
        return parseInputField(etMinutes, 0);
    }

    public int getSeconds() {
        return parseInputField(etSeconds, 0);
    }

    public String getNotificationTime() {
        return etNotificationTime.getText().toString();
    }

    public boolean isNotificationEnabled() {
        return switchNotification.isChecked();
    }

    private int parseInputField(EditText editText, int defaultValue) {
        String text = editText.getText().toString();
        if (text.isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(text);
    }
}