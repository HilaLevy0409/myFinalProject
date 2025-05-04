package com.example.myfinalproject.AdminFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.ManageUserFragment.ManageUserFragment;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.NoticesAdminFragment.NoticesAdminFragment;
import com.example.myfinalproject.R;
import com.google.firebase.firestore.DocumentSnapshot;
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, usersList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String username = document.getString("userName");
                        if (username != null) {
                            usersList.add(username);
                        }
                    }
                    tvNumUsers.setText(tvNumUsers.getText().toString() + usersList.size());
                    adapter.notifyDataSetChanged();
                    spinner.setSelection(0);

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show());
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position > 0) {
                    String selectedUser = (String) parentView.getItemAtPosition(position);
                    navigateToUserProfile(selectedUser);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void navigateToUserProfile(String username) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userName", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("userName", user.getUserName());
                        bundle.putString("userEmail", user.getUserEmail());
                        bundle.putString("userPhone", user.getPhone());
                        bundle.putString("userBirthDate", user.getUserBirthDate());
                        bundle.putInt("badPoints", user.getBadPoints());
                        bundle.putInt("sumCount", user.getSumCount());
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
}