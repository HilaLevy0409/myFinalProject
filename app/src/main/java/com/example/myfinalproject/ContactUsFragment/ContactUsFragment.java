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

import com.example.myfinalproject.Database.NotificationAdminDatabase;
import com.example.myfinalproject.Models.NotificationAdmin;
import com.example.myfinalproject.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;


public class ContactUsFragment extends Fragment {

    private EditText etUserName, etContactDetails, etCustomReason;
    private RadioGroup contactReasonGroup;
    private RadioButton rbOther;
    private TextInputLayout tilCustomReason;
    private Button btnSendContact;
    private TextView tvSubmitStatus;
    private NotificationAdminDatabase notificationRepository;
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

        notificationRepository = new NotificationAdminDatabase();

        // Check if user is logged in and get username from SharedPreferences
        checkUserLoginStatus();

        return view;
    }

    private void checkUserLoginStatus() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        isUserLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isUserLoggedIn) {
            // Get username from SharedPreferences
            loggedInUsername = sharedPreferences.getString("username", "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etUserName = view.findViewById(R.id.etUserName);
        etContactDetails = view.findViewById(R.id.etContactDetails);
        contactReasonGroup = view.findViewById(R.id.contactReasonGroup);
        rbOther = view.findViewById(R.id.rbOther);
        tilCustomReason = view.findViewById(R.id.tilCustomReason);
        etCustomReason = view.findViewById(R.id.etCustomReason);
        btnSendContact = view.findViewById(R.id.btnSendContact);
        tvSubmitStatus = view.findViewById(R.id.tvSubmitStatus);

        if (isUserLoggedIn && !loggedInUsername.isEmpty()) {
            // Set the username field with the logged-in username
            etUserName.setText(loggedInUsername);

            // Make the username field non-editable
            etUserName.setEnabled(false);
            etUserName.setFocusable(false);
            etUserName.setFocusableInTouchMode(false);

            // Optional: Change the appearance to indicate it's non-editable
            etUserName.setBackgroundResource(android.R.drawable.edit_text);
            etUserName.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            // For non-logged-in users, keep the field editable
            etUserName.setEnabled(true);
            etUserName.setFocusable(true);
            etUserName.setFocusableInTouchMode(true);

            // Set a hint for non-logged-in users
            etUserName.setHint("שם משתמש");
        }
        contactReasonGroup.setOnCheckedChangeListener((group, checkedId) -> {
            tilCustomReason.setVisibility(checkedId == R.id.rbOther ? View.VISIBLE : View.GONE);
        });

        btnSendContact.setOnClickListener(v -> sendMessage());
    }


    private void sendMessage() {
        String userName = etUserName.getText().toString().trim();
        String contactDetails = etContactDetails.getText().toString().trim();

        // Validate username only if the user is not logged in
        if (!isUserLoggedIn && userName.isEmpty()) {
            Toast.makeText(getContext(), "נא להזין שם משתמש", Toast.LENGTH_SHORT).show();
            return;
        }

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "anonymous";

        // Use the username from the field (either entered by the user or set from SharedPreferences)
        if (userName.isEmpty()) {
            userName = "משתמש אנונימי";
        }

        NotificationAdmin message = new NotificationAdmin(
                userId,
                userName,
                contactDetails,
                reason,
                "CONTACT"
        );

        btnSendContact.setEnabled(false);
        notificationRepository.addNotification(message)
                .addOnSuccessListener(aVoid -> {
                    tvSubmitStatus.setVisibility(View.VISIBLE);
                    clearForm();
                    btnSendContact.postDelayed(() -> btnSendContact.setEnabled(true), 2000);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSendContact.setEnabled(true);
                });
    }

    private void clearForm() {
        // Don't clear the username if user is logged in
        if (!isUserLoggedIn) {
            etUserName.setText("");
        }

        contactReasonGroup.clearCheck();
        etCustomReason.setText("");
        etContactDetails.setText("");
        tilCustomReason.setVisibility(View.GONE);
    }
}