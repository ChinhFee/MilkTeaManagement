package com.example.milkteamanagement;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.milkteamanagement.models.Product;
import com.example.milkteamanagement.repositories.GoogleDriveRepository;
import com.google.gson.Gson;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PRODUCT = 1;

    private final List<Object> itemList;
    private final GoogleDriveRepository driveRepository = new GoogleDriveRepository();

    public ProductAdapter(List<Object> itemList) {
        this.itemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= itemList.size()) return TYPE_PRODUCT;
        if (itemList.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < 0 || position >= itemList.size()) return;

        if (getItemViewType(position) == TYPE_HEADER) {
            HeaderViewHolder hHolder = (HeaderViewHolder) holder;
            String title = (String) itemList.get(position);
            hHolder.tvHeader.setText(title != null ? title : "Loại khác");
        } else {
            Object item = itemList.get(position);
            if (!(item instanceof Product)) return;
            
            Product product = (Product) item;
            ProductViewHolder pViewHolder = (ProductViewHolder) holder;
            
            pViewHolder.tvName.setText(product.getName() != null ? product.getName() : "Sản phẩm chưa có tên");
            
            try {
                pViewHolder.tvPrice.setText(String.format(Locale.getDefault(), "%,d VNĐ", product.getPrice()));
            } catch (Exception e) {
                pViewHolder.tvPrice.setText("Liên hệ");
            }

            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                String directLink = driveRepository.convertToDirectLink(imageUrl);
                Glide.with(holder.itemView.getContext())
                        .load(directLink)
                        .placeholder(R.drawable.img_placeholder)
                        .error(R.drawable.img_placeholder)
                        .into(pViewHolder.imgProduct);
            } else {
                pViewHolder.imgProduct.setImageResource(R.drawable.img_placeholder);
            }

            if (product.isAvailable()) {
                pViewHolder.viewOutStock.setVisibility(View.GONE);
                pViewHolder.tvOutStock.setVisibility(View.GONE);
                holder.itemView.setAlpha(1.0f);
                holder.itemView.setEnabled(true);
                
                holder.itemView.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
                        intent.putExtra("product_json", new Gson().toJson(product));
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                pViewHolder.viewOutStock.setVisibility(View.VISIBLE);
                pViewHolder.tvOutStock.setVisibility(View.VISIBLE);
                holder.itemView.setAlpha(0.6f);
                holder.itemView.setEnabled(false);
                holder.itemView.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeaderCategory);
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvOutStock;
        View viewOutStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOutStock = itemView.findViewById(R.id.tvOutStock);
            viewOutStock = itemView.findViewById(R.id.viewOutStock);
        }
    }
}
