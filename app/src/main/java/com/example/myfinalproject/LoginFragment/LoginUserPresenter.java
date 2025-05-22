package com.example.myfinalproject.LoginFragment;


import com.example.myfinalproject.CallBacks.LoginCallback;
import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Repositories.UserRepository;
import com.example.myfinalproject.DataModels.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginUserPresenter {

    private final LoginCallback view;
    private final UserRepository userDb;

    public LoginUserPresenter(LoginCallback view) {
        this.view = view;
        this.userDb = new UserRepository();
    }

//    public void loginUser(String username, String password) {
//        userDb.getUser(username, new UserCallback() {
//            @Override
//            public void onUserReceived(User user) {
//
//                String email = user.getUserEmail();
//
//                if (user.getUserPass().equals("RESET_WITH_FIREBASE_AUTH")) {
//                    FirebaseAuth auth = FirebaseAuth.getInstance();
//                    auth.signInWithEmailAndPassword(email, password)
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    user.setUserPass(password);
//                                    userDb.updateUser(user, new UserCallback() {
//                                        @Override
//                                        public void onUserReceived(User updatedUser) {
//                                            view.showLoginSuccess(updatedUser);
//                                        }
//
//                                        @Override
//                                        public void onError(String error) {
//                                            view.showLoginSuccess(user);
//                                        }
//                                    });
//                                } else {
//                                    view.showLoginFailure("סיסמה שגויה");
//                                }
//                            });
//                } else {
//                    if (user.getUserPass().equals(password)) {
//                        view.showLoginSuccess(user);
//                    } else {
//                        view.showLoginFailure("סיסמה שגויה");
//                    }
//                }
//            }
//
//            @Override
//            public void onError(String error) {
//                view.showLoginFailure(error);
//            }
//        });
//    }

    public void loginUser(String username, String password) {
       userDb.getUser(username, new UserCallback() {
           @Override
            public void onUserReceived(User user) {
                String email = user.getUserEmail();
                    if (user.getUserPass().equals(password)) {

                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        view.showLoginSuccess(user);
                                    } else {

                                        view.showLoginFailure("שגיאה בהתחברות ל-Firebase: " + task.getException().getMessage());
                                    }
                                });
                    } else {
                        view.showLoginFailure("סיסמה שגויה");
                    }

            }

            @Override
            public void onError(String error) {
                view.showLoginFailure(error);
            }
        });
    }

    /*
    public void loginUser(String username, String password) {
        userDb.getUser(username, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                String email = user.getUserEmail();

                if (user.getUserPass().equals("RESET_WITH_FIREBASE_AUTH")) {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    user.setUserPass(password);
                                    userDb.updateUser(user, new UserCallback() {
                                        @Override
                                        public void onUserReceived(User updatedUser) {
                                            view.showLoginSuccess(updatedUser);
                                        }

                                        @Override
                                        public void onError(String error) {
                                            view.showLoginFailure("שגיאה בעדכון המשתמש");
                                        }
                                    });
                                } else {
                                    view.showLoginFailure("סיסמה שגויה");
                                }
                            });
                } else {
                    if (user.getUserPass().equals(password)) {
                        view.showLoginSuccess(user);
                    } else {
                        view.showLoginFailure("סיסמה שגויה");
                    }
                }
            }

            @Override
            public void onError(String error) {
                view.showLoginFailure("משתמש לא נמצא");
            }
        });
    }
     */



}