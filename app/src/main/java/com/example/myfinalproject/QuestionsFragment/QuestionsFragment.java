package com.example.myfinalproject.QuestionsFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.ContactUsFragment.ContactUsFragment;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        btnContact = view.findViewById(R.id.btnContact);
        btnContact.setOnClickListener(this);


        LinearLayout headerFaq1 = view.findViewById(R.id.headerFaq1);
        final LinearLayout contentFaq1 = view.findViewById(R.id.contentFaq1);
        final ImageView expandIconFaq1 = view.findViewById(R.id.imgExpandFaq1);


        contentFaq1.setVisibility(View.GONE);
        expandIconFaq1.setImageResource(R.drawable.ic_expand_more);


        headerFaq1.setOnClickListener(v -> {

            if (contentFaq1.getVisibility() == View.VISIBLE) {
                contentFaq1.setVisibility(View.GONE);
                expandIconFaq1.setImageResource(R.drawable.ic_expand_more);
            } else {
                contentFaq1.setVisibility(View.VISIBLE);
                expandIconFaq1.setImageResource(R.drawable.ic_expand_less);
            }
        });


        LinearLayout headerFaq2 = view.findViewById(R.id.headerFaq2);
        final LinearLayout contentFaq2 = view.findViewById(R.id.contentFaq2);
        final ImageView expandIconFaq2 = view.findViewById(R.id.imgExpandFaq2);

        contentFaq2.setVisibility(View.GONE);
        expandIconFaq2.setImageResource(R.drawable.ic_expand_more);

        headerFaq2.setOnClickListener(v -> {
            if (contentFaq2.getVisibility() == View.VISIBLE) {
                contentFaq2.setVisibility(View.GONE);
                expandIconFaq2.setImageResource(R.drawable.ic_expand_more);
            } else {
                contentFaq2.setVisibility(View.VISIBLE);
                expandIconFaq2.setImageResource(R.drawable.ic_expand_less);
            }
        });


        LinearLayout headerFaq3 = view.findViewById(R.id.headerFaq3);
        final LinearLayout contentFaq3 = view.findViewById(R.id.contentFaq3);
        final ImageView expandIconFaq3 = view.findViewById(R.id.imgExpandFaq3);

        contentFaq3.setVisibility(View.GONE);
        expandIconFaq3.setImageResource(R.drawable.ic_expand_more);

        headerFaq3.setOnClickListener(v -> {
            if (contentFaq3.getVisibility() == View.VISIBLE) {
                contentFaq3.setVisibility(View.GONE);
                expandIconFaq3.setImageResource(R.drawable.ic_expand_more);
            } else {
                contentFaq3.setVisibility(View.VISIBLE);
                expandIconFaq3.setImageResource(R.drawable.ic_expand_less);
            }
        });


        LinearLayout headerFaq4 = view.findViewById(R.id.headerFaq4);
        final LinearLayout contentFaq4 = view.findViewById(R.id.contentFaq4);
        final ImageView expandIconFaq4 = view.findViewById(R.id.imgExpandFaq4);

        contentFaq4.setVisibility(View.GONE);
        expandIconFaq4.setImageResource(R.drawable.ic_expand_more);

        headerFaq4.setOnClickListener(v -> {
            if (contentFaq4.getVisibility() == View.VISIBLE) {
                contentFaq4.setVisibility(View.GONE);
                expandIconFaq4.setImageResource(R.drawable.ic_expand_more);
            } else {
                contentFaq4.setVisibility(View.VISIBLE);
                expandIconFaq4.setImageResource(R.drawable.ic_expand_less);
            }
        });


        LinearLayout headerFaq5 = view.findViewById(R.id.headerFaq5);
        final LinearLayout contentFaq5 = view.findViewById(R.id.contentFaq5);
        final ImageView expandIconFaq5 = view.findViewById(R.id.imgExpandFaq5);

        contentFaq5.setVisibility(View.GONE);
        expandIconFaq5.setImageResource(R.drawable.ic_expand_more);

        headerFaq5.setOnClickListener(v -> {
            if (contentFaq5.getVisibility() == View.VISIBLE) {
                contentFaq5.setVisibility(View.GONE);
                expandIconFaq5.setImageResource(R.drawable.ic_expand_more);
            } else {
                contentFaq5.setVisibility(View.VISIBLE);
                expandIconFaq5.setImageResource(R.drawable.ic_expand_less);
            }
        });
    }



    @Override
    public void onClick(View view) {
        if (view == btnContact) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ContactUsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
}