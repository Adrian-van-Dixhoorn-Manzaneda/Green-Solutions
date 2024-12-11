package com.example.greensolutions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PosteAdapter extends RecyclerView.Adapter<PosteAdapter.PosteViewHolder> {

    private List<PosteSensors> posteList;

    public PosteAdapter(List<PosteSensors> posteList) {
        this.posteList = posteList;
    }

    @NonNull
    @Override
    public PosteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sensor_item, parent, false);
        return new PosteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosteViewHolder holder, int position) {
        PosteSensors sensor = posteList.get(position);
        holder.tvTitle.setText(sensor.getTitle());
        holder.tvDescription.setText(sensor.getDescription());
        holder.icon.setImageResource(sensor.getIconResId());
    }

    @Override
    public int getItemCount() {
        return posteList.size();
    }

    static class PosteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageView icon;

        public PosteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
