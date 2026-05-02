package com.example.milkteamanagement.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.R;
import com.example.milkteamanagement.models.Ingredient;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private List<Ingredient> ingredients;

    public IngredientAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.tvName.setText(ingredient.getName());
        holder.tvQuantity.setText(String.valueOf(ingredient.getQuantity()));
        holder.tvUnit.setText(ingredient.getUnit());

        if (ingredient.isLowStock()) {
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
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvUnit, tvStatus;
        LinearLayout layoutBackground;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvQuantity = itemView.findViewById(R.id.tvIngredientQuantity);
            tvUnit = itemView.findViewById(R.id.tvIngredientUnit);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            layoutBackground = itemView.findViewById(R.id.layoutBackground);
        }
    }
}
