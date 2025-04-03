package com.example.myfinalproject.Message;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;

import com.example.myfinalproject.R;

import java.util.ArrayList;
import java.util.List;

public class ChooseMessageFragment extends Fragment {

    private ListView listViewMessages;
    private SearchView searchView;
    private List<String> messagesList;
    private ArrayAdapter<String> adapter;

    public ChooseMessageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_message, container, false);

        listViewMessages = view.findViewById(R.id.listViewMessages);
        searchView = view.findViewById(R.id.searchView);

        messagesList = new ArrayList<>();


        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, messagesList);
        listViewMessages.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return view;
    }
}
