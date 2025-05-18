package com.example.myfinalproject.SumReviewFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.Adapters.ReviewAdapter;
import com.example.myfinalproject.CallBacks.ReviewCallback;
import com.example.myfinalproject.DataModels.Review;
import com.example.myfinalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;

public class SumReviewFragment extends Fragment implements ReviewCallback {

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private TextView tvReviewsCount, tvName;

    private EditText etReviewText;
    private RatingBar ratingBar;
    private Button btnSubmitReview;
    private String summaryId, currentUserId, currentUserName, existingReviewId = null;
    private float initialRating;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean hasReviewed = false;
    private ListenerRegistration reviewsListener;

    public static SumReviewFragment newInstance(String summaryId) {
        SumReviewFragment fragment = new SumReviewFragment();
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
            initialRating = getArguments().getFloat("rating", 0f);

        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }


        reviewList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sum_review, container, false);

        tvReviewsCount = view.findViewById(R.id.tvReviewsCount);
        rvReviews = view.findViewById(R.id.rvReviews);
        tvName = view.findViewById(R.id.tvName);
        etReviewText = view.findViewById(R.id.etReviewText);
        ratingBar = view.findViewById(R.id.ratingBar);
        btnSubmitReview = view.findViewById(R.id.btnSubmitReview);




        if (initialRating > 0 && ratingBar != null) {
            ratingBar.setRating(initialRating);
        }


        reviewAdapter = new ReviewAdapter(reviewList, this);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(reviewAdapter);

        if (btnSubmitReview != null) {
            btnSubmitReview.setOnClickListener(v -> submitReview());
        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            tvName.setText("משתמש אנונימי");  // עדכון אם לא מחובר
            Toast.makeText(getContext(), "יש להתחבר תחילה", Toast.LENGTH_SHORT).show();
            return;
        } else {
            currentUserId = currentUser.getUid();
            fetchCurrentUserName();  // לוודא שהפונקציה נקראת
        }



        if (summaryId == null || summaryId.isEmpty()) {
            return;
        }
        if (reviewsListener != null) {
            reviewsListener.remove();
        }
        Query query = db.collection("reviews")
                .whereEqualTo("summaryId", summaryId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        reviewsListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                processReviewsSnapshot(value);
            }
        });

        checkIfUserReviewed();
    }


    @Override
    public void onPause() {
        super.onPause();

        if (reviewsListener != null) {
            reviewsListener.remove();
            reviewsListener = null;
        }
    }

    private void fetchCurrentUserName() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("userName");
                        if (currentUserName != null && !currentUserName.isEmpty()) {
                            tvName.setText(currentUserName);  // הצגת שם המשתמש
                        } else {
                            tvName.setText("משתמש");  // שם משתמש ברירת מחדל
                        }
                    } else {
                        tvName.setText("משתמש");  // שם משתמש ברירת מחדל
                    }
                })
                .addOnFailureListener(e -> {
                    tvName.setText("משתמש");  // שם משתמש ברירת מחדל
                });
    }





    private void processReviewsSnapshot(QuerySnapshot snapshot) {
        reviewList.clear();

        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Review review = document.toObject(Review.class);
            if (review != null) {
                review.setReviewId(document.getId());
                reviewList.add(review);
            }
        }

        int reviewCount = reviewList.size();

        if (tvReviewsCount != null) {
            tvReviewsCount.setText("(" + reviewCount + ")");
        }

        if (reviewAdapter != null) {
            reviewAdapter.notifyDataSetChanged();
        }

        updateAverageRating();
    }


    private void checkIfUserReviewed() {
        if (currentUserId == null || summaryId == null) {
            hasReviewed = false;
            enableReviewForm();
            return;
        }


        db.collection("reviews")
                .whereEqualTo("summaryId", summaryId)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hasReviewed = !queryDocumentSnapshots.isEmpty();

                    if (hasReviewed) {


                        if (getActivity() != null && !getActivity().isFinishing()) {
                            Toast.makeText(getContext(), "כבר כתבת ביקורת לסיכום זה", Toast.LENGTH_SHORT).show();
                        }


                        disableReviewForm();


                        if (!queryDocumentSnapshots.isEmpty()) {
                            Review userReview = queryDocumentSnapshots.getDocuments().get(0).toObject(Review.class);
                            existingReviewId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            if (userReview != null) {
                                etReviewText.setText(userReview.getReviewText());
                                ratingBar.setRating(userReview.getRating());
                            }
                        }
                    } else {

                        enableReviewForm();
                    }
                })
                .addOnFailureListener(e -> {

                    hasReviewed = false;
                    enableReviewForm();
                });
    }

    private void enableReviewForm() {
        if (etReviewText != null) etReviewText.setEnabled(true);
        if (ratingBar != null) ratingBar.setEnabled(true);
        if (btnSubmitReview != null) btnSubmitReview.setEnabled(true);
    }

    private void disableReviewForm() {
        if (etReviewText != null) etReviewText.setEnabled(false);
        if (ratingBar != null) ratingBar.setEnabled(false);
        if (btnSubmitReview != null) btnSubmitReview.setEnabled(false);
    }

    private void submitReview() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "יש להתחבר תחילה", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hasReviewed) {
            Toast.makeText(getContext(), "כבר כתבת ביקורת לסיכום זה", Toast.LENGTH_SHORT).show();
            return;
        }

        if (summaryId == null || summaryId.isEmpty()) {
            Toast.makeText(getContext(), "מזהה סיכום חסר", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = etReviewText.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (rating == 0) {
            Toast.makeText(getContext(), "אנא דרגו בין 1-5 כוכבים", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reviewText.isEmpty()) {
            Toast.makeText(getContext(), "נא להזין טקסט לביקורת", Toast.LENGTH_SHORT).show();
            return;
        }

        Review newReview = new Review(currentUserId, currentUserName, summaryId, reviewText, rating);


        db.collection("reviews")
                .add(newReview)
                .addOnSuccessListener(documentReference -> {
                    String reviewId = documentReference.getId();


                    etReviewText.setText("");
                    ratingBar.setRating(0);


                    hasReviewed = true;
                    existingReviewId = reviewId;
                    disableReviewForm();

                    Toast.makeText(getContext(), "תודה על הביקורת שלך!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בשליחת הביקורת: " + e.getMessage(), Toast.LENGTH_SHORT).show();});
    }

    private void updateAverageRating() {
        float averageRating = calculateAverageRating();


        if (summaryId != null && !summaryId.isEmpty()) {
            DocumentReference summaryRef = db.collection("summaries").document(summaryId);
            summaryRef.update("averageRating", averageRating);
        }
    }

    @Override
    public void onDeleteReview(Review review, int position) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("מחיקת ביקורת")
                .setMessage("האם למחוק את הביקורת?")
                .setPositiveButton("מחיקה", (dialog, which) -> {
                    if (review.getReviewId() != null) {
                        db.collection("reviews").document(review.getReviewId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {


                                    reviewList.remove(position);
                                    reviewAdapter.notifyItemRemoved(position);

                                    updateAverageRating();

                                    if (currentUserId != null && currentUserId.equals(review.getUserId())) {
                                        hasReviewed = false;
                                        existingReviewId = null;
                                        enableReviewForm();
                                        etReviewText.setText("");
                                        ratingBar.setRating(0);
                                    }

                                    Toast.makeText(getContext(), "הביקורת נמחקה", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "שגיאה במחיקת הביקורת", Toast.LENGTH_SHORT).show();

                                });
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    @Override
    public void onEditReview(Review review, int position) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "עריכת הביקורת שלך", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveChanges(Review review, int position, String newText, float newRating) {
        if (review.getReviewId() == null) {
            Toast.makeText(getContext(), "מזהה ביקורת חסר", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newText.trim().isEmpty()) {
            Toast.makeText(getContext(), "תוכן הביקורת לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newRating == 0) {
            Toast.makeText(getContext(), "אנא דרגו בין 1-5 כוכבים", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference reviewRef = db.collection("reviews").document(review.getReviewId());

        reviewRef.update(
                "reviewText", newText,
                "rating", newRating
        ).addOnSuccessListener(aVoid -> {

            review.setReviewText(newText);
            review.setRating(newRating);
            reviewAdapter.notifyItemChanged(position);

            updateAverageRating();

            Toast.makeText(getContext(), "הביקורת עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "שגיאה בעדכון הביקורת: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    public float calculateAverageRating() {
        if (reviewList.isEmpty()) {
            return 0;
        }

        float sum = 0;
        for (Review review : reviewList) {
            sum += review.getRating();
        }

        return sum / reviewList.size();
    }
}