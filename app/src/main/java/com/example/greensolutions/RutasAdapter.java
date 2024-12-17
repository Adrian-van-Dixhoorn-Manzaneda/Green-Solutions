package com.example.greensolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.RutasViewHolder> {

    private List<RutasData> rutasList;

    public RutasAdapter(List<RutasData> rutasList) {
        this.rutasList = rutasList;
    }

    @NonNull
    @Override
    public RutasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rutas_item, parent, false);
        return new RutasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutasViewHolder holder, int position) {
        RutasData data = rutasList.get(position);
        holder.tvTitle.setText(data.getTitle());
        holder.tvValue.setText(data.getValue());
        holder.icon.setImageResource(data.getIconResId());
    }

    @Override
    public int getItemCount() {
        return rutasList.size();
    }

    static class RutasViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvValue;
        ImageView icon;

        public RutasViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvValue = itemView.findViewById(R.id.tv_value);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
