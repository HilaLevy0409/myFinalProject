package com.example.myfinalproject.QuestionsFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myfinalproject.ContactUsFragment.ContactUsFragment;
import com.example.myfinalproject.NoticesAdminFragment.NoticesAdminFragment;
import com.example.myfinalproject.R;

public class QuestionsFragment extends Fragment implements View.OnClickListener {

    private Button btnContact;

    public QuestionsFragment() {
    }


    public static QuestionsFragment newInstance() {
        QuestionsFragment fragment = new QuestionsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_questions, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnContact = view.findViewById(R.id.btnContact);
        btnContact.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == btnContact) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ContactUsFragment())
                    .commit();

        }
    }
}