package com.example.yoram;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AlarmListFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ArrayList<DayGroup> dayGroupList;
    private DayGroupAdapter adapter;
    private RecyclerView recyclerView;
    private Calendar calendar;
    private String YEAR_MONTH;
    private SharedPreferences day_of_weeks_request_code;
    private SharedPreferences Check_completed;
    private SharedPreferences ALARM_History;

    public AlarmListFragment() {
        // Required empty public constructor
    }

    public static AlarmListFragment newInstance(String param1, String param2) {
        AlarmListFragment fragment = new AlarmListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        calendar = Calendar.getInstance();
        YEAR_MONTH = calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.MONTH);

        Check_completed = getContext().getSharedPreferences("Check_completed", MODE_PRIVATE);
        day_of_weeks_request_code = getContext().getSharedPreferences("day_of_weeks_request_code", MODE_PRIVATE);
        ALARM_History = getContext().getSharedPreferences("ALARM_History", MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);

        recyclerView = view.findViewById(R.id.alarmRecyclerView);
        dayGroupList = new ArrayList<>();
        
        adapter = new DayGroupAdapter(dayGroupList, alarmItem -> {
            try {
                deleteAlarm(alarmItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        try {
            loadAlarmList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    public void loadAlarmList() throws JSONException {
        dayGroupList.clear();
        HashMap<String, ArrayList<AlarmItem>> map = new HashMap<>();

        Map<String, ?> getAll = day_of_weeks_request_code.getAll();
        for(String Day_key : getAll.keySet()){
            Set<String> dayset = day_of_weeks_request_code.getStringSet(Day_key, new HashSet<>());
            ArrayList<AlarmItem> listForDay = new ArrayList<>();
            for (String day : dayset){
                JSONObject object = new JSONObject(day);
                Iterator<String> keyIterator = object.keys();
                while (keyIterator.hasNext()){
                    String requestCodeKey = keyIterator.next();
                    JSONObject times_and_poses = (JSONObject) object.get(requestCodeKey);
                    JSONArray poses = times_and_poses.getJSONArray("poses");
                    listForDay.add(new AlarmItem(Day_key, requestCodeKey, times_and_poses.getInt("Hour"), times_and_poses.getInt("Minute"), poses));
                }
            }
            if (!listForDay.isEmpty()) {
                map.put(Day_key, listForDay);
            }
        }

        for (int i = 1; i <= 7; i++) {
            String dayKey = String.valueOf(i);
            if (map.containsKey(dayKey)) {
                dayGroupList.add(new DayGroup(dayKey, map.get(dayKey)));
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteAlarm(AlarmItem alarmItem) throws JSONException {
        String deleteRequestCode = alarmItem.getRequest_Code();
        String dayKey = alarmItem.getDay();
        
        Set<String> oldset = day_of_weeks_request_code.getStringSet(dayKey, new HashSet<>());
        HashSet<String> newSet = new HashSet<>();

        for (String ob : oldset) {
            JSONObject object = new JSONObject(ob);
            if (object.has(deleteRequestCode)) {
                ALARM_History.edit().putString(deleteRequestCode, String.valueOf(object.getJSONObject(deleteRequestCode))).apply();
            } else {
                newSet.add(ob);
            }
        }

        if (newSet.isEmpty()) {
            day_of_weeks_request_code.edit().remove(dayKey).apply();
        } else {
            day_of_weeks_request_code.edit().putStringSet(dayKey, newSet).apply();
        }

        try {
            deleteAlarmRequestCode(Integer.parseInt(deleteRequestCode));
        } catch (Exception e) {
            Log.e("삭제 오류", e.toString());
        }

        //Check_completed -> 날짜별 알람 삭제
        Delete_requestcode_From_Check_completed(dayKey, deleteRequestCode, alarmItem.getHour(), alarmItem.getMinute());
        
        loadAlarmList();
    }

    private void deleteAlarmRequestCode(int RequestedCode) {
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        intent.setAction("ALARM_ACTION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), RequestedCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent Fail_intent = new Intent(requireContext(), FailReceiver.class);
        PendingIntent Fail_pendingIntent = PendingIntent.getBroadcast(requireContext(), RequestedCode, Fail_intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager.cancel(Fail_pendingIntent);
            pendingIntent.cancel();
            Fail_pendingIntent.cancel();
        }
    }

    private void Delete_requestcode_From_Check_completed(String day, String RequestCode, int HOUR, int MINUTE) throws JSONException {
        //day는 요일정보
        Calendar alarmitem_calendar = Calendar.getInstance();

        for (String key : Check_completed.getAll().keySet()) {
            ArrayList<String> delete_dates = new ArrayList<>();
            String[] split = key.split("_");
            if (split.length < 2) continue;
            int YEAR = Integer.parseInt(split[0]);
            int MONTH = Integer.parseInt(split[1]);
            
            alarmitem_calendar.set(Calendar.YEAR, YEAR);
            alarmitem_calendar.set(Calendar.MONTH, MONTH);
            int lastday = alarmitem_calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 1; i <= lastday; i++) { // i는 1부터 시작함
                // YEAR_MONTH 설정 한 순간 부터
                alarmitem_calendar.set(YEAR, MONTH, i, HOUR, MINUTE, 0);
                if (Integer.parseInt(day) == alarmitem_calendar.get(Calendar.DAY_OF_WEEK)
                        && alarmitem_calendar.getTimeInMillis() > System.currentTimeMillis()) {
                    //여기서 날짜 자체를 제외해도 되는 이유가 삭제 하고자하는 requestcode가 1개 이기 때문에
                    delete_dates.add(String.valueOf(i));
                }
            }

            Set<String> Check_completed_set = Check_completed.getStringSet(key, new HashSet<>());
            Set<String> newSet = new HashSet<>();

            for (String JSONString : Check_completed_set) {
                JSONObject object = new JSONObject(JSONString);
                for (String delete_day : delete_dates) {
                    if (object.has(delete_day)) {
                        JSONObject dayObj = object.getJSONObject(delete_day);
                        dayObj.remove(RequestCode);
                        if (dayObj.length() == 0) {
                            object.remove(delete_day);
                        }
                    }
                }
                newSet.add(object.toString());
            }
            Check_completed.edit().putStringSet(key, newSet).apply();
        }
    }
}
