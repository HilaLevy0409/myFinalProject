package com.example.myfinalproject.DataModels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Message {
    private String text;
    private boolean isSent;
    private String timestamp;
    private Date timestampDate;

    private String messageId;

    public Message(String text, boolean isSent) {
        this.text = text;
        this.isSent = isSent;
        this.timestampDate = new Date();
        formatTimestamp();
    }

    public Message(String text, boolean isSent, Date timestampDate) {
        this.text = text;
        this.isSent = isSent;
        this.timestampDate = timestampDate;
        formatTimestamp();
    }

    private void formatTimestamp() {
        if (timestampDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
            this.timestamp = sdf.format(timestampDate);
        } else {
            this.timestamp = "";
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Date getTimestampDate() {
        return timestampDate;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestampDate(Date timestampDate) {
        this.timestampDate = timestampDate;
        formatTimestamp();
    }



    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", isSent=" + isSent +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}