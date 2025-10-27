package com.example.hrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<Stock> stockItems;
    private OnUseStockListener onUseStockListener;

    public interface OnUseStockListener {
        void onUseStock(Stock stockItem);
    }

    public StockAdapter(List<Stock> stockItems, OnUseStockListener onUseStockListener) {
        this.stockItems = stockItems;
        this.onUseStockListener = onUseStockListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = stockItems.get(position);
        holder.itemNameTextView.setText(stock.getItemName());
        holder.quantityTextView.setText("Qty: " + stock.getQuantity());

        holder.useStockButton.setOnClickListener(v -> {
            if (onUseStockListener != null) {
                onUseStockListener.onUseStock(stock);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemNameTextView;
        public TextView quantityTextView;
        public Button useStockButton;

        public ViewHolder(View view) {
            super(view);
            itemNameTextView = view.findViewById(R.id.itemNameTextView);
            quantityTextView = view.findViewById(R.id.quantityTextView);
            useStockButton = view.findViewById(R.id.useStockButton);
        }
    }
}
