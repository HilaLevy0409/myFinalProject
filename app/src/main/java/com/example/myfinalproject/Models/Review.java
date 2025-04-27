package com.example.myfinalproject.Models;

public class Review {
    private String name;
    private String writing;
    private float rating;
//    private String reviewId;
//    private String userId;
//    private Timestamp timestamp;


    public Review(String userId, String reviewId, float rating) {
//        this.userId = userId;
//        this.reviewId = reviewId;
        this.rating = rating;
        this.writing = writing;
        this.name = name;
//        this.timestamp = timestamp;
    }



    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setWriting(String writing) {
        this.writing = writing;
    }


//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//    public Timestamp getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(Timestamp timestamp) {
//        this.timestamp = timestamp;
//    }
//
//    public String getReviewId() {
//        return reviewId;
//    }
//
//    public void setReviewId(String reviewId) {
//        this.reviewId = reviewId;
//    }



    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public float getRating() {
        return rating;
    }

    public String getWriting() {
        return writing;
    }
}

