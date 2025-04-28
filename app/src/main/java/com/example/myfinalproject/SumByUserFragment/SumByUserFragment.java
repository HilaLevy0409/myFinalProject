package com.example.myfinalproject.SumByUserFragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;

import java.util.ArrayList;
import java.util.List;


public class SumByUserFragment extends Fragment {

    private ListView listViewSummaries;
    private SummaryAdapter summaryAdapter;
    private ArrayList<Summary> summaryList;
    private SumByUserPresenter presenter;
    private SearchView searchView;

    private String userName;
    private TextView tvTitle;

    public SumByUserFragment() {
        // Required empty public constructor
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

        // Get current username if not provided
        if (userName == null || userName.isEmpty()) {
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", requireActivity().MODE_PRIVATE);
            userName = prefs.getString("username", "המשתמש הנוכחי");
        }


        tvTitle.setText("סיכומים שנכתבו על ידי " + userName);

        summaryList = new ArrayList<>();
        summaryAdapter = new SummaryAdapter(getContext(), summaryList);
        listViewSummaries.setAdapter(summaryAdapter);

        // Initialize the presenter with Firestore support
        presenter = new SumByUserPresenter(this, userName);

        listViewSummaries.setOnItemClickListener((parent, viewItem, position, id) -> {
            Summary selectedSummary = summaryList.get(position);
            showManagementDialog(selectedSummary);
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

        // Load summaries for the user
        loadSummaries();
    }

    private void loadSummaries() {
        presenter.loadUserSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    summaryList.clear();
                    if (summaries.isEmpty()) {
                        listViewSummaries.setVisibility(View.GONE);
                    } else {
                        listViewSummaries.setVisibility(View.VISIBLE);
                        summaryList.addAll(summaries);
                    }
                    summaryAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    listViewSummaries.setVisibility(View.GONE);
                    showError("שגיאה: " + message);
                });
            }
        });
    }

    private void showManagementDialog(Summary summary) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("ניהול סיכום");
        String[] options = {"עריכה", "מחיקה", "צפייה בסיכום", "ביטול"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showEditDialog(summary);
                    break;
                case 1:
                    showDeleteConfirmationDialog(summary);
                    break;
                case 2:
                    navigateToSummaryView(summary);
                    break;
                case 3:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void navigateToSummaryView(Summary summary) {
        // Implementation for navigating to the summary view
        Toast.makeText(getContext(), "צפייה בסיכום: " + summary.getSummaryTitle(), Toast.LENGTH_SHORT).show();
        // Add navigation logic here
    }

    private void showEditDialog(Summary summary) {
        if (getContext() == null) return;

        // Implement your edit dialog here
        Toast.makeText(getContext(), "פונקציית עריכה בבניה", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog(Summary summary) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("מחיקת סיכום")
                .setMessage("האם ברצונך למחוק סיכום זה?")
                .setPositiveButton("כן", (dialog, which) -> {
                    presenter.deleteSummary(summary.getSummaryId(), new SummariesCallback() {
                        @Override
                        public void onSuccess(List<Summary> summaries) {
                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "הסיכום נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                                loadSummaries(); // Reload summaries after deletion
                            });
                        }

                        @Override
                        public void onError(String message) {
                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "שגיאה במחיקה: " + message,
                                            Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("לא", null)
                .show();
    }

    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}