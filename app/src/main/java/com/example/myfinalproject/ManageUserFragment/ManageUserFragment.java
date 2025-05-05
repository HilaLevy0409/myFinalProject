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

import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.Message.MessageFragment;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumByUserFragment.SumByUserFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManageUserFragment extends Fragment implements View.OnClickListener {



    private int badPoints = 0;
    private TextView tvBadPoints, tvEmail, tvPhone, tvBirthDate, tvSumNum, tvUsername;
    private Button btnAddPoint, btnRemovePoint, btnShowSums, btnSendMessage, btnDeleteUser;
    private User currentUser;

    private String userId;

    private ImageView imgUserProfile;


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



        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("username")) {
                String username = bundle.getString("username");
                loadUserByUsername(username);
                return;
            }

            String userName = bundle.getString("userName");
            String userEmail = bundle.getString("userEmail");
            String userPhone = bundle.getString("userPhone");
            String userBirthDate = bundle.getString("userBirthDate");
            int badPoints = bundle.getInt("badPoints", 0);
            int sumCount = bundle.getInt("sumCount", 0);

            tvUsername.setText("שם משתמש: " + userName);
            tvEmail.setText("אימייל: " + userEmail);
            tvPhone.setText("מספר טלפון: " + userPhone);
            tvBirthDate.setText("תאריך לידה: " + userBirthDate);
            tvBadPoints.setText("נקודות לרעה: " + badPoints);
            tvSumNum.setText("מספר סיכומים שנכתבו: " + sumCount);
        }
    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAddPoint) {
            if (badPoints < 3) {
                badPoints++;
                updateBadPointsText();
                updateBadPointsInDatabase();
                if (badPoints == 3) {
                    showPointsLimitDialog();
                }
            }
        } else if (view.getId() == R.id.btnRemovePoint) {
            if (badPoints > 0) {
                badPoints--;
                updateBadPointsText();
            }
        } else if (view.getId() == R.id.btnShowSums) {

            SumByUserFragment sumByUserFragment = new SumByUserFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            sumByUserFragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new SumByUserFragment())
                    .addToBackStack(null)
                    .commit();

        } else if (view.getId() == R.id.btnSendMessage) {
//            btnSendMessage.setOnClickListener(v -> {
//                MessageFragment messageFragment = new MessageFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString("userName", currentUser.getUserName());
//                messageFragment.setArguments(bundle);
//
//                getActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.flFragment, new MessageFragment())
//                        .addToBackStack(null)
//                        .commit();
//            });

            MessageFragment messageFragment = new MessageFragment();
            Bundle bundle = new Bundle();

            if (currentUser != null) {
                bundle.putString("userName", currentUser.getUserName());
            }

            messageFragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, messageFragment)
                    .addToBackStack(null)
                    .commit();

        } else if (view.getId() == R.id.btnDeleteUser) {
            String userName = currentUser != null ? currentUser.getUserName() : "משתמש";

            new AlertDialog.Builder(getContext())
                    .setTitle("אישור מחיקה")
                    .setMessage("האם ברצונך למחוק לצמיתות את המשתמש " + userName + "?")
                    .setPositiveButton("כן", (dialog, which) -> {
                        deleteUser();
                    })
                    .setNegativeButton("לא", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

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

    private void showPointsLimitDialog() {
        String userName = currentUser != null ? currentUser.getUserName() : "משתמש";

        new AlertDialog.Builder(getContext())
                .setTitle("התראת נקודות")
                .setMessage("המשתמש " + userName + " הגיע ל-3 נקודות שליליות. האם למחוק את המשתמש?")
                .setPositiveButton("כן", (dialog, which) -> {
                    deleteUser();
                })
                .setNegativeButton("לא", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }



    private void deleteUser() {
        if (currentUser != null && userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {

                        try {

                            Toast.makeText(getContext(), "המשתמש " + currentUser.getUserName() + " נמחק בהצלחה", Toast.LENGTH_SHORT).show();


                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.flFragment, new AdminFragment())
                                    .commit();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "שגיאה במחיקת המשתמש מהאימות: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "שגיאה במחיקת המשתמש: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }



    private void updateBadPointsText() {

        tvBadPoints.setText("נקודות לרעה: " + badPoints);
    }

    private void loadProfilePicture(String profilePicData) {
        // Set default image first as a fallback
        if (imgUserProfile != null) {
            imgUserProfile.setImageResource(R.drawable.newlogo);
        }

        // Handle null or empty profile picture data
        if (profilePicData == null || profilePicData.isEmpty() || imgUserProfile == null) {
            Log.d("ImageDebug", "No profile picture data or imageView is null");
            return;
        }

        Log.d("ImageDebug", "Loading profile picture, data length: " + profilePicData.length());

        // Try Base64 approach first
        if (profilePicData.contains(";base64,") || profilePicData.startsWith("/9j/") ||
                profilePicData.startsWith("iVBOR")) {

            try {
                // Extract base64 part if format is "data:image/jpeg;base64,..."
                String cleanBase64 = profilePicData;
                if (profilePicData.contains(",")) {
                    cleanBase64 = profilePicData.split(",")[1];
                    Log.d("ImageDebug", "Extracted base64 part after comma");
                }

                // Add padding if needed
                while (cleanBase64.length() % 4 != 0) {
                    cleanBase64 += "=";
                }

                byte[] decodedString = android.util.Base64.decode(
                        cleanBase64,
                        android.util.Base64.DEFAULT
                );

                if (decodedString == null || decodedString.length == 0) {
                    Log.e("ImageDebug", "Base64 decoded to empty byte array");
                    return;
                }

                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(
                        decodedString, 0, decodedString.length
                );

                if (bitmap != null) {
                    imgUserProfile.setImageBitmap(bitmap);
                    Log.d("ImageDebug", "Successfully loaded image from base64");
                    return;
                } else {
                    Log.e("ImageDebug", "Failed to create bitmap from decoded base64");
                }
            } catch (IllegalArgumentException e) {
                Log.e("ImageDebug", "Base64 decode failed - Invalid character: " + e.getMessage());
            } catch (Exception e) {
                Log.e("ImageDebug", "Base64 decode failed: " + e.getMessage());
            }
        }

        // Try as a URL/URI if it starts with http/https
        if (profilePicData.startsWith("http")) {
            try {
                // For URL loading, use a library like Glide or Picasso instead
                // This is just a placeholder example using standard Android
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL(profilePicData);
                        final android.graphics.Bitmap bitmap =
                                android.graphics.BitmapFactory.decodeStream(url.openConnection().getInputStream());

                        if (bitmap != null) {
                            getActivity().runOnUiThread(() -> {
                                imgUserProfile.setImageBitmap(bitmap);
                            });
                            Log.d("ImageDebug", "Successfully loaded image from URL");
                        } else {
                            Log.e("ImageDebug", "Failed to decode bitmap from URL");
                        }
                    } catch (Exception e) {
                        Log.e("ImageDebug", "URL loading failed: " + e.getMessage());
                    }
                }).start();
                return;
            } catch (Exception e) {
                Log.e("ImageDebug", "Error setting up URL loading: " + e.getMessage());
            }
        }

        // Try as a local URI
        try {
            android.net.Uri imageUri = android.net.Uri.parse(profilePicData);
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                    getActivity().getContentResolver(), imageUri
            );
            imgUserProfile.setImageBitmap(bitmap);
            Log.d("ImageDebug", "Successfully loaded image from URI");
            return;
        } catch (Exception e) {
            Log.e("ImageDebug", "URI loading failed: " + e.getMessage());
        }

        // If we reach here, all methods failed
        Log.d("ImageDebug", "All image loading methods failed, using default image");
    }


    private void loadUserByUsername(String username) {
        if (username == null || username.isEmpty()) {
            Toast.makeText(getContext(), "שם משתמש לא תקין", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userName", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        try {
                            currentUser = document.toObject(User.class);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "שגיאה בהמרת נתוני המשתמש", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (currentUser != null) {
                            userId = document.getId(); // חשוב מאוד

                            // ✅ טעינת התמונה מתוך השדה imageProfile של המשתמש
                            String profilePicData = currentUser.getImageProfile();
                            loadProfilePicture(profilePicData);

                            // הצגת שאר הנתונים
                            tvUsername.setText("שם משתמש: " + currentUser.getUserName());
                            tvEmail.setText("אימייל: " + currentUser.getUserEmail());
                            tvPhone.setText("מספר טלפון: " + (currentUser.getPhone() != null ? currentUser.getPhone() : "לא זמין"));
                            tvBirthDate.setText("תאריך לידה: " + (currentUser.getUserBirthDate() != null ? currentUser.getUserBirthDate() : "לא זמין"));

                            try {
                                badPoints = currentUser.getBadPoints();
                            } catch (Exception e) {
                                badPoints = 0;
                            }
                            updateBadPointsText();

                            db.collection("summaries")
                                    .whereEqualTo("userId", userId)
                                    .get()
                                    .addOnSuccessListener(summariesSnapshot -> {
                                        int sumCount = summariesSnapshot.size();
                                        tvSumNum.setText("מספר סיכומים שנכתבו: " + sumCount);
                                    })
                                    .addOnFailureListener(e -> {
                                        tvSumNum.setText("מספר סיכומים שנכתבו: לא זמין");
                                    });
                        } else {
                            showUserNotFoundUI(username);
                        }
                    } else {
                        showUserNotFoundUI(username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת פרטי המשתמש: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showUserNotFoundUI(username);
                });
    }


    private void showUserNotFoundUI(String username) {
        Toast.makeText(getContext(), "לא נמצא משתמש בשם " + username, Toast.LENGTH_SHORT).show();
        tvUsername.setText("שם משתמש: " + username + " (לא נמצא)");
        tvEmail.setText("אימייל: לא זמין");
        tvPhone.setText("מספר טלפון: לא זמין");
        tvBirthDate.setText("תאריך לידה: לא זמין");
        tvBadPoints.setText("נקודות לרעה: ");
        tvSumNum.setText("מספר סיכומים שנכתבו: ");

        currentUser = null;
        userId = null;
        badPoints = 0;
    }

}