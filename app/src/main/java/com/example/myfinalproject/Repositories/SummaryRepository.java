package com.example.myfinalproject.Repositories;

import com.example.myfinalproject.CallBacks.AddSummaryCallback;
import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.CallBacks.SummaryCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.DataModels.Summary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SummaryRepository {
    private FirebaseFirestore database; // קישור למסד הנתונים של Firebase Firestore

    // בנאי - אתחול גישה למסד הנתונים
    public SummaryRepository() {
        database = FirebaseFirestore.getInstance();
    }

    // הוספת סיכום חדש למסד הנתונים
    public void addSummary(Summary summary, AddSummaryCallback callback) {
        // שמירת האובייקט באוסף "summaries"
        database.collection("summaries").add(summary)
                .addOnSuccessListener(documentReference -> {
                    // שמירת ה-ID של המסמך באובייקט הסיכום
                    summary.setSummaryId(documentReference.getId());
                    // החזרת הסיכום דרך ה-callback
                    callback.onSummaryAdd(summary);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

//    public void getSummary(String summaryId, SummaryCallback callback) {
//        database.collection("summaries").document(summaryId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        Summary summary = documentSnapshot.toObject(Summary.class);
//                        if (summary != null) {
//                            summary.setSummaryId(documentSnapshot.getId());
//
//
//                            callback.onSuccess(summary);
//                        } else {
//                            callback.onError("נתוני סיכום לא חוקיים");
//                        }
//                    } else {
//                        callback.onError("הסיכום לא נמצא");
//                    }
//                })
//                .addOnFailureListener(e -> callback.onError(e.getMessage()));
//    }

    // שליפת כל הסיכומים ממסד הנתונים
    public void getAllSummaries(SummariesCallback callback) {
        database.collection("summaries")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // יצירת רשימה לאחסון הסיכומים
                    ArrayList<Summary> summaries = new ArrayList<>();
                    // מעבר על כל מסמך בתוצאה
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        // המרת המסמך לאובייקט Summary
                        Summary summary = document.toObject(Summary.class);
                        if (summary != null) {
                            // שמירת ה-ID של הסיכום
                            summary.setSummaryId(document.getId());
                            // הוספת הסיכום לרשימה
                            summaries.add(summary);
                        }
                    }
                    // החזרת כל הסיכומים דרך callback
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

//    public void uploadSummary(Summary summary, SummaryCallback summaryCallback, UserCallback userCallback) {
//        // First upload the summary
//        database.collection("summaries").add(summary)
//                .addOnSuccessListener(documentReference -> {
//                    // Set the ID in the summary object
//                    String summaryId = documentReference.getId();
//                    summary.setSummaryId(summaryId);
//
//                    // Update the document with the ID
//                    documentReference.set(summary)
//                            .addOnSuccessListener(aVoid -> {
//                                // Summary uploaded successfully, now increment the user's summary count
//                                UserRepository userDatabase = new UserRepository();
//                                userDatabase.decrementUserSummaryCount(summary.getUserId(), userCallback);
//
//                                // Notify about successful summary upload
//                                summaryCallback.onSuccess(summary);
//                            })
//                            .addOnFailureListener(e -> summaryCallback.onError(e.getMessage()));
//                })
//                .addOnFailureListener(e -> summaryCallback.onError(e.getMessage()));
//    }



}