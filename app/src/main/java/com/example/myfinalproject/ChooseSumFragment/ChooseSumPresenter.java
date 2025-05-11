package com.example.myfinalproject.ChooseSumFragment;

import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.Repositories.SummaryRepository;
import com.example.myfinalproject.DataModels.Summary;

import java.util.ArrayList;
import java.util.List;

public class ChooseSumPresenter {
    private final ChooseSumFragment view;
    private final SummaryRepository summaryDb;

    public ChooseSumPresenter(ChooseSumFragment view) {
        this.view = view;
        this.summaryDb = new SummaryRepository();
    }



    public void loadSummaries(SummariesCallback callback, String selectedClass, String selectedProfession) {
        summaryDb.getAllSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                if (selectedClass == null && selectedProfession == null) {
                    callback.onSuccess(summaries);
                    return;
                }

                List<Summary> filteredSummaries = new ArrayList<>();

                for (Summary summary : summaries) {
                    boolean classMatch = selectedClass == null ||
                            (summary.getClassOption() != null && summary.getClassOption().equals(selectedClass));

                    boolean professionMatch = selectedProfession == null ||
                            (summary.getProfession() != null && summary.getProfession().equals(selectedProfession));

                    if (classMatch && professionMatch) {
                        filteredSummaries.add(summary);
                    }
                }

                callback.onSuccess(filteredSummaries);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }



    public List<Summary> filterSummariesByTitle(List<Summary> fullList, String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>(fullList);
        }

        List<Summary> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim();
        for (Summary summary : fullList) {
            if (summary.getSummaryTitle().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(summary);
            }
        }
        return filteredList;
    }
}