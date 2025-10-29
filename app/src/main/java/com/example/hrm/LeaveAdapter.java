package com.example.hrm;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.util.ArrayList;

public class LeaveAdapter extends RecyclerView.Adapter<LeaveAdapter.ViewHolder> {

    private final ArrayList<JSONObject> leaveList;

    public LeaveAdapter(ArrayList<JSONObject> leaveList) {
        this.leaveList = leaveList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JSONObject leave = leaveList.get(position);
        holder.bind(leave);
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }

        public void bind(JSONObject leave) {
            String type = leave.optString("type", "N/A");
            String start = leave.optString("start_date", "");
            String end = leave.optString("end_date", "");
            String status = leave.optString("status", "Pending");
            text1.setText(type + " (" + status + ")");
            text2.setText(start + " → " + end);
        }
    }
}
