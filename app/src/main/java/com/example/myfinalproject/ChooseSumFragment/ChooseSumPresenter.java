package com.example.myfinalproject.ChooseSumFragment;

import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.Repositories.SummaryRepository;
import com.example.myfinalproject.DataModels.Summary;

import java.util.ArrayList;
import java.util.List;

public class ChooseSumPresenter {
    private final SummaryRepository summaryDb; // אובייקט שאחראי לגשת למסד הנתונים

    public ChooseSumPresenter() {
        this.summaryDb = new SummaryRepository();
    }

    /**
     * טוען את כל הסיכומים מה־Repository, ואם נבחרו כיתה ומקצוע – מסנן לפיהם.
     *
     * @param callback           קריאה חוזרת עם תוצאה
     */
    public void loadSummaries(SummariesCallback callback, String selectedClass, String selectedProfession) {
        summaryDb.getAllSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {

                // אם אין סינון – החזר את כל הסיכומים
                if (selectedClass == null && selectedProfession == null) {
                    callback.onSuccess(summaries);
                    return;
                }

                // מסנן את הסיכומים לפי הכיתה והמקצוע שנבחרו
                List<Summary> filteredSummaries = new ArrayList<>();

                for (Summary summary : summaries) {
                    // בדיקה אם הסיכום מתאים לכיתה שנבחרה
                    boolean classMatch = selectedClass == null ||
                            (summary.getClassOption() != null && summary.getClassOption().equals(selectedClass));

                    // בדיקה אם הסיכום מתאים למקצוע שנבחר
                    boolean professionMatch = selectedProfession == null ||
                            (summary.getProfession() != null && summary.getProfession().equals(selectedProfession));

                    // אם מתאים לשני התנאים – מוסיפים לרשימה
                    if (classMatch && professionMatch) {
                        filteredSummaries.add(summary);
                    }
                }
                // מחזיר את הרשימה המסוננת לקריאה החוזרת
                callback.onSuccess(filteredSummaries);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }


    /**
     * מסנן רשימת סיכומים לפי נושא – משמש לחיפוש.
     *
     * @param fullList רשימת כל הסיכומים
     * @param query    טקסט לחיפוש
     * @return רשימת סיכומים שהנושא שלהם כולל את הטקסט
     */

    public List<Summary> filterSummariesByTitle(List<Summary> fullList, String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>(fullList); // אם אין טקסט לחיפוש – מחזיר את כל הרשימה
        }

        List<Summary> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase().trim(); // המרה לאותיות קטנות כדי לאפשר חיפוש לא תלוי רישיות
        for (Summary summary : fullList) {
            if (summary.getSummaryTitle().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(summary); // הוספה אם הנושא מכיל את מחרוזת החיפוש
            }
        }
        return filteredList;
    }
}