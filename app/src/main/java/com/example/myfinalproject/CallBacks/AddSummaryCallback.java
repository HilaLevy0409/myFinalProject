package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.Models.Summary;


public interface AddSummaryCallback {
    void onSummaryAdd(Summary summary);
    void onError(String error);
}