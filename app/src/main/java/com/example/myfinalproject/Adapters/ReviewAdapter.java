package com.example.myfinalproject.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.CallBacks.ReviewCallback;
import com.example.myfinalproject.DataModels.Review;
import com.example.myfinalproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList;
    private String userId;
    private ReviewCallback callback;

    public ReviewAdapter(List<Review> reviewList, ReviewCallback callback) {
        this.reviewList = reviewList;
        this.callback = callback;
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
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
        if (reviewList == null || position < 0 || position >= reviewList.size()) {
            return;
        }
        if (reviewList != null && position >= 0 && position < reviewList.size()) {
                Review review = reviewList.get(position);

                String userName = review.getUserName();
                holder.tvUser.setText(userName != null ? userName : "משתמש אנונימי");

                String reviewText = review.getReviewText();
                holder.tvWritingReview.setText(reviewText != null ? reviewText : "");

                holder.rbReviewRating.setRating(review.getRating());

                if (review.getCreatedAt() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
                    holder.tvReviewTimestamp.setText(sdf.format(review.getCreatedAt()));
                }

                boolean isUserReview = userId != null &&
                    userId.equals(review.getUserId());

            holder.imgBtnDeleteReview.setVisibility(isUserReview ? View.VISIBLE : View.GONE);
            holder.imgBtnEditReview.setVisibility(isUserReview ? View.VISIBLE : View.GONE);
            holder.btnSaveChanges.setVisibility(View.GONE);
            holder.tvWritingReview.setFocusable(false);
            holder.tvWritingReview.setClickable(false);
            holder.rbReviewRating.setIsIndicator(true);

            holder.imgBtnDeleteReview.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onDeleteReview(review, holder.getAdapterPosition());
                }
            });

            holder.imgBtnEditReview.setOnClickListener(v -> {
                boolean isEditing = holder.btnSaveChanges.getVisibility() == View.VISIBLE;
                if (!isEditing) {
                    holder.btnSaveChanges.setVisibility(View.VISIBLE);
                    holder.tvWritingReview.setFocusableInTouchMode(true);
                    holder.tvWritingReview.setClickable(true);
                    holder.tvWritingReview.requestFocus();
                    holder.rbReviewRating.setIsIndicator(false);
                    if (callback != null) {
                        callback.onEditReview(review, holder.getAdapterPosition());
                    }
                } else {
                    holder.btnSaveChanges.setVisibility(View.GONE);
                    holder.tvWritingReview.setFocusable(false);
                    holder.tvWritingReview.setClickable(false);
                    holder.rbReviewRating.setIsIndicator(true);
                }
            });
            holder.btnSaveChanges.setOnClickListener(v -> {
                if (callback != null) {
                    String newText = holder.tvWritingReview.getText().toString();
                    float newRating = holder.rbReviewRating.getRating();
                    callback.onSaveChanges(review, holder.getAdapterPosition(), newText, newRating);
                }
                holder.btnSaveChanges.setVisibility(View.GONE);
                holder.tvWritingReview.setFocusable(false);
                holder.tvWritingReview.setClickable(false);
                holder.rbReviewRating.setIsIndicator(true);
            });
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvWritingReview, tvReviewTimestamp;
        RatingBar rbReviewRating;
        ImageButton imgBtnEditReview, imgBtnDeleteReview;
        Button btnSaveChanges;
        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvUser);
            tvWritingReview = itemView.findViewById(R.id.tvWritingReview);
            tvReviewTimestamp = itemView.findViewById(R.id.tvReviewTimestamp);
            rbReviewRating = itemView.findViewById(R.id.rbReviewRating);
            imgBtnEditReview = itemView.findViewById(R.id.ImgBtnEditReview);
            imgBtnDeleteReview = itemView.findViewById(R.id.ImgBtnDeleteReview);
            btnSaveChanges = itemView.findViewById(R.id.btnSaveChanges);
        }
    }
}