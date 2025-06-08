package com.example.myfinalproject.ManageUserFragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Message.MessageFragment;
import com.example.myfinalproject.DataModels.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumByUserFragment.SumByUserFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;


public class ManageUserFragment extends Fragment implements View.OnClickListener {



    private int badPoints = 0;

    private TextView tvBadPoints, tvEmail, tvPhone, tvBirthDate, tvSumNum, tvUsername;
    private Button btnAddPoint, btnRemovePoint, btnShowSums, btnSendMessage, btnDeleteUser;
    private User currentUser;

    private String userId;

    private ImageView imgUserProfile;


    // מאזין לשינויים במספר הסיכומים של המשתמש
    private ListenerRegistration summariesListener;


    public ManageUserFragment() {
    }

    public static ManageUserFragment newInstance() {
        ManageUserFragment fragment = new ManageUserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_user, container, false);

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        tvBadPoints = view.findViewById(R.id.tvBadPoints);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvBirthDate = view.findViewById(R.id.tvBirthDate);
        tvSumNum = view.findViewById(R.id.tvSumNum);
        tvUsername = view.findViewById(R.id.tvUsername);

        btnShowSums = view.findViewById(R.id.btnShowSums);
        btnAddPoint = view.findViewById(R.id.btnAddPoint);
        btnRemovePoint = view.findViewById(R.id.btnRemovePoint);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);

        imgUserProfile = view.findViewById(R.id.imgUserProfile);

        btnAddPoint.setOnClickListener(this);
        btnRemovePoint.setOnClickListener(this);
        btnShowSums.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);

        // קבלת מידע שהועבר ל־Fragment (דרך Bundle)
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("userId")) {
                // טעינה לפי מזהה משתמש
                userId = bundle.getString("userId");
                loadUserById(userId);
                return;
            }

            // טעינה לפי פרטים שהועברו בבאנדל (אם לא הועבר userId)
            String userName = bundle.getString("userName");
            String userEmail = bundle.getString("userEmail");
            loadUserByEmail(userEmail);

            String userPhone = bundle.getString("userPhone");
            String userBirthDate = bundle.getString("userBirthDate");
            int badPoints = bundle.getInt("badPoints", 0);
            int sumCount = bundle.getInt("sumCount", 0);

            // הצגת פרטי המשתמש במסך
            tvUsername.setText("שם משתמש: " + userName);
            tvEmail.setText("אימייל: " + userEmail);
            tvPhone.setText("מספר טלפון: " + userPhone);
            tvBirthDate.setText("תאריך לידה: " + userBirthDate);
            tvBadPoints.setText("נקודות לרעה: " + badPoints);
            tvSumNum.setText("מספר סיכומים שנכתבו: " + sumCount);

            String profilePicData = bundle.getString("profilePicData");
            loadProfilePicture(profilePicData);

            // לחיצה על מחיקת משתמש
            btnDeleteUser.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("מחיקת משתמש")
                        .setMessage("האם ברצונך למחוק את המשתמש? פעולה זו אינה הפיכה.")
                        .setPositiveButton("כן", (dialog, which) -> {
//                            FirebaseFirestore db = FirebaseFirestore.getInstance();
//                            db.collection("users")
//                                    .whereEqualTo("userEmail", userEmail)
//                                    .get()
//                                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
//                                            doc.getReference().delete()
//                                                    .addOnSuccessListener(aVoid -> {
//                                                        Toast.makeText(getContext(), "המשתמש נמחק בהצלחה", Toast.LENGTH_SHORT).show();
//                                                        requireActivity().getSupportFragmentManager().popBackStack();
//                                                    })
//                                                    .addOnFailureListener(e -> {
//                                                        Toast.makeText(getContext(), "שגיאה במחיקה", Toast.LENGTH_SHORT).show();
//                                                    });
//                                        }
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Toast.makeText(getContext(), "שגיאה באיתור המשתמש", Toast.LENGTH_SHORT).show();
//                                    });

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(userId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "המשתמש נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                                        requireActivity().getSupportFragmentManager().popBackStack();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                        })
                        .setNegativeButton("לא", null)
                        .setCancelable(false)
                        .show();
            });
        }
    }

    // מאזין לשינויים בכמות הסיכומים של המשתמש
    private void listenToSummaryCountChanges(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        summariesListener = db.collection("summaries")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w("ManageUser", "listen:error", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        int sumCount = querySnapshot.size();
                        tvSumNum.setText("מספר סיכומים שנכתבו: " + sumCount);

                        db.collection("users").document(userId)
                                .update("sumCount", sumCount)
                                .addOnFailureListener(err ->
                                        Log.e("ManageUser", "שגיאה בעדכון sumCount", err));
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAddPoint) {
            if (badPoints < 3) {
                badPoints++;
                updateBadPointsText();
                updateBadPointsInDatabase();

            }
        } else if (view.getId() == R.id.btnRemovePoint) {
            if (badPoints > 0) {
                badPoints--;
                updateBadPointsText();
                updateBadPointsInDatabase();
            }
        } else if (view.getId() == R.id.btnShowSums) {

            SumByUserFragment sumByUserFragment = new SumByUserFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);

            String extractedUserName = tvUsername.getText().toString().replace("שם משתמש: ", "").trim();
            bundle.putString("userName", extractedUserName);

            sumByUserFragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, sumByUserFragment)
                    .addToBackStack(null)
                    .commit();

        } else if (view.getId() == R.id.btnSendMessage) {

            if (getActivity() == null) return;

            if (currentUser == null) {
                Toast.makeText(getContext(), "לא ניתן לשלוח הודעה: אין מידע על המשתמש", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();

            bundle.putString("receiverId", userId);
            bundle.putString("receiverName", currentUser.getUserName());

            MessageFragment messagesFragment = new MessageFragment();
            messagesFragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, messagesFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
        // עדכון נקודות לרעה במסד
    private void updateBadPointsInDatabase() {
        if (currentUser != null && userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.update("badPoints", badPoints)
                    .addOnSuccessListener(aVoid -> {
                        updateBadPointsText();
                        Toast.makeText(getContext(), "נקודות עודכנו בהצלחה", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "שגיאה בעדכון נקודות: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void updateBadPointsText() {

        tvBadPoints.setText("נקודות לרעה: " + badPoints);
    }

    private void loadProfilePicture(String profilePicData) {
        if (imgUserProfile != null) {
            imgUserProfile.setImageResource(R.drawable.newlogo);
        }

        if (profilePicData == null || profilePicData.isEmpty()) {
            return;
        }

        try {
            String cleanBase64 = profilePicData.trim().replaceAll("\\s", "");

            if (cleanBase64.contains(",")) {
                cleanBase64 = cleanBase64.split(",")[1];
            }

            while (cleanBase64.length() % 4 != 0) {
                cleanBase64 += "=";
            }

            byte[] decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imgUserProfile.setImageBitmap(bitmap);
            }

        } catch (Exception e) {
            Log.e("ImageDebug", "Failed to decode Base64 image: " + e.getMessage());
        }
    }

    private void loadUserById(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            this.userId = documentSnapshot.getId();
                            tvUsername.setText("שם משתמש: " + currentUser.getUserName());
                            tvEmail.setText("אימייל: " + currentUser.getUserEmail());
                            tvPhone.setText("מספר טלפון: " + currentUser.getPhone());
                            tvBirthDate.setText("תאריך לידה: " + currentUser.getUserBirthDate());
                            tvBadPoints.setText("נקודות לרעה: " + currentUser.getBadPoints());
                            tvSumNum.setText("מספר סיכומים שנכתבו: " + currentUser.getSumCount());

                            badPoints = currentUser.getBadPoints();
                            loadProfilePicture(currentUser.getImageProfile());
                            listenToSummaryCountChanges(userId);
                        }
                    } else {
                        Toast.makeText(getContext(), "משתמש לא נמצא", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "שגיאה בטעינת המשתמש: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void loadUserByEmail(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        currentUser = document.toObject(User.class);
                        userId = document.getId();

                        if (currentUser != null) {
                            badPoints = currentUser.getBadPoints();

                            updateBadPointsText();

                            listenToSummaryCountChanges(userId);

                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת מזהה המשתמש", Toast.LENGTH_SHORT).show();
                });
    }

    // ביטול המאזין כשעוזבים את המסך
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (summariesListener != null) {
            summariesListener.remove();
        }
    }


}