package com.example.myfinalproject.WritingSumFragment;

import android.widget.Toast;
import com.example.myfinalproject.CallBacks.AddSummaryCallback;
import com.example.myfinalproject.CallBacks.SummaryCallback;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.Database.SummaryDatabase;

public class SummaryPresenter {
    private WritingSumFragment view;
    private SummaryDatabase summaryDb;


    Summary summary;



    public SummaryPresenter(WritingSumFragment view) {
        this.view = view;
        this.summaryDb = new SummaryDatabase();

    }

//    public void loadSummaries(SummaryDatabase.SummariesCallback callback) {
//        SummaryDatabase.loadSummaries(callback);
//    }
//
//
//    public void updateSummary(Summary summary) {
//        SummaryDatabase.updateSummary(summary, new SummaryDatabase.SummaryCallback() {
//            @Override
//            public void onSuccess(Summary summary) {
//                view.onSummaryUpdated(summary);
//            }
//
//
//            @Override
//            public void onError(String message) {
//                view.onError(message);
//            }
//        });
//    }
//
//
//
   public void deleteSummary(String summaryId) {
       summaryDb.deleteSummary(summaryId, new SummaryCallback() {
           @Override
           public void onSummaryReceived(Summary summary) {
               view.onSummaryDeleted();
           }

           @Override
            public void onError(String message) {
               view.onError(message);
            }
       });
    }
//
//
//
//
//    public void submitClicked(Summary summary) {
//     SummaryDatabase.addSummary(summary, new SummaryDatabase.SummaryCallback() {
//            @Override
//            public void onSuccess(Summary summary) {
//                // Product added successfully, update UI or display a message
//                view.onSuccess(summary);
//            }
//
//
//            public void onError(String message) {
//            }
//        });
//    }
//}



        public void submitSummaryClicked(Summary summary) {
        if (view.getContext() == null) return;

        summaryDb.addSummary(summary, new AddSummaryCallback() {
            @Override
            public void onSummaryAdd(Summary summary) {
                Toast.makeText(view.getContext(), "הסיכום הוזן בהצלחה", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(view.getContext(), "הייתה שגיאה בהזנת הסיכום: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}