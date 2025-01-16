package com.example.myfinalproject.WritingSumFragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;

public class WritingSumFragment extends Fragment implements View.OnClickListener {
    private Button btnUploadPhoto, btnSubmit;
    private SummaryPresenter summaryPresenter;
    private EditText etClass, etProfession, etSummaryTitle, etSummaryContent;

    public WritingSumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_writing_sum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);
        etClass = view.findViewById(R.id.etClass);
        etProfession = view.findViewById(R.id.etProfession);
        etSummaryTitle = view.findViewById(R.id.etSummaryTitle);
        etSummaryContent = view.findViewById(R.id.etSummaryContent);

        summaryPresenter = new SummaryPresenter(this);

        // Set click listeners
        btnSubmit.setOnClickListener(this);
        btnUploadPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            if (validateInputs()) {
                saveSummaryData();
            }
        } else if (v.getId() == R.id.btnUploadPhoto) {
            // Handle photo upload
            // TODO: Implement photo upload functionality
        }
    }

    private boolean validateInputs() {
        if (etClass.getText().toString().trim().isEmpty()) {
            showToast("נא להזין כיתה");
            return false;
        }
        if (etProfession.getText().toString().trim().isEmpty()) {
            showToast("נא להזין מקצוע");
            return false;
        }
        if (etSummaryTitle.getText().toString().trim().isEmpty()) {
            showToast("נא להזין כותרת לסיכום");
            return false;
        }
        if (etSummaryContent.getText().toString().trim().isEmpty()) {
            showToast("נא להזין תוכן סיכום");
            return false;
        }
        return true;
    }

    private void saveSummaryData() {
        String classOption = etClass.getText().toString().trim();
        String profession = etProfession.getText().toString().trim();
        String summaryTitle = etSummaryTitle.getText().toString().trim();
        String summaryContent = etSummaryContent.getText().toString().trim();

        Summary summary = new Summary(classOption, profession, summaryTitle, summaryContent);
        summaryPresenter.submitSummaryClicked(summary);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}