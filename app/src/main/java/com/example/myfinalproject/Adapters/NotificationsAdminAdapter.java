package com.example.myfinalproject.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.CallBacks.OnNotificationClickListenerCallback;
import com.example.myfinalproject.DataModels.NotificationAdmin;
import com.example.myfinalproject.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationsAdminAdapter extends RecyclerView.Adapter<NotificationsAdminAdapter.NotificationViewHolder> {

    private List<NotificationAdmin> notificationsList;
    private final OnNotificationClickListenerCallback callback;


    public NotificationsAdminAdapter(List<NotificationAdmin> notificationsList, OnNotificationClickListenerCallback callback) {
        this.notificationsList = notificationsList != null ? notificationsList : new ArrayList<>();
        this.callback = callback;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onerow_admin_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationAdmin notification = notificationsList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public void updateData(List<NotificationAdmin> newNotifications) {
        this.notificationsList = newNotifications;
        notifyDataSetChanged();
    }


    public void removeNotification(NotificationAdmin notification) {
        int position = notificationsList.indexOf(notification);
        if (position != -1) {
            notificationsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvTimestamp;
        private final TextView tvContent;
        private final TextView tvType;
        private final TextView tvReason;
        private final MaterialCardView cardNotification;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvType = itemView.findViewById(R.id.tvType);
            tvReason = itemView.findViewById(R.id.tvReason);
            cardNotification = itemView.findViewById(R.id.cardNotification);
        }

        public void bind(NotificationAdmin notification) {
            tvUserName.setText(notification.getUserName());
            tvContent.setText(notification.getContent());


            Timestamp timestamp = notification.getTimestamp();
            if (timestamp != null) {
                Date date = timestamp.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("he", "IL"));
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
                tvTimestamp.setText(sdf.format(date));
            } else {
                tvTimestamp.setText("");
            }

            if ("REPORT".equals(notification.getType())) {
                tvType.setText("דיווח");
                tvType.setTextColor(itemView.getContext().getResources().getColor(R.color.red));
                tvReason.setVisibility(View.VISIBLE);
                tvReason.setText("סיבה: " + notification.getReportReason());
            } else {
                tvType.setText("הודעה");
                tvType.setTextColor(itemView.getContext().getResources().getColor(R.color.orange));
                tvReason.setVisibility(View.VISIBLE);
                tvReason.setText("סיבה: " + notification.getContactReason());
            }


            cardNotification.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onNotificationClick(notification);
                }
            });
        }
    }
}