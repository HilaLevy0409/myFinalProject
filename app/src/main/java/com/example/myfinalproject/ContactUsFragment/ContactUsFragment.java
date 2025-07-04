package com.example.myfinalproject.ContactUsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Repositories.NotificationAdminRepository;
import com.example.myfinalproject.DataModels.NotificationAdmin;
import com.example.myfinalproject.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ContactUsFragment extends Fragment {

    private EditText etUserName, etContactDetails, etCustomReason;
    private RadioGroup contactReasonGroup;
    private TextInputLayout tilCustomReason;
    private Button btnSendContact;
    private TextView tvSubmitStatus;
    private NotificationAdminRepository notificationRepository;

    // משתנים לזיהוי האם המשתמש מחובר ומה שמו
    private boolean isUserLoggedIn = false;
    private String loggedInUsername = "";

    public ContactUsFragment() {
    }

    public static ContactUsFragment newInstance() {
        return new ContactUsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);

        // יצירת מופע של המחלקה שאחראית לשמירת ההודעות למסד הנתונים
        notificationRepository = new NotificationAdminRepository();

        checkUserLoginStatus();
        return view;
    }

    // בודקת אם המשתמש מחובר ושומרת את שמו
    private void checkUserLoginStatus() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        isUserLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isUserLoggedIn) {
            loggedInUsername = sharedPreferences.getString("username", "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etUserName = view.findViewById(R.id.etUserName);
        etContactDetails = view.findViewById(R.id.etContactDetails);
        contactReasonGroup = view.findViewById(R.id.contactReasonGroup);
        tilCustomReason = view.findViewById(R.id.tilCustomReason);
        etCustomReason = view.findViewById(R.id.etCustomReason);
        btnSendContact = view.findViewById(R.id.btnSendContact);
        tvSubmitStatus = view.findViewById(R.id.tvSubmitStatus);

        // אם המשתמש מחובר – מציגים את שמו, אחרת "אורח"
        if (isUserLoggedIn && !loggedInUsername.isEmpty()) {
            etUserName.setText(loggedInUsername);
        } else {
            etUserName.setText("אורח");
        }

        // שדה שם המשתמש לא ניתן לעריכה
        etUserName.setEnabled(false);
        etUserName.setFocusable(false);
        etUserName.setFocusableInTouchMode(false);
        etUserName.setBackgroundResource(android.R.drawable.edit_text);
        etUserName.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // מאזין לשינוי בבחירת סיבת הפנייה – מציג שדה טקסט מותאם אם נבחר "אחר"
        contactReasonGroup.setOnCheckedChangeListener((group, checkedId) -> {
            tilCustomReason.setVisibility(checkedId == R.id.rbOther ? View.VISIBLE : View.GONE);
        });

        btnSendContact.setOnClickListener(v -> sendMessage());

        if (!isUserLoggedIn) {
            btnSendContact.setEnabled(false);
            btnSendContact.setAlpha(0.5f);
            tvSubmitStatus.setText("רק משתמשים רשומים יכולים לשלוח דיווחים");
            tvSubmitStatus.setVisibility(View.VISIBLE);
        }
    }

    // פעולה ששולחת את ההודעה לאחר בדיקות תקינות
    private void sendMessage() {
        String userName = etUserName.getText().toString().trim();
        String contactDetails = etContactDetails.getText().toString().trim();

        if (contactDetails.isEmpty()) {
            Toast.makeText(getContext(), "נא להזין את פרטי הפנייה", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contactReasonGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "נא לבחור סיבת פנייה", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason;
        int selectedRadioButtonId = contactReasonGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == R.id.rbTechnical) {
            reason = "בעיה טכנית";
        } else if (selectedRadioButtonId == R.id.rbSupport) {
            reason = "תמיכה ועזרה";
        } else if (selectedRadioButtonId == R.id.rbFeedback) {
            reason = "משוב וייעול";
        } else {
            reason = etCustomReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "נא לפרט את הסיבה", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // קבלת מזהה המשתמש (אם מחובר), אחרת מציין "anonymous"
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "anonymous";

        // יצירת אובייקט ההודעה ושליחתה למסד הנתונים
        NotificationAdmin message = new NotificationAdmin(
                userId,
                userName,
                contactDetails,
                reason,
                "CONTACT"
        );

// חסימת כפתור השליחה למניעת שליחה כפולה
        btnSendContact.setEnabled(false);

        // ניסיון להוספת ההודעה למסד
        notificationRepository.addNotification(message)
                .addOnSuccessListener(aVoid -> {
                    tvSubmitStatus.setVisibility(View.VISIBLE); // הצגת אישור
                    clearForm(); // ניקוי הטופס
                    // איפשור מחדש של הכפתור אחרי 2 שניות
                    btnSendContact.postDelayed(() -> btnSendContact.setEnabled(true), 2000);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSendContact.setEnabled(true); // איפשור כפתור מחדש במקרה של שגיאה
                });
    }

    private void clearForm() {
        contactReasonGroup.clearCheck();
        etCustomReason.setText("");
        etContactDetails.setText("");
        tilCustomReason.setVisibility(View.GONE);
    }
}