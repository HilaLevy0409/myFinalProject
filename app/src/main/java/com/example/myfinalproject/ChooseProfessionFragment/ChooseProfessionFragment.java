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
import com.example.myfinalproject.ChooseSumFragment.ChooseSumFragment;
import com.example.myfinalproject.DataModels.Profession;
import com.example.myfinalproject.R;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;
import java.util.List;

public class ChooseProfessionFragment extends Fragment implements ProfessionAdapter.ProfessionClickListener {

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
        if (getArguments() != null) {
            selectedClass = getArguments().getString("selected_class", "");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choose_professional, container, false);
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

        ImageView emptyStateImage = emptyState.findViewById(R.id.emptyStateImage);
        if (emptyStateImage != null) {
            emptyStateImage.setImageResource(android.R.drawable.ic_menu_search);
        }

        view.findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            searchView.setQuery("", false);
            searchView.clearFocus();

            TabLayout.Tab tab = tabLayout.getTabAt(0);
            if (tab != null) {
                tab.select();
            }
        });

        List<Profession> professions = createProfessionsList();
        adapter = new ProfessionAdapter(requireContext(), professions, this);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerProfessions.setLayoutManager(layoutManager);
        recyclerProfessions.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String category = "הכל";

                if (tab.getPosition() == 1) {
                    category = "מקצועות חובה";
                } else if (tab.getPosition() == 2) {
                    category = "מגמות";
                }

                adapter.filterByCategory(category);
                checkForEmptyState();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

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