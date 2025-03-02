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

import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumByUserFragment.SumByUserFragment;

public class ManageUserFragment extends Fragment implements View.OnClickListener {



    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private int badPoints = 0;
    private TextView tvBadPoints, tvEmail, tvPhone, tvBirthDate, tvSumNum, tvUsername;
    private Button btnAddPoint, btnRemovePoint, btnShowSums, btnSendMessage;
    private User currentUser;


    public ManageUserFragment() {
        // Required empty public constructor
    }

    public static ManageUserFragment newInstance(String param1, String param2) {
        ManageUserFragment fragment = new ManageUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
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

        btnAddPoint.setOnClickListener(this);
        btnRemovePoint.setOnClickListener(this);
        btnShowSums.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);
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
                Intent intent = new Intent(getActivity(), Message.class);
                startActivity(intent);
            });

        }

    }

    private void updateBadPointsText() {
        tvBadPoints.setText("נקודות לרעה: " + badPoints);
    }


    public void displayUserData(User user) {
        this.currentUser = user;
        tvEmail.setText("אימייל: " + user.getUserEmail());
        tvPhone.setText("מספר טלפון: " + user.getPhone());
        tvBirthDate.setText("תאריך לידה: " + user.getUserBirthDate());
        tvUsername.setText("שם משתמש: " + user.getUserName());
        tvBadPoints.setText("נקודות לרעה: " + user.getBadPoints());
        tvSumNum.setText("מספר סיכומים שנכתבו: " + user.getSumCount());
    }
}
