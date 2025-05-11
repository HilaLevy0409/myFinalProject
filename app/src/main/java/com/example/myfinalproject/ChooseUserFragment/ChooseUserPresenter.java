package com.example.myfinalproject.ChooseUserFragment;

import com.example.myfinalproject.CallBacks.UsersCallback;
import com.example.myfinalproject.Repositories.UserRepository;
import com.example.myfinalproject.DataModels.User;

import java.util.ArrayList;
import java.util.List;

public class ChooseUserPresenter {

    private final ChooseUserFragment view;
    private final UserRepository userDatabase;
    private List<User> fullUserList = new ArrayList<>();

    public ChooseUserPresenter(ChooseUserFragment view) {
        this.view = view;
        this.userDatabase = new UserRepository();
    }

    public void loadUsers() {
        userDatabase.getAllUsers(new UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                fullUserList.clear();
                fullUserList.addAll(users);
                view.onUsersLoaded(users);
            }

            @Override
            public void onError(String message) {
                view.onUsersLoadError(message);
            }
        });
    }

    public void filterUsersByName(String query) {
        List<User> filteredList = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullUserList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (User user : fullUserList) {
                if (user.getUserName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(user);
                }
            }
        }

        view.onUsersFiltered(filteredList);
    }
}
