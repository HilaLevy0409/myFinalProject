package com.example.myfinalproject.Message;

import android.net.Uri;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChooseMessageFragment extends Fragment {

    private static final String TAG = "ChooseMessageFragment";
    private ListView listViewMessages;
    private SearchView searchView;
    private List<Chat> chatsList;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    public ChooseMessageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_message, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Fix: Handle both regular user and admin login
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        } else if (Admin.isAdminLoggedIn()) {
            currentUserId = "admin";
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

        chatsList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatsList);
        listViewMessages.setAdapter(chatAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                chatAdapter.getFilter().filter(newText);
                return false;
            }
        });

        listViewMessages.setOnItemClickListener((parent, view1, position, id) -> {
            Log.d(TAG, "ListView item clicked at position: " + position);

            try {

                Object item = chatAdapter.getItem(position);
                Log.d(TAG, "Item retrieved: " + (item != null ? item.getClass().getSimpleName() : "null"));

                if (item instanceof Chat) {
                    Chat selectedChat = (Chat) item;
                    Log.d(TAG, "Chat selected: " + selectedChat.getOtherUserId() + ", " + selectedChat.getOtherUserName());
                    openChatWithUser(selectedChat.getOtherUserId(), selectedChat.getOtherUserName());
                } else {
                    Log.e(TAG, "Item is not a Chat object: " + item);
                    Toast.makeText(getContext(), "שגיאה בפתיחת השיחה", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling item click", e);
                Toast.makeText(getContext(), "שגיאה בפתיחת השיחה", Toast.LENGTH_SHORT).show();
            }
        });

        loadUserChats();

        return view;
    }

    private void loadUserChats() {
        Log.d(TAG, "Loading chats for user: " + currentUserId);

        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading chats", error);
                        Toast.makeText(getContext(), "שגיאה בטעינת ההתכתבויות", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.getDocuments().isEmpty()) {
                        Log.d(TAG, "No chats found for user: " + currentUserId);
                        chatAdapter.updateChatList(new ArrayList<>());
                        return;
                    }

                    Log.d(TAG, "Found " + value.getDocuments().size() + " chats");

                    List<Chat> tempChatsList = new ArrayList<>();
                    int totalChats = 0;

                    for (DocumentSnapshot document : value.getDocuments()) {
                        List<String> participants = (List<String>) document.get("participants");
                        if (participants != null && (participants.size() == 1 || participants.size() == 2)) {
                            totalChats++;
                        }
                    }

                    if (totalChats == 0) {
                        chatAdapter.updateChatList(new ArrayList<>());
                        return;
                    }

                    for (DocumentSnapshot document : value.getDocuments()) {
                        String chatId = document.getId();
                        List<String> participants = (List<String>) document.get("participants");
                        String lastMessage = document.getString("lastMessage");
                        Date lastMessageTime = document.getDate("lastMessageTime");

                        if (participants == null || (participants.size() != 1 && participants.size() != 2)) continue;

                        String otherUserId = (participants.size() == 1)
                                ? currentUserId // שיחה עם עצמי
                                : (participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0));

                        final String finalOtherUserId = otherUserId;
                        final int finalTotalChats = totalChats;

                        // Fix: Handle admin user lookup
                        if (finalOtherUserId.equals("admin")) {
                            String adminName = "הנהלה";
                            Chat chat = new Chat(chatId, finalOtherUserId, adminName, lastMessage, lastMessageTime, "ADMIN_DEFAULT");

                            synchronized (tempChatsList) {
                                tempChatsList.add(chat);
                                if (tempChatsList.size() == finalTotalChats) {
                                    updateSortedChatList(tempChatsList);
                                }
                            }
                        } else {
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
                                        Log.e(TAG, "Error loading user name for ID: " + finalOtherUserId, e);
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

    private void updateSortedChatList(List<Chat> chatList) {
        Collections.sort(chatList, (c1, c2) -> {
            if (c1.getLastMessageTime() == null && c2.getLastMessageTime() == null) return 0;
            if (c1.getLastMessageTime() == null) return 1;
            if (c2.getLastMessageTime() == null) return -1;
            return c2.getLastMessageTime().compareTo(c1.getLastMessageTime());
        });

        chatAdapter.updateChatList(new ArrayList<>(chatList));
    }

    private void openChatWithUser(String receiverId, String receiverName) {
        Log.d(TAG, "Opening chat with user: " + receiverId + ", name: " + receiverName);

        try {
            Bundle args = new Bundle();
            args.putString("receiverId", receiverId);
            args.putString("receiverName", receiverName);

            MessageFragment messageFragment = new MessageFragment();
            messageFragment.setArguments(args);

            if (getActivity() != null) {
                Log.d(TAG, "Starting fragment transaction");
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, messageFragment)
                        .addToBackStack(null)
                        .commit();
                Log.d(TAG, "Fragment transaction committed");
            } else {
                Log.e(TAG, "Activity is null, cannot open chat");
                Toast.makeText(getContext(), "שגיאה בפתיחת השיחה", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening chat", e);
            Toast.makeText(getContext(), "שגיאה בפתיחת השיחה", Toast.LENGTH_SHORT).show();
        }
    }
}