package com.example.myfinalproject.AdminLoginFragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.R;

public class AdminLoginFragment extends Fragment implements View.OnClickListener {

    private Button btnContinue;
    private EditText etAdmin, etPasswordA;

    public AdminLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnContinue = view.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(this);

        etAdmin = view.findViewById(R.id.etAdmin);
        etPasswordA = view.findViewById(R.id.etPasswordA);
    }

    @Override
    public void onClick(View v) {
        if (v == btnContinue) {
            String username = etAdmin.getText().toString().trim();
            String password = etPasswordA.getText().toString().trim();


            if (username.equals("admin") && password.equals("Admin123!")) {
                Toast.makeText(getContext(), "התחברת בהצלחה", Toast.LENGTH_SHORT).show();

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, new AdminFragment())
                        .commit();
            } else {
                Toast.makeText(getContext(), "שם משתמש או סיסמה לא נכונים", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
