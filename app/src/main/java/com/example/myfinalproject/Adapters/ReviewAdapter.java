package com.example.myfinalproject.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Models.Review;
import com.example.myfinalproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final String TAG = "ReviewAdapter";

    private List<Review> reviewList;
    private String currentUserId;
    private ReviewInteractionListener listener;

    public interface ReviewInteractionListener {
        void onDeleteReview(Review review, int position);
        void onEditReview(Review review, int position);
        void onSaveChanges(Review review, int position, String newText, float newRating);
    }

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    public ReviewAdapter(List<Review> reviewList, ReviewInteractionListener listener) {
        this.reviewList = reviewList;
        this.listener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
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
        try {
            Review review = reviewList.get(position);

            // Set username
            holder.tvUser.setText(review.getUserName() != null ? review.getUserName() : "משתמש אנונימי");

            // Set review content
            holder.tvWritingReview.setText(review.getReviewText() != null ? review.getReviewText() : "");

            // Set rating
            holder.rbReviewRating.setRating(review.getRating());

            // Set timestamp
            if (review.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                holder.tvReviewTimestamp.setText(sdf.format(review.getCreatedAt()));
            } else {
                holder.tvReviewTimestamp.setText("תאריך לא ידוע");
            }

            // Show edit/delete buttons only for the current user's reviews
            boolean isCurrentUserReview = currentUserId != null &&
                    currentUserId.equals(review.getUserId());

            holder.imgBtnDeleteReview.setVisibility(isCurrentUserReview ? View.VISIBLE : View.GONE);
            holder.imgBtnEditReview.setVisibility(isCurrentUserReview ? View.VISIBLE : View.GONE);

            // Initially hide save button and make text view not editable
            holder.btnSaveChanges.setVisibility(View.GONE);
            holder.tvWritingReview.setFocusable(false);
            holder.tvWritingReview.setClickable(false);
            holder.rbReviewRating.setIsIndicator(true);

            // Set up button listeners
            holder.imgBtnDeleteReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteReview(review, holder.getAdapterPosition());
                }
            });

            holder.imgBtnEditReview.setOnClickListener(v -> {
                // Toggle edit mode
                boolean isEditing = holder.btnSaveChanges.getVisibility() == View.VISIBLE;

                if (!isEditing) {
                    // Enter edit mode
                    holder.btnSaveChanges.setVisibility(View.VISIBLE);
                    holder.tvWritingReview.setFocusableInTouchMode(true);
                    holder.tvWritingReview.setClickable(true);
                    holder.tvWritingReview.requestFocus();
                    holder.rbReviewRating.setIsIndicator(false);

                    if (listener != null) {
                        listener.onEditReview(review, holder.getAdapterPosition());
                    }
                } else {
                    // Exit edit mode
                    holder.btnSaveChanges.setVisibility(View.GONE);
                    holder.tvWritingReview.setFocusable(false);
                    holder.tvWritingReview.setClickable(false);
                    holder.rbReviewRating.setIsIndicator(true);
                }
            });

            holder.btnSaveChanges.setOnClickListener(v -> {
                if (listener != null) {
                    String newText = holder.tvWritingReview.getText().toString();
                    float newRating = holder.rbReviewRating.getRating();
                    listener.onSaveChanges(review, holder.getAdapterPosition(), newText, newRating);
                }

                // Exit edit mode
                holder.btnSaveChanges.setVisibility(View.GONE);
                holder.tvWritingReview.setFocusable(false);
                holder.tvWritingReview.setClickable(false);
                holder.rbReviewRating.setIsIndicator(true);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder", e);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviewList = newReviews;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvWritingReview, tvReviewTimestamp;
        RatingBar rbReviewRating;
        ImageButton imgBtnEditReview, imgBtnDeleteReview;
        Button btnSaveChanges;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
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