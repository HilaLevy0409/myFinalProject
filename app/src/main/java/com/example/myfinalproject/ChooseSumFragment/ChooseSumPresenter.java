package com.example.myfinalproject.ChooseSumFragment;

import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.CallBacks.SummaryCallback;
import com.example.myfinalproject.Database.SummaryDatabase;
import com.example.myfinalproject.Models.Summary;

import java.util.ArrayList;
import java.util.List;

public class ChooseSumPresenter {
    private final ChooseSumFragment view;
    private final SummaryDatabase summaryDatabase;

    public ChooseSumPresenter(ChooseSumFragment view) {
        this.view = view;
        this.summaryDatabase = new SummaryDatabase();
    }

    public void loadSummaries(SummariesCallback callback) {
        loadSummaries(callback, null, null);
    }

    // Overloaded method with filtering
    public void loadSummaries(SummariesCallback callback, String selectedClass, String selectedProfession) {
        summaryDatabase.getAllSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                // If both filters are null, return all summaries
                if (selectedClass == null && selectedProfession == null) {
                    callback.onSuccess(summaries);
                    return;
                }

                // Filter summaries based on selected class and profession
                List<Summary> filteredSummaries = new ArrayList<>();

                for (Summary summary : summaries) {
                    boolean classMatch = selectedClass == null ||
                            (summary.getClassOption() != null && summary.getClassOption().equals(selectedClass));

                    boolean professionMatch = selectedProfession == null ||
                            (summary.getProfession() != null && summary.getProfession().equals(selectedProfession));

                    // Add summaries that match the provided filters
                    if (classMatch && professionMatch) {
                        filteredSummaries.add(summary);
                    }
                }

                // Pass the filtered list back through the callback
                callback.onSuccess(filteredSummaries);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void updateSummary(Summary summary) {
        summaryDatabase.updateSummary(summary, new SummaryCallback() {
            @Override
            public void onSuccess(Summary summary1) {
           //     view.onSummaryUpdated(summary1);
            }

            @Override
            public void onError(String message) {
                view.onError(message);
            }
        });
    }

    public void deleteSummary(String summaryId) {
        summaryDatabase.deleteSummary(summaryId, new SummaryCallback() {
            @Override
            public void onSuccess(Summary summary1) {

            }

            @Override
            public void onError(String message) {
                view.onError(message);
            }
        });
    }

    public void filterSummaries(String query, ArrayList<Summary> originalList, SummaryAdapter adapter) {
        ArrayList<Summary> filteredList = new ArrayList<>();

        for (Summary summary : originalList) {
            if (summary.getSummaryTitle().toLowerCase().contains(query.toLowerCase()) ||
                    summary.getProfession().toLowerCase().contains(query.toLowerCase()) ||
                    summary.getClassOption().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(summary);
            }
        }

        adapter.updateSummaries(filteredList);
    }
}