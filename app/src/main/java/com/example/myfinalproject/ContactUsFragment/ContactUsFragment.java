package com.example.myfinalproject.ContactUsFragment;

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

public class ContactUsFragment extends Fragment {

    private EditText etUserName;
    private EditText etContactDetails;
    private RadioGroup contactReasonGroup;
    private RadioButton rbOther;
    private TextInputLayout tilCustomReason;
    private EditText etCustomReason;
    private Button btnSendContact;
    private TextView tvSubmitStatus;
    private NotificationAdminDatabase notificationRepository;

    public ContactUsFragment() {
        // Required empty public constructor
    }

    public static ContactUsFragment newInstance() {
        return new ContactUsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);


        notificationRepository = new NotificationAdminDatabase();
        setupListeners();

        return view;
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
    }


    private void setupListeners() {
        contactReasonGroup.setOnCheckedChangeListener((group, checkedId) -> {
            tilCustomReason.setVisibility(checkedId == R.id.rbOther ? View.VISIBLE : View.GONE);
        });

        btnSendContact.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "anonymous";
        String userName = currentUser != null ? currentUser.getDisplayName() : "משתמש אנונימי";

        if (userName == null || userName.isEmpty()) {
            userName = "משתמש";
        }

        NotificationAdmin message = new NotificationAdmin(
                userId,
                userName,
                contactDetails,
                reason
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
        contactReasonGroup.clearCheck();
        etCustomReason.setText("");
        etContactDetails.setText("");
        tilCustomReason.setVisibility(View.GONE);
    }
}