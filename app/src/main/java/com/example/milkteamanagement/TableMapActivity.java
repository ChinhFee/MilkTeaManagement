package com.example.milkteamanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.TableModel;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class TableMapActivity extends AppCompatActivity {

    private RecyclerView rvTables;
    private TableAdapter adapter;
    private List<TableModel> allTables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_map);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvTables = findViewById(R.id.rvTables);
        TabLayout tabLayout = findViewById(R.id.tabLayoutFloors);

        rvTables.setLayoutManager(new GridLayoutManager(this, 3));
        
        initTableData();
        filterTables(1);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterTables(tab.getPosition() + 1);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initTableData() {
        allTables = new ArrayList<>();
        for (int floor = 1; floor <= 3; floor++) {
            for (int i = 1; i <= 22; i++) {
                int tableNum = ((floor - 1) * 22) + i;
                String status = "Available";
                
                if (tableNum % 7 == 0) status = "Ordering";
                else if (tableNum % 5 == 0) status = "Occupied";
                
                allTables.add(new TableModel(tableNum, status, floor));
            }
        }
    }

    private void filterTables(int floor) {
        List<TableModel> filtered = new ArrayList<>();
        for (TableModel t : allTables) {
            if (t.getFloor() == floor) filtered.add(t);
        }
        adapter = new TableAdapter(filtered);
        rvTables.setAdapter(adapter);
    }

    private class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {
        private final List<TableModel> tables;
        public TableAdapter(List<TableModel> tables) { this.tables = tables; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TableModel table = tables.get(position);
            holder.tvNum.setText(String.format("%02d", table.getTableNumber()));
            holder.tvStatus.setText(table.getStatus());

            int color;
            switch (table.getStatus()) {
                case "Ordering":
                    color = ContextCompat.getColor(holder.itemView.getContext(), R.color.successColor);
                    break;
                case "Occupied":
                    color = ContextCompat.getColor(holder.itemView.getContext(), R.color.errorColor);
                    break;
                default:
                    color = ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent);
                    holder.card.setCardBackgroundColor(android.graphics.Color.parseColor("#1AFFFFFF"));
                    return;
            }
            holder.card.setCardBackgroundColor(color);

            holder.itemView.setOnClickListener(v -> {
                if (table.getStatus().equals("Ordering")) {
                    table.setStatus("Occupied");
                    notifyItemChanged(position);
                } else if (table.getStatus().equals("Occupied")) {
                    table.setStatus("Available");
                    notifyItemChanged(position);
                } else {
                    table.setStatus("Ordering");
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() { return tables.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNum, tvStatus;
            CardView card;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvNum = v.findViewById(R.id.tvTableNumber);
                tvStatus = v.findViewById(R.id.tvTableStatus);
                card = (CardView) v;
            }
        }
    }
}
