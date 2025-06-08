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
        database.collection("summaries").add(summary)
                .addOnSuccessListener(documentReference -> {
                    // שמירת ה-ID של המסמך באובייקט הסיכום
                    summary.setSummaryId(documentReference.getId());
                    // החזרת הסיכום דרך ה-callback
                    callback.onSummaryAdd(summary);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

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
}