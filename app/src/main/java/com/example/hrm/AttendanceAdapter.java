package com.example.hrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<DailyAttendance> dailyRecords;

    public AttendanceAdapter(List<DailyAttendance> dailyRecords) {
        this.dailyRecords = dailyRecords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_history_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyAttendance record = dailyRecords.get(position);
        holder.dateTextView.setText(record.getDate());
        holder.punchInTextView.setText(record.getPunchInTime());
        holder.punchOutTextView.setText(record.getPunchOutTime());
    }

    @Override
    public int getItemCount() {
        return dailyRecords.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView punchInTextView;
        public TextView punchOutTextView;

        public ViewHolder(View view) {
            super(view);
            dateTextView = view.findViewById(R.id.dateTextView);
            punchInTextView = view.findViewById(R.id.punchInTextView);
            punchOutTextView = view.findViewById(R.id.punchOutTextView);
        }
    }
}
