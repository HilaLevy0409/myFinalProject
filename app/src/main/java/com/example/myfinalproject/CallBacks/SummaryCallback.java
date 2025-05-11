package com.example.myfinalproject.CallBacks;


import com.example.myfinalproject.DataModels.Summary;

public interface SummaryCallback {

    void onSuccess(Summary summary1);

    void onError(String error);
}