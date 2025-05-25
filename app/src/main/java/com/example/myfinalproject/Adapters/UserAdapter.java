package com.example.myfinalproject.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myfinalproject.CallBacks.OnUserClickListener;
import com.example.myfinalproject.DataModels.User;
import com.example.myfinalproject.R;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private OnUserClickListener onClickedReport;
    private OnUserClickListener onClickedSummaryByUser;
    private OnUserClickListener onClickedSendMessage;

    public UserAdapter(Context context, List<User> users, OnUserClickListener onClickedReport, OnUserClickListener onClickedSummaryByUser,
                       OnUserClickListener onClickedSendMessage) {
        super(context, R.layout.onerow_user, users);
        this.context = context;
        this.users = users;
        this.onClickedReport = onClickedReport;
        this.onClickedSummaryByUser = onClickedSummaryByUser;
        this.onClickedSendMessage = onClickedSendMessage;
    }

    // פונקציה שמציירת כל שורה ברשימה
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        // מימוש ViewHolder לשיפור ביצועים
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.onerow_user, parent, false);
            holder = new ViewHolder();

            // קישור רכיבי התצוגה
            holder.imgUserProfile = convertView.findViewById(R.id.imgUserProfile);
            holder.tvUserName = convertView.findViewById(R.id.tvUserName);
            holder.tvSumNumTitle = convertView.findViewById(R.id.tvSumNumTitle);
            holder.btnReport = convertView.findViewById(R.id.btnReport);
            holder.btnSummaryByUser = convertView.findViewById(R.id.btnSummaryByUser);
            holder.btnSendMessage = convertView.findViewById(R.id.btnSendMessage);

            holder.btnReport.setOnClickListener(v -> onClickedReport.onUserClick(position));
            holder.btnSummaryByUser.setOnClickListener(v -> onClickedSummaryByUser.onUserClick(position));
            holder.btnSendMessage.setOnClickListener(v -> onClickedSendMessage.onUserClick(position));

            convertView.setTag(holder); // שמירה לשימוש חוזר
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = users.get(position);

        holder.tvUserName.setText(user.getUserName());
        holder.tvSumNumTitle.setText("מספר סיכומים שנכתבו: " + (user.getSumCount() > 0 ? user.getSumCount() : "0"));


        // טעינת תמונת פרופיל אם קיימת, אחרת הצגת לוגו ברירת מחדל
        if (user.getImageProfile() != null && !user.getImageProfile().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(user.getImageProfile(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgUserProfile.setImageBitmap(decodedByte);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "שגיאה בטעינת תמונת הפרופיל", Toast.LENGTH_SHORT).show();
                holder.imgUserProfile.setImageResource(R.drawable.newlogo);
            }
        }

        return convertView;
    }

    // מחלקה פנימית – שומרת הפניות לרכיבי התצוגה (ViewHolder Pattern)
    private static class ViewHolder {
        ImageView imgUserProfile;
        TextView tvUserName, tvSumNumTitle;
        Button btnReport, btnSummaryByUser, btnSendMessage;
    }

}
