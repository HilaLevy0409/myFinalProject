package com.example.myfinalproject.ReportFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Database.NotificationAdminDatabase;
import com.example.myfinalproject.Models.NotificationAdmin;
import com.example.myfinalproject.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ReportFragment extends Fragment {

    private EditText etUserNameOrTopic, etUserName;
    private RadioGroup reportReasonGroup;
    private RadioButton rbOther;
    private TextInputLayout tilCustomReason;
    private EditText etCustomReason, etReportDetails;
    private Button btnSendReport;
    private TextView tvSubmitStatus;
    private NotificationAdminDatabase notificationRepository;

    private boolean isUserLoggedIn = false;
    private String loggedInUsername = "";

    public ReportFragment() {
    }

    public static ReportFragment newInstance() {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        notificationRepository = new NotificationAdminDatabase();

        checkUserLoginStatus();

        return view;
    }

    private void checkUserLoginStatus() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        isUserLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isUserLoggedIn) {
            loggedInUsername = sharedPreferences.getString("username", "");
        }
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etUserNameOrTopic = view.findViewById(R.id.etUserNameOrTopic);
        reportReasonGroup = view.findViewById(R.id.reportReasonGroup);
        rbOther = view.findViewById(R.id.rbOther);
        tilCustomReason = view.findViewById(R.id.tilCustomReason);
        etCustomReason = view.findViewById(R.id.etCustomReason);
        etReportDetails = view.findViewById(R.id.etReport);
        btnSendReport = view.findViewById(R.id.btnSendReport);
        tvSubmitStatus = view.findViewById(R.id.tvSubmitStatus);
        etUserName = view.findViewById(R.id.etUserName);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String userName = bundle.getString("userName");
            etUserNameOrTopic.setText(userName);
            etUserNameOrTopic.setEnabled(false);
        }

        if (isUserLoggedIn && !loggedInUsername.isEmpty()) {
            etUserName.setText(loggedInUsername);

            etUserName.setEnabled(false);
            etUserName.setFocusable(false);
            etUserName.setFocusableInTouchMode(false);

            etUserName.setBackgroundResource(android.R.drawable.edit_text);
            etUserName.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            etUserName.setEnabled(true);
            etUserName.setFocusable(true);
            etUserName.setFocusableInTouchMode(true);

            etUserName.setHint("שם משתמש");
        }

        reportReasonGroup.setOnCheckedChangeListener((group, checkedId) -> {
            tilCustomReason.setVisibility(checkedId == R.id.rbOther ? View.VISIBLE : View.GONE);
        });

        btnSendReport.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String reportDetails = etReportDetails.getText().toString().trim();

        if (reportDetails.isEmpty()) {
            Toast.makeText(getContext(), "נא להזין את פרטי הדיווח", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reportReasonGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "נא לבחור סיבת דיווח", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason;
        int selectedRadioButtonId = reportReasonGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == R.id.rbInappropriate) {
            reason = "תוכן לא ראוי";
        } else if (selectedRadioButtonId == R.id.rbSpam) {
            reason = "תוכן ספאם";
        } else if (selectedRadioButtonId == R.id.rbIncorrectInfo) {
            reason = "מידע שגוי/לא מדויק";
        } else {
            reason = etCustomReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "נא לפרט את הסיבה", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";

        String reporterName = etUserName.getText().toString().trim();

        if (reporterName.isEmpty()) {
            if (isUserLoggedIn && !loggedInUsername.isEmpty()) {
                reporterName = loggedInUsername;
            } else {
                reporterName = "משתמש אנונימי";
            }
        }

        NotificationAdmin report = new NotificationAdmin(
                userId,
                reporterName,
                reportDetails,
                reason,
                "REPORT"
        );

        String reportedUserName = etUserNameOrTopic.getText().toString().trim();
        report.setReportedUserName(reportedUserName);

        btnSendReport.setEnabled(false);
        notificationRepository.addNotification(report)
                .addOnSuccessListener(aVoid -> {
                    tvSubmitStatus.setVisibility(View.VISIBLE);
                    clearForm();
                    btnSendReport.postDelayed(() -> btnSendReport.setEnabled(true), 2000);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSendReport.setEnabled(true);
                });
    }

    private void clearForm() {
        if (!isUserLoggedIn) {
            etUserName.setText("");
        }

        reportReasonGroup.clearCheck();
        etCustomReason.setText("");
        etReportDetails.setText("");
        tilCustomReason.setVisibility(View.GONE);
    }


}