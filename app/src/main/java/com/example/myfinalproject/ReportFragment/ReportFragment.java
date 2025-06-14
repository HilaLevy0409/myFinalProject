package com.example.myfinalproject.ReportFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Repositories.NotificationAdminRepository;
import com.example.myfinalproject.DataModels.NotificationAdmin;
import com.example.myfinalproject.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ReportFragment extends Fragment {

    private EditText etUserNameOrTopic, etUserName, etCustomReason, etReportDetails;
    private RadioGroup reportReasonGroup;
    private TextInputLayout tilCustomReason;
    private Button btnSendReport;
    private TextView tvSubmitStatus;
    private NotificationAdminRepository notificationRepository;

    // משתנים לזיהוי אם המשתמש מחובר ומה שמו
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
        notificationRepository = new NotificationAdminRepository();
        checkUserLoginStatus();
        return view;
    }

    // בדיקה אם המשתמש מחובר ושליפת שם המשתמש מה-SharedPreferences
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
        tilCustomReason = view.findViewById(R.id.tilCustomReason);
        etCustomReason = view.findViewById(R.id.etCustomReason);
        etReportDetails = view.findViewById(R.id.etReport);
        btnSendReport = view.findViewById(R.id.btnSendReport);
        tvSubmitStatus = view.findViewById(R.id.tvSubmitStatus);
        etUserName = view.findViewById(R.id.etUserName);

        // אם הגיע שם משתמש דרך bundle, מציגים אותו
        Bundle bundle = getArguments();
        if (bundle != null) {
            String userName = bundle.getString("userName");
            etUserNameOrTopic.setText(userName);
            etUserNameOrTopic.setEnabled(false);
        }

        // אם המשתמש מחובר – מציגים את שמו; אחרת – אורח
        if (isUserLoggedIn && !loggedInUsername.isEmpty()) {
            etUserName.setText(loggedInUsername);
        } else {
            etUserName.setText("אורח");
        }

        // מניעת עריכה של שדה שם המשתמש
        etUserName.setEnabled(false);
        etUserName.setFocusable(false);
        etUserName.setFocusableInTouchMode(false);
        etUserName.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // הצגת שדה סיבה אחרת רק אם נבחר "אחר"
        reportReasonGroup.setOnCheckedChangeListener((group, checkedId) -> {
            tilCustomReason.setVisibility(checkedId == R.id.rbOther ? View.VISIBLE : View.GONE);
        });

        btnSendReport.setOnClickListener(v -> submitReport());


        if (!isUserLoggedIn) {
            btnSendReport.setEnabled(false);
            btnSendReport.setAlpha(0.5f);
            tvSubmitStatus.setText("רק משתמשים רשומים יכולים לשלוח דיווחים");
            tvSubmitStatus.setVisibility(View.VISIBLE);
        }

        String reporterName = etUserName.getText().toString().trim();
        String reportedUserName = etUserNameOrTopic.getText().toString().trim();

        // מניעת דיווח עצמי
        if (reportedUserName.equals(reporterName)) {
            btnSendReport.setEnabled(false);
            btnSendReport.setAlpha(0.5f);
            tvSubmitStatus.setText("לא ניתן לדווח על עצמך");
            tvSubmitStatus.setVisibility(View.VISIBLE);
        }

    }

    // שליחת דיווח
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
            // אם נבחר "אחר" – בודקים שיש סיבה מותאמת אישית
            reason = etCustomReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "נא לפרט את הסיבה", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // זיהוי מזהה המשתמש המדווח
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";

        String reporterName = etUserName.getText().toString().trim();
        String reportedUserName = etUserNameOrTopic.getText().toString().trim();

        // מניעת דיווח עצמי
        if (reportedUserName.equals(reporterName)) {
            Toast.makeText(getContext(), "לא ניתן לדווח על עצמך", Toast.LENGTH_SHORT).show();
            return;
        }
        // יצירת אובייקט דיווח
        NotificationAdmin report = new NotificationAdmin(
                userId,
                reporterName,
                reportDetails,
                reason,
                "REPORT"
        );

        report.setReportedUserName(reportedUserName);

        // בדיקה אם מדובר בדיווח על סיכום (ולא על משתמש)
        Bundle bundle = getArguments();
        String summaryId = null;
        if (bundle != null && bundle.containsKey("summaryId")) {
            summaryId = bundle.getString("summaryId");
            if (summaryId != null && !summaryId.isEmpty()) {

                report.setId(summaryId);

            }
        }

        btnSendReport.setEnabled(false); // מניעת לחיצה חוזרת

        // אם מדובר בדיווח על סיכום – שמירה ישירות ל-Firestore עם מזהה הסיכום
        if (summaryId != null) {
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("userId", report.getUserId());
            reportData.put("userName", report.getUserName());
            reportData.put("content", report.getContent());
            reportData.put("type", report.getType());
            reportData.put("reportReason", report.getReportReason());
            reportData.put("reportedUserName", report.getReportedUserName());
            reportData.put("timestamp", Timestamp.now());
            reportData.put("isSummaryReport", true);
            reportData.put("summaryId", summaryId);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("notifications")
                    .add(reportData)
                    .addOnSuccessListener(documentReference -> {
                        tvSubmitStatus.setVisibility(View.VISIBLE);
                        clearForm(); // איפוס הטופס
                        btnSendReport.postDelayed(() -> btnSendReport.setEnabled(true), 2000);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSendReport.setEnabled(true);
                    });
        } else {
            // הוספת הדיווח למסד הנתונים
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
    }

    private void clearForm() {
        reportReasonGroup.clearCheck();
        etCustomReason.setText("");
        etReportDetails.setText("");
        tilCustomReason.setVisibility(View.GONE);
    }


}