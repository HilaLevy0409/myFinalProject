package com.example.myfinalproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.CallBacks.ProfessionClickListener;
import com.example.myfinalproject.DataModels.Profession;
import com.example.myfinalproject.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ProfessionAdapter extends RecyclerView.Adapter<ProfessionAdapter.ProfessionViewHolder> {

    private List<Profession> professions;
    private List<Profession> filteredProfessions;
    private Context context;
    private ProfessionClickListener callback;

    public ProfessionAdapter(Context context, List<Profession> professions, ProfessionClickListener callback) {
        this.context = context;
        this.professions = professions;
        this.filteredProfessions = new ArrayList<>(professions);
        this.callback = callback;
    }

    @NonNull
    @Override
    public ProfessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.onerow_profession, parent, false);
        return new ProfessionViewHolder(view);
    }

    // קישור נתוני מקצוע לתצוגה (ViewHolder)
    @Override
    public void onBindViewHolder(@NonNull ProfessionViewHolder holder, int position) {
        Profession profession = filteredProfessions.get(position); // מקצוע נוכחי
        holder.tvProfessionName.setText(profession.getName()); // הצגת שם מקצוע

        // טיפול בלחיצה על כרטיס מקצוע
        holder.cardProfession.setOnClickListener(v -> {
            if (callback != null) {
                callback.onProfessionClick(profession); // קריאה לקולבק
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredProfessions.size();
    }

    // סינון מקצועות לפי קטגוריה
    public void filterByCategory(String category) {
        filteredProfessions.clear(); // ניקוי קודם

        if (category.equals("הכל")) {
            filteredProfessions.addAll(professions); // אם "הכל", להחזיר את כל הרשימה
        } else {
            for (Profession profession : professions) {
                if (profession.getCategory().equals(category)) {
                    filteredProfessions.add(profession);  // הוספת מקצוע שתואם קטגוריה
                }
            }
        }
        notifyDataSetChanged();
    }

    // סינון מקצועות לפי טקסט חיפוש
    public void filterByQuery(String query) {
        filteredProfessions.clear();

        if (query.isEmpty()) {
            filteredProfessions.addAll(professions); // אם שדה ריק – להחזיר הכל
        } else {
            String lowerCaseQuery = query.toLowerCase(); // הפיכת מחרוזת לאותיות קטנות
            for (Profession profession : professions) {
                if (profession.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProfessions.add(profession); // הוספת מקצוע שתואם לחיפוש
                }
            }
        }
        notifyDataSetChanged();
    }

    // מחלקת ViewHolder פנימית – מייצגת פריט מקצוע בודד ברשימה
    public static class ProfessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvProfessionName;
        MaterialCardView cardProfession;

        public ProfessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProfessionName = itemView.findViewById(R.id.tvProName);
            cardProfession = itemView.findViewById(R.id.cardPro);
        }
    }


}