package com.example.myfinalproject.SumByUserFragment;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myfinalproject.CallBacks.SummariesCallback;
import com.example.myfinalproject.ChooseSumFragment.ChooseSumPresenter;
import com.example.myfinalproject.ChooseSumFragment.SummaryAdapter;
import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SumByUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SumByUserFragment extends Fragment {

    private ListView listViewSummaries;
    private SummaryAdapter summaryAdapter;
    private ArrayList<Summary> summaryList;
    private ChooseSumPresenter chooseSumPresenter;
    private SearchView searchView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SumByUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SumByMeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SumByUserFragment newInstance(String param1, String param2) {
        SumByUserFragment fragment = new SumByUserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            summaryList = new ArrayList<>();
//            chooseSumPresenter = new ChooseSumPresenter(this);
//            requestPermissions();
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sum_by_user, container, false);
//        initializeViews(view);
//        setupListeners();
//        loadSummaries();
//        return view;
    }


    private void initializeViews(View view) {
        listViewSummaries = view.findViewById(R.id.listViewSummaries);
        searchView = view.findViewById(R.id.searchView);

        summaryAdapter = new SummaryAdapter(getContext(), summaryList);
        listViewSummaries.setAdapter(summaryAdapter);
    }

    private void setupListeners() {

        listViewSummaries.setOnItemClickListener((parent, view, position, id) -> {
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
                chooseSumPresenter.filterSummaries(newText, summaryList, summaryAdapter);
                return true;
            }
        });
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
                        Toast.makeText(getContext(), "Error loading summaries: " + message,
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showManagementDialog(Summary summary) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("ניהול סיכום");
        String[] options = {"עריכה", "מחיקה", "ביטול"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showEditDialog(summary);
                    break;
                case 1:
                    showDeleteConfirmationDialog(summary);
                    break;
                case 2:
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