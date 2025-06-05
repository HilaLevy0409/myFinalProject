package com.example.myfinalproject.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Admin;
import com.example.myfinalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.myfinalproject.LoginFragment.LoginFragment;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MessageFragment extends Fragment {

    private static final String TAG = "MessageFragment";
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private EditText etMessageInput;
    private TextView tvUserName;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId, receiverId, chatId;
    private ImageButton imgBtnBack;

    public MessageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // בדיקת משתמש מחובר או מנהל
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        } else if (Admin.isAdminLoggedIn()) {
            currentUserId = "admin";
        } else {
            // אם אין משתמש – חזרה למסך התחברות
            Toast.makeText(getContext(), "יש להתחבר תחילה", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, new LoginFragment())
                        .commit();
            }
            return view;
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        etMessageInput = view.findViewById(R.id.etMessageInput);

        tvUserName = view.findViewById(R.id.tvUserName);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        imgBtnBack = view.findViewById(R.id.imgBtnBack);

        // קבלת מזהי המשתמש והצ'אט מה־Arguments שהועברו
        if (getArguments() != null) {
            receiverId = getArguments().getString("receiverId");
            String receiverName = getArguments().getString("receiverName");
            tvUserName.setText(receiverName);

            chatId = generateChatId(currentUserId, receiverId);

            checkIfAdminStartedChat(); // בדיקה אם המנהל התחיל את השיחה
        }

        // אתחול רשימת הודעות ואדפטר
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);

        loadMessages(); // טעינת ההודעות ממסד הנתונים

        btnSend.setOnClickListener(v -> {
            String messageText = etMessageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            } else {
                Toast.makeText(getContext(), "הקלידו הודעה לשליחה", Toast.LENGTH_SHORT).show();
            }
        });

        imgBtnBack.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new ChooseChatFragment())
                    .commit();
        });

        return view;
    }

    // יצירת מזהה ייחודי לצ'אט לפי שני המשתמשים
    private String generateChatId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    private Date getIsraelTime() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        return calendar.getTime();
    }

    // טעינת ההודעות של הצ'אט מהמסד
    private void loadMessages() {
        if (chatId == null) {
            return;
        }
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        messages.clear();
                        Date lastDateHeader = null;

                        for (DocumentSnapshot document : value.getDocuments()) {
                            String messageText = document.getString("text");
                            String senderId = document.getString("senderId");
                            Date timestamp = document.getDate("timestamp");

                            boolean isSent = senderId.equals(currentUserId);

                            // הוספת כותרת תאריך אם זהו יום חדש
                            if (lastDateHeader == null || !isSameDay(lastDateHeader, timestamp)) {
                                messages.add(new Message(timestamp));
                                lastDateHeader = timestamp;
                            }
                            messages.add(new Message(messageText, isSent, timestamp));
                        }
                        messageAdapter.notifyDataSetChanged();

                        // גלילה להודעה האחרונה
                        if (messages.size() > 0) {
                            recyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }

    // בדיקה אם שני תאריכים הם באותו יום
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // שליחת הודעה לצ'אט
    private void sendMessage(String messageText) {
        if (chatId == null) {
            return;
        }

        String senderId;
        String senderName;

        // קביעת השולח לפי הרשאות
        if (Admin.isAdminLoggedIn()) {
            senderId = "admin";
            senderName = "הנהלה";
        } else {
            senderId = currentUserId;
            senderName = auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null
                    ? auth.getCurrentUser().getDisplayName()
                    : "משתמש";
        }

        // יצירת אובייקט של הודעה
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", messageText);
        messageData.put("senderId", senderId);
        messageData.put("senderName", senderName);
        messageData.put("receiverId", receiverId);
        messageData.put("timestamp", getIsraelTime());

        // שליחת ההודעה למסד
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    etMessageInput.setText("");

                    updateChatMetadata(messageText); // עדכון הודעה אחרונה של השיחה
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בשליחת ההודעה", Toast.LENGTH_SHORT).show();
                });
    }

    // עדכון נתוני צ'אט (למשל הודעה אחרונה וזמן)
    private void updateChatMetadata(String lastMessage) {
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("lastMessage", lastMessage);
        chatData.put("lastMessageTime", getIsraelTime());

        List<String> participants = new ArrayList<>();
        participants.add(currentUserId);
        participants.add(receiverId);
        chatData.put("participants", participants);

        db.collection("chats")
                .document(chatId)
                .set(chatData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat metadata updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating chat metadata", e));
    }

    // בדיקה אם המנהל התחיל את השיחה (ולא לאפשר למשתמש לענות)
    private void checkIfAdminStartedChat() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot firstMessage = queryDocumentSnapshots.getDocuments().get(0);
                        String senderId = firstMessage.getString("senderId");

                        // אם ההודעה הראשונה מהנהלה, המשתמש לא יוכל להשיב
                        if (!Admin.isAdminLoggedIn() && "admin".equals(senderId)) {
                            etMessageInput.setVisibility(View.GONE);
                            getView().findViewById(R.id.btnSend).setVisibility(View.GONE);
                            Toast.makeText(getContext(), "לא ניתן להשיב להודעות הנהלה", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to check who started chat", e));
    }
}