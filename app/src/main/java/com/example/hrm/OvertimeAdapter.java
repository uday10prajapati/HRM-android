package com.example.hrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OvertimeAdapter extends RecyclerView.Adapter<OvertimeAdapter.ViewHolder> {

    private List<OvertimeRecord> overtimeRecords;

    public OvertimeAdapter(List<OvertimeRecord> overtimeRecords) {
        this.overtimeRecords = overtimeRecords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_overtime_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OvertimeRecord record = overtimeRecords.get(position);
        holder.dateTextView.setText(record.getDate());
        holder.hoursTextView.setText(String.format("%.2f hours", record.getHours()));
    }

    @Override
    public int getItemCount() {
        return overtimeRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView hoursTextView;

        public ViewHolder(View view) {
            super(view);
            dateTextView = view.findViewById(R.id.dateTextView);
            hoursTextView = view.findViewById(R.id.hoursTextView);
        }
    }
}
