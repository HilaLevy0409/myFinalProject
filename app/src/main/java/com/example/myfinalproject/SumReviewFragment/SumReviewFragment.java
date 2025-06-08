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
import com.example.myfinalproject.Admin;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String summaryCreatorId;
    private boolean isAdmin = false;

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
            // קבלת מזהה הסיכום ודרוג התחלתית אם נשלחו
            summaryId = getArguments().getString("summaryId");
            initialRating = getArguments().getFloat("rating", 0f);
        }
        // אתחול Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // קבלת מזהה המשתמש הנוכחי
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

        // אם נשלח דירוג כעבר - הגדר אותו ב-RatingBar
        if (initialRating > 0 && ratingBar != null) {
            ratingBar.setRating(initialRating);
        }
        // אתחול RecyclerView והמתאם
        reviewAdapter = new ReviewAdapter(reviewList, this);
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(reviewAdapter);

        if (btnSubmitReview != null) {
            btnSubmitReview.setOnClickListener(v -> submitReview());
        }

        loadReviews();

        return view;
    }

    // מתבצע כשמסך ה-Fragment חוזר להיות פעיל (על המסך)
    @Override
    public void onResume() {
        super.onResume();

        // בדיקה אם מחובר משתמש מנהל
        isAdmin = Admin.isAdminLoggedIn();
        if (reviewAdapter != null) {
            reviewAdapter.setIsAdmin(isAdmin);
        }
        // אם אין משתמש מחובר – חסום אפשרות כתיבה
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            tvName.setText("משתמש אנונימי");
            disableReviewForm();
            Toast.makeText(getContext(), "כדי לכתוב ביקורת, יש להתחבר תחילה", Toast.LENGTH_SHORT).show();
            loadReviews(); // טען ביקורות לצפייה בלבד
            return;
        } else {
            currentUserId = currentUser.getUid();
            fetchCurrentUserName(); // טען שם משתמש
            checkIfUserIsAdmin(); // בדוק אם המשתמש הוא מנהל
        }
        // טען ביקורות ובדוק אם המשתמש הוא כותב הסיכום
        loadReviews();
        checkIfUserIsSummaryCreator();
    }

    private void loadReviews() {
        if (summaryId == null || summaryId.isEmpty()) {
            return;
        }
        if (reviewsListener != null) {
            reviewsListener.remove();
        }
        // טען את כל הביקורות לסיכום הנוכחי
        Query query = db.collection("reviews")
                .whereEqualTo("summaryId", summaryId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // מאזין דינמי לשינויים
        reviewsListener = query.addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) processReviewsSnapshot(value);
        });
    }

    // בדיקה אם המשתמש הנוכחי הוא מנהל
    private void checkIfUserIsAdmin() {
        if (currentUserId == null) return;

        // אם המנהל מחובר דרך מחלקת Admin
        if (Admin.isAdminLoggedIn()) {
            isAdmin = true;
            if (reviewAdapter != null) {
                reviewAdapter.setIsAdmin(true);
            }
            return;
        }
        // בדיקה מול בסיס הנתונים אם המשתמש הוא מנהל
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean adminValue = documentSnapshot.getBoolean("isAdmin");
                        isAdmin = adminValue != null && adminValue;

                        if (reviewAdapter != null) {
                            reviewAdapter.setIsAdmin(isAdmin);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    isAdmin = false;
                    if (reviewAdapter != null) {
                        reviewAdapter.setIsAdmin(false);
                    }
                });
    }

    // הסרת ה־listener של הביקורות כאשר ה־Fragment נעצר
    @Override
    public void onPause() {
        super.onPause();
        if (reviewsListener != null) {
            reviewsListener.remove();
            reviewsListener = null;
        }
    }

    // הבאת שם המשתמש הנוכחי מה־Firebase
    private void fetchCurrentUserName() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("userName");
                        if (currentUserName != null && !currentUserName.isEmpty()) {
                            tvName.setText(currentUserName);
                        } else {
                            tvName.setText("משתמש");
                        }
                    } else {
                        tvName.setText("משתמש");
                    }
                })
                .addOnFailureListener(e -> {
                    tvName.setText("משתמש");
                });
    }

    // בדיקה אם המשתמש הנוכחי הוא יוצר הסיכום
    private void checkIfUserIsSummaryCreator() {
        if (summaryId == null || currentUserId == null) return;

        db.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        summaryCreatorId = documentSnapshot.getString("userId");

                        if (currentUserId.equals(summaryCreatorId)) {
                            disableReviewForm();
                            Toast.makeText(getContext(), "לא ניתן לכתוב ביקורת על סיכום שכתבת בעצמך", Toast.LENGTH_LONG).show();
                        } else {
                            checkIfUserReviewed();
                        }
                    } else {
                        Toast.makeText(getContext(), "הסיכום לא נמצא", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בבדיקת יוצר הסיכום", Toast.LENGTH_SHORT).show();
                });
    }

    // עיבוד התוצאות של הביקורות מתוך המסמך
    private void processReviewsSnapshot(QuerySnapshot snapshot) {
        reviewList.clear();

        boolean userAlreadyReviewed = false;
        Review userReview = null;
        // מעבר על כל הביקורות
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Review review = document.toObject(Review.class);
            if (review != null) {
                review.setReviewId(document.getId());
                reviewList.add(review);

                // בדיקה אם המשתמש כבר כתב ביקורת
                if (currentUserId != null && currentUserId.equals(review.getUserId())) {
                    userAlreadyReviewed = true;
                    userReview = review;
                }
            }
        }

        // עדכון מספר הביקורות והצגתן
        if (tvReviewsCount != null) {
            tvReviewsCount.setText("(" + reviewList.size() + ")");
        }
        if (reviewAdapter != null) {
            reviewAdapter.notifyDataSetChanged();
        }
        updateAverageRating();

        // אם המשתמש כבר כתב ביקורת - נטען אותה לטופס
        if (userAlreadyReviewed) {
            hasReviewed = true;
            existingReviewId = userReview.getReviewId();
            disableReviewForm();
            if (userReview != null) {
                etReviewText.setText(userReview.getReviewText());
                ratingBar.setRating(userReview.getRating());
            }
        } else {
            hasReviewed = false;
            existingReviewId = null;
            etReviewText.setText("");
            ratingBar.setRating(0);
            enableReviewForm();
        }
    }

    // בדיקה אם המשתמש כבר כתב ביקורת על הסיכום
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

    // הפעלת הטופס להזנת ביקורת
    private void enableReviewForm() {
        if (currentUserId == null) {
            disableReviewForm();
            return;
        }

        if (etReviewText != null) etReviewText.setEnabled(true);
        if (ratingBar != null) ratingBar.setEnabled(true);
        if (btnSubmitReview != null) btnSubmitReview.setEnabled(true);
    }

    // השבתת טופס הביקורת
    private void disableReviewForm() {
        if (etReviewText != null) etReviewText.setEnabled(false);
        if (ratingBar != null) ratingBar.setEnabled(false);
        if (btnSubmitReview != null) btnSubmitReview.setEnabled(false);
    }

    // שליחת ביקורת חדשה
    private void submitReview() {
        if (currentUserId == null) {
            disableReviewForm();
            Toast.makeText(getContext(), "כדי לכתוב ביקורת, יש להתחבר תחילה", Toast.LENGTH_SHORT).show();
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
        com.google.firebase.Timestamp timestamp = com.google.firebase.Timestamp.now();

        Review newReview = new Review(currentUserId, currentUserName, summaryId, reviewText, rating);

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", currentUserId);
        reviewData.put("userName", currentUserName);
        reviewData.put("summaryId", summaryId);
        reviewData.put("reviewText", reviewText);
        reviewData.put("rating", rating);
        reviewData.put("createdAt", timestamp);

        // שמירת הביקורת ל־Firebase
        db.collection("reviews")
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    String reviewId = documentReference.getId();

                    // שליפת הביקורת המלאה מחדש
                    db.collection("reviews").document(reviewId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Review completeReview = documentSnapshot.toObject(Review.class);
                                    if (completeReview != null) {
                                        completeReview.setReviewId(reviewId);

                                        reviewList.add(0, completeReview);
                                        reviewAdapter.notifyItemInserted(0);

                                        if (tvReviewsCount != null) {
                                            tvReviewsCount.setText("(" + reviewList.size() + ")");
                                        }
                                        updateAverageRating();
                                    }
                                }

                                etReviewText.setText("");
                                ratingBar.setRating(0);
                                hasReviewed = true;
                                existingReviewId = reviewId;
                                disableReviewForm();

                                Toast.makeText(getContext(), "תודה על הביקורת שלך!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // גם אם נכשל, עדיין ננקה את הטופס
                                etReviewText.setText("");
                                ratingBar.setRating(0);
                                hasReviewed = true;
                                existingReviewId = reviewId;
                                disableReviewForm();

                                Toast.makeText(getContext(), "תודה על הביקורת שלך!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בשליחת הביקורת: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // עדכון הדירוג הממוצע של הסיכום
    private void updateAverageRating() {
        float averageRating = calculateAverageRating();

        if (summaryId != null && !summaryId.isEmpty()) {
            DocumentReference summaryRef = db.collection("summaries").document(summaryId);
            summaryRef.update("averageRating", averageRating);
        }
    }

    // מחיקת ביקורת
    @Override
    public void onDeleteReview(Review review, int position) {
        if (getContext() == null) return;

        boolean canDelete = false;

        // בדיקת הרשאה למחיקה
        if (currentUserId != null && currentUserId.equals(review.getUserId())) {
            canDelete = true;
        }
        if (Admin.isAdminLoggedIn()) {
            isAdmin = true;
            canDelete = true;
        }
        if (isAdmin) {
            canDelete = true;
        }
        if (!canDelete) {
            Toast.makeText(getContext(), "אין לך הרשאה למחוק ביקורת זו", Toast.LENGTH_SHORT).show();
            return;
        }

        // תיבת דו-שיח לאישור מחיקה
        String dialogTitle = (isAdmin && (currentUserId == null || !currentUserId.equals(review.getUserId()))) ?
                "מחיקת ביקורת (הנהלה)" : "מחיקת ביקורת";

        new AlertDialog.Builder(getContext())
                .setTitle(dialogTitle)
                .setMessage("האם למחוק את הביקורת?")
                .setPositiveButton("מחיקה", (dialog, which) -> {
                    if (review.getReviewId() != null) {
                        db.collection("reviews").document(review.getReviewId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    reviewList.remove(position);
                                    reviewAdapter.notifyItemRemoved(position);

                                    if (tvReviewsCount != null) {
                                        tvReviewsCount.setText("(" + reviewList.size() + ")");
                                    }

                                    // עדכון הדירוג הממוצע מחדש
                                    db.collection("reviews")
                                            .whereEqualTo("summaryId", review.getSummaryId())
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                if (querySnapshot.isEmpty()) {
                                                    db.collection("summaries").document(review.getSummaryId())
                                                            .update("averageRating", 0);
                                                } else {
                                                    float total = 0;
                                                    for (DocumentSnapshot doc : querySnapshot) {
                                                        Double rating = doc.getDouble("rating");
                                                        if (rating != null) total += rating;
                                                    }
                                                    float average = total / querySnapshot.size();
                                                    db.collection("summaries").document(review.getSummaryId())
                                                            .update("averageRating", average);
                                                }
                                            });

                                    // אם המשתמש מחק את הביקורת של עצמו
                                    if (currentUserId != null && currentUserId.equals(review.getUserId())) {
                                        hasReviewed = false;
                                        existingReviewId = null;
                                        enableReviewForm();
                                        etReviewText.setText("");
                                        ratingBar.setRating(0);
                                    }

                                    String successMessage = (isAdmin && (currentUserId == null || !currentUserId.equals(review.getUserId()))) ?
                                            "הביקורת נמחקה על ידי ההנהלה" : "הביקורת נמחקה";
                                    Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "שגיאה במחיקת הביקורת", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // פעולה כאשר נלחץ כפתור עריכת ביקורת
    @Override
    public void onEditReview(Review review, int position) {
        if (getContext() != null) {
            Toast.makeText(getContext(), "עריכת הביקורת שלך", Toast.LENGTH_SHORT).show();
        }
    }

    // שמירת שינויים בביקורת קיימת
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

    // חישוב ממוצע דירוגים
    public float calculateAverageRating() {
        if (reviewList == null || reviewList.isEmpty()) return 0;
        float sum = 0;
        for (Review review : reviewList) {
            sum += review.getRating();
        }
        return reviewList.size() > 0 ? sum / reviewList.size() : 0;
    }
}