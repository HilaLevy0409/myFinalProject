package com.example.myfinalproject.ManageUserFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.Message.MessageFragment;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.NoticesAdminFragment.NoticesAdminFragment;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumByUserFragment.SumByUserFragment;

public class ManageUserFragment extends Fragment implements View.OnClickListener {



    private int badPoints = 0;
    private TextView tvBadPoints, tvEmail, tvPhone, tvBirthDate, tvSumNum, tvUsername;
    private Button btnAddPoint, btnRemovePoint, btnShowSums, btnSendMessage, btnBack;
    private User currentUser;


    public ManageUserFragment() {
    }

    public static ManageUserFragment newInstance(String param1, String param2) {
        ManageUserFragment fragment = new ManageUserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_user, container, false);

    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        tvBadPoints = view.findViewById(R.id.tvBadPoints);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvSumNum = view.findViewById(R.id.tvSumNum);
        tvUsername = view.findViewById(R.id.tvUsername);

        btnShowSums = view.findViewById(R.id.btnShowSums);
        btnAddPoint = view.findViewById(R.id.btnAddPoint);
        btnRemovePoint = view.findViewById(R.id.btnRemovePoint);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(this);
        btnAddPoint.setOnClickListener(this);
        btnRemovePoint.setOnClickListener(this);
        btnShowSums.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String userName = bundle.getString("userName");
            String userEmail = bundle.getString("userEmail");
            String userPhone = bundle.getString("userPhone");
            String userBirthDate = bundle.getString("userBirthDate");
            int badPoints = bundle.getInt("badPoints", 0);
            int sumCount = bundle.getInt("sumCount", 0);

            tvUsername.setText("שם משתמש: " + userName);
            tvEmail.setText("אימייל: " + userEmail);
            tvPhone.setText("מספר טלפון: " + userPhone);
            tvBirthDate.setText("תאריך לידה: " + userBirthDate);
            tvBadPoints.setText("נקודות לרעה: " + badPoints);
            tvSumNum.setText("מספר סיכומים שנכתבו: " + sumCount);
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAddPoint) {
            if (badPoints < 3) {
                badPoints++;
                updateBadPointsText();
            }
        } else if (view.getId() == R.id.btnRemovePoint) {
            if (badPoints > 0) {
                badPoints--;
                updateBadPointsText();
            }
        } else if (view.getId() == R.id.btnShowSums) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new SumByUserFragment())
                    .commit();
        } else if (view.getId() == R.id.btnSendMessage) {
            btnSendMessage.setOnClickListener(v -> {
                MessageFragment messageFragment = new MessageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userName", currentUser.getUserName());
                bundle.putString("userProfilePic", currentUser.getImageProfile());
                messageFragment.setArguments(bundle);

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, new MessageFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
        else if (view.getId() == R.id.btnBack) {
            btnBack.setOnClickListener(v -> {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, new AdminFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void updateBadPointsText() {
        tvBadPoints.setText("נקודות לרעה: " + badPoints);
    }



}
