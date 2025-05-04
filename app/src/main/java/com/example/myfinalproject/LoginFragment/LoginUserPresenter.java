package com.example.myfinalproject.LoginFragment;

import com.example.myfinalproject.CallBacks.UserCallback;
import com.example.myfinalproject.Database.UserDatabase;
import com.example.myfinalproject.Models.User;
import com.google.firebase.auth.FirebaseAuth;

public class LoginUserPresenter {

    private final LoginView view;
    private final UserDatabase userDb;

    public LoginUserPresenter(LoginView view) {
        this.view = view;
        this.userDb = new UserDatabase();
    }

    public void loginUser(String username, String password) {
        userDb.getUser(username, new UserCallback() {
            @Override
            public void onUserReceived(User user) {
                // Get the email from the user object
                String email = user.getUserEmail();

                // Check if the password is the placeholder from a reset
                if (user.getUserPass().equals("RESET_WITH_FIREBASE_AUTH")) {
                    // This user reset their password via email, authenticate directly with Firebase
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Update the password in Firestore
                                    user.setUserPass(password);
                                    userDb.updateUser(user, new UserCallback() {
                                        @Override
                                        public void onUserReceived(User updatedUser) {
                                            view.showLoginSuccess(updatedUser);
                                        }

                                        @Override
                                        public void onError(String error) {
                                            view.showLoginSuccess(user);
                                        }
                                    });
                                } else {
                                    view.showLoginFailure("סיסמה שגויה");
                                }
                            });
                } else {
                    // Normal login flow
                    if (user.getUserPass().equals(password)) {
                        view.showLoginSuccess(user);
                    } else {
                        view.showLoginFailure("סיסמה שגויה");
                    }
                }
            }

            @Override
            public void onError(String error) {
                view.showLoginFailure(error);
            }
        });
    }


}