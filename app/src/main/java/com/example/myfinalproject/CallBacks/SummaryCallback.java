package com.example.myfinalproject.CallBacks;


import com.example.myfinalproject.Models.Summary;

public interface SummaryCallback {

    void onSuccess(Summary summary1);

    void onError(String error);
}