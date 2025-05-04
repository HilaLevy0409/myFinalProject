package com.example.myfinalproject.ChooseUserFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myfinalproject.Adapters.UserAdapter;
import com.example.myfinalproject.CallBacks.OnUserClickListener;
import com.example.myfinalproject.CallBacks.UsersCallback;

import com.example.myfinalproject.Message.MessageFragment;
import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;
import com.example.myfinalproject.ReportFragment.ReportFragment;
import com.example.myfinalproject.SumByUserFragment.SumByUserFragment;


import java.util.ArrayList;

import java.util.List;


public class ChooseUserFragment extends Fragment {

    private ListView listViewUsers;
    private UserAdapter userAdapter;
    private ArrayList<User> userList;
    private ChooseUserPresenter presenter;
    private SearchView searchView;
    private ArrayList<User> fullUserList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userList = new ArrayList<>();
        presenter = new ChooseUserPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_user, container, false);
        loadUsers();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listViewUsers = view.findViewById(R.id.listViewUsers);
        searchView = view.findViewById(R.id.searchView);

        OnUserClickListener reportClickListener = position -> {
            navigateToReportFragment(userList.get(position));
        };

        OnUserClickListener summaryClickListener = position -> {
            navigateToSummaryByUserFragment(userList.get(position));
        };

        OnUserClickListener messageClickListener = position -> {
            navigateToMessagesFragment(userList.get(position));
        };

        userAdapter = new UserAdapter(
                getContext(),
                userList,
                reportClickListener,
                summaryClickListener,
                messageClickListener
        );

        listViewUsers.setAdapter(userAdapter);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsersByName(newText);
                return true;
            }
        });
    }

    private void navigateToReportFragment(User user) {
        if (getActivity() == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("userName", user.getUserName());

        ReportFragment reportFragment = new ReportFragment();
        reportFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, reportFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToSummaryByUserFragment(User user) {
        if (getActivity() == null) return;

        SumByUserFragment sumByUserFragment = SumByUserFragment.newInstance(user.getUserName());

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, sumByUserFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToMessagesFragment(User user) {
        if (getActivity() == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("receiverId", user.getId());
        bundle.putString("receiverName", user.getUserName());

        MessageFragment messagesFragment = new MessageFragment();
     messagesFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, messagesFragment)
                .addToBackStack(null)
                .commit();
    }

    private void filterUsersByName(String query) {
        userList.clear();
        if (query.isEmpty()) {
            userList.addAll(fullUserList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (User user : fullUserList) {
                if (user.getUserName().toLowerCase().contains(lowerCaseQuery)) {
                    userList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    private void loadUsers() {
        presenter.loadUsers(new UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    userList.clear();
                    fullUserList.clear();
                    fullUserList.addAll(users);
                    userList.addAll(users);
                    userAdapter.notifyDataSetChanged();
                });
            }


            @Override
            public void onError(String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "שגיאה בטעינת משתמשים:  " + message,
                                Toast.LENGTH_SHORT).show()
                );
            }
        });
    }



    public ChooseUserFragment() {
    }


    public static ChooseUserFragment newInstance() {
        ChooseUserFragment fragment = new ChooseUserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }


}


