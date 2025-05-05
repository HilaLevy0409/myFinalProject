package com.example.myfinalproject.Models;



import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


public class Summary {
    private String summaryId;
    private String classOption;
    private String profession;
    private String summaryTitle;
    private String summaryContent;
    private String image;
    private @ServerTimestamp Date createdDate;
    private float rating;
    private String userName;
    private String userId;




    public Summary(String classOption, String profession, String summaryTitle, String summaryContent, String userName, String userId) {
        this.summaryId = java.util.UUID.randomUUID().toString();
        this.classOption = classOption;
        this.profession = profession;
        this.summaryTitle = summaryTitle;
        this.summaryContent = summaryContent;
        this.userName = userName;
        this.userId = userId;


    }

    public Summary() {

    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }






    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getSummaryId() { return summaryId; }
    public void setSummaryId(String summaryId) { this.summaryId = summaryId; }

    public String getClassOption() { return classOption; }
    public void setClassOption(String classOption) { this.classOption = classOption; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public String getSummaryTitle() { return summaryTitle; }
    public void setSummaryTitle(String summaryTitle) { this.summaryTitle = summaryTitle; }

    public String getSummaryContent() { return summaryContent; }
    public void setSummaryContent(String summaryContent) { this.summaryContent = summaryContent; }


}