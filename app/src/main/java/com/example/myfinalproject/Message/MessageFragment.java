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

import com.example.myfinalproject.Adapters.MessageAdapter;
import com.example.myfinalproject.DataModels.Message;
import com.example.myfinalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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


        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
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

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        etMessageInput = view.findViewById(R.id.etMessageInput);
        tvUserName = view.findViewById(R.id.tvUserName);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        imgBtnBack = view.findViewById(R.id.imgBtnBack);

        if (getArguments() != null) {
            receiverId = getArguments().getString("receiverId");
            String receiverName = getArguments().getString("receiverName");
            tvUserName.setText(receiverName);

            chatId = generateChatId(currentUserId, receiverId);
            Log.d(TAG, "Chat ID: " + chatId);
        }

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);

        loadMessages();

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
                    .replace(R.id.flFragment, new ChooseMessageFragment())
                    .commit();
        });

        return view;
    }

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


    private void loadMessages() {
        if (chatId == null) {
            Log.e(TAG, "Chat ID is null. Cannot load messages.");
            return;
        }

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading messages", error);
                        return;
                    }

                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                QueryDocumentSnapshot document = dc.getDocument();
                                String messageText = document.getString("text");
                                String senderId = document.getString("senderId");
                                String receiverId = document.getString("receiverId");
                                Date timestamp = document.getDate("timestamp");

                                Log.d("MessagFragment", "sender: " + senderId);
                                Log.d("MessagFragment", "receiver: " + receiverId);
                                boolean isSent = senderId.equals(currentUserId);
                                Message message = new Message(messageText, isSent, timestamp);
                                messages.add(message);
                            }
                        }
                        messageAdapter.notifyDataSetChanged();

                        if (messages.size() > 0) {
                            recyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }
                });
    }


    private void sendMessage(String messageText) {
        if (chatId == null) {
            Log.e(TAG, "Chat ID is null. Cannot send message.");
            return;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", messageText);
        messageData.put("senderId", currentUserId);
        messageData.put("receiverId", receiverId);
        messageData.put("timestamp", getIsraelTime());
        messageData.put("read", false);

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message sent with ID: " + documentReference.getId());
                    etMessageInput.setText("");

                    updateChatMetadata(messageText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(getContext(), "שגיאה בשליחת ההודעה", Toast.LENGTH_SHORT).show();
                });
    }


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
}