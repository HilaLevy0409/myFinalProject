package com.example.myfinalproject.Models;


public class Profession {
    private String name; //שם של מקצוע ספציפי
    private String category; //מקצוע חובה או מגמה

    public Profession(String name, String category) {
        this.name = name;
        this.category = category;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




}