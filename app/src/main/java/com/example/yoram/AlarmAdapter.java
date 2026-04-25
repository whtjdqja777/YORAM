package com.example.yoram;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private ArrayList<AlarmItem> alarmlist;
    private onAlarmDeleteListener deletelistener;
    public interface onAlarmDeleteListener{
        void onAlarmDelete(AlarmItem alarmItem);
    }

    public AlarmAdapter(ArrayList<AlarmItem> alarmlist, onAlarmDeleteListener deletelistener){
        this.alarmlist = alarmlist;
        this.deletelistener = deletelistener;
    }
    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item, parent, false);
        return new AlarmViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AlarmAdapter.AlarmViewHolder holder, int position) {
        AlarmItem item = alarmlist.get(position);
        holder.date_id.setText(item.getDay());
        holder.delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deletelistener!= null)
                deletelistener.onAlarmDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return alarmlist.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder{
        Button delete_button;
        TextView date_id;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            delete_button = itemView.findViewById(R.id.delete_button);
            date_id = itemView.findViewById(R.id.date_id);
        }
    }


}
