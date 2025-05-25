package com.example.myfinalproject.SaveSummaryFragment;


import com.example.myfinalproject.DataModels.Summary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SaveSummaryPresenter {

    private SaveSummaryFragment fragment;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private List<Summary> allSummaries;

    public SaveSummaryPresenter(SaveSummaryFragment fragment) {
        this.fragment = fragment;
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.allSummaries = new ArrayList<>();
    }

    // טוען את רשימת הסיכומים השמורים (Favorites) של המשתמש הנוכחי
    public void loadSavedSummaries() {
        if (auth.getCurrentUser() == null) {
            return; // אם אין משתמש מחובר – אין טעם להמשיך
        }

        String userId = auth.getCurrentUser().getUid();

        // ניגשים למסמך של המשתמש ואז לתת-אוסף בשם "favorites"
        firestore.collection("users").document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // בכל מסמך יש שדה summaryId שמכיל את מזהה הסיכום
                        String summaryId = document.getString("summaryId");
                        if (summaryId != null) {
                            loadSummary(summaryId); // טוען את הסיכום עצמו
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    fragment.showLoadError("שגיאה בטעינת הסיכומים השמורים");
                });
    }


    // טוען את הסיכום לפי מזהה ממסמך Firestore
    private void loadSummary(String summaryId) {
        firestore.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Summary summary = documentSnapshot.toObject(Summary.class);
                        if (summary != null) {
                            // במידה ולסיכום אין ID, משחזרים אותו מהמסמך
                            if (summary.getSummaryId() == null || summary.getSummaryId().isEmpty()) {
                                summary.setSummaryId(documentSnapshot.getId());
                            }
                            allSummaries.add(summary); // מוסיפים לרשימה המקומית
                            fragment.updateSummaryList(allSummaries); // מעדכנים תצוגה
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    fragment.showLoadError("שגיאה בטעינת הסיכום");
                });
    }


    // כאשר המשתמש לוחץ על סיכום – מבצעים ניווט דרך הפרגמנט
    public void handleSummaryClick(Summary summary) {
        if (summary == null) {
            return;
        }

        fragment.navigateToSummary(summary);
    }

    // סינון סיכומים לפי טקסט שהוזן בחיפוש
    public void filterSummaries(String query) {
        List<Summary> filteredList = new ArrayList<>();
        for (Summary summary : allSummaries) {
            if (summary.getSummaryTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(summary);  // מוסיפים לרשימת המסוננים
            }
        }
        fragment.updateSummaryList(filteredList); // מציגים רק את הסיכומים המסוננים
    }

}