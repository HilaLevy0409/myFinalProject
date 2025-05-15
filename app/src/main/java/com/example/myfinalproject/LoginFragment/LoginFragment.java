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

            FirebaseAuth auth = FirebaseAuth.getInstance();

            database.collection("users")
                    .whereEqualTo("userEmail", emailSend)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            auth.sendPasswordResetEmail(emailSend)
                                    .addOnCompleteListener(resetTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        btnFinish.setVisibility(View.VISIBLE);

                                        if (resetTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "מייל לשחזור סיסמה נשלח!", Toast.LENGTH_LONG).show();

                                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PasswordResetPrefs", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean(emailSend, true);
                                            editor.apply();

                                            dialog.dismiss();

                                            askUserToUpdatePasswordInFirestore();

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


    //קוד מקורי, למטה חדש עם שיפורים


//    private void askUserToUpdatePasswordInFirestore() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("עדכון סיסמה במערכת");
//
//        final EditText input = new EditText(getContext());
//        input.setHint("הקלד/י את הסיסמה החדשה");
//        builder.setView(input);
//
//        builder.setPositiveButton("עידכון", (dialog, which) -> {
//            String newPassword = input.getText().toString().trim();
//
//            String validation = Validator.isValidPassword(newPassword);
//            if (!validation.isEmpty()) {
//                Toast.makeText(getContext(), validation, Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//            if (firebaseUser != null) {
//                FirebaseFirestore.getInstance()
//                        .collection("users")
//                        .document(firebaseUser.getUid())
//                        .update("userPassword", newPassword)
//                        .addOnSuccessListener(aVoid -> {
//                            Toast.makeText(getContext(), "הסיסמה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
//                        })
//                        .addOnFailureListener(e -> {
//                            Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        });
//            }
//        });
//
//        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());
//        builder.show();
//    }




    private void askUserToUpdatePasswordInFirestore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("עדכון סיסמה במערכת");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText input = new EditText(getContext());
        input.setHint("הקלד/י את הסיסמה החדשה");
        input.setGravity(Gravity.RIGHT);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final ImageView togglePasswordVisibility = new ImageView(getContext());
        togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
        togglePasswordVisibility.setPadding(20, 20, 20, 20);

        LinearLayout passwordLayout = new LinearLayout(getContext());
        passwordLayout.setOrientation(LinearLayout.HORIZONTAL);
        passwordLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        passwordLayout.addView(input, inputParams);
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
                String newPassword = input.getText().toString().trim();

                if (TextUtils.isEmpty(newPassword)) {
                    input.setError("יש להזין סיסמה");
                    input.requestFocus();
                    return;
                }

                String validationMessage = isValidPassword(newPassword);
                if (!validationMessage.isEmpty()) {
                    input.setError(validationMessage);
                    input.requestFocus();
                    return;
                }

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.updatePassword(newPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String email = firebaseUser.getEmail();
                                    if (email == null) {
                                        Toast.makeText(getContext(), "לא ניתן למצוא את כתובת האימייל של המשתמש", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    FirebaseFirestore.getInstance()
                                            .collection("users")
                                            .whereEqualTo("userEmail", email)
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                                    document.getReference().update("userPass", newPassword)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(getContext(), "הסיסמה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
                                                                dialog.dismiss();
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
                                    
                                } else {
                                    Exception exception = task.getException();
                                    Toast.makeText(getContext(), "שגיאה בעדכון הסיסמה ב-Firebase: " + (exception != null ? exception.getMessage() : ""), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            });

            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override public void afterTextChanged(Editable s) {
                    updatePasswordStrengthView(s.toString(), passwordStrength);
                }
            });

            togglePasswordVisibility.setOnClickListener(v -> {
                if (input.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                    togglePasswordVisibility.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                        togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
                        togglePasswordVisibility.animate().alpha(1f).setDuration(200).start();
                    }).start();
                } else {
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    togglePasswordVisibility.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                        togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
                        togglePasswordVisibility.animate().alpha(1f).setDuration(200).start();
                    }).start();
                }

                input.setSelection(input.getText().length());
            });

        });

        dialog.show();
    }

    private void updatePasswordStrengthView(String password, TextView passwordStrength) {
        int strength = calculatePasswordStrength(password);
        String strengthMessage;

        if (strength < 3) {
            strengthMessage = "סיסמה חלשה";
            passwordStrength.setTextColor(Color.RED);
        } else if (strength < 5) {
            strengthMessage = "סיסמה בינונית";
            passwordStrength.setTextColor(Color.YELLOW);
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