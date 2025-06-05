package com.example.myfinalproject.ChooseUserFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.myfinalproject.Adapters.UserAdapter;
import com.example.myfinalproject.CallBacks.OnUserClickListener;

import com.example.myfinalproject.Message.MessageFragment;
import com.example.myfinalproject.DataModels.User;
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
        return inflater.inflate(R.layout.fragment_choose_user, container, false);
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

// יצירת מופע של UserAdapter - אחראי על חיבור הנתונים של המשתמשים לתצוגה ברשימה
        userAdapter = new UserAdapter(
                getContext(),
                userList,
                reportClickListener,
                summaryClickListener,
                messageClickListener
        );

// הצמדת ה־Adapter לרכיב התצוגה ListView - כך שהרשימה תוצג למשתמש בפועל
        listViewUsers.setAdapter(userAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.filterUsersByName(newText);
                return true;
            }
        });
        presenter.loadUsers();
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

        SumByUserFragment sumByUserFragment = SumByUserFragment.newInstance(user.getId(), user.getUserName());

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
// מופעלת כאשר המשתמשים נטענו בהצלחה מהמסד נתונים
    public void onUsersLoaded(List<User> users) {
        userList.clear();             // ניקוי הרשימה שמוצגת כרגע למשתמש
        fullUserList.clear();        // ניקוי הרשימה המלאה של המשתמשים
        fullUserList.addAll(users);  // שמירה של כל המשתמשים ברשימה מלאה (שימושית לחיפוש וסינון)
        userList.addAll(users);      // העתקה גם לרשימה שמוצגת בפועל
        userAdapter.notifyDataSetChanged(); // עדכון ה־Adapter כדי שירענן את התצוגה
    }

    public void onUsersLoadError(String message) {
        Toast.makeText(getContext(), "שגיאה בטעינת משתמשים: " + message, Toast.LENGTH_SHORT).show();
    }

    // מופעלת לאחר סינון המשתמשים
    public void onUsersFiltered(List<User> filteredUsers) {
        userList.clear();                  // ניקוי הרשימה הקיימת מהתוצאות הישנות
        userList.addAll(filteredUsers);   // הוספת התוצאות המסוננות לרשימה שמוצגת
        userAdapter.notifyDataSetChanged(); // עדכון ה־Adapter כדי שירענן את התצוגה
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


