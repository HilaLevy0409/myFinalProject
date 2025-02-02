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

//
//    public void addSummary(Summary summary, AddSummaryCallback callback) {
//        database.collection("summary").add(summary)
//                .addOnCompleteListener(task1 -> {
//                    if(task1.isSuccessful()) {
//                        String productId = task1.getResult().getId();
//                        summary.setId(productId);
//                        callback.onSuccess(summary);
//                    } else {
//                        callback.onError(Objects.requireNonNull(task1.getException()).getMessage());
//                    }
//                });
//    }

//    public void loadSummaries(SummariesCallback callback) {
//        database.collection("summary")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<Summary> products = new ArrayList<>();
//                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
//                        Summary summary = document.toObject(Summary.class);
//                        if (summary != null) {
//                            summary.setId(document.getId());
//                            summaries.add(summary);
//                        }
//                    }
//                    callback.onSuccess(summaries);
//                })
//                .addOnFailureListener(e -> callback.onError(e.getMessage()));
//    }
//
//
//
//
//    public void updateSummary(Summary summary, SummaryCallback callback) {
//        database.collection("product").document(product.getId())
//                .set(summary)
//                .addOnSuccessListener(aVoid -> callback.onSuccess(summary))
//                .addOnFailureListener(e -> callback.onError(e.getMessage()));
//    }
//
//
   public void deleteSummary(String productId, SummaryCallback callback) {
        database.collection("summary").document(productId).delete()
                .addOnSuccessListener(aVoid -> callback.onSummaryReceived(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
//
//
//
//
//    public void getSummary(Summary summary) {
//
//
//    }
//
//    public interface SummaryCallback {
//        void onSuccess(Summary summary);
//        void onError(String message);
//    }
//
//    public interface SummariessCallback {
//        void onSuccess(List<Summary> summaries);
//        void onError(String message);
//    }
//
//}


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
