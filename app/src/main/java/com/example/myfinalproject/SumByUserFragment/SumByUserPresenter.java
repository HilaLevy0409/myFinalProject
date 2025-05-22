package com.example.myfinalproject.SumByUserFragment;

import android.util.Log;

import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.DataModels.Summary;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SumByUserPresenter {
    private final SumByUserFragment view;
    private final String userId;
    private final CollectionReference summariesCollection;

    public SumByUserPresenter(SumByUserFragment view, String userId){
        this.view = view;
        this.userId = userId;
        this.summariesCollection = FirebaseFirestore.getInstance().collection("summaries");
    }

    public void loadUserSummaries(SummariesCallback callback) {

        summariesCollection.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Summary> summaries = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Summary summary = document.toObject(Summary.class);
                                summary.setSummaryId(document.getId());
                                summaries.add(summary);
                            } catch (Exception e) {
                                Log.e("loadUserSummaries", "שגיאה בהמרת מסמך ל-Summary: " + e.getMessage(), e);
                            }
                        }

                        callback.onSuccess(summaries);
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() :
                                "שגיאה בטעינת סיכומים");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError("שגיאה בגישה למסד נתונים: " + e.getMessage());
                });

    }

    public void filterSummaries(String query, List<Summary> originalList, SummaryAdapter adapter) {
        ArrayList<Summary> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            loadUserSummaries(new SummariesCallback() {
                @Override
                public void onSuccess(List<Summary> summaries) {
                    originalList.clear();
                    originalList.addAll(summaries);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String message) {
                    view.showError("שגיאה בטעינת סיכומים: " + message);
                }
            });
        } else {
            for (Summary summary : originalList) {
                if (summary.getSummaryTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(summary);
                }
            }

            adapter.clear();
            adapter.addAll(filteredList);
            adapter.notifyDataSetChanged();
        }
    }
}