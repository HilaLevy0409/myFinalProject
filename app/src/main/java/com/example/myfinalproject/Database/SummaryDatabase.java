package com.example.myfinalproject.Database;

import com.example.myfinalproject.CallBacks.AddSummaryCallback;
import com.example.myfinalproject.CallBacks.SummaryCallback;
import com.example.myfinalproject.Models.Summary;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SummaryDatabase {

    private FirebaseFirestore database;


    public SummaryDatabase() {
        database = FirebaseFirestore.getInstance();
    }


    public void addSummary(Summary summary, AddSummaryCallback callback) {
        DocumentReference summaryRef = database.collection("summaries").document(summary.getSummaryId());

        summaryRef.set(summary)
                .addOnSuccessListener(aVoid -> callback.onSummaryAdd(summary))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


     public void getSummary(final String summaryId, final SummaryCallback callback) {
         DocumentReference summaryRef = database.collection("summaries").document(summaryId);

         summaryRef.get()
                 .addOnSuccessListener(documentSnapshot -> {
                     if (documentSnapshot.exists()) {
                         Summary summary = documentSnapshot.toObject(Summary.class);
                         if (summary != null) {
                             callback.onSummaryReceived(summary);
                         } else {
                             callback.onError("Summary data is invalid.");
                         }
                     } else {
                         callback.onError("Summary not found.");
                     }
                 })
                 .addOnFailureListener(e -> callback.onError("Database error: " + e.getMessage()));
     }
}
