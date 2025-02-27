package com.example.myfinalproject.ChooseUserFragment;

import com.example.myfinalproject.CallBacks.UsersCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Database.UserDatabase;
import com.example.myfinalproject.Models.User;


import java.util.ArrayList;
import java.util.List;

public class ChooseUserPresenter {

    private final ChooseUserFragment view;
    private final UserDatabase userDatabase;

    public ChooseUserPresenter(ChooseUserFragment view) {
        this.view = view;
        this.userDatabase = new UserDatabase();
    }

    public void loadUsers(UsersCallback callback) {
        userDatabase.getAllUsers(new UsersCallback() {

           @Override
          public void onSuccess(List<User> users) {
                callback.onSuccess(users);
            }

            @Override
            public void onError(String message) {
                 callback.onError(message);
            }
      });
    }

//    public void updateUser(User user) {
//        userDatabase.updateUser(user, new UserCallback() {
//            @Override
//            public void onSuccess(User user1) {
//                //     view.onUserUpdated(users1);
//            }
//
//            @Override
//            public void onError(String message) {
//                view.onError(message);
//            }
//        });
//    }

//    public void deleteUser(String userId) {
//       userDatabase.deleteUser(userId, new UserCallback() {
//            @Override
//            public void onSuccess(User user1) {
//
//            }

//            @Override
//            public void onError(String message) {
//                view.onError(message);
//            }
//        });
//    }

//    public void filterUsers(String query, ArrayList<User> originalList, UserAdapter adapter) {
//        ArrayList<User> filteredList = new ArrayList<>();
//
//        for (User user : originalList) {
//            if (user.getUserName().toLowerCase().contains(query.toLowerCase()) ||
//                    user.getSumCount().toLowerCase().contains(query.toLowerCase())) {
//                filteredList.add(user);
//            }
//        }
//
//        adapter.updateUsers(filteredList);
//    }
}
