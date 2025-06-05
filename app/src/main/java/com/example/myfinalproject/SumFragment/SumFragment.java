package com.example.myfinalproject.SumFragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;

import com.example.myfinalproject.Admin;
import com.example.myfinalproject.R;

import com.example.myfinalproject.ReportFragment.ReportFragment;
import com.example.myfinalproject.SumReviewFragment.SumReviewFragment;
import com.example.myfinalproject.WritingSumFragment.WritingSumFragment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.SeekBar;
import java.util.Locale;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SumFragment extends Fragment implements View.OnClickListener {

    private Button btnReport, btnSaveSummary, btnStart, btnReset;
    private RatingBar ratingBarSum;
    private TextView tvText, tvTopic, tvAuthor, tvAverage;
    private ImageView sumImage;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String summaryId;
    private boolean isFavorite = false;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private SeekBar seekBarSpeed;
    private float speechRate = 1.0f;
    private FloatingActionButton fabExport;
    private ImageButton ImgBtnDeleteSum, ImgBtnEditSum;
    private boolean isAuthor = false;
    private boolean isAdmin = false;

    private DocumentSnapshot currentSummaryDocument;

    public SumFragment() {}

    public static SumFragment newInstance(String summaryId) {
        SumFragment fragment = new SumFragment();
        Bundle args = new Bundle();
        args.putString("summaryId", summaryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            summaryId = getArguments().getString("summaryId");
            float rating = getArguments().getFloat("rating", 0f);
        }
        // אתחול הפיירסטור ואימות המשתמש
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sum, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        btnReport = view.findViewById(R.id.btnReport);
        btnSaveSummary = view.findViewById(R.id.btnSaveSummary);
        ratingBarSum = view.findViewById(R.id.ratingBarSum);
        tvText = view.findViewById(R.id.tvText);
        tvTopic = view.findViewById(R.id.tvTopic);
        tvAuthor = view.findViewById(R.id.tvAuthor);
        tvAverage = view.findViewById(R.id.tvAverage);
        sumImage = view.findViewById(R.id.imgSum);
        fabExport = view.findViewById(R.id.fabExport);
        btnStart = view.findViewById(R.id.btnStart);
        btnReset = view.findViewById(R.id.btnReset);
        seekBarSpeed = view.findViewById(R.id.seekBarSpeed);
        btnReport.setOnClickListener(this);
        btnSaveSummary.setOnClickListener(this);
        fabExport.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        ImgBtnDeleteSum = view.findViewById(R.id.ImgBtnDeleteSum);
        ImgBtnEditSum = view.findViewById(R.id.ImgBtnEditSum);
        ImgBtnDeleteSum.setOnClickListener(this);
        ImgBtnEditSum.setOnClickListener(this);
        ImgBtnDeleteSum.setVisibility(View.GONE);
        ImgBtnEditSum.setVisibility(View.GONE);

        // אתחול מנוע ההקראה והגדרת בקרת מהירות הקראה
        initTextToSpeech();
        speedControl();

        // בדיקה אם הסיכום הגיע ממועדפים, לעדכן את המצב בהתאם
        if (getArguments() != null && getArguments().getBoolean("fromFavorites", false)) {
            isFavorite = true;
            updateFavoriteButton();
        }
        // בדיקה אם המשתמש מנהל והצגת כפתור המחיקה בהתאם
        isAdmin = Admin.isAdminLoggedIn();
        checkIfUserIsAdmin();

        loadSummaryData();
        checkIfFavorite();

        // מאזין לשינוי בדירוג - מעבר לפראגמנט של ביקורות עם הדירוג שנבחר
        ratingBarSum.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                SumReviewFragment reviewFragment = new SumReviewFragment();
                Bundle args = new Bundle();
                args.putString("summaryId", summaryId);
                args.putFloat("rating", rating);
                reviewFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.flFragment, reviewFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    // בודק אם המשתמש הנוכחי הוא מנהל
    private void checkIfUserIsAdmin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        // בדיקה אם משתמש הוא מנהל דרך מחלקת Admin (שיטה חיצונית)
        if (Admin.isAdminLoggedIn()) {
            isAdmin = true;
            return;
        }

        // במקרה שלא, בודק במסד הנתונים האם יש לשדה isAdmin ערך true
        String currentUserId = currentUser.getUid();
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean adminValue = documentSnapshot.getBoolean("isAdmin");
                        isAdmin = adminValue != null && adminValue;
                        updateActionButtonsVisibility();
                    }
                })
                .addOnFailureListener(e -> {
                    isAdmin = false;
                });
    }

    // עדכון מצב כפתורי העריכה והמחיקה בהתאם למי שמציג את הסיכום
    private void updateActionButtonsVisibility() {
        if (isAuthor) {
            ImgBtnEditSum.setVisibility(View.VISIBLE);
            ImgBtnDeleteSum.setVisibility(View.VISIBLE);
        } else if (isAdmin) {
            ImgBtnEditSum.setVisibility(View.GONE);
            ImgBtnDeleteSum.setVisibility(View.VISIBLE);
        } else {
            ImgBtnEditSum.setVisibility(View.GONE);
            ImgBtnDeleteSum.setVisibility(View.GONE);
        }
    }

    private void editSummary() {
        if (!isAuthor) {
            Toast.makeText(getContext(), "רק יוצר הסיכום יכול לערוך אותו", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentSummaryDocument == null) {
            Toast.makeText(getContext(), "שגיאה בטעינת נתוני הסיכום", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("summaryId", summaryId);
        args.putString("summaryTitle", currentSummaryDocument.getString("summaryTitle"));
        args.putString("summaryContent", currentSummaryDocument.getString("summaryContent"));
        args.putString("image", currentSummaryDocument.getString("image"));
        args.putString("selected_class", currentSummaryDocument.getString("classOption"));
        args.putString("selected_profession", currentSummaryDocument.getString("profession"));
        args.putBoolean("isEditMode", true);

        WritingSumFragment writingSumFragment = new WritingSumFragment();
        writingSumFragment.setArguments(args);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.flFragment, writingSumFragment)
                .addToBackStack(null)
                .commit();
    }

    // אתחול מנוע ההקראה בשפה העברית
    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = textToSpeech.setLanguage(new Locale("he", "IL"));

                // בדיקת תמיכה בשפה העברית
                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                        langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getContext(), "שפה עברית אינה נתמכת במכשיר זה", Toast.LENGTH_SHORT).show();
                } else {
                    // מאזין לאירועי הקראה: התחלה, סיום ושגיאה
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    isSpeaking = true;
                                    updateButtons(); // מעדכן את מצב הכפתורים
                                });
                            }
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    isSpeaking = false;
                                    updateButtons();
                                });
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    isSpeaking = false;
                                    updateButtons();
                                    Toast.makeText(getContext(), "שגיאה בהקראה", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(getContext(), "אתחול מנוע הקראה נכשל", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // הגדרת מהירות ההקראה לפי SeekBar
    private void speedControl() {
        seekBarSpeed.setMax(20);
        seekBarSpeed.setProgress(10); // מהירות ברירת מחדל 1.0f
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // המרה לערך בין 0.5 ל-2.0 לערך מהירות הדיבור
                speechRate = (float) progress / 10.0f;
                if (speechRate < 0.5f) speechRate = 0.5f;
                textToSpeech.setSpeechRate(speechRate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // הפעלת ההקראה של הטקסט המוצג
    private void speakText() {
        if (textToSpeech == null) return;

        String text = tvText.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(getContext(), "אין טקסט להקראה", Toast.LENGTH_SHORT).show();
            return;
        }
        btnStart.setEnabled(false);
        btnReset.setEnabled(true);

        // מפעיל הקראה עם מזהה "fullText"
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fullText");
        isSpeaking = true;
    }

    // עצירת ההקראה וניקוי מצב
    private void resetSpeech() {
        if (textToSpeech == null) return;
        textToSpeech.stop();
        isSpeaking = false;
        btnStart.setEnabled(true);
        btnReset.setEnabled(false);
    }

    // עדכון מצב הכפתורים Start ו-Reset לפי מצב ההקראה והאם יש טקסט להצגה
    private void updateButtons() {
        String text = tvText.getText().toString();
        boolean hasText = !text.isEmpty();

        if (isSpeaking) {
            btnStart.setEnabled(false);
            btnReset.setEnabled(true);
        } else {
            btnStart.setEnabled(hasText);
            btnReset.setEnabled(false);
        }
    }

    // טעינת נתוני הסיכום מ-Firestore והצגת המידע
    private void loadSummaryData() {
        if (summaryId == null || summaryId.isEmpty()) {
            return;
        }
        // קריאה למסמך סיכום לפי מזהה
        db.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentSummaryDocument = documentSnapshot;

                        String authorId = documentSnapshot.getString("userId");
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (currentUser != null && authorId != null &&
                                authorId.equals(currentUser.getUid())) {
                            isAuthor = true;
                        } else {
                            isAuthor = false;
                        }
                        updateActionButtonsVisibility();
                        displaySummaryData(documentSnapshot);
                    } else {
                        Toast.makeText(getContext(), "הסיכום לא נמצא", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת הסיכום", Toast.LENGTH_SHORT).show();
                });
    }

    // מציג את נתוני הסיכום במסך בהתאם למסמך שנשלף מ-Firestore
    private void displaySummaryData(DocumentSnapshot document) {
        //  נושא הסיכום והצגתו
        String topic = document.getString("summaryTitle");
        if (topic != null) {
            tvTopic.setText(topic);
        }

        Boolean isEdited = document.getBoolean("isEdited");
        if (isEdited != null && isEdited) {
            tvTopic.setText(topic + " (סיכום ערוך)");
        }

        // ניסיון לקרוא את שם המחבר מתוך המסמך (אם קיים)
        String authorName = document.getString("userName");
        if (authorName != null && !authorName.isEmpty()) {
            // הצגת שם המחבר אם קיים
            tvAuthor.setText("נכתב על ידי: " + authorName);
        } else {
            // אם שם המחבר לא קיים במסמך הסיכום, מנסים להביא אותו מהמסמך של המשתמש לפי userId
            String authorId = document.getString("userId");

            if (authorId != null) {
                db.collection("users").document(authorId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                // שליפת שם המשתמש מתוך מסמך המשתמשים
                                String userName = userDoc.getString("userName");
                                if (userName != null && !userName.isEmpty()) {
                                    tvAuthor.setText("נכתב על ידי: " + userName);
                                } else {
                                    // במקרה שהשם לא קיים, מציג ברירת מחדל
                                    tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                                }
                            } else {
                                // במקרה שהמסמך לא קיים
                                tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                            }
                        })
                        .addOnFailureListener(e -> {
                            tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                        });
            } else {
                // אין userId זמין - הצגת טקסט ברירת מחדל
                tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
            }
        }

        // שליפת דירוג ממוצע והצגתו
        Double averageRating = document.getDouble("averageRating");
        if (averageRating != null) {
            ratingBarSum.setRating(averageRating.floatValue());
            tvAverage.setText("ציון ממוצע לסיכום: " + String.format("%.1f", averageRating));
        }

        // קריאת תוכן הסיכום או תמונה אם קיימת
        String summaryContent = document.getString("summaryContent");
        String imageData = document.getString("image");

        if (imageData != null && !imageData.isEmpty()) {
            // אם יש תמונה, מציגים את התמונה ומסתירים את הטקסט
            tvText.setVisibility(View.GONE);
            sumImage.setVisibility(View.VISIBLE);

            try {
                byte[] decodedString = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (bitmap != null) {
                    sumImage.setImageBitmap(bitmap);
                } else {
                    sumImage.setImageResource(R.drawable.newlogo);
                }
            } catch (Exception e) {
                sumImage.setImageResource(R.drawable.newlogo);
            }
        } else if (summaryContent != null && !summaryContent.isEmpty()) {
            // אם אין תמונה, מציגים טקסט הסיכום ומסתירים את התמונה
            tvText.setVisibility(View.VISIBLE);
            sumImage.setVisibility(View.GONE);
            tvText.setText(summaryContent);
        } else {
            // במקרה שאין גם טקסט וגם תמונה - מציגים הודעה מתאימה
            tvText.setVisibility(View.VISIBLE);
            sumImage.setVisibility(View.GONE);
            tvText.setText("אין תוכן זמין");
        }
        updateButtons();
    }

    // פונקציה למחיקת סיכום, רק אם המשתמש הוא יוצר הסיכום או מנהל
    private void deleteSummary() {
        // בדיקה אם המשתמש מורשה למחוק (מחבר הסיכום או מנהל)
        if (!isAuthor && !isAdmin) {
            Toast.makeText(getContext(), "רק יוצר הסיכום או ההנהלה יכולים למחוק את הסיכום", Toast.LENGTH_SHORT).show();
            return;
        }

        // קביעת כותרת והודעה בדיאלוג בהתאם למי שמבצע את המחיקה
        String dialogTitle = isAdmin && !isAuthor ?
                "מחיקת סיכום (הנהלה)" : "מחיקת סיכום";

        String deleteMessage = isAdmin && !isAuthor ?
                "האם ברצונך למחוק סיכום זה? פעולה זו תתבצע כהנהלה." :
                "האם ברצונך למחוק את הסיכום?";

        // יצירת דיאלוג לאישור המחיקה
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(dialogTitle)
                .setMessage(deleteMessage)
                .setPositiveButton("כן", (dialog, which) -> {
                    // לאחר אישור, טוענים את מסמך הסיכום כדי לקבל userId לצורך עדכון מספר הסיכומים של המשתמש
                    db.collection("summaries").document(summaryId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String userId = documentSnapshot.getString("userId");

                                    // מחיקת הסיכום ממסד הנתונים
                                    db.collection("summaries").document(summaryId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                String successMessage = isAdmin && !isAuthor ?
                                                        "הסיכום נמחק על ידי ההנהלה" :
                                                        "הסיכום נמחק בהצלחה";

                                                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();

                                                // עדכון מספר הסיכומים של המשתמש
                                                updateUserSummaryCount(userId);

                                                if (getActivity() != null) {
                                                    getActivity().getSupportFragmentManager().popBackStack();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "שגיאה במחיקת הסיכום", Toast.LENGTH_SHORT).show();
                                            });

                                } else {
                                    Toast.makeText(getContext(), "הסיכום לא נמצא", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "שגיאה בטעינת הסיכום", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("לא", null)
                .show();
    }

    // פונקציה לעדכון מספר הסיכומים של משתמש (למשל לשימוש להצגת נתונים בפרופיל)
    private void updateUserSummaryCount(String userId) {
        if (userId == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // שאילתה למספר הסיכומים של המשתמש
        db.collection("summaries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    // עדכון שדה sumCount במסמך המשתמש עם המספר החדש
                    db.collection("users").document(userId)
                            .update("sumCount", count)
                            .addOnFailureListener(e ->
                                    Log.e("SumByUserFragment", "שגיאה בעדכון sumCount", e));
                });
    }

    // AsyncTask להורדת תמונה מהרשת והצגתה ב-ImageView
//    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        ImageView imageView;
//
//        public DownloadImageTask(ImageView imageView) {
//            this.imageView = imageView;
//        }
//
//        // הורדת התמונה ברקע (ב-thread נפרד)
//        @Override
//        protected Bitmap doInBackground(String... urls) {
//            String urlDisplay = urls[0];
//            Bitmap bitmap = null;
//            try {
//                URL url = new URL(urlDisplay);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setDoInput(true);
//                connection.connect();
//                InputStream input = connection.getInputStream();
//                bitmap = BitmapFactory.decodeStream(input);
//            } catch (IOException e) {
//                Log.e("", "שגיאה בהורדת התמונה", e);
//            }
//            return bitmap;
//        }
//
//        // לאחר סיום ההורדה, מציגים את התמונה או תמונת ברירת מחדל במקרה של כשלון
//        @Override
//        protected void onPostExecute(Bitmap result) {
//            if (result != null && imageView != null) {
//                imageView.setImageBitmap(result);
//            } else {
//                imageView.setImageResource(R.drawable.newlogo);
//            }
//        }
//    }

    // בדיקה האם הסיכום נמצא במועדפים של המשתמש הנוכחי
    private void checkIfFavorite() {
        // אם המשתמש לא מחובר או שאין מזהה לסיכום, יוצא מהפונקציה
        if (mAuth.getCurrentUser() == null || summaryId == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        // קורא למסמך של הסיכום במאגר המועדפים של המשתמש
        db.collection("users").document(userId)
                .collection("favorites").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // אם המסמך קיים – הסיכום נמצא במועדפים
                    isFavorite = documentSnapshot.exists();
                    // מעדכן את הטקסט של כפתור המועדפים בהתאם למצב
                    updateFavoriteButton();
                })
                .addOnFailureListener(e -> {
                    Log.e("", "שגיאה בבדיקת מצב מועדף", e);

                });
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            btnSaveSummary.setText("❤️הסרה ממועדפים❤️");
        } else {
            btnSaveSummary.setText("⭐שמירה במועדפים⭐");
        }
    }

    // משנה את מצב המועדף – מוסיף או מסיר את הסיכום מהמועדפים של המשתמש
    private void toggleFavorite() {
        // אם המשתמש לא מחובר או אין מזהה לסיכום, מראה הודעת שגיאה ומפסיק
        if (mAuth.getCurrentUser() == null || summaryId == null) {
            Toast.makeText(getContext(), "יש להתחבר כדי לשמור למועדפים", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference favRef = db.collection("users").document(userId)
                .collection("favorites").document(summaryId);

        if (isFavorite) {
            // אם הסיכום כבר במועדפים, מבצע מחיקה שלו מרשימת המועדפים
            favRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateFavoriteButton();
                        Toast.makeText(getContext(), "הוסר מהמועדפים", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "שגיאה בהסרה מהמועדפים", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // אם הסיכום לא במועדפים, מוסיף אותו לרשימת המועדפים עם תאריך הוספה
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("summaryId", summaryId);
            favorite.put("addedAt", FieldValue.serverTimestamp());

            favRef.set(favorite)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateFavoriteButton();
                        Toast.makeText(getContext(), "נוסף למועדפים", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "שגיאה בהוספה למועדפים", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btnReport) {
            ReportFragment reportFragment = new ReportFragment();
            Bundle args = new Bundle();
            args.putString("userName", tvTopic.getText().toString());
            if (summaryId != null && !summaryId.isEmpty()) {
                args.putString("summaryId", summaryId);
            }
            reportFragment.setArguments(args);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.flFragment, reportFragment)
                    .addToBackStack(null)
                    .commit();
        } else if (view == btnSaveSummary) {
            toggleFavorite();
        } else if (view == btnStart) {
            speakText();
        } else if (view == fabExport) {
            shareSummary(tvTopic.getText().toString());
        } else if(view == btnReset) {
            resetSpeech();
        } else if (view == ImgBtnDeleteSum) {
            deleteSummary();
        } else if (view == ImgBtnEditSum) {
            editSummary();
        }
    }

    // מחזיר משאבים של TextToSpeech כשמחלקת ה-Fragment נהרסת
    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    // מתבצע כשמסך ה-Fragment חוזר להיות פעיל (על המסך)
    @Override
    public void onResume() {
        super.onResume();
        // בודק אם המשתמש הנוכחי מנהל
        isAdmin = Admin.isAdminLoggedIn();
        checkIfUserIsAdmin();
        // מעדכן האם כפתורי הפעולה צריכים להיות גלויים לפי הרשאות
        updateActionButtonsVisibility();

        loadSummaryData();
    }

    private void shareSummary(String summaryTitle) {
        String summaryContent = tvText.getText().toString();

        if (summaryContent != null && !summaryContent.isEmpty()) {
            String shareContent = "נושא הסיכום: " + summaryTitle + "\n\n" + summaryContent;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
            startActivity(Intent.createChooser(shareIntent, "שיתוף סיכום באמצעות:"));
        } else {
            Toast.makeText(getContext(), "אין תוכן לשתף", Toast.LENGTH_SHORT).show();
        }
    }
}