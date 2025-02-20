package com.example.myfinalproject.ChooseUserFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myfinalproject.Models.User;
import com.example.myfinalproject.R;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;

    public UserAdapter(Context context, List<User> users) {
        super(context,  R.layout.onerow_user, users);
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.onerow_user, parent, false);
            holder = new ViewHolder();
            holder.imgUserProfile = convertView.findViewById(R.id.imgUserProfile);
            holder.tvUserName = convertView.findViewById(R.id.tvUserName);
            holder.tvSumNumTitle = convertView.findViewById(R.id.tvSumNumTitle);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = users.get(position);

        holder.tvUserName.setText(user.getUserName());
        holder.tvSumNumTitle.setText(user.getSumCount());


        if (user.getImageProfile() != null) {
            try {
                byte[] decodedString = Base64.decode(user.getImageProfile(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imgUserProfile.setImageBitmap(decodedByte);
            } catch (Exception e) {
                Toast.makeText(context, "שגיאה בטעינת תמונה", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                holder.imgUserProfile.setImageResource(R.drawable.newlogo);
            }
        } else {
            holder.imgUserProfile.setImageResource(R.drawable.newlogo);
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView imgUserProfile;
        TextView tvUserName, tvSumNumTitle;

    }

    public void updateUsers(List<User> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

}
