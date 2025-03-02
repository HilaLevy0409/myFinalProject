package com.example.myfinalproject.NoticesAdminFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.R;


public class NoticesAdminFragment extends Fragment implements View.OnClickListener {

    private TextView tvMessages, tvReports;
    private Button btnBack;




    public NoticesAdminFragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        tvMessages = view.findViewById(R.id.tvMessages);
        tvReports = view.findViewById(R.id.tvReports);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notices_admin, container, false);


    }

    @Override
    public void onClick(View v) {
        if(v == btnBack){
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new AdminFragment())
                    .commit();
        }

    }
}