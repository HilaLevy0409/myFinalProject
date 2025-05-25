package com.example.myfinalproject.Message;

import java.util.List;

public interface MessageCallback {
    interface View {
        void displayMessages(List<Message> messages);
    }

    interface Presenter {
        void sendMessage(String messageText);
    }
}

