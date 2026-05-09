package com.example.yoram;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DayGroupAdapter extends RecyclerView.Adapter<DayGroupAdapter.ViewHolder> {
    private ArrayList<DayGroup> dayGroups;
    private AlarmAdapter.onAlarmDeleteListener deleteListener;

    public DayGroupAdapter(ArrayList<DayGroup> dayGroups, AlarmAdapter.onAlarmDeleteListener deleteListener) {
        this.dayGroups = dayGroups;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayGroup group = dayGroups.get(position);
        holder.dayTitle.setText(getDayName(group.getDay()));

        AlarmAdapter childAdapter = new AlarmAdapter(group.getAlarms(), deleteListener);
        holder.childRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.childRecyclerView.setAdapter(childAdapter);
    }

    private String getDayName(String day) {
        switch (day) {
            case "1": return "일요일";
            case "2": return "월요일";
            case "3": return "화요일";
            case "4": return "수요일";
            case "5": return "목요일";
            case "6": return "금요일";
            case "7": return "토요일";
            default: return "알람";
        }
    }

    @Override
    public int getItemCount() {
        return dayGroups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTitle;
        RecyclerView childRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.day_title);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}
