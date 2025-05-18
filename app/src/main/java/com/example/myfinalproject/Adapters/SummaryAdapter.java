package com.example.myfinalproject.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myfinalproject.DataModels.Summary;
import com.example.myfinalproject.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SummaryAdapter extends ArrayAdapter<Summary> {

    private Context context;
    private List<Summary> summaries;
    private FirebaseFirestore db;


    public SummaryAdapter(Context context, List<Summary> summaries) {
        super(context, R.layout.onerow_summary, summaries);
        this.context = context;
        this.summaries = summaries;
        this.db = FirebaseFirestore.getInstance();

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
            holder.tvCreatedDate = convertView.findViewById(R.id.tvCreatedDate);
            holder.ratingBarSum = convertView.findViewById(R.id.ratingBarSum);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        Summary summary = summaries.get(position);

        holder.tvClass.setText("כיתה: " + summary.getClassOption());
        holder.tvProfessional.setText("מקצוע: " + summary.getProfession());
        holder.tvSummaryTitle.setText("נושא: " + summary.getSummaryTitle());
        holder.ratingBarSum.setRating(summary.getRating());

        float rating = summary.getRating();
        holder.ratingBarSum.setRating(rating);

        fetchRatingFromFirestore(summary.getSummaryId(), holder);

        Date createdDate = summary.getCreatedDate();
        if (createdDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
            holder.tvCreatedDate.setText("נוצר בתאריך: " + sdf.format(createdDate));
        } else {
            holder.tvCreatedDate.setText("תאריך לא זמין");
            Log.d("SummaryAdapter", "Date is null for summary: " + summary.getSummaryId());
        }


        if (summary.getImage() != null && !summary.getImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(summary.getImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageSum.setImageBitmap(decodedByte);
            } catch (Exception e) {
                Toast.makeText(context, "שגיאה בטעינת התמונה", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                holder.imageSum.setImageResource(R.drawable.newlogo);
            }
        } else {
            holder.imageSum.setImageResource(R.drawable.newlogo);
        }
        return convertView;
    }

    private void fetchRatingFromFirestore(String summaryId, ViewHolder holder) {
        if (summaryId == null || summaryId.isEmpty()) {
            return;
        }

        db.collection("summaries").document(summaryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double averageRating = documentSnapshot.getDouble("averageRating");
                        if (averageRating != null) {
                            float rating = averageRating.floatValue();
                            holder.ratingBarSum.setRating(rating);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SummaryAdapter", "Error fetching rating", e);
                });
    }


    private static class ViewHolder {
        ImageView imageSum;
        TextView tvClass;
        TextView tvProfessional;
        TextView tvSummaryTitle;
        TextView tvCreatedDate;
        RatingBar ratingBarSum;

    }

    public void updateSummaries(List<Summary> newSummaries) {
        summaries.clear();
        summaries.addAll(newSummaries);
        notifyDataSetChanged();
    }

}
