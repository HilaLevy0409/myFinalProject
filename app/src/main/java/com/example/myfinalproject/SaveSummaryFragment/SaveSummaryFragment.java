package com.example.myfinalproject.SaveSummaryFragment;

import android.os.Bundle;
import android.util.Log;
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
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumFragment.SumFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SaveSummaryFragment extends Fragment {

    private static final String TAG = "SaveSummaryFragment";
    private ListView listViewSummaries;
    private SearchView searchView;
    private SummaryAdapter summaryAdapter;
    private List<Summary> savedSummaries;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadSavedSummaries();

        listViewSummaries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Summary selectedSummary = (Summary) parent.getItemAtPosition(position);
                navigateToSumFragment(selectedSummary);
            }
        });

        searchView();
    }


    private void navigateToSumFragment(Summary summary) {
        if (summary == null) {
            Log.e(TAG, "Cannot navigate to SumFragment: summary is null");
            return;
        }

        Log.d(TAG, "Navigating to SumFragment with summary ID: " + summary.getSummaryId());

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
        } else {
            Log.e(TAG, "Cannot navigate: getActivity is null");
        }
    }

    private void loadSavedSummaries() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String summaryId = document.getString("summaryId");
                        if (summaryId != null) {
                            loadSummaryData(summaryId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting saved summaries", e);
                    Toast.makeText(getContext(), "שגיאה בטעינת הסיכומים השמורים", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSummaryData(String summaryId) {
        db.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Summary summary = documentSnapshot.toObject(Summary.class);
                        if (summary != null) {
                            if (summary.getSummaryId() == null || summary.getSummaryId().isEmpty()) {
                                summary.setSummaryId(documentSnapshot.getId());
                            }
                            savedSummaries.add(summary);
                            summaryAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Loaded summary: " + summary.getSummaryTitle() + " with ID: " + summary.getSummaryId());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading summary", e);
                });
    }

    private void searchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Summary> filteredList = new ArrayList<>();
                for (Summary summary : savedSummaries) {
                    if (summary.getSummaryTitle().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(summary);
                    }
                }
                summaryAdapter.updateSummaries(filteredList);
                return true;
            }
        });
    }
}