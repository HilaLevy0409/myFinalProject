package com.example.myfinalproject.NoticesAdminFragment;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Adapters.NotificationsAdminAdapter;
import com.example.myfinalproject.Repositories.NotificationAdminRepository;
import com.example.myfinalproject.ManageUserFragment.ManageUserFragment;
import com.example.myfinalproject.DataModels.NotificationAdmin;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumFragment.SumFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import com.example.myfinalproject.CallBacks.OnNotificationClickListenerCallback;

public class NoticesAdminFragment extends Fragment implements OnNotificationClickListenerCallback {

    private TabLayout tabLayout;
    private RecyclerView recyclerNotifications;
    private NotificationsAdminAdapter adapter;
    private NotificationAdminRepository notificationRepository;

    private AlertDialog currentDialog;

    private static final int TAB_ALL = 0;
    private static final int TAB_MESSAGES = 1;
    private static final int TAB_REPORTS = 2;

    public NoticesAdminFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationRepository = NotificationAdminRepository.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notices_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadNotifications(int tabPosition) {
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
    public void onNotificationClick(NotificationAdmin notification) {
        showNotificationDetailsDialog(notification);
    }

    private void showNotificationDetailsDialog(NotificationAdmin notification) {
        String title = "REPORT".equals(notification.getType()) ? "פרטי דיווח" : "פרטי הודעה";

        SpannableStringBuilder messageBuilder = new SpannableStringBuilder();


        if ("REPORT".equals(notification.getType())) {

            String reporterName = notification.getUserName();
            if (reporterName == null || reporterName.isEmpty()) {
                reporterName = "משתמש אנונימי";
            }

            messageBuilder.append("מדווח על ידי: ");

            if (!reporterName.equals("אורח")) {
                int start = messageBuilder.length();
                messageBuilder.append(reporterName);
                int end = messageBuilder.length();

                final String finalReporterName = reporterName;
                ClickableSpan reporterClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        navigateToManageUserFragment(finalReporterName);

                        if (currentDialog != null && currentDialog.isShowing()) {
                            currentDialog.dismiss();
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(getResources().getColor(R.color.orange));
                        ds.setUnderlineText(true);
                    }
                };

                messageBuilder.setSpan(reporterClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                messageBuilder.append(reporterName);
            }

            messageBuilder.append("\n\n");

            if (notification.getReportedUserName() != null && !notification.getReportedUserName().isEmpty()) {
                String reportedName = notification.getReportedUserName();


                boolean isSummary = false;
                if (notification.getId() != null) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("summaries").document(notification.getId()).get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    SumFragment sumFragment = SumFragment.newInstance(notification.getId());
                                    getActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.flFragment, sumFragment)
                                            .addToBackStack(null)
                                            .commit();

                                    if (currentDialog != null && currentDialog.isShowing()) {
                                        currentDialog.dismiss();
                                    }
                                }
                            });
                }

                messageBuilder.append("דיווח על: ");

                int start = messageBuilder.length();
                messageBuilder.append(reportedName);
                int end = messageBuilder.length();

                ClickableSpan reportedItemClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("summaries")
                                .whereEqualTo("summaryTitle", reportedName)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                        String summaryId = document.getId();

                                        SumFragment sumFragment = SumFragment.newInstance(summaryId);
                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.flFragment, sumFragment)
                                                .addToBackStack(null)
                                                .commit();
                                    } else {
                                        Toast.makeText(getContext(), "לא נמצא ", Toast.LENGTH_SHORT).show();
                                    }

                                    if (currentDialog != null && currentDialog.isShowing()) {
                                        currentDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "שגיאה בעת חיפוש סיכום", Toast.LENGTH_SHORT).show();

                                    if (currentDialog != null && currentDialog.isShowing()) {
                                        currentDialog.dismiss();
                                    }
                                });
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(getResources().getColor(R.color.orange));
                        ds.setUnderlineText(true);
                    }
                };

                messageBuilder.setSpan(reportedItemClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageBuilder.append("\n\n");
            }

            messageBuilder.append("סיבת דיווח: ").append(notification.getReportReason()).append("\n\n");
        }
        else if ("CONTACT".equals(notification.getType())) {
            String senderName = notification.getUserName();
            if (senderName == null || senderName.isEmpty()) {
                if (notification.getUserId() != null && !notification.getUserId().isEmpty()) {
                    senderName = "User " + notification.getUserId().substring(0, Math.min(notification.getUserId().length(), 5));
                } else {
                    senderName = "אורח";
                }
            }

            messageBuilder.append("מאת: ");

            if (!senderName.equals("אורח") && !senderName.startsWith("User ")) {
                int start = messageBuilder.length();
                messageBuilder.append(senderName);
                int end = messageBuilder.length();

                final String finalSenderName = senderName;
                ClickableSpan senderClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        navigateToManageUserFragment(finalSenderName);

                        if (currentDialog != null && currentDialog.isShowing()) {
                            currentDialog.dismiss();
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(getResources().getColor(R.color.orange));
                        ds.setUnderlineText(true);
                    }
                };

                messageBuilder.setSpan(senderClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                messageBuilder.append(senderName);
            }

            messageBuilder.append("\n\n");
            messageBuilder.append("סיבת פנייה: ").append(notification.getContactReason()).append("\n\n");
        }
        else {
            String displayName = notification.getUserName();
            if (displayName == null || displayName.isEmpty()) {
                if (notification.getUserId() != null && !notification.getUserId().isEmpty()) {
                    displayName = "User " + notification.getUserId().substring(0, Math.min(notification.getUserId().length(), 5));
                } else {
                    displayName = "אורח";
                }
            }

            messageBuilder.append("מאת: ");

            if (!displayName.equals("אורח") && !displayName.startsWith("User ")) {
                int start = messageBuilder.length();
                messageBuilder.append(displayName);
                int end = messageBuilder.length();

                final String finalDisplayName = displayName;
                ClickableSpan displayClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        navigateToManageUserFragment(finalDisplayName);

                        if (currentDialog != null && currentDialog.isShowing()) {
                            currentDialog.dismiss();
                        }
                    }

                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(getResources().getColor(R.color.orange));
                        ds.setUnderlineText(true);
                    }
                };

                messageBuilder.setSpan(displayClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                messageBuilder.append(displayName);
            }

            messageBuilder.append("\n\n");
        }

        messageBuilder.append("תוכן:\n").append(notification.getContent());

        TextView messageView = new TextView(getContext());
        messageView.setText(messageBuilder);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        messageView.setHighlightColor(getResources().getColor(android.R.color.transparent));
        messageView.setPadding(32, 16, 32, 16);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        messageView.setTextDirection(View.TEXT_DIRECTION_RTL);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setView(messageView)
                .setPositiveButton("סגירה", null)
                .setNeutralButton("סימון כטופל", (dialogInterface, which) -> {
                    adapter.removeNotification(notification);
                    notificationRepository.deleteNotification(notification.getId());
                });

        currentDialog = builder.create();
        currentDialog.show();
    }

    private void navigateToManageUserFragment(String username) {
        Fragment manageUserFragment = new ManageUserFragment();

        Bundle args = new Bundle();
        args.putString("username", username);
        manageUserFragment.setArguments(args);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, manageUserFragment)
                .addToBackStack(null)
                .commit();
    }




    @Override
    public void onStart() {
        super.onStart();
        int selectedTab = tabLayout.getSelectedTabPosition();
        loadNotifications(selectedTab);
    }
}