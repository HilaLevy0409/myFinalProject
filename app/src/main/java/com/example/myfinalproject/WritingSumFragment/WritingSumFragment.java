package com.example.myfinalproject.WritingSumFragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WritingSumFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class WritingSumFragment extends Fragment implements View.OnClickListener{


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;


    private Button btnUploadPhoto, btnSubmit;
    private SummaryPresenter summaryPresenter;
    private EditText etClass, etProfession, etSummaryTitle, etSummaryContent;


    public static WritingSumFragment newInstance(String param1, String param2) {
        WritingSumFragment fragment = new WritingSumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public WritingSumFragment() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_writing_sum, container, false);



    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnUploadPhoto = view.findViewById(R.id.btnUploadPhoto);

        summaryPresenter = new SummaryPresenter(this);

        btnUploadPhoto.setOnClickListener(this);
        btnUploadPhoto.setOnClickListener(this);




    }

    public void onClick(View v) {
        if (v == btnSubmit) {

        }
    }

    private void saveSummaryData(String ...) {
        String classOption = etClass.getText().toString();
        String profession = etProfession.getText().toString();
        String summaryTitle = etSummaryTitle.getText().toString();
        String summaryContent = etSummaryContent.getText().toString();



        Summary summary = new Summary(classOption, profession, summaryTitle, summaryContent);
        submitClicked(summary);

    }
    public void submitClicked(Summary summary) {
        summaryPresenter.submitClicked(summary);
    }


}