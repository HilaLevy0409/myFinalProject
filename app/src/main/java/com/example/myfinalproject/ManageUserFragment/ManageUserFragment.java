package com.example.myfinalproject.ManageUserFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.myfinalproject.R;

public class ManageUserFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private int badPoints = 0;
    private TextView tvBadPoints;

    public ManageUserFragment() {
        // Required empty public constructor
    }

    public static ManageUserFragment newInstance(String param1, String param2) {
        ManageUserFragment fragment = new ManageUserFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manage_user, container, false);

        tvBadPoints = rootView.findViewById(R.id.tvBadPoints);

        Button btnAddPoint = rootView.findViewById(R.id.btnAddPoint);
        Button btnRemovePoint = rootView.findViewById(R.id.btnRemovePoint);

        updateBadPointsText();

        btnAddPoint.setOnClickListener(v -> {
            if (badPoints < 3) {
                badPoints++;
                updateBadPointsText();
            }
        });

        btnRemovePoint.setOnClickListener(v -> {
            if (badPoints > 0) {
                badPoints--;
                updateBadPointsText();
            }
        });

        return rootView;
    }

    private void updateBadPointsText() {
        tvBadPoints.setText("נקודות לרעה: " + badPoints);
    }
}
