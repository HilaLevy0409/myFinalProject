package com.example.myfinalproject.LoginFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.ChooseClassFragment.ChooseClassFragment;
import com.example.myfinalproject.MainActivity;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.RegistrationFragment.RegistrationFragment;
import com.example.myfinalproject.Utils.Validator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;


public class LoginFragment extends Fragment implements View.OnClickListener, LoginView {

    private Button btnNext, btnForgotPass, btnFinish, btnRegisterNow;
    private EditText etUsername, etPassword, etEmailS;
    private DatabaseReference mDatabase;
    private FirebaseFirestore database;
    private LoginUserPresenter presenter;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnNext = view.findViewById(R.id.btnNext);
        etUsername = view.findViewById(R.id.etUser);
        etPassword = view.findViewById(R.id.etPassword);
        btnForgotPass = view.findViewById(R.id.btnForgotPass);
        btnRegisterNow = view.findViewById(R.id.btnRegisterNow);

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        database = FirebaseFirestore.getInstance();
        presenter = new LoginUserPresenter(this);


        btnNext.setOnClickListener(this);
        btnForgotPass.setOnClickListener(this);
        btnRegisterNow.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnNext) {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            String validUsername = Validator.isValidUsername(username);
            String validPassword = Validator.isValidPassword(password);
            if (!validUsername.isEmpty()) {
                Toast.makeText(getContext(), validUsername, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validPassword.isEmpty()) {
                Toast.makeText(getContext(), validPassword, Toast.LENGTH_SHORT).show();
                return;
            }

            presenter.loginUser(username, password);
        }
        if (view == btnForgotPass) {
            createCustomDialog();
        }
        if (view == btnRegisterNow) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new RegistrationFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void createCustomDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setTitle("שחזור סיסמה");
        dialog.setContentView(R.layout.pass_dialog);

        etEmailS = dialog.findViewById(R.id.etEmailS);
        btnFinish = dialog.findViewById(R.id.btnFinish);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);

        btnFinish.setOnClickListener(v -> {
            String emailSend = etEmailS.getText().toString().trim();

            if (emailSend.isEmpty()) {
                Toast.makeText(getContext(), "נא להזין אימייל", Toast.LENGTH_SHORT).show();
                return;
            }

            String validEmail = Validator.isValidEmail(emailSend);
            if (!validEmail.isEmpty()) {
                Toast.makeText(getContext(), validEmail, Toast.LENGTH_SHORT).show();
                return;
            }

            btnFinish.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            FirebaseAuth auth = FirebaseAuth.getInstance();

            database.collection("users")
                    .whereEqualTo("userEmail", emailSend)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Email exists, now send the password reset
                            auth.sendPasswordResetEmail(emailSend)
                                    .addOnCompleteListener(resetTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnFinish.setVisibility(View.VISIBLE);

                                        if (resetTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "מייל לשחזור סיסמה נשלח!", Toast.LENGTH_LONG).show();

                                            // Store the reset state for this email
                                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PasswordResetPrefs", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean(emailSend, true);
                                            editor.apply();

                                            dialog.dismiss();
                                        } else {
                                            Exception exception = resetTask.getException();
                                            if (exception != null) {
                                                String errorMessage = exception.getMessage();

                                                if (errorMessage != null && errorMessage.contains("no user record")) {
                                                    Toast.makeText(getContext(), "האימייל שהזנת אינו רשום במערכת", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getContext(), "שגיאה בשליחת המייל לשחזור סיסמה: " + errorMessage, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnFinish.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "האימייל שהזנת אינו רשום במערכת", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        btnFinish.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "שגיאה בבדיקת האימייל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    @Override
    public void showLoginSuccess(User user) {
        Toast.makeText(getContext(), "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
        saveUserToLocalStorage(user);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateNavigationHeader();
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, new ChooseClassFragment())
                .commit();
    }

    private void saveUserToLocalStorage(User user) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("userId", user.getId());
        editor.putString("username", user.getUserName());
        editor.putBoolean("isLoggedIn", true);


        editor.apply();
    }

    @Override
    public void showLoginFailure(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }
}
