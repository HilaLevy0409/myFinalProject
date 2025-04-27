
 package com.example.myfinalproject.NoticesAdminFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Adapters.NotificationsAdminAdapter;
import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.Database.NotificationAdminDatabase;
import com.example.myfinalproject.Models.NotificationAdmin;
import com.example.myfinalproject.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class NoticesAdminFragment extends Fragment implements View.OnClickListener, NotificationsAdminAdapter.OnNotificationClickListener {

    private TabLayout tabLayout;
    private Button btnBack;
    private RecyclerView recyclerNotifications;
    private NotificationsAdminAdapter adapter;
    private NotificationAdminDatabase notificationRepository;

    private static final int TAB_ALL = 0;
    private static final int TAB_MESSAGES = 1;
    private static final int TAB_REPORTS = 2;

    public NoticesAdminFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationRepository = NotificationAdminDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notices_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerNotifications = view.findViewById(R.id.recyclerNotifications);

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationsAdminAdapter(new ArrayList<>(), this);
        recyclerNotifications.setAdapter(adapter);

        loadNotifications(TAB_ALL);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadNotifications(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // אין צורך
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // אין צורך
            }
        });
    }



    private void loadNotifications(int tabPosition) {
        // Get all notifications
        notificationRepository.getAllNotifications()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationAdmin> allNotifications = queryDocumentSnapshots.toObjects(NotificationAdmin.class);
                    List<NotificationAdmin> filteredList = new ArrayList<>();

                    if (tabPosition == TAB_ALL) {
                        filteredList = allNotifications;
                    } else if (tabPosition == TAB_MESSAGES) {
                        for (NotificationAdmin notification : allNotifications) {
                            if ("MESSAGE".equals(notification.getType()) ||
                                    "CONTACT".equals(notification.getType())) {
                                filteredList.add(notification);
                            }
                        }
                    } else if (tabPosition == TAB_REPORTS) {
                        for (NotificationAdmin notification : allNotifications) {
                            if ("REPORT".equals(notification.getType())) {
                                filteredList.add(notification);
                            }
                        }
                    }

                    adapter.updateData(filteredList);


                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "שגיאה בטעינת הודעות", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, new AdminFragment())
                    .commit();
        }
    }

    @Override
    public void onNotificationClick(NotificationAdmin notification) {
        showNotificationDetailsDialog(notification);
    }

    private void showNotificationDetailsDialog(NotificationAdmin notification) {
        String title = "REPORT".equals(notification.getType()) ? "פרטי דיווח" : "פרטי הודעה";

        StringBuilder messageBuilder = new StringBuilder();

        android.util.Log.d("NotificationDialog", "UserName: " + notification.getUserName()
                + ", UserId: " + notification.getUserId()
                + ", Type: " + notification.getType());

        if ("REPORT".equals(notification.getType())) {
            String reporterName = notification.getUserName();

            if (reporterName == null || reporterName.isEmpty()) {
                reporterName = "משתמש אנונימי";
            }

            messageBuilder.append("מדווח על ידי: ").append(reporterName).append("\n\n");

            if (notification.getReportedUserName() != null && !notification.getReportedUserName().isEmpty()) {
                messageBuilder.append("משתמש מדווח: ").append(notification.getReportedUserName()).append("\n\n");
            }

            messageBuilder.append("סיבת דיווח: ").append(notification.getReportReason()).append("\n\n");
        }
        else if ("CONTACT".equals(notification.getType())) {
            String senderName = notification.getUserName();
            if (senderName == null || senderName.isEmpty()) {
                if (notification.getUserId() != null && !notification.getUserId().isEmpty()) {
                    senderName = "User " + notification.getUserId().substring(0, Math.min(notification.getUserId().length(), 5));
                } else {
                    senderName = "אנונימי";
                }
            }

            messageBuilder.append("מאת: ").append(senderName).append("\n\n");
            messageBuilder.append("סיבת פנייה: ").append(notification.getContactReason()).append("\n\n");
        }
        else {
            String displayName = notification.getUserName();
            if (displayName == null || displayName.isEmpty()) {
                if (notification.getUserId() != null && !notification.getUserId().isEmpty()) {
                    displayName = "User " + notification.getUserId().substring(0, Math.min(notification.getUserId().length(), 5));
                } else {
                    displayName = "אנונימי";
                }
            }

            messageBuilder.append("מאת: ").append(displayName).append("\n\n");
        }

        messageBuilder.append("תוכן:\n").append(notification.getContent());

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setMessage(messageBuilder.toString())
                .setPositiveButton("סגור", null)
                .setNeutralButton("סימון כטופל", (dialog, which) -> {
                    adapter.removeNotification(notification);
                    notificationRepository.deleteNotification(notification.getId());
                })
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        int selectedTab = tabLayout.getSelectedTabPosition();
        loadNotifications(selectedTab);
    }
}