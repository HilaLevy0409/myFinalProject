package com.example.myfinalproject.SumReviewFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.Adapters.ReviewAdapter;
import com.example.myfinalproject.Models.Review;
import com.example.myfinalproject.R;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;


public class SumReviewFragment extends Fragment {

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private DatabaseReference reviews;


    private EditText etReviewText;
    private RatingBar ratingBar;
    private Button btnSubmitReview;
    private TextView tvName;
    private String summaryId;

    private float rating;
    private List<Review> reviewsList = new ArrayList<>();


    public static SumReviewFragment newInstance(String summaryId) {
        SumReviewFragment fragment = new SumReviewFragment();
        Bundle args = new Bundle();
        args.putString("summaryId", summaryId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            summaryId = getArguments().getString("summaryId");
            rating = getArguments().getFloat("rating", 0f);

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sum_review, container, false);

        rvReviews = view.findViewById(R.id.rvReviews);
        tvName = view.findViewById(R.id.tvName);
        etReviewText = view.findViewById(R.id.etReviewText);
        ratingBar = view.findViewById(R.id.ratingBar);
        btnSubmitReview = view.findViewById(R.id.btnSubmitReview);

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(reviewAdapter);

        ratingBar.setIsIndicator(false);

        loadReviews();

        btnSubmitReview.setOnClickListener(v -> submitReview());

        return view;
    }

    private void loadReviews() {

        reviewAdapter.notifyDataSetChanged();
    }


    private void submitReview() {
        String name = tvName.getText().toString();
        String reviewText = etReviewText.getText().toString().trim();
        float rating = ratingBar.getRating();


        if (rating == 0) {
            Toast.makeText(getContext(), "אנא דרגו בין 1-5 כוכבים", Toast.LENGTH_SHORT).show();
            return;
        }

        Review newReview = new Review(name, reviewText, rating);
        reviewList.add(0, newReview);
        reviewAdapter.notifyItemInserted(0);
        rvReviews.scrollToPosition(0);

        tvName.setText("");
        etReviewText.setText("");
        ratingBar.setRating(0);

        Toast.makeText(getContext(), "תודה על הביקורת שלך!", Toast.LENGTH_SHORT).show();
    }

    public float calculateAverageRating() {
        if (reviewList.isEmpty()) {
            return 0;
        }

        float sum = 0;
        for (Review review : reviewList) {
            sum += review.getRating();
        }

        return sum / reviewList.size();
    }
}