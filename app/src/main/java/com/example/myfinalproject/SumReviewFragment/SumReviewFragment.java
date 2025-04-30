package com.example.myfinalproject.SumReviewFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.Adapters.ReviewAdapter;
import com.example.myfinalproject.Models.Review;
import com.example.myfinalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SumReviewFragment extends Fragment implements ReviewAdapter.ReviewInteractionListener {
    private static final String TAG = "SumReviewFragment";

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private TextView tvReviewsCount;

    private EditText etReviewText;
    private RatingBar ratingBar;
    private Button btnSubmitReview;
    private TextView tvName;
    private String summaryId;
    private float initialRating;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String currentUserName;
    private boolean hasReviewed = false;
    private String existingReviewId = null;

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
            Log.d(TAG, "SummaryId from arguments: " + summaryId);
            Log.d(TAG, "Initial rating: " + initialRating);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize review list
        reviewList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sum_review, container, false);

        try {
            // Find all views
            tvReviewsCount = view.findViewById(R.id.tvReviewsCount);
            rvReviews = view.findViewById(R.id.rvReviews);
            tvName = view.findViewById(R.id.tvName);
            etReviewText = view.findViewById(R.id.etReviewText);
            ratingBar = view.findViewById(R.id.ratingBar);
            btnSubmitReview = view.findViewById(R.id.btnSubmitReview);

            // Check if views are found
            if (tvReviewsCount == null) {
                Log.e(TAG, "tvReviewsCount not found in layout");
            }
            if (rvReviews == null) {
                Log.e(TAG, "rvReviews not found in layout");
            }

            // Set initial rating if provided
            if (initialRating > 0 && ratingBar != null) {
                ratingBar.setRating(initialRating);
            }

            // Set up the RecyclerView with empty adapter initially
            reviewAdapter = new ReviewAdapter(reviewList, this);
            rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
            rvReviews.setAdapter(reviewAdapter);

            // Set up the submit button
            btnSubmitReview.setOnClickListener(v -> submitReview());

        } catch (Exception e) {
            Log.e(TAG, "Error setting up views", e);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get current user in onResume to ensure auth state is current
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            Log.d(TAG, "Current user ID: " + currentUserId);
            fetchCurrentUserName();
        } else {
            Log.d(TAG, "No user is currently logged in");
            tvName.setText("משתמש אנונימי");
        }

        // Set up real-time listener for reviews
        setupReviewsListener();

        // Check if current user has already reviewed
        checkIfUserReviewed();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove listener when fragment is paused
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
                            tvName.setText(currentUserName);
                            Log.d(TAG, "Current user name: " + currentUserName);
                        } else {
                            tvName.setText("משתמש");
                            Log.d(TAG, "Username not found in user document");
                        }
                    } else {
                        tvName.setText("משתמש");
                        Log.d(TAG, "User document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    tvName.setText("משתמש");
                    Log.e(TAG, "Error fetching user document", e);
                });
    }

    private void setupReviewsListener() {
        if (summaryId == null || summaryId.isEmpty()) {
            Log.e(TAG, "Summary ID is missing, cannot set up reviews listener");
            return;
        }

        // Remove any existing listener
        if (reviewsListener != null) {
            reviewsListener.remove();
        }

        Log.d(TAG, "Setting up real-time listener for reviews of summary: " + summaryId);

        // Create a query for all reviews for this summary, ordered by most recent first
        Query query = db.collection("reviews")
                .whereEqualTo("summaryId", summaryId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // Set up a snapshot listener to get real-time updates
        reviewsListener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error listening for review changes", error);
                return;
            }

            if (value != null) {
                processReviewsSnapshot(value);
            }
        });
    }

    private void processReviewsSnapshot(QuerySnapshot snapshot) {
        // Clear existing list
        reviewList.clear();

        // Process each document
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            try {
                Review review = document.toObject(Review.class);
                if (review != null) {
                    review.setReviewId(document.getId());
                    reviewList.add(review);
                    Log.d(TAG, "Added/Updated review: " + review.getReviewId() + " by " + review.getUserName());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing review document", e);
            }
        }

        // Update UI
        int reviewCount = reviewList.size();
        Log.d(TAG, "Total reviews: " + reviewCount);

        if (tvReviewsCount != null) {
            tvReviewsCount.setText("(" + reviewCount + ")");
        }

        if (reviewAdapter != null) {
            reviewAdapter.notifyDataSetChanged();
        }

        // Update average rating
        updateAverageRating();
    }

    private void checkIfUserReviewed() {
        if (currentUserId == null || summaryId == null) {
            hasReviewed = false;
            Log.d(TAG, "Cannot check if user reviewed: user ID or summary ID is null");
            enableReviewForm();
            return;
        }

        Log.d(TAG, "Checking if user " + currentUserId + " has already reviewed summary " + summaryId);

        db.collection("reviews")
                .whereEqualTo("summaryId", summaryId)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hasReviewed = !queryDocumentSnapshots.isEmpty();

                    if (hasReviewed) {
                        // User has already reviewed this summary
                        Log.d(TAG, "User has already reviewed this summary");

                        if (getActivity() != null && !getActivity().isFinishing()) {
                            Toast.makeText(getContext(), "כבר כתבת ביקורת לסיכום זה", Toast.LENGTH_SHORT).show();
                        }

                        // Disable review submission
                        disableReviewForm();

                        // Show the user's existing review
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Review userReview = queryDocumentSnapshots.getDocuments().get(0).toObject(Review.class);
                            existingReviewId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            if (userReview != null) {
                                etReviewText.setText(userReview.getReviewText());
                                ratingBar.setRating(userReview.getRating());
                            }
                        }
                    } else {
                        // User has not reviewed yet
                        Log.d(TAG, "User has not reviewed this summary yet");
                        enableReviewForm();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking if user reviewed", e);
                    // Assume user hasn't reviewed on error
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

        // Create a new review object
        Review newReview = new Review(currentUserId, currentUserName, summaryId, reviewText, rating);

        // Add the review to Firestore
        db.collection("reviews")
                .add(newReview)
                .addOnSuccessListener(documentReference -> {
                    String reviewId = documentReference.getId();
                    Log.d(TAG, "Review added with ID: " + reviewId);

                    // Update UI via snapshot listener (no need to manually update)

                    // Clear the review form
                    etReviewText.setText("");
                    ratingBar.setRating(0);

                    // Mark that user has reviewed and disable the review form
                    hasReviewed = true;
                    existingReviewId = reviewId;
                    disableReviewForm();

                    Toast.makeText(getContext(), "תודה על הביקורת שלך!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בשליחת הביקורת: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding review", e);
                });
    }

    private void updateAverageRating() {
        float averageRating = calculateAverageRating();

        // Update the summary document with the new average rating
        if (summaryId != null && !summaryId.isEmpty()) {
            DocumentReference summaryRef = db.collection("summaries").document(summaryId);
            summaryRef.update("averageRating", averageRating)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Average rating updated to " + averageRating))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating average rating", e));
        }
    }

    @Override
    public void onDeleteReview(Review review, int position) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("מחיקת ביקורת")
                .setMessage("האם למחוק את הביקורת?")
                .setPositiveButton("מחק", (dialog, which) -> {
                    if (review.getReviewId() != null) {
                        db.collection("reviews").document(review.getReviewId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Review deleted: " + review.getReviewId());

                                    // If this was the current user's review, allow them to review again
                                    if (currentUserId != null && currentUserId.equals(review.getUserId())) {
                                        hasReviewed = false;
                                        existingReviewId = null;
                                        enableReviewForm();
                                        etReviewText.setText("");
                                        ratingBar.setRating(0);
                                    }

                                    // UI will update via snapshot listener
                                    Toast.makeText(getContext(), "הביקורת נמחקה", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "שגיאה במחיקת הביקורת", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error deleting review", e);
                                });
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    @Override
    public void onEditReview(Review review, int position) {
        // This method is called when the edit button is clicked
        // The editing UI is already handled in the adapter
        if (getContext() != null) {
            Toast.makeText(getContext(), "ערוך את הביקורת שלך", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveChanges(Review review, int position, String newText, float newRating) {
        if (review.getReviewId() == null) {
            Toast.makeText(getContext(), "מזהה ביקורת חסר", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input
        if (newText.trim().isEmpty()) {
            Toast.makeText(getContext(), "תוכן הביקורת לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newRating == 0) {
            Toast.makeText(getContext(), "אנא דרגו בין 1-5 כוכבים", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference reviewRef = db.collection("reviews").document(review.getReviewId());

        // Update the review in Firestore
        reviewRef.update(
                "reviewText", newText,
                "rating", newRating
        ).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Review updated successfully: " + review.getReviewId());

            // UI will update via snapshot listener
            Toast.makeText(getContext(), "הביקורת עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "שגיאה בעדכון הביקורת: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error updating review", e);
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