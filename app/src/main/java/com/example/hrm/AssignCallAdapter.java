package com.example.hrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AssignCallAdapter extends RecyclerView.Adapter<AssignCallAdapter.ViewHolder> {

    private final List<AssignCall> callList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AssignCall call);
    }

    public AssignCallAdapter(List<AssignCall> callList, OnItemClickListener listener) {
        this.callList = callList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assign_call, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignCall call = callList.get(position);
        holder.dairyNameTextView.setText(call.getDairyName());
        holder.problemTextView.setText(call.getProblem());
        holder.statusTextView.setText(call.getStatus());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(call));
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dairyNameTextView, problemTextView, statusTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dairyNameTextView = itemView.findViewById(R.id.dairyNameTextView);
            problemTextView = itemView.findViewById(R.id.problemTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
}
