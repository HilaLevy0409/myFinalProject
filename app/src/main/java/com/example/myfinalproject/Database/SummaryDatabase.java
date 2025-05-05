package com.example.myfinalproject.Database;

import com.example.myfinalproject.CallBacks.AddSummaryCallback;
import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.CallBacks.SummaryCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Models.Summary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SummaryDatabase {
    private FirebaseFirestore database;

    public SummaryDatabase() {

        database = FirebaseFirestore.getInstance();
    }

    public void addSummary(Summary summary, AddSummaryCallback callback) {
        database.collection("summaries").add(summary)
                .addOnSuccessListener(documentReference -> {
                    summary.setSummaryId(documentReference.getId());
                    callback.onSummaryAdd(summary);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getSummary(String summaryId, SummaryCallback callback) {
        database.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Summary summary = documentSnapshot.toObject(Summary.class);
                        if (summary != null) {
                            summary.setSummaryId(documentSnapshot.getId());


                            callback.onSuccess(summary);
                        } else {
                            callback.onError("נתוני סיכום לא חוקיים");
                        }
                    } else {
                        callback.onError("הסיכום לא נמצא");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getAllSummaries(SummariesCallback callback) {
        database.collection("summaries")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Summary> summaries = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Summary summary = document.toObject(Summary.class);
                        if (summary != null) {
                            summary.setSummaryId(document.getId());
                            summaries.add(summary);
                        }
                    }
                    callback.onSuccess(summaries);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
//
//    public void updateSummary(Summary summary, SummaryCallback callback) {
//        database.collection("summaries").document(summary.getSummaryId())
//                .set(summary)
//                .addOnSuccessListener(aVoid -> callback.onSuccess(summary))
//                .addOnFailureListener(e -> callback.onError(e.getMessage()));
//    }

    public void deleteSummary(String summaryId, SummaryCallback callback) {
        database.collection("summaries").document(summaryId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void uploadSummary(Summary summary, SummaryCallback summaryCallback, UserCallback userCallback) {
        // First upload the summary
        database.collection("summaries").add(summary)
                .addOnSuccessListener(documentReference -> {
                    // Set the ID in the summary object
                    String summaryId = documentReference.getId();
                    summary.setSummaryId(summaryId);

                    // Update the document with the ID
                    documentReference.set(summary)
                            .addOnSuccessListener(aVoid -> {
                                // Summary uploaded successfully, now increment the user's summary count
                                UserDatabase userDatabase = new UserDatabase();
                                userDatabase.decrementUserSummaryCount(summary.getUserId(), userCallback);

                                // Notify about successful summary upload
                                summaryCallback.onSuccess(summary);
                            })
                            .addOnFailureListener(e -> summaryCallback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> summaryCallback.onError(e.getMessage()));
    }



}