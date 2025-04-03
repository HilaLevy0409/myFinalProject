package com.example.myfinalproject.Models;

public class Review {
    private String name;
    private String writing;
    private float rating;


    public Review(String name, float rating, String writing) {
        this.name = name;
        this.rating = rating;
        this.writing = writing;
    }

    public String getName() {
        return name;
    }

    public float getRating() {
        return rating;
    }

    public String getWriting() {
        return writing;
    }
}

