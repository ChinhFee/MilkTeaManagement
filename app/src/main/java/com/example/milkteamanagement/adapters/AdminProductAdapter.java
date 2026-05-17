package com.example.milkteamanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.milkteamanagement.R;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.repositories.GoogleDriveRepository;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
        void onToggleAvailability(Product product, boolean isAvailable);
    }

    public AdminProductAdapter(List<Product> productList, OnProductActionListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(product.getPrice()));

        String directLink = new GoogleDriveRepository().convertToDirectLink(product.getImageUrl());
        Glide.with(holder.itemView.getContext())
                .load(directLink)
                .placeholder(R.drawable.img_placeholder)
                .into(holder.imgProduct);

        holder.switchAvailable.setOnCheckedChangeListener(null); // Tránh trigger loop
        holder.switchAvailable.setChecked(product.isAvailable());
        holder.switchAvailable.setText(product.isAvailable() ? "Còn" : "Hết");
        
        holder.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            product.setAvailable(isChecked);
            holder.switchAvailable.setText(isChecked ? "Còn" : "Hết");
            listener.onToggleAvailability(product, isChecked);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvCategory, tvPrice;
        ImageButton btnEdit, btnDelete;
        androidx.appcompat.widget.SwitchCompat switchAvailable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgAdminProduct);
            tvName = itemView.findViewById(R.id.tvAdminProductName);
            tvCategory = itemView.findViewById(R.id.tvAdminProductCategory);
            tvPrice = itemView.findViewById(R.id.tvAdminProductPrice);
            btnEdit = itemView.findViewById(R.id.btnEditProduct);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
            switchAvailable = itemView.findViewById(R.id.switchProductAvailable);
        }
    }
}
