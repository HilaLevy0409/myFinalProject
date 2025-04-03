package com.example.myfinalproject.Adapters;

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

import com.example.myfinalproject.Models.Summary;
import com.example.myfinalproject.R;

import java.util.List;

public class SummaryAdapter extends ArrayAdapter<Summary> {

    private Context context;
    private List<Summary> Summaries;


    public SummaryAdapter(Context context, List<Summary> Summaries) {
        super(context, R.layout.onerow_summary, Summaries);
        this.context = context;
        this.Summaries = Summaries;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;


        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.onerow_summary, parent, false);
            holder = new ViewHolder();
            holder.imageSum = convertView.findViewById(R.id.imageSum);
            holder.tvClass = convertView.findViewById(R.id.tvClass);
            holder.tvProfessional = convertView.findViewById(R.id.tvProfession);
            holder.tvSummaryTitle = convertView.findViewById(R.id.tvSummaryTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        Summary summary = Summaries.get(position);

        holder.tvClass.setText("כיתה: " + summary.getClassOption());
        holder.tvProfessional.setText("מקצוע: " + summary.getProfession());
        holder.tvSummaryTitle.setText("נושא: " + summary.getSummaryTitle());



        if (summary.getImage() != null) {
            try {
                byte[] decodedString = Base64.decode(summary.getImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageSum.setImageBitmap(decodedByte);
            } catch (Exception e) {
                Toast.makeText(context, "test", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                holder.imageSum.setImageResource(R.drawable.newlogo);
            }
        } else {
            Toast.makeText(context, "", Toast.LENGTH_LONG).show();
            holder.imageSum.setImageResource(R.drawable.newlogo);
        }


        return convertView;
    }


    private static class ViewHolder {
        ImageView imageSum;
        TextView tvClass;
        TextView tvProfessional;
        TextView tvSummaryTitle;
    }


    public void updateSummaries(List<Summary> newSummaries) {
        Summaries.clear();
        Summaries.addAll(newSummaries);
        notifyDataSetChanged();
    }

}
