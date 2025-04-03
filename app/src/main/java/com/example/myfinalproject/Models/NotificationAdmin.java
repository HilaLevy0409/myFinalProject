package com.example.myfinalproject.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class NotificationAdmin {
    @DocumentId
    private String id;
    private String userId;
    private String userName;
    private String content;
    private String type; //הודעה או דיווח
    private String reportReason;
    private Timestamp timestamp;
    private boolean isRead;

    public NotificationAdmin() {
    }

    public NotificationAdmin(String userId, String userName, String content) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.type = "MESSAGE";
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    public NotificationAdmin(String userId, String userName, String content, String reportReason) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.reportReason = reportReason;
        this.type = "REPORT";
        this.timestamp = Timestamp.now();
        this.isRead = false;
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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
