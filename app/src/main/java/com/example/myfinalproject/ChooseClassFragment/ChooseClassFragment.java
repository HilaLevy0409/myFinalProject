package com.example.myfinalproject.ChooseClassFragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.example.myfinalproject.ChooseProfessionFragment.ChooseProfessionFragment;
import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.example.myfinalproject.R;
import com.example.myfinalproject.RegistrationFragment.RegistrationFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


import com.example.myfinalproject.Adapters.ClassAdapter;


public class ChooseClassFragment extends Fragment {


    public ChooseClassFragment() {
    }

    public static ChooseClassFragment newInstance(String param1, String param2) {
        ChooseClassFragment fragment = new ChooseClassFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        GridView gridView = view.findViewById(R.id.gridView);

        String[] classes = {"ז", "ח", "ט", "י", "יא", "יב"};
        ClassAdapter adapter = new ClassAdapter(getContext(), classes);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedClass = classes[position];
            showClassNotificationDialog(selectedClass);
        });
    }

    private void showClassNotificationDialog(String className) {
        new AlertDialog.Builder(requireContext())
                .setTitle("עדכונים על סיכומים")
                .setMessage("האם ברצונך לקבל עדכונים בעת עליית סיכום בכיתה " + className + "?")
                .setPositiveButton("כן, אשמח!", (dialog, which) -> {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() == null) {
                        showAuthRequiredDialog(className);
                    } else {
                        Toast.makeText(getContext(), "נרשמת לקבלת עדכונים עבור כיתה " + className, Toast.LENGTH_SHORT).show();
                        subscribeToProfessionNotifications(className);
                        goToProfessionScreen(className, false);
                    }
                })
                .setNegativeButton("לא, רוצה לעבור למסך הבא", (dialog, which) -> {
                    goToProfessionScreen(className, false);
                })
                .setNeutralButton("לא רוצה לקבל עדכונים יותר", (dialog, which) -> {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() == null) {
                        Toast.makeText(getContext(), "אינך רשום לקבלת עדכונים", Toast.LENGTH_SHORT).show();
                        goToProfessionScreen(className, false);
                    } else {
                        unsubscribeFromProfessionNotifications(className);
                        unsubscribeFromFCMTopic(className);
                        goToProfessionScreen(className, false);
                    }
                })
                .show();
    }

    private void showAuthRequiredDialog(String className) {
        new AlertDialog.Builder(requireContext())
                .setTitle("נדרשת הזדהות")
                .setMessage("נדרשת התחברות כדי לקבל עדכונים על סיכומים בכיתה " + className)
                .setPositiveButton("התחברות", (dialog, which) -> {
                    navigateToLoginScreen();
                })
                .setNegativeButton("הרשמה", (dialog, which) -> {
                    navigateToRegisterScreen();
                })
                .setNeutralButton("המשך כאורח", (dialog, which) -> {
                    goToProfessionScreen(className, true);
                })
                .show();
    }

    private void navigateToLoginScreen() {
        Toast.makeText(getContext(), "מעבר למסך התחברות", Toast.LENGTH_SHORT).show();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.flFragment, new LoginFragment())
                .commit();
    }

    private void navigateToRegisterScreen() {
        Toast.makeText(getContext(), "מעבר למסך הרשמה", Toast.LENGTH_SHORT).show();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.flFragment, new RegistrationFragment())
                .commit();
    }

    private void subscribeToProfessionNotifications(String className) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            showAuthRequiredDialog(className);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("notifications_subscriptions")
                .document(userId)
                .set(new HashMap<String, Object>() {{
                    put("className", className);
                }})
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "נרשמת לקבלת התראות עבור כיתה " + className, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בהרשמה", Toast.LENGTH_SHORT).show();
                });
    }

    private void unsubscribeFromProfessionNotifications(String className) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser().getUid();

        db.collection("notifications_subscriptions")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "הסרת הרשמה מהתראות עבור " + className, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בהסרת ההרשמה", Toast.LENGTH_SHORT).show();
                });
    }

    private void unsubscribeFromFCMTopic(String className) {
    }

    private void goToProfessionScreen(String className, boolean isGuest) {
        Bundle args = new Bundle();
        args.putString("selected_class", className);
        args.putBoolean("is_guest", isGuest);

        ChooseProfessionFragment fragment = new ChooseProfessionFragment();

        fragment.setArguments(args);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }
}