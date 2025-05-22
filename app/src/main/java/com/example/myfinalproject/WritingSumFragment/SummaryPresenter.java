package com.example.myfinalproject.WritingSumFragment;

import android.util.Log;
import android.widget.Toast;
import com.example.myfinalproject.CallBacks.AddSummaryCallback;
import com.example.myfinalproject.CallBacks.SummaryCallback;
import com.example.myfinalproject.DataModels.Summary;
import com.example.myfinalproject.Repositories.SummaryRepository;
import com.example.myfinalproject.SumFragment.SumFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class SummaryPresenter {
    private WritingSumFragment view;
    private SummaryRepository summaryDb;

    public SummaryPresenter(WritingSumFragment view) {
        this.view = view;
        this.summaryDb = new SummaryRepository();

    }


        public void submitSummaryClicked(Summary summary) {
        if (view.getContext() == null) return;

        summaryDb.addSummary(summary, new AddSummaryCallback() {
            @Override
            public void onSummaryAdd(Summary summary) {
                Toast.makeText(view.getContext(), "הסיכום הוזן בהצלחה", Toast.LENGTH_SHORT).show();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(summary.getUserId());

                userRef.update("sumCount", FieldValue.increment(1))
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Summary", "מונה סיכומים עודכן");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Summary", "שגיאה בעדכון מונה סיכומים", e);
                        });


                if (view.getActivity() != null) {
                    view.getActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(view.getContext(), "הייתה שגיאה בהזנת הסיכום: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}