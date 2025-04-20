package com.example.myfinalproject.CallBacks;

import com.example.myfinalproject.Models.Summary;

import java.util.List;

public interface SummariesCallback {
     void onSuccess(List<Summary> summaries);
     void onError(String message);
}
