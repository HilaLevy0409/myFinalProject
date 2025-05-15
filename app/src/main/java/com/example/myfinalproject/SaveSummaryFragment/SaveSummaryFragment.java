package com.example.myfinalproject.SaveSummaryFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.DataModels.Summary;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumFragment.SumFragment;

import java.util.ArrayList;
import java.util.List;

public class SaveSummaryFragment extends Fragment {

    private ListView listViewSummaries;
    private SearchView searchView;
    private SummaryAdapter summaryAdapter;
    private List<Summary> savedSummaries;
    private SaveSummaryPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_save_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listViewSummaries = view.findViewById(R.id.listViewSummaries);
        searchView = view.findViewById(R.id.searchView);
        savedSummaries = new ArrayList<>();
        summaryAdapter = new SummaryAdapter(getContext(), savedSummaries);
        listViewSummaries.setAdapter(summaryAdapter);


        presenter = new SaveSummaryPresenter(this);


        listViewSummaries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Summary selectedSummary = (Summary) parent.getItemAtPosition(position);
                presenter.handleSummaryClick(selectedSummary);
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.filterSummaries(newText);
                return true;
            }
        });


        presenter.loadSavedSummaries();
    }



    public void updateSummaryList(List<Summary> summaries) {
        summaryAdapter.updateSummaries(summaries);
    }

    public void showLoadError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }


    public void navigateToSummary(Summary summary) {
        if (summary == null) {
            return;
        }


        SumFragment sumFragment = SumFragment.newInstance(summary.getSummaryId());

        Bundle args = sumFragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putBoolean("fromFavorites", true);
        sumFragment.setArguments(args);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, sumFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

}