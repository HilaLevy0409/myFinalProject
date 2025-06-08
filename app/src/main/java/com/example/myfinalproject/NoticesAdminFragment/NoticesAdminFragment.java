package com.example.myfinalproject.NoticesAdminFragment;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.Log;
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

import com.example.myfinalproject.CallBacks.OnNotificationClickListener;

public class NoticesAdminFragment extends Fragment implements OnNotificationClickListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerNotifications;
    private NotificationsAdminAdapter adapter;
    private NotificationAdminRepository notificationRepository;
    private AlertDialog currentDialog;
    private static final int TAB_ALL = 0;
    private static final int TAB_MESSAGES = 1;
    private static final int TAB_REPORTS = 2;

    public NoticesAdminFragment() {}

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

        // מאזין לשינוי טאבים
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

    // טוען את ההודעות לפי הטאב שנבחר (הכול, הודעות, דיווחים)
    private void loadNotifications(int tabPosition) {
        notificationRepository.getAllNotifications()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationAdmin> allNotifications = queryDocumentSnapshots.toObjects(NotificationAdmin.class);
                    List<NotificationAdmin> filteredList = new ArrayList<>();

                    // סינון לפי סוג הודעה
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
                    adapter.updateData(filteredList); // עדכן את הרשימה בתצוגה
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "שגיאה בטעינת הודעות", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //  לוחץ על הודעה, מוצג דיאלוג עם פרטי ההודעה
    @Override
    public void onNotificationClick(NotificationAdmin notification) {
        showNotificationDetailsDialog(notification);
    }


    // מציג דיאלוג עם פרטי ההודעה + קישורים לניווט למשתמש/סיכום במידת הצורך
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

                final String finalUserId = notification.getUserId();
                final String finalUserName = notification.getUserName();

                ClickableSpan displayClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (finalUserId != null && !finalUserId.isEmpty()) {
                            navigateToManageUserFragmentById(finalUserId);
                        } else if (finalUserName != null && !finalUserName.isEmpty() && !finalUserName.equals("אורח")) {
                            findUserByUsernameAndNavigate(finalUserName);
                        } else {
                            Toast.makeText(getContext(), "לא ניתן לאתר את המשתמש", Toast.LENGTH_SHORT).show();
                        }

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
                messageBuilder.append(reporterName);
            }

            messageBuilder.append("\n\n");

            if (notification.getReportedUserName() != null && !notification.getReportedUserName().isEmpty()) {
                String reportedName = notification.getReportedUserName();
                messageBuilder.append("דיווח על: ");

                int start = messageBuilder.length();
                messageBuilder.append(reportedName);
                int end = messageBuilder.length();

                ClickableSpan reportedItemClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        findUserByUsernameAndNavigate(reportedName, () -> {
                            findSummaryByTitleAndNavigate(reportedName);
                        });

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

                final String finalUserId = notification.getUserId();
                final String finalUserName = notification.getUserName();

                ClickableSpan senderClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (finalUserId != null && !finalUserId.isEmpty()) {
                            navigateToManageUserFragmentById(finalUserId);
                        } else if (finalUserName != null && !finalUserName.isEmpty()) {
                            findUserByUsernameAndNavigate(finalUserName);
                        } else {
                            Toast.makeText(getContext(), "לא ניתן לאתר את המשתמש", Toast.LENGTH_SHORT).show();
                        }

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

                final String finalUserId = notification.getUserId();
                final String finalUserName = notification.getUserName();

                ClickableSpan displayClickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        if (finalUserId != null && !finalUserId.isEmpty()) {
                            navigateToManageUserFragmentById(finalUserId);
                        } else if (finalUserName != null && !finalUserName.isEmpty()) {
                            findUserByUsernameAndNavigate(finalUserName);
                        } else {
                            Toast.makeText(getContext(), "לא ניתן לאתר את המשתמש", Toast.LENGTH_SHORT).show();
                        }

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

    // מחפש משתמש לפי שם משתמש וניגש אליו, אם לא נמצא – יכול להריץ קוד חלופי (onNotFound)
    private void findUserByUsernameAndNavigate(String username) {
        findUserByUsernameAndNavigate(username, null);
    }

    private void findUserByUsernameAndNavigate(String username, Runnable onNotFound) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String[] possibleUsernameFields = {"username", "userName", "displayName"};

        findUserByField(db, possibleUsernameFields, 0, username, onNotFound);
    }

    // חיפוש רקורסיבי בשדות שונים כדי למצוא משתמש לפי שם
    private void findUserByField(FirebaseFirestore db, String[] fields, int fieldIndex, String username, Runnable onNotFound) {
        if (fieldIndex >= fields.length) {
            if (onNotFound != null) {
                onNotFound.run();
            } else {
                Toast.makeText(getContext(), "לא נמצא משתמש בשם: " + username, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String currentField = fields[fieldIndex];
        db.collection("users")
                .whereEqualTo(currentField, username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String userId = document.getId();
                        navigateToManageUserFragmentById(userId);
                    } else {
                        findUserByField(db, fields, fieldIndex + 1, username, onNotFound);
                    }
                })
                .addOnFailureListener(e -> {
                    findUserByField(db, fields, fieldIndex + 1, username, onNotFound);
                });
    }

    // מחפש סיכום לפי נושא וניגש לפרגמנט שמציג אותו
    private void findSummaryByTitleAndNavigate(String summaryTitle) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("summaries")
                .whereEqualTo("summaryTitle", summaryTitle)
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
                        Toast.makeText(getContext(), "לא נמצא סיכום בשם: " + summaryTitle, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "שגיאה בחיפוש", Toast.LENGTH_SHORT).show();
                    Log.e("NoticesAdmin", "Error searching for summary: " + e.getMessage());
                });
    }

    // ניווט לניהול משתמש לפי מזהה (userId)
    private void navigateToManageUserFragmentById(String userId) {
        Fragment manageUserFragment = new ManageUserFragment();

        Bundle args = new Bundle();
        args.putString("userId", userId);
        manageUserFragment.setArguments(args);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, manageUserFragment)
                .addToBackStack(null)
                .commit();
    }

    // בכל פעם שהפרגמנט חוזר לפוקוס – טען מחדש את ההודעות לפי הטאב הנבחר
    @Override
    public void onStart() {
        super.onStart();
        int selectedTab = tabLayout.getSelectedTabPosition();
        loadNotifications(selectedTab);
    }
}