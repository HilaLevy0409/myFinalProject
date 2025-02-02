package com.example.myfinalproject.UserProfileFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.NoticesAdminFragment.NoticesAdminFragment;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumByMeFragment.SumByMeFragment;

import java.util.Calendar;

public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private TextView tvEmail, tvPhoneNumber, tvBirthDate, tvUsername, tvBadPoints, tvSumNum;
    private Button btnShowSums, btnDeleteUser, btnLogOut, btnEdit, btnBirthDate;
    private ImageView imageView, imageViewProfile;
    private UserProfilePresenter presenter;
    private EditText etEmail, etPhoneNumber, etUsername, etBirthDate;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);
        // Initialize presenter
        presenter = new UserProfilePresenter(this);
        presenter.loadUserData();
    }

    private void initializeViews(View view) {
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvBadPoints = view.findViewById(R.id.tvBadPoints);
        tvSumNum = view.findViewById(R.id.tvSumNum);

        btnShowSums = view.findViewById(R.id.btnShowSums);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnEdit = view.findViewById(R.id.btnEdit);
        imageView = view.findViewById(R.id.imageView);

        etEmail = view.findViewById(R.id.etEmail);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        etUsername = view.findViewById(R.id.etUsername);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);


        btnShowSums.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);
        btnLogOut.setOnClickListener(this);
        btnEdit.setOnClickListener(this);

    }

    public void displayUserData(User user) {
        tvEmail.setText("אימייל: " + user.getUserEmail());
        tvPhoneNumber.setText("מספר טלפון: " + user.getPhone());
        tvUsername.setText("שם משתמש: " + user.getUserName());
        tvBadPoints.setText("נקודות לרעה: " + user.getBadPoints());
        tvSumNum.setText("מספר סיכומים שנכתבו: " + user.getSumCount());
    }

    @Override
    public void onClick(View view) {
        if (view == btnShowSums) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new SumByMeFragment())
                    .commit();
        } else if (view == btnDeleteUser) {
            presenter.deleteUser();
        } else if (view == btnLogOut) {
            presenter.logOut();
        } else if (view == btnEdit) {
            createCustomDialog();
        }else if (view == btnBirthDate) {
            openDialog();
        }
    }

    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void onLogOutSuccess() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // TODO: Replace with your login fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, new LoginFragment())
                .commit();
    }

    public void onDeleteSuccess() {
        Toast.makeText(getContext(), "המשתמש נמחק בהצלחה", Toast.LENGTH_SHORT).show();
        onLogOutSuccess();
    }

    private void createCustomDialog() {
        Dialog dialog  = new Dialog(getContext());
        dialog.setTitle("עריכת פרטים");
        dialog.setContentView(R.layout.edit_user_profile);
        dialog.show();

        btnBirthDate = dialog.findViewById(R.id.btnBirthDate);
        btnBirthDate.setOnClickListener(this);
    }

    public void openDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                },
                year, month, day
        );

        datePickerDialog.show();



    }
}