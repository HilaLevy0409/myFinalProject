package com.example.myfinalproject.ChooseSumFragment;


import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;
import com.example.myfinalproject.WritingSumFragment.WritingSumFragment;

import com.example.myfinalproject.SumFragment.SumFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import java.util.List;

public class ChooseSumFragment extends Fragment implements View.OnClickListener {


    private Button btnAdd;
    private ListView listViewSummaries;
    private SummaryAdapter summaryAdapter;
    private ArrayList<Summary> summaryList;
    private ChooseSumPresenter chooseSumPresenter;
    private SearchView searchView;
    private String selectedClass, selectedProfession;
    private ArrayList<Summary> fullSummaryList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        summaryList = new ArrayList<>();
        chooseSumPresenter = new ChooseSumPresenter(this);
        if (getArguments() != null) {
            selectedClass = getArguments().getString("selected_class", "");
            selectedProfession = getArguments().getString("selected_profession", "");
            Log.d("ChooseSumFragment", "Received: class=" + selectedClass + ", profession=" + selectedProfession);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_sum, container, false);
        loadSummaries();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnAdd = view.findViewById(R.id.btnAdd);
        listViewSummaries = view.findViewById(R.id.listViewSummaries);
        searchView = view.findViewById(R.id.searchView);

        summaryAdapter = new SummaryAdapter(getContext(), summaryList);
        listViewSummaries.setAdapter(summaryAdapter);

//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            btnAdd.setVisibility(View.GONE);
//        } else {
//            btnAdd.setVisibility(View.VISIBLE);
//            btnAdd.setEnabled(true);
//        }

        btnAdd.setOnClickListener(this);


        listViewSummaries.setOnItemClickListener((parent, view1, position, id) -> {
            Summary selectedSummary = summaryList.get(position);
            showSum(selectedSummary);
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSummariesByTitle(newText);
                return true;
            }
        });

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAdd) {
            Bundle args = new Bundle();
            args.putString("selected_class", selectedClass);
            args.putString("selected_profession", selectedProfession);

            WritingSumFragment writingSumFragment = new WritingSumFragment();
            writingSumFragment.setArguments(args);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, writingSumFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }




    private void loadSummaries() {
        chooseSumPresenter.loadSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    summaryList.clear();
                    fullSummaryList.clear();
                    fullSummaryList.addAll(summaries);
                    summaryList.addAll(summaries);
                    summaryAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "שגיאה בהצגת הסיכומים: " + message,
                                Toast.LENGTH_SHORT).show()
                );
            }
        }, selectedClass, selectedProfession);
    }


    private void showSum(Summary summary) {
        if (getContext() == null) return;

        SumFragment sumFragment = new SumFragment();

        Bundle args = new Bundle();
        args.putString("summaryId", summary.getSummaryId());

        sumFragment.setArguments(args);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, sumFragment)
                .commit();
    }


    private void filterSummariesByTitle(String query) {
        summaryList.clear();
        if (query.isEmpty()) {
            summaryList.addAll(fullSummaryList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Summary summary : fullSummaryList) {
                if (summary.getSummaryTitle().toLowerCase().contains(lowerCaseQuery)) {
                    summaryList.add(summary);
                }
            }
        }
        summaryAdapter.notifyDataSetChanged();
    }

}