package com.example.hrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assign_call_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignCall call = callList.get(position);
        holder.callIdTextView.setText(String.valueOf(call.getCallId()));
        holder.priorityTextView.setText("Medium"); // This seems to be hardcoded in the image
        holder.statusTextView.setText(call.getStatus());
        holder.nameTextView.setText(call.getName());
        holder.dairyNameTextView.setText(call.getDairyName());
        // holder.locationTextView.setText(""); // No location data in AssignCall
        holder.complaintTextView.setText(call.getProblem());
        holder.solutionTextView.setText(call.getDescription()); // Assuming description is solution
        holder.assignedToTextView.setText(call.getName()); // Assuming the person who created the call is the one it's assigned to
        holder.callButton.setText(String.valueOf(call.getMobileNumber()));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(call));
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView callIdTextView, priorityTextView, statusTextView, nameTextView, dairyNameTextView, locationTextView, complaintTextView, solutionTextView, assignedToTextView;
        Button callButton;
        ImageView feedbackImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            callIdTextView = itemView.findViewById(R.id.callIdTextView);
            priorityTextView = itemView.findViewById(R.id.priorityTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dairyNameTextView = itemView.findViewById(R.id.dairyNameTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            complaintTextView = itemView.findViewById(R.id.complaintTextView);
            solutionTextView = itemView.findViewById(R.id.solutionTextView);
            assignedToTextView = itemView.findViewById(R.id.assignedToTextView);
            callButton = itemView.findViewById(R.id.callButton);
            feedbackImageView = itemView.findViewById(R.id.feedbackImageView);
        }
    }
}
