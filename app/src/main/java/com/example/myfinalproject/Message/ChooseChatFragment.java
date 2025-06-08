package com.example.myfinalproject.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myfinalproject.Admin;
import com.example.myfinalproject.R;
import com.example.myfinalproject.LoginFragment.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChooseChatFragment extends Fragment {

    private ListView listViewMessages;
    private SearchView searchView;
    private List<Chat> chatsList;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    public ChooseChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_message, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // בדיקה אם המשתמש מחובר או אם זה מנהל
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid(); // משתמש רגיל
        } else if (Admin.isAdminLoggedIn()) {
            currentUserId = "admin"; // התחברות של מנהל
        } else {
            Toast.makeText(getContext(), "יש להתחבר תחילה", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, new LoginFragment())
                        .commit();
            }
            return view;
        }

        listViewMessages = view.findViewById(R.id.listViewMessages);
        searchView = view.findViewById(R.id.searchView);

        // אתחול רשימת שיחות ואדפטר
        chatsList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatsList, new OnChatClickListener() {
            @Override
            public void onChatSelected(String userId, String userName) {
                openChatWithUser(userId, userName);
            }
        });

        listViewMessages.setAdapter(chatAdapter); // חיבור האדפטר לרשימה

        // האזנה לשינויים בטקסט החיפוש
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chatAdapter.getFilter().filter(newText); // מסנן את רשימת השיחות לפי הקלט
                return false;
            }
        });

        loadUserChats();

        return view;
    }

    // פעולה שמביאה את השיחות של המשתמש ממסד הנתונים
    private void loadUserChats() {

        db.collection("chats")
                .whereArrayContains("participants", currentUserId) // מחפש שיחות שמשתמש בהן המשתמש הנוכחי
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "שגיאה בטעינת ההתכתבויות", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.getDocuments().isEmpty()) {
                        chatAdapter.updateChatList(new ArrayList<>()); // אין שיחות
                        return;
                    }
                    List<Chat> tempChatsList = new ArrayList<>();
                    int totalChats = 0;

                    // סופרים רק שיחות תקינות (1 או 2 משתתפים)
                    for (DocumentSnapshot document : value.getDocuments()) {
                        List<String> participants = (List<String>) document.get("participants");
                        if (participants != null && (participants.size() == 1 || participants.size() == 2)) {
                            totalChats++;
                        }
                    }

                    // אם מספר השיחות הכולל הוא 0 – אין שיחות להצגה
                    if (totalChats == 0) {
                        // מעדכן את רשימת השיחות באדפטר לרשימה ריקה – כדי שלא יוצגו שיחות במסך
                        chatAdapter.updateChatList(new ArrayList<>());

                        // יוצא מהפונקציה מייד – לא ממשיך הלאה
                        return;
                    }

                    // מעבר על כל שיחה ובניית אובייקט Chat
                    for (DocumentSnapshot document : value.getDocuments()) {
                        String chatId = document.getId();
                        List<String> participants = (List<String>) document.get("participants");
                        String lastMessage = document.getString("lastMessage");
                        Date lastMessageTime = document.getDate("lastMessageTime");

                        if (participants == null || (participants.size() != 1 && participants.size() != 2)) continue;

                        // זיהוי הצד השני בשיחה (אם זה עם עצמי או עם מישהו אחר)
                        String otherUserId = (participants.size() == 1)
                                ? currentUserId // שיחה עם עצמי
                                : (participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0));

                        final String finalOtherUserId = otherUserId;
                        final int finalTotalChats = totalChats;

                        if (finalOtherUserId.equals("admin")) {
                            // שיחה עם ההנהלה
                            String adminName = "הנהלה";
                            Chat chat = new Chat(chatId, finalOtherUserId, adminName, lastMessage, lastMessageTime, "ADMIN_DEFAULT");

                            synchronized (tempChatsList) {
                                tempChatsList.add(chat);
                                if (tempChatsList.size() == finalTotalChats) {
                                    updateSortedChatList(tempChatsList); // עדכון הרשימה רק כשסיימנו להוסיף הכל
                                }
                            }
                        } else {
                            // שיחה עם משתמש רגיל – מביאים את פרטי המשתמש מה-DB
                            db.collection("users")
                                    .document(finalOtherUserId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String otherUserName;
                                        String userProfileImage;

                                        if (finalOtherUserId.equals(currentUserId)) {
                                            // שיחה עם עצמי
                                            otherUserName = userDoc.getString("userName");
                                            if (otherUserName == null || otherUserName.trim().isEmpty()) {
                                                otherUserName = userDoc.getString("name");
                                            }
                                            if (otherUserName == null || otherUserName.trim().isEmpty()) {
                                                otherUserName = "אני";
                                            } else {
                                                otherUserName += " (אני)";
                                            }

                                            if (Admin.isAdminLoggedIn()) {
                                                // מזהה שנשלח לאדפטר שיטפל בתמונה בעצמו
                                                userProfileImage = "ADMIN_DEFAULT";
                                            } else {
                                                userProfileImage = userDoc.getString("imageProfile");
                                                if (userProfileImage == null) {
                                                    userProfileImage = "";
                                                }
                                            }

                                        } else {
                                            // משתמש אחר
                                            otherUserName = userDoc.getString("userName");
                                            if (otherUserName == null || otherUserName.trim().isEmpty()) {
                                                otherUserName = userDoc.getString("name");
                                            }
                                            if (otherUserName == null || otherUserName.trim().isEmpty()) {
                                                otherUserName = "משתמש לא ידוע";
                                            }

                                            userProfileImage = userDoc.getString("imageProfile");
                                        }

                                        Chat chat = new Chat(chatId, finalOtherUserId, otherUserName, lastMessage, lastMessageTime, userProfileImage);

                                        synchronized (tempChatsList) {
                                            tempChatsList.add(chat);
                                            if (tempChatsList.size() == finalTotalChats) {
                                                updateSortedChatList(tempChatsList);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // אם קרתה שגיאה, נשתמש בנתונים כלליים
                                        String fallbackName = finalOtherUserId.equals(currentUserId) ? "אני" : "משתמש לא ידוע";
                                        Chat chat = new Chat(chatId, finalOtherUserId, fallbackName, lastMessage, lastMessageTime, null);

                                        synchronized (tempChatsList) {
                                            tempChatsList.add(chat);
                                            if (tempChatsList.size() == finalTotalChats) {
                                                updateSortedChatList(tempChatsList);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    // ממיינת את רשימת השיחות לפי זמן הודעה אחרונה (מהחדש לישן)
    private void updateSortedChatList(List<Chat> chatList) {
        Collections.sort(chatList, (c1, c2) -> {
            if (c1.getLastMessageTime() == null && c2.getLastMessageTime() == null) return 0;
            if (c1.getLastMessageTime() == null) return 1;
            if (c2.getLastMessageTime() == null) return -1;
            return c2.getLastMessageTime().compareTo(c1.getLastMessageTime());
        });

        chatAdapter.updateChatList(new ArrayList<>(chatList));
    }

    // פתיחת שיחה עם משתמש ספציפי
    private void openChatWithUser(String receiverId, String receiverName) {
        try {
            Bundle args = new Bundle();
            args.putString("receiverId", receiverId);
            args.putString("receiverName", receiverName);

            MessageFragment messageFragment = new MessageFragment();
            messageFragment.setArguments(args);

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, messageFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "שגיאה בפתיחת השיחה", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "שגיאה בפתיחת השיחה", Toast.LENGTH_SHORT).show();
        }
    }
}

// שימוש במילת המפתח synchronized כדי למנוע מצב שבו מספר תהליכונים (Threads)
// ייגשו בו-זמנית לרשימת tempChatsList. זה מבטיח שרק תהליכון אחד בכל רגע נתון
// יוכל להיכנס לבלוק הקוד הזה ולבצע שינוי ברשימה. כך נמנעים מתקלות כמו נתונים כפולים,
// שגיאות בגישה לרשימה, או קריסות עקב גישה בו-זמנית לאובייקט משותף.