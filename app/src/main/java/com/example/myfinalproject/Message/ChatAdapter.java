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
    private List<Chat> chatList; // רשימת השיחות המלאה
    private List<Chat> chatListFiltered; // רשימת שיחות מסוננת (לפי חיפוש)
    private LayoutInflater inflater; // אחראי על יצירת תצוגת שורה אחת

    private OnChatClickListener chatClickListener;


    public ChatAdapter(Context context, List<Chat> chatList,  OnChatClickListener listener) {
        this.context = context;
        this.chatList = chatList;
        this.chatListFiltered = new ArrayList<>(chatList); // העתקה לרשימה מסוננת
        this.inflater = LayoutInflater.from(context);
        this.chatClickListener = listener;
    }

    @Override
    public int getCount() {
        // מחזיר את מספר הפריטים ברשימה
        return chatListFiltered.size();
    }

    @Override
    public Object getItem(int position) {
        // מחזיר את האובייקט שנמצא במקום מסוים ברשימה (לפי האינדקס)
        return chatListFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        // מחזיר מזהה ייחודי לכל פריט – כאן פשוט מחזירים את המיקום שלו כרשומה
        // אפשר להשתמש בזה במקרים שבהם צריך מזהה יציב לפריט (כמו בעת קליקים)
        return position;
    }

    // יוצר/ממחזר תצוגת שורה ברשימת השיחות
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // אם עדיין לא נוצרה שורה – ניצור אותה
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.onerow_message_list, parent, false);
            holder = new ViewHolder();
            holder.tvUsername = convertView.findViewById(R.id.tvUsername);
            holder.tvLastMessage = convertView.findViewById(R.id.tvLastMessage);
            holder.tvMessageTimeDate = convertView.findViewById(R.id.tvMessageTimeDate);
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.holderView = convertView.findViewById(R.id.holderView);

            convertView.setTag(holder); // שמירת הקישור בתצוגה לשימוש חוזר
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Chat chat = chatListFiltered.get(position);

        holder.holderView.setOnClickListener(v -> {
            if (chatClickListener != null) {
                chatClickListener.onChatSelected(chat.getOtherUserId(), chat.getOtherUserName());
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
            // אם זו תמונת ברירת מחדל של מנהל – נטען לוגו ברירת מחדל
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

    // פעולה לפתיחת שיחה עם משתמש אחר
    private void openChatWithUser(String receiverId, String receiverName) {
        try {
            Bundle args = new Bundle();
            args.putString("receiverId", receiverId);
            args.putString("receiverName", receiverName);

            MessageFragment messageFragment = new MessageFragment();
            messageFragment.setArguments(args);

// בדיקה: האם ה-context הוא מסוג FragmentActivity (כלומר Activity שתומכת בניהול פרגמנטים)
            if (context instanceof FragmentActivity) {
                // המרה (casting): מאחר שה-context הוא FragmentActivity, אפשר להמיר אותו למשתנה מהסוג FragmentActivity
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

    // פעולה לעדכון רשימת השיחות, אחרי סינון
    public void updateChatList(List<Chat> newChatList) {
        this.chatList.clear();
        this.chatList.addAll(newChatList);
        this.chatListFiltered.clear();
        this.chatListFiltered.addAll(newChatList);
        notifyDataSetChanged();
    }

    // מימוש תמיכה בסינון (חיפוש)
    @Override
    public Filter getFilter() {
        return new Filter() {
            // תהליך הסינון עצמו (מאחורי הקלעים)
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Chat> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(chatList);  // אם אין טקסט חיפוש – החזר הכל
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Chat chat : chatList) {
                        if (chat.getOtherUserName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(chat); // מוסיף רק התאמות לשם משתמש
                        }
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            // הצגת התוצאות המסוננות בפועל
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