package com.example.milkteamanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.R;
import com.example.milkteamanagement.models.Topping;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminToppingAdapter extends RecyclerView.Adapter<AdminToppingAdapter.ViewHolder> {

    private List<Topping> toppingList;
    private OnToppingActionListener listener;

    public interface OnToppingActionListener {
        void onEdit(Topping topping);
        void onDelete(Topping topping);
    }

    public AdminToppingAdapter(List<Topping> toppingList, OnToppingActionListener listener) {
        this.toppingList = toppingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topping_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topping topping = toppingList.get(position);
        holder.tvName.setText(topping.getName());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText("+ " + formatter.format(topping.getPrice()));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(topping));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(topping));
    }

    @Override
    public int getItemCount() {
        return toppingList != null ? toppingList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminToppingName);
            tvPrice = itemView.findViewById(R.id.tvAdminToppingPrice);
            btnEdit = itemView.findViewById(R.id.btnEditTopping);
            btnDelete = itemView.findViewById(R.id.btnDeleteTopping);
        }
    }
}
