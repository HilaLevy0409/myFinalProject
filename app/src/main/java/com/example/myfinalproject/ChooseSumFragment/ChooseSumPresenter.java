package com.example.myfinalproject.ChooseSumFragment;

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
        summaryDatabase.getAllSummaries(new SummariesCallback() {

            @Override
            public void onSuccess(List<Summary> summaries) {
                callback.onSuccess(summaries);
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