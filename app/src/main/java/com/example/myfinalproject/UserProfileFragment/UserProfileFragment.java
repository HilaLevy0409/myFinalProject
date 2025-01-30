package com.example.myfinalproject.UserProfileFragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.NoticesAdminFragment.NoticesAdminFragment;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumByMeFragment.SumByMeFragment;

public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private TextView tvEmail, tvPhoneNumber, tvUsername, tvBadPoints, tvSumNum;
    private Button btnShowSums, btnDeleteUser, btnLogOut, btnEdit;
    private ImageView imageView;
    private UserProfilePresenter presenter;

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
        tvBadPoints = view.findViewById(R.id.tvBadPoints);
        tvSumNum = view.findViewById(R.id.tvSumNum);

        btnShowSums = view.findViewById(R.id.btnShowSums);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnEdit = view.findViewById(R.id.btnEdit);
        imageView = view.findViewById(R.id.imageView);

        // Set click listeners
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
            // Navigate to edit profile fragment
            // TODO: Implement edit functionality
        }
    }

    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void onLogOutSuccess() {
        // Clear SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        // Navigate to login
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
}