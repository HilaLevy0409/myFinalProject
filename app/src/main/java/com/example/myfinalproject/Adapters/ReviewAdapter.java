package com.example.myfinalproject.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Models.Review;
import com.example.myfinalproject.R;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onerow_review, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.tvReviewName.setText(review.getName());
        holder.rbReviewRating.setRating(review.getRating());
        holder.tvWritingReview.setText(review.getWriting());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewName, tvWritingReview;
        RatingBar rbReviewRating;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewName = itemView.findViewById(R.id.tvReviewName);
            rbReviewRating = itemView.findViewById(R.id.rbReviewRating);
            tvWritingReview = itemView.findViewById(R.id.tvWritingReview);
        }
    }
}