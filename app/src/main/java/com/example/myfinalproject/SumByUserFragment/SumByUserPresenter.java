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
    private static final String TAG = "SumByUserPresenter";
    private final SumByUserFragment view;
    private final String userName;
    private final CollectionReference summariesCollection;

    public SumByUserPresenter(SumByUserFragment view, String userName) {
        this.view = view;
        this.userName = userName;
        this.summariesCollection = FirebaseFirestore.getInstance().collection("summaries");
        Log.d(TAG, "Initialized presenter for username: " + userName);
    }

    public void loadUserSummaries(SummariesCallback callback) {
        Log.d(TAG, "Loading summaries for user: " + userName);

        summariesCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total summaries in collection: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String author = doc.getString("userName");
                        String title = doc.getString("summaryTitle");
                        Log.d(TAG, "Summary found - ID: " + doc.getId() +
                                ", Title: " + title +
                                ", Author: " + author);
                    }


                    summariesCollection.whereEqualTo("userName", userName)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    List<Summary> summaries = new ArrayList<>();
                                    Log.d(TAG, "Query completed for user '" + userName +
                                            "', result count: " + task.getResult().size());

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        try {
                                            Summary summary = document.toObject(Summary.class);
                                            summary.setSummaryId(document.getId());
                                            summaries.add(summary);
                                            Log.d(TAG, "Added summary: " + summary.getSummaryTitle());
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error converting document to Summary: " + e.getMessage());
                                        }
                                    }

                                    Log.d(TAG, "Returning " + summaries.size() + " summaries for user: " + userName);
                                    callback.onSuccess(summaries);
                                } else {
                                    Log.e(TAG, "Error loading summaries", task.getException());
                                    callback.onError(task.getException() != null ?
                                            task.getException().getMessage() :
                                            "שגיאה בטעינת סיכומים");
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting collection data", e);
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