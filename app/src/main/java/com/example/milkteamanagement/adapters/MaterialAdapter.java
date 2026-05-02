package com.example.milkteamanagement.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.milkteamanagement.R;
import com.example.milkteamanagement.models.Material;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {
    private List<Material> materials;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Material material);
    }

    public MaterialAdapter(List<Material> materials) {
        this.materials = materials;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Material material = materials.get(position);
        holder.tvName.setText(material.getName());
        holder.tvQuantity.setText(String.valueOf(material.getQuantity()));
        holder.tvUnit.setText(material.getUnit());

        if (material.getImageUrl() != null && !material.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(material.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
        }

        if (material.isLowStock()) {
            holder.layoutBackground.setBackgroundColor(Color.parseColor("#FFEBEE")); // Light red
            holder.tvStatus.setText("Sắp hết!");
            holder.tvStatus.setTextColor(Color.RED);
            holder.tvQuantity.setTextColor(Color.RED);
        } else {
            holder.layoutBackground.setBackgroundColor(Color.WHITE);
            holder.tvStatus.setText("Ổn định");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.tvQuantity.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(material);
            }
        });
    }

    @Override
    public int getItemCount() {
        return materials != null ? materials.size() : 0;
    }

    static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvUnit, tvStatus;
        ImageView ivImage;
        LinearLayout layoutBackground;

        public MaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMaterialName);
            tvQuantity = itemView.findViewById(R.id.tvMaterialQuantity);
            tvUnit = itemView.findViewById(R.id.tvMaterialUnit);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivImage = itemView.findViewById(R.id.ivMaterialImage);
            layoutBackground = itemView.findViewById(R.id.layoutBackground);
        }
    }
}
