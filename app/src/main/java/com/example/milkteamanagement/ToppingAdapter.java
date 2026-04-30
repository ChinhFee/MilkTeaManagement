package com.example.milkteamanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.Topping;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ToppingAdapter extends RecyclerView.Adapter<ToppingAdapter.ToppingViewHolder> {

    private final List<Topping> toppingList;
    private final List<Topping> selectedToppings = new ArrayList<>();

    public ToppingAdapter(List<Topping> toppingList) {
        this.toppingList = toppingList;
    }

    @NonNull
    @Override
    public ToppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topping, parent, false);
        return new ToppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToppingViewHolder holder, int position) {
        Topping topping = toppingList.get(position);
        holder.cbTopping.setText(topping.getName());
        holder.tvToppingPrice.setText(String.format(Locale.getDefault(), "+ %,dđ", topping.getPrice()));

        holder.cbTopping.setOnCheckedChangeListener(null);
        holder.cbTopping.setChecked(selectedToppings.contains(topping));

        holder.cbTopping.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedToppings.add(topping);
            } else {
                selectedToppings.remove(topping);
            }
        });
    }

    @Override
    public int getItemCount() {
        return toppingList != null ? toppingList.size() : 0;
    }

    public List<Topping> getSelectedToppings() {
        return selectedToppings;
    }

    public static class ToppingViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbTopping;
        TextView tvToppingPrice;

        public ToppingViewHolder(@NonNull View itemView) {
            super(itemView);
            cbTopping = itemView.findViewById(R.id.cbTopping);
            tvToppingPrice = itemView.findViewById(R.id.tvToppingPrice);
        }
    }
}