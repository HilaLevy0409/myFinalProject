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

    public void loadSavedSummaries() {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String summaryId = document.getString("summaryId");
                        if (summaryId != null) {
                            loadSummary(summaryId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    fragment.showLoadError("שגיאה בטעינת הסיכומים השמורים");
                });
    }

    private void loadSummary(String summaryId) {
        firestore.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Summary summary = documentSnapshot.toObject(Summary.class);
                        if (summary != null) {
                            if (summary.getSummaryId() == null || summary.getSummaryId().isEmpty()) {
                                summary.setSummaryId(documentSnapshot.getId());
                            }
                            allSummaries.add(summary);
                            fragment.updateSummaryList(allSummaries);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    fragment.showLoadError("שגיאה בטעינת הסיכום");
                });
    }

    public void handleSummaryClick(Summary summary) {
        if (summary == null) {
            return;
        }

        fragment.navigateToSummary(summary);
    }

    public void filterSummaries(String query) {
        List<Summary> filteredList = new ArrayList<>();
        for (Summary summary : allSummaries) {
            if (summary.getSummaryTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(summary);
            }
        }
        fragment.updateSummaryList(filteredList);
    }

}