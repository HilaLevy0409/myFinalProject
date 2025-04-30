package com.example.myfinalproject.SumFragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import com.example.myfinalproject.R;

import com.example.myfinalproject.ReportFragment.ReportFragment;
import com.example.myfinalproject.SumReviewFragment.SumReviewFragment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
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

    private static final String TAG = "SumFragment";

    private Button btnReport, btnSaveSummary, btnStart, btnStopContinue, btnReset;
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
    private String currentText = "";
    private FloatingActionButton fabExport;

    private int currentPosition = 0;
    private static final int PARAGRAPH_LENGTH = 200;

    private ImageButton ImgBtnDeleteSum;
    private boolean isAuthor = false;

    public SumFragment() {
    }

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
        btnStopContinue = view.findViewById(R.id.btnStopContinue);
        btnReset = view.findViewById(R.id.btnReset);

        seekBarSpeed = view.findViewById(R.id.seekBarSpeed);

        btnReport.setOnClickListener(this);
        btnSaveSummary.setOnClickListener(this);
        fabExport.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnStopContinue.setOnClickListener(this);
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

        loadSummaryData();
        checkIfFavorite();

//        ratingBarSum.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
//
//            if (fromUser) {
//                getActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.flFragment, new SumReviewFragment())
//                        .addToBackStack(null)
//                        .commit();
//            }
//        });

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
                                    if (utteranceId.equals("endOfText")) {
                                        isSpeaking = false;
                                        currentPosition = 0;
                                        updateButtons();
                                    } else {
                                        int chunkIndex = Integer.parseInt(utteranceId.replace("chunk_", ""));
                                        currentPosition = Math.min(chunkIndex * PARAGRAPH_LENGTH, currentText.length());

                                        if (isSpeaking && currentPosition < currentText.length()) {
                                            speakFromCurrentPosition();
                                        } else {
                                            isSpeaking = false;
                                            updateButtons();
                                        }
                                    }
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

        if (currentText.isEmpty() && tvText.getVisibility() == View.VISIBLE) {
            currentText = tvText.getText().toString();
        }

        if (currentText.isEmpty()) {
            Toast.makeText(getContext(), "אין טקסט להקראה", Toast.LENGTH_SHORT).show();
            return;
        }

        btnStart.setEnabled(false);
        btnStopContinue.setEnabled(true);
        btnReset.setEnabled(true);

        speakFromCurrentPosition();
        isSpeaking = true;
        btnStopContinue.setText("עצור");
    }

    private void speakFromCurrentPosition() {
        if (currentPosition >= currentText.length()) {
            currentPosition = 0;
        }

        String textToRead;
        if (currentPosition + PARAGRAPH_LENGTH >= currentText.length()) {
            textToRead = currentText.substring(currentPosition);

            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "endOfText");
            textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, params);
        } else {
            textToRead = currentText.substring(currentPosition, currentPosition + PARAGRAPH_LENGTH);

            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "chunk_" + (currentPosition + PARAGRAPH_LENGTH) / PARAGRAPH_LENGTH);
            textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    private void restartSpeech() {
        if (textToSpeech == null) return;

        textToSpeech.stop();
        currentPosition = 0;
        isSpeaking = false;
        btnStopContinue.setText("עצור");

        if (currentText.isEmpty() && tvText.getVisibility() == View.VISIBLE) {
            currentText = tvText.getText().toString();
        }

        if (currentText.isEmpty()) {
            Toast.makeText(getContext(), "אין טקסט להקראה", Toast.LENGTH_SHORT).show();
            return;
        }

        speakFromCurrentPosition();
        isSpeaking = true;
        updateButtons();
    }

    private void resetSpeech() {
        if (textToSpeech == null) return;

        textToSpeech.stop();
        currentPosition = 0;
        isSpeaking = false;
        btnStopContinue.setText("עצור");

        btnStart.setEnabled(true);
        btnStopContinue.setEnabled(false);
        btnReset.setEnabled(false);
    }

    private void updateButtons() {
        boolean hasText = currentText != null && !currentText.isEmpty();

        if (isSpeaking) {
            btnStart.setEnabled(false);
            btnStopContinue.setEnabled(true);
            btnReset.setEnabled(true);
        } else {
            if (currentPosition == 0) {
                btnStart.setEnabled(hasText);
                btnStopContinue.setEnabled(false);
                btnReset.setEnabled(false);
            }
        }
    }

    private void loadSummaryData() {
        if (summaryId == null || summaryId.isEmpty()) {
            Log.e(TAG, "Summary ID is null or empty");
            return;
        }

        db.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        displaySummaryData(documentSnapshot);
                    } else {
                        Log.e(TAG, "No such document");
                        Toast.makeText(getContext(), "הסיכום לא נמצא", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting document", e);
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
            Log.d(TAG, "Author name set from summary: " + authorName);
        } else {
            // Fallback to getting username from user document if not in summary
            String authorId = document.getString("userId");
            if (authorId != null) {
                db.collection("users").document(authorId)
                        .get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                String userName = userDoc.getString("userName");
                                if (userName != null && !userName.isEmpty()) {
                                    tvAuthor.setText("נכתב על ידי: " + userName);
                                    Log.d(TAG, "Author name set from user document: " + userName);
                                } else {
                                    tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                                    Log.d(TAG, "Username not found in user document");
                                }

                                // Check if current user is the author
                                if (mAuth.getCurrentUser() != null &&
                                        authorId.equals(mAuth.getCurrentUser().getUid())) {
                                    isAuthor = true;
                                    ImgBtnDeleteSum.setVisibility(View.VISIBLE);
                                } else {
                                    isAuthor = false;
                                    ImgBtnDeleteSum.setVisibility(View.GONE);
                                }
                            } else {
                                tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                                Log.d(TAG, "User document not found for ID: " + authorId);
                            }
                        })
                        .addOnFailureListener(e -> {
                            tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                            Log.e(TAG, "Error fetching user document", e);
                        });
            } else {
                tvAuthor.setText("נכתב על ידי: משתמש לא ידוע");
                Log.d(TAG, "No author ID in summary document");
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
            currentText = "";
            currentPosition = 0;

            try {
                Log.d(TAG, "Loading Base64 image");
                byte[] decodedString = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (bitmap != null) {
                    sumImage.setImageBitmap(bitmap);
                    Log.d(TAG, "Base64 image set successfully");
                } else {
                    Log.d(TAG, "Failed to decode bitmap from Base64");
                    sumImage.setImageResource(R.drawable.newlogo);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling image", e);
                sumImage.setImageResource(R.drawable.newlogo);
            }
        } else if (summaryContent != null && !summaryContent.isEmpty()) {
            tvText.setVisibility(View.VISIBLE);
            sumImage.setVisibility(View.GONE);
            tvText.setText(summaryContent);
            currentText = summaryContent;
            currentPosition = 0;
        } else {
            tvText.setVisibility(View.VISIBLE);
            sumImage.setVisibility(View.GONE);
            tvText.setText("אין תוכן זמין");
            currentText = "";
            currentPosition = 0;
        }

        updateButtons();


    }

    private void deleteSummary() {
        if (!isAuthor) {
            Toast.makeText(getContext(), "רק יוצר הסיכום יכול למחוק אותו", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("מחיקת סיכום")
                .setMessage("האם ברצונך למחוק את הסיכום?")
                .setPositiveButton("כן", (dialog, which) -> {
                    db.collection("summaries").document(summaryId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "הסיכום נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting summary", e);
                                Toast.makeText(getContext(), "שגיאה במחיקת הסיכום", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("לא", null)
                .show();
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
                Log.e(TAG, "Error downloading image", e);
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
                    Log.e(TAG, "Error checking favorite status", e);
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

                        db.collection("summaries").document(summaryId)
                                .update("favoritesCount", FieldValue.increment(-1));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error removing from favorites", e);
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

                        db.collection("summaries").document(summaryId)
                                .update("favoritesCount", FieldValue.increment(1));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding to favorites", e);
                        Toast.makeText(getContext(), "שגיאה בהוספה למועדפים", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onClick(View view) {
        // This code should be in the onClick method of the SumFragment class
        // Update this part in the onClick method of the SumFragment class
        // Replace this part in the onClick method of SumFragment
        if (view == btnReport) {
            // Log the current summaryId
            android.util.Log.d("SumFragment", "Report clicked with summaryId: " + summaryId);

            // Create a new instance of ReportFragment
            ReportFragment reportFragment = new ReportFragment();

            // Create bundle to pass data
            Bundle args = new Bundle();

            // Pass the summary title as the reportedName
            args.putString("userName", tvTopic.getText().toString());

            // Make sure summaryId is valid before passing it
            if (summaryId != null && !summaryId.isEmpty()) {
                args.putString("summaryId", summaryId);
                // Add debug log
                android.util.Log.d("SumFragment", "Passing summaryId to report: " + summaryId);
            } else {
                android.util.Log.d("SumFragment", "Warning: No summaryId available to pass!");
            }

            reportFragment.setArguments(args);

            // Navigate to the report fragment
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.flFragment, reportFragment)
                    .addToBackStack(null)  // Add to back stack so user can go back
                    .commit();
        } else if (view == btnSaveSummary) {
            toggleFavorite();
        } else if (view == btnStart) {
            speakText();
        } else if (view == btnStopContinue) {
            if (isSpeaking) {
                textToSpeech.stop();
                isSpeaking = false;
                btnStopContinue.setText("המשך");
            } else {
                speakFromCurrentPosition();
                isSpeaking = true;
                btnStopContinue.setText("עצור");
                btnStart.setEnabled(false);
                btnReset.setEnabled(true);
            }
        } else if (view == fabExport) {
            shareSummary();
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

    private void shareSummary() {
        if (currentText != null && !currentText.isEmpty()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, currentText);
            startActivity(Intent.createChooser(shareIntent, "שיתוף סיכום באמצעות:"));
        } else {
            Toast.makeText(getContext(), "אין תוכן לשתף", Toast.LENGTH_SHORT).show();
        }
    }


}