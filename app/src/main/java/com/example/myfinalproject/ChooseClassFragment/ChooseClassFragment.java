package com.example.myfinalproject.ChooseClassFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


import com.example.myfinalproject.ChooseProfessionFragment.ChooseProfessionFragment;

import com.example.myfinalproject.R;



import com.example.myfinalproject.Adapters.ClassAdapter;

public class ChooseClassFragment extends Fragment {

    public ChooseClassFragment() {}

    public static ChooseClassFragment newInstance() {
        ChooseClassFragment fragment = new ChooseClassFragment();
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
        return inflater.inflate(R.layout.fragment_choose_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        GridView gridView = view.findViewById(R.id.gridView);

        // מערך שמכיל את שכבות הכיתה שיוצגו בגריד
        String[] classes = {"ז", "ח", "ט", "י", "יא", "יב"};

        // מחבר את המערך לגריד באמצעות מתאם
        ClassAdapter adapter = new ClassAdapter(getContext(), classes);
        gridView.setAdapter(adapter);

        // מאזין ללחיצה על אחת מהכיתות
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedClass = classes[position]; // מקבל את הכיתה שנבחרה

            // שומר את הכיתה שנבחרה ב-Bundle לצורך העברה לפרגמנט הבא
            Bundle args = new Bundle();
            args.putString("selected_class", selectedClass);
            args.putBoolean("is_visitor", false); // מציין שהמשתמש אינו אורח

            ChooseProfessionFragment chooseProfessionFragment = new ChooseProfessionFragment();
            chooseProfessionFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, chooseProfessionFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}