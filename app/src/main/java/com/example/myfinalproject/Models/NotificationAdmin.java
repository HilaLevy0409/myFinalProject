package com.example.myfinalproject.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NotificationAdmin {
    @DocumentId
    private String id;
    private String userId;
    private String userName;
    private String reportedUserId;
    private String reportedUserName;
    private String content;
    private String type; //הודעה או דיווח
    private String reportReason;
    private String contactReason;
    private Timestamp timestamp;

    public NotificationAdmin() {
    }



    public NotificationAdmin(String userId, String userName, String content, String reason, String type) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.type = type;
        this.timestamp = Timestamp.now();


        if (type.equals("REPORT")) {
            this.reportReason = reason;

        } else if (type.equals("CONTACT")) {
            this.contactReason = reason;
        }
    }

    public void setReportedUserInfo(String reportedUserId, String reportedUserName) {
        this.reportedUserId = reportedUserId;
        this.reportedUserName = reportedUserName;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportedUserName() {
        return reportedUserName;
    }

    public void setReportedUserName(String reportedUserName) {
        this.reportedUserName = reportedUserName;
    }

    public String getContactReason() {
        return contactReason;
    }

    public void setContactReason(String contactReason) {
        this.contactReason = contactReason;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}