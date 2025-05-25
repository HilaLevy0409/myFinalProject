package com.example.myfinalproject.AdminFragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.AdminLoginFragment.AdminLoginFragment;
import com.example.myfinalproject.Admin;
import com.example.myfinalproject.ManageUserFragment.ManageUserFragment;
import com.example.myfinalproject.DataModels.User;
import com.example.myfinalproject.NoticesAdminFragment.NoticesAdminFragment;
import com.example.myfinalproject.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;


public class AdminFragment extends Fragment implements View.OnClickListener{


    private Button btnSend;
    private TextView tvNumUsers;

    public AdminFragment() {
    }


    public static AdminFragment newInstance() {
        AdminFragment fragment = new AdminFragment();
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
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Spinner spinner = view.findViewById(R.id.spinner);
        btnSend = view.findViewById(R.id.btnSend);
        tvNumUsers = view.findViewById(R.id.tvNumUsers);


        btnSend.setOnClickListener(this);


        List<String> usersList = new ArrayList<>();
        usersList.add("בחירת משתמש"); // אפשרות ברירת מחדל

        // יצירת מתאם לרשימת המשתמשים
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, usersList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // טעינת המשתמשים מ-Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getString("userName");
                        if (username != null) {
                            usersList.add(username); // הוספת שם לרשימה
                        }
                    }

                    // עדכון הטקסט עם מספר המשתמשים
                    tvNumUsers.setText(tvNumUsers.getText().toString() + usersList.size());
                    adapter.notifyDataSetChanged(); // עדכון התצוגה
                    spinner.setSelection(0); // ברירת מחדל

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show());
        spinner.setAdapter(adapter); // חיבור המתאם לספינר

        // טיפול בבחירת משתמש
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position > 0) { // מתעלם מהאפשרות הראשונה
                    String selectedUser = (String) parentView.getItemAtPosition(position);
                    navigateToUserProfile(selectedUser);  // מעבר למסך ניהול המשתמש
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }
    // מעבר למסך ניהול משתמש מסוים
    private void navigateToUserProfile(String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userName", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);

                        // העברת פרטי המשתמש כ-Bundle
                        Bundle bundle = new Bundle();
                        bundle.putString("userName", user.getUserName());
                        bundle.putString("userEmail", user.getUserEmail());
                        bundle.putString("userPhone", user.getPhone());
                        bundle.putString("userBirthDate", user.getUserBirthDate());
                        bundle.putInt("badPoints", user.getBadPoints());
                        bundle.putInt("sumCount", user.getSumCount());
                        bundle.putString("profilePicData", user.getImageProfile());

                        // מעבר למסך ניהול המשתמש
                        ManageUserFragment manageUserFragment = new ManageUserFragment();
                        manageUserFragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flFragment, manageUserFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה בטעינת פרטי משתמש", Toast.LENGTH_SHORT).show());
    }



    public void onClick(View v) {
            if(v == btnSend ) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, new NoticesAdminFragment())
                        .addToBackStack(null)
                        .commit();
        }
    }

    // מתבצע כשחוזרים לפרגמנט – בודק אם התחברות ההנהלה פגה
    @Override
    public void onResume() {
        super.onResume();

        if (Admin.isSessionExpired()) {
            showSessionExpiredDialog(); // הצגת דיאלוג אם פג תוקף
        } else {
            // בדיקה נוספת לאחר 5 דקות
            new Handler().postDelayed(() -> {
                if (Admin.isSessionExpired()) {
                    showSessionExpiredDialog();
                }
            }, 5 * 60 * 1000);
        }
    }

    private void showSessionExpiredDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("התחברות הנהלה הסתיימה")
                .setMessage("האם ברצונך להתחבר שוב או להתנתק?")
                .setPositiveButton("התחברות מחדש", (dialog, which) -> {
                    Admin.login();
                    Toast.makeText(getContext(), "התחברת מחדש!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("התנתקות", (dialog, which) -> {
                    Admin.logout();
                    Toast.makeText(getContext(), "נותקת מההנהלה", Toast.LENGTH_SHORT).show();

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flFragment, new AdminLoginFragment())
                            .commit();
                })
                .setCancelable(false)
                .show();
    }

}