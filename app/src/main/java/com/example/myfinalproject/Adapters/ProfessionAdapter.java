package com.example.myfinalproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfinalproject.Models.Profession;
import com.example.myfinalproject.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ProfessionAdapter extends RecyclerView.Adapter<ProfessionAdapter.ProfessionViewHolder> {

    private List<Profession> professions;
    private List<Profession> filteredProfessions;
    private Context context;
    private ProfessionClickListener listener;

    public ProfessionAdapter(Context context, List<Profession> professions, ProfessionClickListener listener) {
        this.context = context;
        this.professions = professions;
        this.filteredProfessions = new ArrayList<>(professions);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.onerow_profession, parent, false);
        return new ProfessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessionViewHolder holder, int position) {
        Profession profession = filteredProfessions.get(position);
        holder.tvProfessionName.setText(profession.getName());

        holder.cardProfession.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProfessionClick(profession);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredProfessions.size();
    }

    public void filterByCategory(String category) {
        filteredProfessions.clear();

        if (category.equals("הכל")) {
            filteredProfessions.addAll(professions);
        } else {
            for (Profession profession : professions) {
                if (profession.getCategory().equals(category)) {
                    filteredProfessions.add(profession);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByQuery(String query) {
        filteredProfessions.clear();

        if (query.isEmpty()) {
            filteredProfessions.addAll(professions);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Profession profession : professions) {
                if (profession.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProfessions.add(profession);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class ProfessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvProfessionName;
        MaterialCardView cardProfession;

        public ProfessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProfessionName = itemView.findViewById(R.id.tvProName);
            cardProfession = itemView.findViewById(R.id.cardPro);
        }
    }

    public interface ProfessionClickListener {
        void onProfessionClick(Profession profession);
    }
}