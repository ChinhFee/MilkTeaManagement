package com.example.milkteamanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.CartItem;
import com.example.milkteamanagement.models.Topping;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartChangeListener listener;
    private DecimalFormat formatter = new DecimalFormat("#,###đ");

    public interface OnCartChangeListener {
        void onQuantityChanged();
        void onItemRemoved(int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartChangeListener listener) {
        this.cartItems = new ArrayList<>(cartItems);
        this.listener = listener;
    }

    public void updateData(List<CartItem> newData) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CartDiffCallback(this.cartItems, newData));
        this.cartItems.clear();
        this.cartItems.addAll(newData);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getProductName());
        
        StringBuilder options = new StringBuilder("Size " + item.getSize());
        if (item.getToppings() != null && !item.getToppings().isEmpty()) {
            options.append(", ");
            for (int i = 0; i < item.getToppings().size(); i++) {
                options.append(item.getToppings().get(i).getName());
                if (i < item.getToppings().size() - 1) options.append(", ");
            }
        }
        holder.tvOptions.setText(options.toString());
        holder.tvPrice.setText(formatter.format(item.getSubTotal()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        holder.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            item.calculateSubTotal();
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) listener.onQuantityChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                item.calculateSubTotal();
                notifyItemChanged(currentPos);
                if (listener != null) listener.onQuantityChanged();
            } else {
                if (listener != null) listener.onItemRemoved(currentPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvOptions, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvOptions = itemView.findViewById(R.id.tvOptions);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }

    private static class CartDiffCallback extends DiffUtil.Callback {
        private final List<CartItem> oldList;
        private final List<CartItem> newList;

        public CartDiffCallback(List<CartItem> oldList, List<CartItem> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }
        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            CartItem oldItem = oldList.get(oldItemPosition);
            CartItem newItem = newList.get(newItemPosition);
            return oldItem.getProductId().equals(newItem.getProductId()) && 
                   oldItem.getSize().equals(newItem.getSize());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            CartItem oldItem = oldList.get(oldItemPosition);
            CartItem newItem = newList.get(newItemPosition);
            return oldItem.getQuantity() == newItem.getQuantity() && 
                   oldItem.getSubTotal() == newItem.getSubTotal();
        }
    }
}
