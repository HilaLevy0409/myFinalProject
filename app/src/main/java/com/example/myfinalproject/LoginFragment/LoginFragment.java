package com.example.myfinalproject.LoginFragment;

import static com.example.myfinalproject.Utils.Validator.isValidPassword;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.CallBacks.LoginCallback;
import com.example.myfinalproject.ChooseClassFragment.ChooseClassFragment;
import com.example.myfinalproject.MainActivity;
import com.example.myfinalproject.DataModels.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.RegistrationFragment.RegistrationFragment;
import com.example.myfinalproject.Utils.Validator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginFragment extends Fragment implements View.OnClickListener, LoginCallback {

    private Button btnNext, btnForgotPass, btnFinish, btnRegisterNow;
    private EditText etUsername, etPassword, etEmailS;
    private FirebaseFirestore database;
    private LoginUserPresenter presenter;

    private String resetEmail;

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
            String validPassword = isValidPassword(password);
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

            // Store email for later use
            resetEmail = emailSend;

            FirebaseAuth auth = FirebaseAuth.getInstance();

            // First check if email exists in Firestore
            database.collection("users")
                    .whereEqualTo("userEmail", emailSend)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Email exists in Firestore, now send reset email
                            auth.sendPasswordResetEmail(emailSend)
                                    .addOnCompleteListener(resetTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnFinish.setVisibility(View.VISIBLE);

                                        if (resetTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "מייל לשחזור סיסמה נשלח!", Toast.LENGTH_LONG).show();
                                            dialog.dismiss();

                                            // Show message to user about next steps
                                            showPasswordResetInstructions();

                                        } else {
                                            Exception exception = resetTask.getException();
                                            if (exception != null) {
                                                String errorMessage = exception.getMessage();
                                                if (errorMessage != null && errorMessage.contains("no user record")) {
                                                    Toast.makeText(getContext(), "האימייל שהזנת אינו רשום במערכת Firebase", Toast.LENGTH_SHORT).show();
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

    private void showPasswordResetInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("הוראות לשחזור סיסמה");
        builder.setMessage("מייל לשחזור סיסמה נשלח לכתובת שלך.\n\n" +
                "שלבים:\n" +
                "1. בדוק את תיבת המייל שלך\n" +
                "2. לחץ על הקישור במייל\n" +
                "3. צור סיסמה חדשה\n" +
                "4. חזור לאפליקציה ולחץ על 'עדכן סיסמה במערכת'\n\n" +
                "האם ביצעת כבר את השלבים הללו?");

        builder.setPositiveButton("כן, עדכנתי את הסיסמה", (dialog, which) -> {
            dialog.dismiss();
            askUserToUpdatePasswordInFirestore();
        });

        builder.setNegativeButton("עדיין לא", (dialog, which) -> {
            dialog.dismiss();
            Toast.makeText(getContext(), "בצע את השלבים ואז חזור כאן", Toast.LENGTH_LONG).show();
        });

        builder.setNeutralButton("שלח מייל שוב", (dialog, which) -> {
            dialog.dismiss();
            if (resetEmail != null) {
                resendPasswordResetEmail();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void resendPasswordResetEmail() {
        if (resetEmail == null || resetEmail.isEmpty()) {
            Toast.makeText(getContext(), "שגיאה: לא נמצאה כתובת מייל", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(resetEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "מייל נשלח שוב!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "שגיאה בשליחת המייל: " +
                                        (task.getException() != null ? task.getException().getMessage() : ""),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

     private void askUserToUpdatePasswordInFirestore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("עדכון סיסמה במערכת");
        builder.setMessage("כדי להשלים את התהליך, אנא הזן את הסיסמה החדשה שיצרת:");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Email input (for authentication)
        final EditText emailInput = new EditText(getContext());
        emailInput.setHint("כתובת האימייל שלך");
        emailInput.setGravity(Gravity.RIGHT);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (resetEmail != null) {
            emailInput.setText(resetEmail);
        }
        layout.addView(emailInput);

        // Password input
        final EditText passwordInput = new EditText(getContext());
        passwordInput.setHint("הסיסמה החדשה");
        passwordInput.setGravity(Gravity.RIGHT);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final ImageView togglePasswordVisibility = new ImageView(getContext());
        togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
        togglePasswordVisibility.setPadding(20, 20, 20, 20);

        LinearLayout passwordLayout = new LinearLayout(getContext());
        passwordLayout.setOrientation(LinearLayout.HORIZONTAL);
        passwordLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        passwordLayout.addView(passwordInput, inputParams);
        passwordLayout.addView(togglePasswordVisibility);

        layout.addView(passwordLayout);

        final TextView passwordStrength = new TextView(getContext());
        passwordStrength.setTextSize(14);
        passwordStrength.setPadding(0, (int)(8 * getResources().getDisplayMetrics().density), 0, 0);
        layout.addView(passwordStrength);

        builder.setView(layout);

        builder.setPositiveButton("עדכון", null);
        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String email = emailInput.getText().toString().trim();
                String newPassword = passwordInput.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    emailInput.setError("יש להזין כתובת אימייל");
                    emailInput.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    passwordInput.setError("יש להזין סיסמה");
                    passwordInput.requestFocus();
                    return;
                }

                String validationMessage = isValidPassword(newPassword);
                if (!validationMessage.isEmpty()) {
                    passwordInput.setError(validationMessage);
                    passwordInput.requestFocus();
                    return;
                }

                // Test authentication with new password to verify it was changed
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, newPassword)
                        .addOnCompleteListener(signInTask -> {
                            if (signInTask.isSuccessful()) {
                                // Password is correct, now update Firestore
                                updatePasswordInFirestore(email, newPassword, dialog);
                                // DON'T sign out here - keep the user authenticated
                                // The user should remain logged in after successful password update
                            } else {
                                Toast.makeText(getContext(),
                                        "הסיסמה שהזנת אינה תואמת לסיסמה שיצרת. אנא ודא שהזנת את הסיסמה הנכונה.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            });

            passwordInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override public void afterTextChanged(Editable s) {
                    updatePasswordStrengthView(s.toString(), passwordStrength);
                }
            });

            togglePasswordVisibility.setOnClickListener(v -> {
                if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
                } else {
                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
                }
                passwordInput.setSelection(passwordInput.getText().length());
            });
        });

        dialog.show();
    }

    private void updatePasswordInFirestore(String email, String newPassword, AlertDialog dialog) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        // Get user data to save to local storage
                        User user = document.toObject(User.class);
                        if (user != null) {
                            user.setId(document.getId());
                        }

                        document.getReference().update("userPass", newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "הסיסמה עודכנה בהצלחה במערכת!", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    resetEmail = null; // Clear stored email

                                    // Save user to local storage and update UI
                                    if (user != null) {
                                        saveUserToLocalStorage(user);

                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).updateNavigationHeader();
                                        }

                                        // Navigate to ChooseClassFragment
                                        requireActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.flFragment, new ChooseClassFragment())
                                                .commit();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "שגיאה בעדכון הסיסמה במסד הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "לא נמצא משתמש תואם במסד הנתונים", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בגישה למסד הנתונים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePasswordStrengthView(String password, TextView passwordStrength) {
        int strength = calculatePasswordStrength(password);
        String strengthMessage;

        if (strength < 3) {
            strengthMessage = "סיסמה חלשה";
            passwordStrength.setTextColor(Color.RED);
        } else if (strength < 5) {
            strengthMessage = "סיסמה בינונית";
            passwordStrength.setTextColor(Color.rgb(255, 165, 0)); // Orange color
        } else {
            strengthMessage = "סיסמה חזקה";
            passwordStrength.setTextColor(Color.GREEN);
        }

        passwordStrength.setText(strengthMessage);
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;

        if (password.matches(".*[A-Z].*")) {
            strength++;
        }

        if (password.matches(".*[a-z].*")) {
            strength++;
        }

        if (password.matches(".*\\d.*")) {
            strength++;
        }

        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            strength++;
        }

        if (password.length() >= 8) {
            strength++;
        }

        return strength;
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
        editor.putString("imageProfile", user.getImageProfile());

        editor.apply();
    }




    @Override
    public void showLoginFailure(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }
}