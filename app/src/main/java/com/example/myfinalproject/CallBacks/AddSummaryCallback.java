package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.DataModels.Summary;


public interface AddSummaryCallback {
    void onSummaryAdd(Summary summary);
    void onError(String error);
}