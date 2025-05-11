package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.DataModels.Review;

public interface ReviewCallback {
    void onDeleteReview(Review review, int position);
    void onEditReview(Review review, int position);
    void onSaveChanges(Review review, int position, String newText, float newRating);
}
