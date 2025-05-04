package com.example.myfinalproject.ManageUserFragment;

import android.app.AlertDialog;
import android.os.Bundle;
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
        if (profilePicData == null || profilePicData.isEmpty() || imgUserProfile == null) {
            imgUserProfile.setImageResource(R.drawable.newlogo);
            return;
        }

        try {
            if (profilePicData.startsWith("/9j/") || profilePicData.startsWith("iVBOR")) {
                byte[] decodedString = android.util.Base64.decode(profilePicData, android.util.Base64.DEFAULT);
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (bitmap != null) {
                    imgUserProfile.setImageBitmap(bitmap);
                } else {
                    imgUserProfile.setImageResource(R.drawable.newlogo);
                }
            }
            else {
                android.net.Uri imageUri = android.net.Uri.parse(profilePicData);
                android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                imgUserProfile.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            imgUserProfile.setImageResource(R.drawable.newlogo);
        }
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
                        DocumentSnapshot document = null;
                        try {
                            document = queryDocumentSnapshots.getDocuments().get(0);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "שגיאה בעיבוד תוצאות החיפוש", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (document == null) {
                            Toast.makeText(getContext(), "מסמך המשתמש ריק", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        userId = document.getId();

                        String profilePicData = null;

                        if (document.contains("profileImage")) {
                            profilePicData = document.getString("profileImage");
                        } else if (document.contains("profilePicture")) {
                            profilePicData = document.getString("profilePicture");
                        } else if (document.contains("profilePic")) {
                            profilePicData = document.getString("profilePic");
                        } else if (document.contains("imageProfile")) {
                            profilePicData = document.getString("imageProfile");
                        } else if (document.contains("profilePicUrl")) {
                            profilePicData = document.getString("profilePicUrl");
                        } else if (document.contains("profilePicBase64")) {
                            profilePicData = document.getString("profilePicBase64");
                        }

                        loadProfilePicture(profilePicData);

                        try {
                            currentUser = document.toObject(User.class);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "שגיאה בהמרת נתוני המשתמש", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (currentUser != null) {
                            tvUsername.setText("שם משתמש: " + currentUser.getUserName());
                            tvEmail.setText("אימייל: " + currentUser.getUserEmail());
                            tvPhone.setText("מספר טלפון: " + (currentUser.getPhone() != null ? currentUser.getPhone() : "לא זמין"));
                            tvBirthDate.setText("תאריך לידה: " + (currentUser.getUserBirthDate() != null ? currentUser.getUserBirthDate() : "לא זמין"));

                            try {
                                badPoints = currentUser.getBadPoints();
                                updateBadPointsText();
                            } catch (Exception e) {
                                badPoints = 0;
                                updateBadPointsText();
                            }

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
