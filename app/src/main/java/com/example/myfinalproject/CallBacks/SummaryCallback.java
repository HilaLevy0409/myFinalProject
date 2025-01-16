package com.example.myfinalproject.CallBacks;


import com.example.myfinalproject.Models.Summary;

public interface SummaryCallback {
    void onSummaryReceived(Summary summary);
    void onError(String error);
}