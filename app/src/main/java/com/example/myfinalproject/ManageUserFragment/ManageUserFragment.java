package com.example.myfinalproject.ManageUserFragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class ManageUserFragment extends Fragment implements View.OnClickListener {



    private int badPoints = 0;
    private TextView tvBadPoints, tvEmail, tvPhone, tvBirthDate, tvSumNum, tvUsername;
    private Button btnAddPoint, btnRemovePoint, btnShowSums, btnSendMessage, btnBack, btnDeleteUser;
    private User currentUser;

    private String userId;

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
        btnBack = view.findViewById(R.id.btnBack);
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser);


        btnBack.setOnClickListener(this);
        btnAddPoint.setOnClickListener(this);
        btnRemovePoint.setOnClickListener(this);
        btnShowSums.setOnClickListener(this);
        btnSendMessage.setOnClickListener(this);
        btnDeleteUser.setOnClickListener(this);


        Bundle bundle = getArguments();
        if (bundle != null) {
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

        }
        else if (view.getId() == R.id.btnBack) {
            btnBack.setOnClickListener(v -> {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, new AdminFragment())
                        .addToBackStack(null)
                        .commit();
            });
        } else if (view.getId() == R.id.btnDeleteUser) {
            showDeleteUserConfirmation();
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
        } else {
            Toast.makeText(getContext(), "לא ניתן לעדכן: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
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

    private void showDeleteUserConfirmation() {
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
        } else {
            Toast.makeText(getContext(), "לא ניתן למחוק: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateBadPointsText() {

        tvBadPoints.setText("נקודות לרעה: " + badPoints);
    }



}
