 package com.example.myfinalproject.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.myfinalproject.R;

import java.util.Random;

public class ClassAdapter extends BaseAdapter {
    private Context context;     // משתנה להקשר – נדרש ליצירת Layout וכו'
    private String[] classes;
    private Random random;

    public ClassAdapter(Context context, String[] classes) {
        this.context = context;
        this.classes = classes;
        this.random = new Random();
    }

    @Override
    public int getCount() {
        return classes.length;
    }

    @Override
    public Object getItem(int position) {
        return classes[position];
    }     // מחזיר את הכיתה שנמצאת במיקום מסוים


    @Override
    public long getItemId(int position) {
        return position;
    }     // מחזיר מזהה ייחודי לכיתה – את המיקום שלו

    // אחראי על יצירת התצוגה של כל פריט ברשימה (Grid)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // אם אין תצוגה ממוחזרת, מייצר תצוגה חדשה מהקובץ grid_item.xml
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        }

        // מאחזר את ה-TextView מתוך התצוגה
        TextView tvClass = convertView.findViewById(R.id.tvClass);

        // מגדיר את הטקסט של ה-TextView לפי הכיתה שבמיקום הנוכחי
        tvClass.setText(classes[position]);

        // יוצר צבע רקע אקראי ומגדיר אותו לפריט הנוכחי
        int color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        convertView.setBackgroundColor(color);

        // מחזיר את התצוגה המעודכנת
        return convertView;
    }
}
