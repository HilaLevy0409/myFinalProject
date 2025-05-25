package com.example.myfinalproject.Message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;

import com.example.myfinalproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<Chat> chatList;
    private List<Chat> chatListFiltered;
    private LayoutInflater inflater;

    public ChatAdapter(Context context, List<Chat> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.chatListFiltered = new ArrayList<>(chatList);
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatListFiltered.size();
    }

    @Override
    public Object getItem(int position) {
        return chatListFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.onerow_message_list, parent, false);
            holder = new ViewHolder();
            holder.tvUsername = convertView.findViewById(R.id.tvUsername);
            holder.tvLastMessage = convertView.findViewById(R.id.tvLastMessage);
            holder.tvMessageTimeDate = convertView.findViewById(R.id.tvMessageTimeDate);
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.holderView = convertView.findViewById(R.id.holderView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Chat chat = chatListFiltered.get(position);

        holder.holderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatWithUser(chat.getOtherUserId(), chat.getOtherUserName());
            }
        });

        holder.tvUsername.setText(chat.getOtherUserName());
        holder.tvLastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "אין הודעות");

        if (chat.getLastMessageTime() != null) {
            holder.tvMessageTimeDate.setText(formatDateTime(chat.getLastMessageTime()));
        } else {
            holder.tvMessageTimeDate.setText("");
        }

        String base64Image = chat.getUserProfileImage();

        if ("ADMIN_DEFAULT".equals(base64Image)) {
            holder.imageView.setImageResource(R.drawable.newlogo);
        } else if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setImageResource(R.drawable.newlogo);
            }
        } else {
            holder.imageView.setImageResource(R.drawable.newlogo);
        }

        return convertView;
    }

    private void openChatWithUser(String receiverId, String receiverName) {
        try {
            Bundle args = new Bundle();
            args.putString("receiverId", receiverId);
            args.putString("receiverName", receiverName);

            MessageFragment messageFragment = new MessageFragment();
            messageFragment.setArguments(args);

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, messageFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy | HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Jerusalem"));
        return dateFormat.format(date);
    }

    public void updateChatList(List<Chat> newChatList) {
        this.chatList.clear();
        this.chatList.addAll(newChatList);
        this.chatListFiltered.clear();
        this.chatListFiltered.addAll(newChatList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Chat> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(chatList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Chat chat : chatList) {
                        if (chat.getOtherUserName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(chat);
                        }
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                chatListFiltered.clear();
                chatListFiltered.addAll((List<Chat>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    static class ViewHolder {
        TextView tvUsername;
        TextView tvLastMessage;
        TextView tvMessageTimeDate;
        ConstraintLayout holderView;
        ImageView imageView;
    }
}