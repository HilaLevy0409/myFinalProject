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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.Utils.Validator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import ChooseClassFragment.ChooseClassFragment;


public class LoginFragment extends Fragment implements View.OnClickListener, LoginView {



    private Button btnNext, btnForgotPass;
    private Button  btnFinish;
    private EditText etUsername, etPassword;
    private EditText etEmailS, etPassS, etPassS2;
    private DatabaseReference mDatabase;
    private LoginUserPresenter presenter;




    public LoginFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnNext = view.findViewById(R.id. btnNext);
        etUsername = view.findViewById(R.id.etUser);
        etPassword = view.findViewById(R.id.etPassword);
        btnForgotPass = view.findViewById(R.id.btnForgotPass);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        presenter = new LoginUserPresenter(this);

        btnNext.setOnClickListener(this);
        btnForgotPass.setOnClickListener(this);



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
            if(view == btnForgotPass){
                 createCustomDialog();
            }

//           if(view == btnForgotPass) {
//                    getActivity().getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.flFragment, new dialogPass())
//                            .commit();
//                }




/*
            mDatabase.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String storedPassword = userSnapshot.child("password").getValue(String.class);

                            if (storedPassword != null && storedPassword.equals(password)) {
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.flFragment, new ChooseClassFragment())
                                        .commit();
                            }
                        }


                    } else {
                        Toast.makeText(getActivity(), "משתמש לא נמצא", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            ChooseClassFragment fragment = new ChooseClassFragment();
            transaction.replace(R.id.main, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return;


        }
         */

    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Find the button in the layout
//        Button button = view.findViewById(R.id.btnForgotPass);
//
//        // Set up a click listener for the button
//        button.setOnClickListener(v -> {
//            // Start the DialogActivity when the button is clicked
//            Intent intent = new Intent(getContext(), .class);
//            startActivity(intent);
//        });
//    }


    private void createCustomDialog() {
        Dialog dialog  = new Dialog(getContext());
        dialog.setTitle("שחזור סיסמה");
        dialog.setContentView(R.layout.pass_dialog);
        btnFinish = dialog.findViewById(R.id.btnFinish);
        etEmailS = dialog.findViewById(R.id.etEmailS);
        etPassS = dialog.findViewById(R.id.etPassS);
        etPassS2 = dialog.findViewById(R.id.etPassS2);
        dialog.show();
        btnFinish.setOnClickListener(v -> {
            String emailSend = etEmailS.getText().toString();
            String emailSubject = "שינוי סיסמה";
            String newPassW = etPassS.getText().toString();
            String emailBody ="הסיסמה שונתה\nהסיסמה החדשה היא: \n" + newPassW;

            String password = etPassS.getText().toString().trim();
            String validPassword = Validator.isValidPassword(password);
            String validEmail = Validator.isValidEmail(emailSend);
            if(!validEmail.isEmpty()){
                Toast.makeText(getContext(), validEmail, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validPassword.isEmpty()) {
                Toast.makeText(getContext(), validPassword, Toast.LENGTH_SHORT).show();
                return;
            }

            if(!(etPassS.getText().toString().equals(etPassS2.getText().toString()))){
                Toast.makeText(getContext(), "הסיסמאות לא תואמות", Toast.LENGTH_SHORT).show();
                return;
            }
            else {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailSend});
                intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                intent.putExtra(Intent.EXTRA_TEXT, emailBody);
                intent.setType("message/rfc822");

                startActivity(Intent.createChooser(intent, "Choose an Email client :"));
                dialog.dismiss();
            }
        });

    }

    @Override
    public void showLoginSuccess(User user) {
        Toast.makeText(getContext(), "logged in", Toast.LENGTH_SHORT).show();
        saveUserToLocalStorage(user);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, new ChooseClassFragment())
                .commit();

    }

    private void saveUserToLocalStorage(User user) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", user.getId());


        editor.apply();
    }

    @Override
    public void showLoginFailure(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
    }
}
