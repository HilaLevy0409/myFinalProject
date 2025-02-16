package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.Models.Summary;

import java.util.List;

public interface SummariesCallback {
    public void onSuccess(List<Summary> summaries);
    public void onError(String message);
}
