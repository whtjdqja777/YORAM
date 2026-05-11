package com.example.yoram;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private ArrayList<AlarmItem> alarmlist;
    private onAlarmDeleteListener deletelistener;

    public interface onAlarmDeleteListener{
        void onAlarmDelete(AlarmItem alarmItem) throws JSONException;
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


        Log.d("Adapter_alarmList", String.valueOf(alarmlist));

        String alarmTime = item.getHour() + "시" + " " + item.getMinute() + "분";
        holder.date_id.setText(alarmTime);

        holder.delete_button.setOnClickListener(v -> {
            if (deletelistener != null) {
                try {
                    deletelistener.onAlarmDelete(item);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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
