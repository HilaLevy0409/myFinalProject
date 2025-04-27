package com.example.myfinalproject.SumByUserFragment;

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
import com.example.myfinalproject.ChooseSumFragment.ChooseSumPresenter;
import com.example.myfinalproject.Adapters.SummaryAdapter;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;

import java.util.ArrayList;
import java.util.List;


public class SumByUserFragment extends Fragment {

    private ListView listViewSummaries;
    private SummaryAdapter summaryAdapter;
    private ArrayList<Summary> summaryList;
    private ChooseSumPresenter chooseSumPresenter;
    private SearchView searchView;

    private String userName;
    private TextView tvTitle;



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

        tvTitle.setText("סיכומים שנכתבו על ידי " + userName);

        summaryList = new ArrayList<>();
        summaryAdapter = new SummaryAdapter(getContext(), summaryList);
        listViewSummaries.setAdapter(summaryAdapter);

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
                if (chooseSumPresenter != null) {
                    chooseSumPresenter.filterSummaries(newText, summaryList, summaryAdapter);
                }
                return true;
            }
        });

        // loadSummaries();
    }

    private void loadSummaries() {
        chooseSumPresenter.loadSummaries(new SummariesCallback() {
            @Override
            public void onSuccess(List<Summary> summaries) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    summaryList.clear();
                    summaryList.addAll(summaries);
                    summaryAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), ":שגיאה " + message,
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showManagementDialog(Summary summary) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("ניהול סיכום");
        String[] options = {"עריכה", "מחיקה","מעבר לסיכום", "ביטול"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showEditDialog(summary);
                    break;
                case 1:
                    showDeleteConfirmationDialog(summary);
                    break;
                case 3:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }
    private void showEditDialog(Summary summary) {
        if (getContext() == null) return;
    /*
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_summary, null);

        EditText etClass = dialogView.findViewById(R.id.etClass);
        EditText etProfession = dialogView.findViewById(R.id.etProfession);
        EditText etSummaryTitle = dialogView.findViewById(R.id.etSummaryTitle);
        ImageView imgSum = dialogView.findViewById(R.id.imageSum);
        Button btnChangeImage = dialogView.findViewById(R.id.btnChangeImage);

        etClass.setText(summary.getClassOption());
        etProfession.setText(summary.getProfession());
        etSummaryTitle.setText(summary.getSummaryTitle());
        loadBase64Image(summary.getImage(), imgSum);

        btnChangeImage.setOnClickListener(v -> {
            imageSum = imgSum;
            showPictureDialog();
        });

        builder.setView(dialogView)
                .setPositiveButton("שמור", (dialog, which) -> {
                    summary.setClassOption(etClass.getText().toString());
                    summary.setProfession(etProfession.getText().toString());
                    summary.setSummaryTitle(etSummaryTitle.getText().toString());
                    if (imgSum.getDrawable() != null) {
                        summary.setImage(imageViewToBase64(imgSum));
                    }
                    chooseSumPresenter.updateSummary(summary);
                })
                .setNegativeButton("ביטול", (dialog, which) -> dialog.cancel());

        builder.show();

     */
    }

    private void showDeleteConfirmationDialog(Summary summary) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("מחיקת סיכום")
                .setMessage("האם ברצונך למחוק סיכום זה?")
                .setPositiveButton("כן", (dialog, which) ->
                        chooseSumPresenter.deleteSummary(summary.getSummaryId()))
                .setNegativeButton("לא", null)
                .show();
    }
}