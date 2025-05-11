package com.example.myfinalproject.SumByUserFragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.DataModels.Summary;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumFragment.SumFragment;
import com.example.myfinalproject.UserProfileFragment.UserProfileFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class SumByUserFragment extends Fragment {
    private static final String TAG = "SumByUserFragment";

    private ListView listViewSummaries;
    private SummaryAdapter summaryAdapter;
    private ArrayList<Summary> summaryList;
    private SumByUserPresenter presenter;
    private SearchView searchView;
    private TextView tvNoSummaries, tvTitle;

    private String userName;


    public SumByUserFragment() {
    }

    public static SumByUserFragment newInstance(String userName) {
        SumByUserFragment fragment = new SumByUserFragment();
        Bundle args = new Bundle();
        args.putString("userName", userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString("userName");
            Log.d(TAG, "Username from arguments: " + userName);
        }
        summaryList = new ArrayList<>();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sum_by_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvTitle);
        listViewSummaries = view.findViewById(R.id.listViewSummaries);
        searchView = view.findViewById(R.id.searchView);
        tvNoSummaries = view.findViewById(R.id.tvNoSummaries);

        if (userName == null || userName.isEmpty()) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
            userName = prefs.getString("username", "המשתמש הנוכחי");
            Log.d(TAG, "Username from SharedPreferences: " + userName);
        }

        tvTitle.setText("סיכומים שנכתבו על ידי " + userName);

        summaryList = new ArrayList<>();
        summaryAdapter = new SummaryAdapter(getContext(), summaryList);
        listViewSummaries.setAdapter(summaryAdapter);

        presenter = new SumByUserPresenter(this, userName);

        listViewSummaries.setOnItemClickListener((parent, viewItem, position, id) -> {
            Summary selectedSummary = summaryList.get(position);
            Log.d(TAG, "Summary selected: " + selectedSummary.getSummaryTitle() +
                    ", ID: " + selectedSummary.getSummaryId());
            navigateToSummaryView(selectedSummary);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (presenter != null) {
                    presenter.filterSummaries(newText, summaryList, summaryAdapter);
                }
                return true;
            }
        });


        loadSummaries();
    }

    private void loadSummaries() {
        Log.d(TAG, "Loading summaries for user: " + userName);
        presenter.loadUserSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                if (getActivity() == null) return;

                Log.d(TAG, "Successfully loaded " + summaries.size() + " summaries");

                getActivity().runOnUiThread(() -> {
                    summaryList.clear();
                    if (summaries.isEmpty()) {
                        Log.d(TAG, "No summaries found, showing empty message");
                        listViewSummaries.setVisibility(View.GONE);
                        tvNoSummaries.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "Displaying " + summaries.size() + " summaries");
                        listViewSummaries.setVisibility(View.VISIBLE);
                        tvNoSummaries.setVisibility(View.GONE);
                        summaryList.addAll(summaries);

                        for (Summary summary : summaries) {
                            Log.d(TAG, "Summary in list: ID=" + summary.getSummaryId() +
                                    ", Title=" + summary.getSummaryTitle() +
                                    ", Author=" + summary.getUserName());
                        }
                    }
                    summaryAdapter.notifyDataSetChanged();


                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;

                Log.e(TAG, "Error loading summaries: " + message);

                getActivity().runOnUiThread(() -> {
                    listViewSummaries.setVisibility(View.GONE);
                    tvNoSummaries.setVisibility(View.VISIBLE);
                    showError("שגיאה: " + message);
                });
            }
        });
    }






    private void navigateToSummaryView(Summary summary) {
        if (getActivity() != null) {
            Log.d(TAG, "Navigating to SumFragment with summaryId: " + summary.getSummaryId());

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            SumFragment sumFragment = SumFragment.newInstance(summary.getSummaryId());

            transaction.replace(R.id.flFragment, sumFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void showError(String message) {
        Log.e(TAG, "Error: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}