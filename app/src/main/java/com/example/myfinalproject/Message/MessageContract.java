package com.example.myfinalproject.Message;

import com.example.myfinalproject.DataModels.Message;

import java.util.List;

public interface MessageContract {
    interface View {
        void displayMessages(List<Message> messages);
    }

    interface Presenter {
        void sendMessage(String messageText);
    }
}

