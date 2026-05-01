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

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<AlarmItem> alarmList;
    private AlarmAdapter adapter;
    private RecyclerView recyclerView;
    private Calendar calendar;
    private String YEAR_MONTH;
    private SharedPreferences day_of_weeks_request_code;
    private SharedPreferences Check_completed;

    public AlarmListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlarmList.
     */
    // TODO: Rename and change types and number of parameters
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);


        recyclerView = view.findViewById(R.id.alarmRecyclerView);
        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmList, alarmItem -> {
            // 알람 삭제 로직 구현
            deleteAlarm(alarmItem);
            Log.d("dayofweek_requestcode", String.valueOf(day_of_weeks_request_code.getAll()));

        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        try {
            loadAlarmList();
            Log.d("AlarmList", String.valueOf(alarmList));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void loadAlarmList() throws JSONException {
        alarmList.clear();
        Map<String, ?> getAll = day_of_weeks_request_code.getAll();
        for(String Day_key : getAll.keySet()){

            Set<String> dayset = day_of_weeks_request_code.getStringSet(Day_key, new HashSet<>());
            for (String day : dayset){
                JSONObject object = new JSONObject(day);
                Iterator<String> key = object.keys();
                while (key.hasNext()){
                    String requestCodeKey = key.next();
                    JSONObject times_and_poses = (JSONObject) object.get(requestCodeKey);

                    Log.d("times_and_poses", String.valueOf(times_and_poses));
                    JSONArray poses = times_and_poses.getJSONArray("poses");
                    Log.d("poses", String.valueOf(poses));

//                    Log.d("pose", pose);
                    alarmList.add(new AlarmItem(Day_key, requestCodeKey, times_and_poses.getInt("Hour"),times_and_poses.getInt("Minute"), poses));

                }

            }

        }
        adapter.notifyDataSetChanged();
    }

    private void deleteAlarm(AlarmItem alarmItem) throws JSONException {

        HashSet newSet = new HashSet<>();
        String deleteRequestCode = alarmItem.getRequest_Code();
        Log.d("삭제할 Request_code", deleteRequestCode);
        for (int i = 0; i < alarmList.size(); i++){

            if (alarmList.get(i).getRequest_Code().equals(deleteRequestCode) ){
                alarmList.remove(i);
                adapter.notifyDataSetChanged();
                Set<String> oldset = day_of_weeks_request_code.getStringSet(alarmItem.getDay(), new HashSet<>());
                day_of_weeks_request_code.edit().remove(alarmItem.getDay()).apply();

                for (String ob :  oldset){

                    JSONObject object = new JSONObject(ob);

                    if (object.has(deleteRequestCode)){
                        Log.d("delete_object", object.toString());
                    }else{
                        newSet.add(ob);
                    }

                    }
            }
        }
        if (!newSet.isEmpty()){
            day_of_weeks_request_code.edit().putStringSet(alarmItem.getDay(), newSet).apply();

        }
        try {
            deleteAlarmRequestCode(Integer.parseInt(deleteRequestCode));
        }catch (Exception e){
            Log.e("삭제 오류", e.toString());
        }
        Delete_requestcode_From_Check_completed(alarmItem.getDay(), deleteRequestCode);
        //Check_completed -> 날짜별 알람 삭제
    }
    private void deleteAlarmRequestCode (int RequstedCode){

        Log.d("삭제 requsetcode 전달", String.valueOf(RequstedCode));
        Intent intent = new Intent(requireContext(), AlarmReceiver.class );
        intent.setAction("ALARM_ACTION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), RequstedCode, intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT );

        Intent Fail_intent = new Intent(requireContext(), FailReceiver.class );

        PendingIntent Fail_pendingIntent = PendingIntent.getBroadcast(requireContext(), RequstedCode, Fail_intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            alarmManager.cancel(Fail_pendingIntent);

            pendingIntent.cancel();
            Fail_pendingIntent.cancel();
            Log.d("알람, 실패 알람 삭제", pendingIntent.toString());
        }


    }
    private void Delete_requestcode_From_Check_completed(String day, String RequestCode) throws JSONException {
        int lastday = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        ArrayList<String> delete_dates = new ArrayList<>();
        //day는 요일정보
        Log.d("요일정보", day);
        for (int i = calendar.get(Calendar.DAY_OF_MONTH); i <= lastday; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            if (Integer.parseInt(day) == calendar.get(Calendar.DAY_OF_WEEK)) {
                delete_dates.add(String.valueOf(i));
            }

        }
        Log.d("delete_dates", String.valueOf(delete_dates));

        Set<String> Check_completed_set = Check_completed.getStringSet(YEAR_MONTH, new HashSet<>());
        Set<String> newSet = new HashSet<>();

            for (String JSONString : Check_completed_set) {
                JSONObject object = new JSONObject(JSONString);

                for(String delete_day : delete_dates) {

                    if(object.has(delete_day)){
                        JSONObject newdayobj = (JSONObject) object.get(delete_day);
                        newdayobj.remove(RequestCode);
                        object.remove(delete_day);
                        if (newdayobj.length() > 0) {
                            object.put(delete_day, newdayobj);
                        }
                    }else{
                        Log.d("해당 키가 없습니다.", delete_day);
                    }


            }
                newSet.add(object.toString());
        }

        Log.d("newSet", String.valueOf(newSet));
        Check_completed.edit().putStringSet(YEAR_MONTH, newSet).apply();
        Log.d("Check_completed", String.valueOf(Check_completed.getAll()));

    }
}