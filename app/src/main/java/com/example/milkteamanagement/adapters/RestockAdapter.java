package com.example.milkteamanagement.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.milkteamanagement.R;
import com.example.milkteamanagement.models.Material;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestockAdapter extends RecyclerView.Adapter<RestockAdapter.RestockViewHolder> {
    private List<Material> materials;
    private Map<String, Double> restockQuantities = new HashMap<>();

    public RestockAdapter(List<Material> materials) {
        this.materials = materials;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
        notifyDataSetChanged();
    }

    public Map<String, Double> getRestockQuantities() {
        return restockQuantities;
    }

    @NonNull
    @Override
    public RestockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restock_material, parent, false);
        return new RestockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestockViewHolder holder, int position) {
        Material material = materials.get(position);
        holder.tvName.setText(material.getName());
        holder.tvCurrentStock.setText("Hiện có: " + material.getQuantity() + " " + material.getUnit());

        if (material.getImageUrl() != null && !material.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(material.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_launcher_background);
        }

        double currentRestock = restockQuantities.getOrDefault(material.getId(), 0.0);
        holder.etQuantity.setText(String.valueOf(currentRestock));

        holder.btnPlus.setOnClickListener(v -> {
            double val = Double.parseDouble(holder.etQuantity.getText().toString());
            val += 1.0;
            holder.etQuantity.setText(String.valueOf(val));
            restockQuantities.put(material.getId(), val);
        });

        holder.btnMinus.setOnClickListener(v -> {
            double val = Double.parseDouble(holder.etQuantity.getText().toString());
            if (val > 0) {
                val -= 1.0;
                holder.etQuantity.setText(String.valueOf(val));
                restockQuantities.put(material.getId(), val);
            }
        });

        holder.etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double val = Double.parseDouble(s.toString());
                    restockQuantities.put(material.getId(), val);
                } catch (NumberFormatException e) {
                    restockQuantities.put(material.getId(), 0.0);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return materials != null ? materials.size() : 0;
    }

    static class RestockViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCurrentStock;
        ImageView ivImage;
        EditText etQuantity;
        Button btnPlus, btnMinus;

        public RestockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMaterialName);
            tvCurrentStock = itemView.findViewById(R.id.tvCurrentStock);
            ivImage = itemView.findViewById(R.id.ivMaterialImage);
            etQuantity = itemView.findViewById(R.id.etQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}
