package com.example.myfinalproject.ChooseProfessionFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Adapters.ProfessionAdapter;
import com.example.myfinalproject.CallBacks.ProfessionClickListener;
import com.example.myfinalproject.ChooseSumFragment.ChooseSumFragment;
import com.example.myfinalproject.DataModels.Profession;
import com.example.myfinalproject.R;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;

public class ChooseProfessionFragment extends Fragment implements ProfessionClickListener {

    private RecyclerView recyclerProfessions;
    private ProfessionAdapter adapter;
    private TabLayout tabLayout;
    private SearchView searchView;
    private LinearLayout emptyState;
    private TextView tvSubtitle;
    private String selectedClass;


    public ChooseProfessionFragment() {
    }

    public static ChooseProfessionFragment newInstance() {
        ChooseProfessionFragment fragment = new ChooseProfessionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // קבלת הכיתה שנבחרה מהפרגמנט הקודם
        if (getArguments() != null) {
            selectedClass = getArguments().getString("selected_class", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_profession, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerProfessions = view.findViewById(R.id.recyclerSubjects);
        tabLayout = view.findViewById(R.id.tabLayout);
        searchView = view.findViewById(R.id.searchViewPro);
        emptyState = view.findViewById(R.id.emptyState);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        tvSubtitle.setText("כיתה " + selectedClass);

        // הגדרת אייקון ברירת מחדל למסך ריק
        ImageView emptyStateImage = emptyState.findViewById(R.id.emptyStateImage);
        if (emptyStateImage != null) {
            emptyStateImage.setImageResource(android.R.drawable.ic_menu_search);
        }
        // איפוס חיפוש וטאב כאשר לוחצים על "ניקוי סינון "
        view.findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            searchView.setQuery("", false);
            searchView.clearFocus();

            TabLayout.Tab tab = tabLayout.getTabAt(0); // מביא את הטאב הראשון (במיקום 0) מתוך ה-TabLayout
            if (tab != null) { // בודק שהטאב לא null (כלומר – באמת קיים טאב במיקום הזה)
                tab.select();     // בוחר את הטאב הזה – כאילו המשתמש לחץ עליו
            }
        });

        // יצירת רשימת מקצועות
        List<Profession> professions = createProfessionsList();

        // הגדרת המתאם לרשימת מקצועות
        adapter = new ProfessionAdapter(requireContext(), professions, this);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerProfessions.setLayoutManager(layoutManager);
        recyclerProfessions.setAdapter(adapter);

        // טיפול בלחיצה על טאבים (קטגוריות)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String category = "הכל";

                if (tab.getPosition() == 1) {
                    category = "מקצועות חובה";
                } else if (tab.getPosition() == 2) {
                    category = "מגמות";
                }

                adapter.filterByCategory(category); // סינון לפי קטגוריה
                checkForEmptyState(); // הצגת מסך ריק אם אין תוצאות
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // סינון הרשימה תוך כדי הקלדה בשדה החיפוש
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterByQuery(newText);
                checkForEmptyState();
                return true;
            }
        });
    }

    private List<Profession> createProfessionsList() {
        List<Profession> professions = new ArrayList<>();

        String mandatoryCategory = "מקצועות חובה";
        professions.add(new Profession("לשון", mandatoryCategory));
        professions.add(new Profession("היסטוריה", mandatoryCategory));
        professions.add(new Profession("אזרחות", mandatoryCategory));
        professions.add(new Profession("מתמטיקה", mandatoryCategory));
        professions.add(new Profession("אנגלית", mandatoryCategory));
        professions.add(new Profession("ספרות", mandatoryCategory));
        professions.add(new Profession("תנ״ך", mandatoryCategory));

        String majorsCategory = "מגמות";
        professions.add(new Profession("מדעי המחשב", majorsCategory));
        professions.add(new Profession("כימיה", majorsCategory));
        professions.add(new Profession("פיזיקה", majorsCategory));
        professions.add(new Profession("ביולוגיה", majorsCategory));
        professions.add(new Profession("מנהל וכלכלה", majorsCategory));
        professions.add(new Profession("גיאוגרפיה", majorsCategory));
        professions.add(new Profession("מדעי החברה", majorsCategory));
        professions.add(new Profession("ערבית", majorsCategory));
        professions.add(new Profession("קולנוע ותקשורת", majorsCategory));
        professions.add(new Profession("רפואה", majorsCategory));
        professions.add(new Profession("עיצוב", majorsCategory));
        professions.add(new Profession("מדעי הספורט", majorsCategory));
        professions.add(new Profession("משפטים", majorsCategory));

        return professions;
    }

    // בדיקה האם יש מקצועות להציג, אחרת הצגת מסך ריק
    private void checkForEmptyState() {
        if (adapter.getItemCount() == 0) {
            recyclerProfessions.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerProfessions.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProfessionClick(Profession profession) {
        ChooseSumFragment chooseSumFragment = new ChooseSumFragment();
        Bundle args = new Bundle();
        args.putString("selected_class", selectedClass);
        args.putString("selected_profession", profession.getName());
        chooseSumFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, chooseSumFragment)
                .addToBackStack(null)
                .commit();
    }
}