package com.example.myfinalproject.Message;

import java.util.ArrayList;
import java.util.List;

public class MessagePresenter implements MessageCallback.Presenter {

    private MessageCallback.View view;
    private List<Message> messages;

    public MessagePresenter(MessageCallback.View view) {
        this.view = view;
        this.messages = new ArrayList<>();
    }

    @Override
    public void sendMessage(String messageText) {
        Message newMessage = new Message(messageText, true);
        messages.add(newMessage);
        view.displayMessages(messages);
    }
}


