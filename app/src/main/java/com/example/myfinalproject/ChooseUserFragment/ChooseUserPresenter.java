package com.example.myfinalproject.ChooseUserFragment;

import com.example.myfinalproject.CallBacks.UsersCallback;
import com.example.myfinalproject.Repositories.UserRepository;
import com.example.myfinalproject.DataModels.User;

import java.util.ArrayList;
import java.util.List;

public class ChooseUserPresenter {

    private final ChooseUserFragment view;         // הפניה ל־Fragment שמציג את המשתמשים
    private final UserRepository userDatabase;     // מחלקה שאחראית על שליפת נתוני משתמשים
    private List<User> fullUserList = new ArrayList<>();  // רשימה מלאה של כל המשתמשים (לשימוש בסינון)

    public ChooseUserPresenter(ChooseUserFragment view) {
        this.view = view;
        this.userDatabase = new UserRepository();
    }

    // פעולה שמביאה את כל המשתמשים מהמסד נתונים דרך UserRepository
    public void loadUsers() {
        userDatabase.getAllUsers(new UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                // ניקוי והוספה של המשתמשים שהתקבלו לרשימה מלאה
                fullUserList.clear();
                fullUserList.addAll(users);

                // קריאה לפונקציה בתצוגה עם הנתונים שהתקבלו
                view.onUsersLoaded(users);
            }

            @Override
            public void onError(String message) {
                view.onUsersLoadError(message);
            }
        });
    }


    // פעולה שמסננת את המשתמשים לפי שם ומחזירה לרשימה חדשה רק את אלו שעונים על החיפוש
    public void filterUsersByName(String query) {
        List<User> filteredList = new ArrayList<>();

        // אם לא הוזן טקסט לחיפוש – מחזירים את כל המשתמשים
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullUserList);
        } else {
            // אחרת, מבצעים סינון לפי השם
            String lowerCaseQuery = query.toLowerCase().trim();
            for (User user : fullUserList) {
                if (user.getUserName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(user);
                }
            }
        }
        // עדכון התצוגה עם הרשימה המסוננת
        view.onUsersFiltered(filteredList);
    }
}
