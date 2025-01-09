package com.example.myfinalproject.Models;

public class SummaryClass {
    private String etClass;
    private String etProfession;
    private String etSummaryTitle;
    private String etSummaryContent;

    public String getEtSummaryContent() {
        return etSummaryContent;
    }

    public void setEtSummaryContent(String etSummaryContent) {
        this.etSummaryContent = etSummaryContent;
    }

    public String getEtSummaryTitle() {
        return etSummaryTitle;
    }

    public void setEtSummaryTitle(String etSummaryTitle) {
        this.etSummaryTitle = etSummaryTitle;
    }

    public String getEtProfession() {
        return etProfession;
    }

    public void setEtProfession(String etProfession) {
        this.etProfession = etProfession;
    }

    public String getEtClass() {
        return etClass;
    }

    public void setEtClass(String etClass) {
        this.etClass = etClass;
    }




    public SummaryClass(String etClass, String etSummaryContent, String etSummaryTitle, String etProfession) {
        this.etClass = etClass;
        this.etSummaryContent = etSummaryContent;
        this.etSummaryTitle = etSummaryTitle;
        this.etProfession = etProfession;
    }



}
