package com.example.myfinalproject.NoticesAdminFragment;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Adapters.NotificationsAdminAdapter;
import com.example.myfinalproject.AdminFragment.AdminFragment;
import com.example.myfinalproject.Database.NotificationAdminDatabase;
import com.example.myfinalproject.ManageUserFragment.ManageUserFragment;
import com.example.myfinalproject.Models.NotificationAdmin;
import com.example.myfinalproject.R;
import com.example.myfinalproject.SumFragment.SumFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NoticesAdminFragment extends Fragment implements View.OnClickListener, NotificationsAdminAdapter.OnNotificationClickListener {

    private TabLayout tabLayout;
    private Button btnBack;
    private RecyclerView recyclerNotifications;
    private NotificationsAdminAdapter adapter;
    private NotificationAdminDatabase notificationRepository;

    // Add this as a class field in the NoticesAdminFragment class
    private AlertDialog currentDialog;

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

        // Create the message content first
        SpannableStringBuilder messageBuilder = new SpannableStringBuilder();

        // Log all relevant data for debugging
        android.util.Log.d("NotificationDialog", "UserName: " + notification.getUserName()
                + ", UserId: " + notification.getUserId()
                + ", Type: " + notification.getType()
                + ", ReportedUserName: " + notification.getReportedUserName()
                + ", ReportedUserId: " + notification.getReportedUserId()
                + ", NotificationId: " + notification.getId());

        if ("REPORT".equals(notification.getType())) {
            // Add reporter name with clickable span
            String reporterName = notification.getUserName();
            if (reporterName == null || reporterName.isEmpty()) {
                reporterName = "משתמש אנונימי";
            }

            messageBuilder.append("מדווח על ידי: ");

            // Only make the reporter name clickable if it's not anonymous
            if (!reporterName.equals("משתמש אנונימי")) {
                int start = messageBuilder.length();
                messageBuilder.append(reporterName);
                int end = messageBuilder.length();

                final String finalReporterName = reporterName;
                ClickableSpan reporterClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // Navigate to the reporter's profile
                        navigateToManageUserFragment(finalReporterName);

                        // Dismiss dialog after navigation
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

            // Add reported item (user or summary)
            if (notification.getReportedUserName() != null && !notification.getReportedUserName().isEmpty()) {
                String reportedName = notification.getReportedUserName();

                // Check if this is likely a summary by looking at the ID format/pattern
                // This is a heuristic approach - if you have a better way to identify summaries, use it
                boolean isSummary = false;
                if (notification.getId() != null) {
                    // Try to get summary by ID
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("summaries").document(notification.getId()).get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    // It's a summary! Navigate to it
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

                // Since we don't know for sure, let's assume it's a user report for now in the UI
                messageBuilder.append("דיווח על: ");

                int start = messageBuilder.length();
                messageBuilder.append(reportedName);
                int end = messageBuilder.length();

                // Create clickable span for reported item
                ClickableSpan reportedItemClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        // First, try to see if this is a summary by checking in the summaries collection
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("summaries")
                                .whereEqualTo("summaryTitle", reportedName)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        // It's a summary! Navigate to it
                                        android.util.Log.d("ClickableSpan", "Found summary with title: " + reportedName);
                                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                        String summaryId = document.getId();

                                        SumFragment sumFragment = SumFragment.newInstance(summaryId);
                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .replace(R.id.flFragment, sumFragment)
                                                .addToBackStack(null)
                                                .commit();
                                    } else {
                                        // Not found as a summary, navigate to user
                                        android.util.Log.d("ClickableSpan", "No summary found, navigating to user: " + reportedName);
                                        navigateToManageUserFragment(reportedName);
                                    }

                                    // Dismiss dialog after navigation
                                    if (currentDialog != null && currentDialog.isShowing()) {
                                        currentDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Error searching for summary, fall back to user navigation
                                    android.util.Log.d("ClickableSpan", "Error searching summaries: " + e.getMessage());
                                    navigateToManageUserFragment(reportedName);

                                    // Dismiss dialog after navigation
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
                    senderName = "אנונימי";
                }
            }

            messageBuilder.append("מאת: ");

            // Make sender name clickable if not anonymous
            if (!senderName.equals("אנונימי") && !senderName.startsWith("User ")) {
                int start = messageBuilder.length();
                messageBuilder.append(senderName);
                int end = messageBuilder.length();

                final String finalSenderName = senderName;
                ClickableSpan senderClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        navigateToManageUserFragment(finalSenderName);

                        // Dismiss dialog after navigation
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
                    displayName = "אנונימי";
                }
            }

            messageBuilder.append("מאת: ");

            // Make sender name clickable if not anonymous
            if (!displayName.equals("אנונימי") && !displayName.startsWith("User ")) {
                int start = messageBuilder.length();
                messageBuilder.append(displayName);
                int end = messageBuilder.length();

                final String finalDisplayName = displayName;
                ClickableSpan displayClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        navigateToManageUserFragment(finalDisplayName);

                        // Dismiss dialog after navigation
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

        // Create a TextView programmatically to handle the clickable spans
        TextView messageView = new TextView(getContext());
        messageView.setText(messageBuilder);
        messageView.setMovementMethod(LinkMovementMethod.getInstance()); // This is crucial for clickable spans
        messageView.setHighlightColor(getResources().getColor(android.R.color.transparent));
        messageView.setPadding(32, 16, 32, 16); // Add some padding (in pixels)
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Set text size
        messageView.setTextDirection(View.TEXT_DIRECTION_RTL); // For RTL text

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(title)
                .setView(messageView) // Use our custom TextView with clickable spans
                .setPositiveButton("סגור", null)
                .setNeutralButton("סימון כטופל", (dialogInterface, which) -> {
                    adapter.removeNotification(notification);
                    notificationRepository.deleteNotification(notification.getId());
                });

        // Store the dialog in a class-level variable for access in click handlers
        currentDialog = builder.create();
        currentDialog.show();
    }

    private void navigateToManageUserFragment(String username) {
        // Create a new instance of ManageUserFragment
        Fragment manageUserFragment = new ManageUserFragment();

        // Create a bundle to pass data
        Bundle args = new Bundle();
        args.putString("username", username);
        manageUserFragment.setArguments(args);

        // Navigate to the fragment
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, manageUserFragment)
                .addToBackStack(null)  // Add to back stack so admin can return
                .commit();
    }


    private void navigateToSumFragment(String summaryTopic) {
        // First, query Firebase to find the summary ID by topic
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("summaries")
                .whereEqualTo("summaryTitle", summaryTopic)
                .limit(1)  // We only need one result
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (should be the only one)
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String summaryId = document.getId();

                        // Create a new instance of SumFragment
                        SumFragment sumFragment = SumFragment.newInstance(summaryId);

                        // Navigate to the fragment
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.flFragment, sumFragment)
                                .addToBackStack(null)  // Add to back stack so admin can return
                                .commit();
                    } else {
                        // No summary found with that title
                        Toast.makeText(getContext(), "לא נמצא סיכום בנושא זה", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בטעינת הסיכום: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        int selectedTab = tabLayout.getSelectedTabPosition();
        loadNotifications(selectedTab);
    }
}