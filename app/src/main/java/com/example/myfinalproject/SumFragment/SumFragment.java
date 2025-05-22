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

    private ImageButton ImgBtnDeleteSum;
    private boolean isAuthor = false;

    private boolean isAdmin = false;


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
        ImgBtnDeleteSum.setOnClickListener(this);
        ImgBtnDeleteSum.setVisibility(View.GONE);

        initTextToSpeech();
        speedControl();

        if (getArguments() != null && getArguments().getBoolean("fromFavorites", false)) {
            isFavorite = true;
            updateFavoriteButton();
        }

        isAdmin = Admin.isAdminLoggedIn();
        checkIfUserIsAdmin();

        loadSummaryData();
        checkIfFavorite();

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

    private void checkIfUserIsAdmin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        if (Admin.isAdminLoggedIn()) {
            isAdmin = true;
            return;
        }

        String currentUserId = currentUser.getUid();
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean adminValue = documentSnapshot.getBoolean("isAdmin");
                        isAdmin = adminValue != null && adminValue;

                        updateDeleteButtonVisibility();
                    }
                })
                .addOnFailureListener(e -> {
                    isAdmin = false;
                });
    }


    private void updateDeleteButtonVisibility() {
        if (isAuthor || isAdmin) {
            ImgBtnDeleteSum.setVisibility(View.VISIBLE);
        } else {
            ImgBtnDeleteSum.setVisibility(View.GONE);
        }
    }


    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = textToSpeech.setLanguage(new Locale("he", "IL"));

                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                        langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getContext(), "שפה עברית אינה נתמכת במכשיר זה", Toast.LENGTH_SHORT).show();

                } else {
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    isSpeaking = true;
                                    updateButtons();
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

    private void speedControl() {
        seekBarSpeed.setMax(20);
        seekBarSpeed.setProgress(10);
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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

    private void speakText() {
        if (textToSpeech == null) return;

        String text = tvText.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(getContext(), "אין טקסט להקראה", Toast.LENGTH_SHORT).show();
            return;
        }
        btnStart.setEnabled(false);
        btnReset.setEnabled(true);

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "fullText");
        isSpeaking = true;
    }

    private void resetSpeech() {
        if (textToSpeech == null) return;
        textToSpeech.stop();
        isSpeaking = false;
        btnStart.setEnabled(true);
        btnReset.setEnabled(false);
    }

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


    private void loadSummaryData() {
        if (summaryId == null || summaryId.isEmpty()) {
            return;
        }
        db.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String authorId = documentSnapshot.getString("userId");
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (currentUser != null && authorId != null &&
                                authorId.equals(currentUser.getUid())) {
                            isAuthor = true;
                        } else {
                            isAuthor = false;
                        }

                        updateDeleteButtonVisibility();

                        displaySummaryData(documentSnapshot);
                    } else {
                        Toast.makeText(getContext(), "הסיכום לא נמצא", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת הסיכום", Toast.LENGTH_SHORT).show();
                });
    }

    private void displaySummaryData(DocumentSnapshot document) {
        String topic = document.getString("summaryTitle");
        if (topic != null) {
            tvTopic.setText(topic);
        }

        String authorName = document.getString("userName");
        if (authorName != null && !authorName.isEmpty()) {
            tvAuthor.setText("נכתב על ידי: " + authorName);
        } else {
            String authorId = document.getString("userId");

            if (authorId != null) {
                db.collection("users").document(authorId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                String userName = userDoc.getString("userName");
                                if (userName != null && !userName.isEmpty()) {
                                    tvAuthor.setText("נכתב על ידי: " + userName);
                                } else {
                                    tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                                }
                            } else {
                                tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                            }
                        })
                        .addOnFailureListener(e -> {
                            tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                        });
            } else {
                tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
            }
        }

        Double averageRating = document.getDouble("averageRating");
        if (averageRating != null) {
            ratingBarSum.setRating(averageRating.floatValue());
            tvAverage.setText("ציון ממוצע לסיכום: " + String.format("%.1f", averageRating));
        }

        String summaryContent = document.getString("summaryContent");
        String imageData = document.getString("image");

        if (imageData != null && !imageData.isEmpty()) {
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
            tvText.setVisibility(View.VISIBLE);
            sumImage.setVisibility(View.GONE);
            tvText.setText(summaryContent);
        } else {
            tvText.setVisibility(View.VISIBLE);
            sumImage.setVisibility(View.GONE);
            tvText.setText("אין תוכן זמין");
        }
        updateButtons();
    }



    private void deleteSummary() {
        if (!isAuthor && !isAdmin) {
            Toast.makeText(getContext(), "רק יוצר הסיכום או ההנהלה יכולים למחוק את הסיכום", Toast.LENGTH_SHORT).show();
            return;
        }

        String dialogTitle = isAdmin && !isAuthor ?
                "מחיקת סיכום (הנהלה)" : "מחיקת סיכום";

        String deleteMessage = isAdmin && !isAuthor ?
                "האם ברצונך למחוק סיכום זה? פעולה זו תתבצע כהנהלה." :
                "האם ברצונך למחוק את הסיכום?";

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(dialogTitle)
                .setMessage(deleteMessage)
                .setPositiveButton("כן", (dialog, which) -> {
                    db.collection("summaries").document(summaryId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String userId = documentSnapshot.getString("userId");

                                    db.collection("summaries").document(summaryId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                String successMessage = isAdmin && !isAuthor ?
                                                        "הסיכום נמחק על ידי ההנהלה" :
                                                        "הסיכום נמחק בהצלחה";

                                                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();

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

    private void updateUserSummaryCount(String userId) {

        if (userId == null) {
            Log.e("SumFragment", "userId is null. Cannot update summary count.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("summaries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    db.collection("users").document(userId)
                            .update("sumCount", count)
                            .addOnFailureListener(e ->
                                    Log.e("SumByUserFragment", "שגיאה בעדכון sumCount", e));
                });
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlDisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e("", "שגיאה בהורדת התמונה", e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null && imageView != null) {
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(R.drawable.newlogo);
            }
        }
    }

    private void checkIfFavorite() {
        if (mAuth.getCurrentUser() == null || summaryId == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFavorite = documentSnapshot.exists();
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

    private void toggleFavorite() {
        if (mAuth.getCurrentUser() == null || summaryId == null) {
            Toast.makeText(getContext(), "יש להתחבר כדי לשמור למועדפים", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference favRef = db.collection("users").document(userId)
                .collection("favorites").document(summaryId);

        if (isFavorite) {
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
        }else if (view == ImgBtnDeleteSum) {
            deleteSummary();
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        isAdmin = Admin.isAdminLoggedIn();
        checkIfUserIsAdmin();

        updateDeleteButtonVisibility();
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