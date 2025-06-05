package com.example.myfinalproject.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Admin;
import com.example.myfinalproject.CallBacks.ReviewCallback;
import com.example.myfinalproject.DataModels.Review;
import com.example.myfinalproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList; // רשימת הביקורות
    private String userId; // מזהה המשתמש המחובר
    private ReviewCallback callback; // ממשק ללחיצות (מחיקה, עריכה, שמירה)
    private boolean isAdmin = false; // האם המשתמש הנוכחי הוא מנהל


    public ReviewAdapter(List<Review> reviewList, ReviewCallback callback) {
        this.reviewList = reviewList;
        this.callback = callback;
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        this.isAdmin = Admin.isAdminLoggedIn(); // בדיקת הרשאת מנהל
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onerow_review, parent, false);
        return new ReviewViewHolder(itemView);
    }

    // קישור נתוני ביקורת לתצוגה
    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        if (reviewList == null || position < 0 || position >= reviewList.size()) {
            return;
        }

        Review review = reviewList.get(position);

        // הצגת שם המשתמש
        String userName = review.getUserName();
        holder.tvUser.setText(userName != null ? userName : "משתמש אנונימי");

        // הצגת תוכן הביקורת
        String reviewText = review.getReviewText();
        holder.tvWritingReview.setText(reviewText != null ? reviewText : "");

        // הצגת דירוג (כוכבים)
        holder.rbReviewRating.setRating(review.getRating());

        // הצגת תאריך יצירה
        if (review.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
            holder.tvReviewTimestamp.setText(sdf.format(review.getCreatedAt()));
        }

        // בדיקה אם הביקורת שייכת למשתמש הנוכחי
        boolean isUserReview = userId != null && userId.equals(review.getUserId());

        //  בדיקה מחדש אם המשתמש מנהל - אם אחד מהם אומר שהמשתמש אדמין – נניח שהוא אדמין
        boolean currentIsAdmin = isAdmin || Admin.isAdminLoggedIn(); //הגדר את currentIsAdmin כ־true אם isAdmin הוא true או אם Admin.isAdminLoggedIn() מחזירה true.

        // הצגת כפתור מחיקה רק אם המשתמש כתב את הביקורת או אם הוא מנהל
        holder.imgBtnDeleteReview.setVisibility((isUserReview || currentIsAdmin) ? View.VISIBLE : View.GONE);

        // הצגת כפתור עריכה רק אם המשתמש כתב את הביקורת
        holder.imgBtnEditReview.setVisibility(isUserReview ? View.VISIBLE : View.GONE);
        holder.btnSaveChanges.setVisibility(View.GONE);

        holder.tvWritingReview.setEnabled(false); // מניעת עריכה טקסט
        holder.rbReviewRating.setIsIndicator(true); // מניעת שינוי כוכבים

        // לחיצה על כפתור מחיקה
        holder.imgBtnDeleteReview.setOnClickListener(v -> {
            if (callback != null) {
                callback.onDeleteReview(review, holder.getAdapterPosition());
            }
        });

        // לחיצה על כפתור עריכה
        holder.imgBtnEditReview.setOnClickListener(v -> {
            boolean isEditing = holder.btnSaveChanges.getVisibility() == View.VISIBLE;
            if (!isEditing) {
                // הפעלה של מצב עריכה
                holder.btnSaveChanges.setVisibility(View.VISIBLE);
                holder.tvWritingReview.setEnabled(true);
                holder.tvWritingReview.requestFocus();
                holder.rbReviewRating.setIsIndicator(false);

                if (callback != null) {
                    callback.onEditReview(review, holder.getAdapterPosition());
                }
            } else {
                // ביטול מצב עריכה
                holder.btnSaveChanges.setVisibility(View.GONE);
                holder.tvWritingReview.setEnabled(false);
                holder.rbReviewRating.setIsIndicator(true);
            }
        });

        // לחיצה על כפתור שמירת שינויים
        holder.btnSaveChanges.setOnClickListener(v -> {
            if (callback != null) {
                String newText = holder.tvWritingReview.getText().toString();
                float newRating = holder.rbReviewRating.getRating();
                callback.onSaveChanges(review, holder.getAdapterPosition(), newText, newRating);
            }
            // חזרה למצב רגיל
            holder.btnSaveChanges.setVisibility(View.GONE);
            holder.tvWritingReview.setEnabled(false);
            holder.rbReviewRating.setIsIndicator(true);
        });
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    // מחלקת ViewHolder פנימית – מייצגת ביקורת אחת
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvReviewTimestamp;
        EditText tvWritingReview;
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

    // מאפשר לעדכן האם המשתמש הוא מנהל
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }
}