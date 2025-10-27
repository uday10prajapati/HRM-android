package com.example.hrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.ViewHolder> {

    private List<Leave> leaveRecords;

    public LeaveAdapter(List<Leave> leaveRecords) {
        this.leaveRecords = leaveRecords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leave_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Leave leave = leaveRecords.get(position);
        holder.leaveTypeTextView.setText(leave.getType());
        holder.leaveDatesTextView.setText(leave.getStartDate() + " - " + leave.getEndDate());
        holder.leaveStatusTextView.setText(leave.getStatus());
        holder.leaveReasonTextView.setText(leave.getReason());
    }

    @Override
    public int getItemCount() {
        return leaveRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView leaveTypeTextView;
        public TextView leaveDatesTextView;
        public TextView leaveStatusTextView;
        public TextView leaveReasonTextView;

        public ViewHolder(View view) {
            super(view);
            leaveTypeTextView = view.findViewById(R.id.leaveTypeTextView);
            leaveDatesTextView = view.findViewById(R.id.leaveDatesTextView);
            leaveStatusTextView = view.findViewById(R.id.leaveStatusTextView);
            leaveReasonTextView = view.findViewById(R.id.leaveReasonTextView);
        }
    }
}
