package com.example.myfinalproject.Models;

public class Summary {
    private String summaryId;
    private String classOption;
    private String profession;
    private String summaryTitle;
    private String summaryContent;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String image;

    // Empty constructor required for Firestore
    public Summary() {}

    public Summary(String classOption, String profession, String summaryTitle, String summaryContent) {
        this.summaryId = java.util.UUID.randomUUID().toString();
        this.classOption = classOption;
        this.profession = profession;
        this.summaryTitle = summaryTitle;
        this.summaryContent = summaryContent;
    }

    // Getters and setters
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