package com.example.hrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EngineerAdapter extends RecyclerView.Adapter<EngineerAdapter.ViewHolder> {

    private List<User> engineers;

    public EngineerAdapter(List<User> engineers) {
        this.engineers = engineers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_engineer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User engineer = engineers.get(position);
        holder.nameTextView.setText(engineer.getName());
        holder.emailTextView.setText(engineer.getEmail());
    }

    @Override
    public int getItemCount() {
        return engineers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView emailTextView;

        public ViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.engineerNameTextView);
            emailTextView = view.findViewById(R.id.engineerEmailTextView);
        }
    }
}
