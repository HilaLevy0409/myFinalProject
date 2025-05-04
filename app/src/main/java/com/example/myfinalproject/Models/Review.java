package com.example.myfinalproject.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Review {
    private String reviewId;
    private String userId;
    private String userName;
    private String summaryId;
    private String reviewText;
    private float rating;
    private @ServerTimestamp Date createdAt;

    public Review() {
    }

    public Review(String userId, String userName, String summaryId, String reviewText, float rating) {
        this.userId = userId;
        this.userName = userName;
        this.summaryId = summaryId;
        this.reviewText = reviewText;
        this.rating = rating;
    }



    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(String summaryId) {
        this.summaryId = summaryId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}