package com.example.myfinalproject.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.DataModels.Message;
import com.example.myfinalproject.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messages;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.isSent() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onerow_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if (message.isSent()) {
            holder.sentLayout.setVisibility(View.VISIBLE);
            holder.receivedLayout.setVisibility(View.GONE);

            holder.tvSentMessage.setText(message.getText());

            if (message.getTimestamp() != null) {
                holder.tvSentTime.setText((message.getTimestamp()));
            }
        } else {
            holder.receivedLayout.setVisibility(View.VISIBLE);
            holder.sentLayout.setVisibility(View.GONE);

            holder.tvReceivedMessage.setText(message.getText());

            if (message.getTimestamp() != null) {
                holder.tvReceivedTime.setText((message.getTimestamp()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout sentLayout;
        ConstraintLayout receivedLayout;
        TextView tvSentMessage;
        TextView tvReceivedMessage;
        TextView tvSentTime;
        TextView tvReceivedTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentLayout = itemView.findViewById(R.id.sentLayout);
            receivedLayout = itemView.findViewById(R.id.receivedLayout);
            tvSentMessage = itemView.findViewById(R.id.tvSentMessage);
            tvReceivedMessage = itemView.findViewById(R.id.tvReceivedMessage);
            tvSentTime = itemView.findViewById(R.id.tvSentTime);
            tvReceivedTime = itemView.findViewById(R.id.tvReceivedTime);
        }
    }
}