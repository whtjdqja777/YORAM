package com.example.yoram;


import static android.content.Context.MODE_PRIVATE;

import android.app.AlarmManager;
import android.app.Activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class HomeFragment extends Fragment {
    TimePicker timePicker;
    Button button_Set_yoga;
    String offColor = "#FF4469";
    String onColor = "#A2D5F2";
    int[] buttonIds = {R.id.Sun, R.id.Tue, R.id.Wed, R.id.Thu, R.id.Fri, R.id.Sat, R.id.Mon};
    Map<Integer, Button> dayButtons = new HashMap<>();
    String currentActiveDay = null;
    Button setAlarmbtn;
    Calendar calendar;
    SharedPreferences Check_Clicked_prefs;
    SharedPreferences prefs;
    SharedPreferences prefs2;
    SharedPreferences day_of_weeks_request_code;
    HashSet<String> dayofweeks;
    HashSet Clicked;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_yoram_home, container, false);
        timePicker = view.findViewById(R.id.timePicker);
        button_Set_yoga = view.findViewById(R.id.buttonSetyoga);
        Log.d("홈 프레그먼트", "홈 프레그먼트");
        calendar = Calendar.getInstance();
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));
//        initializeDayButtons(view);

//        restoreState();


        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dayofweeks = new HashSet<>();
        day_of_weeks_request_code = getContext().getSharedPreferences("day_of_weeks_request_code",MODE_PRIVATE);
        Check_Clicked_prefs = getContext().getSharedPreferences("Clicked",MODE_PRIVATE);
        if (!day_of_weeks_request_code.contains("days")) {
            day_of_weeks_request_code.edit().putStringSet(String.valueOf("days"), new HashSet<>());
        }
        for (int i = 1; i < 8; i++){
            dayButtons.put(i, view.findViewById(buttonIds[i-1]));
            Log.d("daybuttons", String.valueOf(dayButtons));
            String key = String.valueOf(i);
            if (!day_of_weeks_request_code.contains(key)){
                day_of_weeks_request_code.edit().putStringSet(key, new HashSet<>()).apply();
            }
            String value = Check_Clicked_prefs.getString(key, "-1");
            if (!"0".equals(value) &&
                    !"1".equals(value)){
                Check_Clicked_prefs.edit().putString(String.valueOf(i), "0").apply();
                dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(offColor)));


            } else if ("0".equals(value)) {
                dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(offColor)));


            }else{
                dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(onColor)));
                HashSet tmpset = new HashSet(day_of_weeks_request_code.getStringSet("days", new HashSet<>()));
                tmpset.add(String.valueOf(i));
                day_of_weeks_request_code.edit().putStringSet("days", tmpset).apply();
            }
        }
        Log.d("알람 설정할 요일", String.valueOf(day_of_weeks_request_code.getStringSet("days", new HashSet<>())));
        prefs = getContext().getSharedPreferences("next_request_code",MODE_PRIVATE);
        prefs2 = getContext().getSharedPreferences("yoga", MODE_PRIVATE);
        setAlarmbtn = view.findViewById(R.id.setAlarmbtn);

        setAlarmbtn.setOnClickListener(v -> {

            if (prefs2.getStringSet("pose", new HashSet<>()).size() != 0){

                for (String i : day_of_weeks_request_code.getStringSet("days", new HashSet<>())){

                    setAlarm(i);
                }

                Log.d("prefs pose", String.valueOf(prefs2.getStringSet("pose", new HashSet<>())));
            }else{
                Toast.makeText(getContext(),"자세를 선택해 주세요", Toast.LENGTH_SHORT).show();
                Log.d("prefs pose", String.valueOf(prefs2.getStringSet("pose", new HashSet<>())));
            }

        });
        setDayButtonListeners();
//        initializeDayButtons(view);
        setSetYogaButtonListener();
    }

//    private void initializeDayButtons(View view) {
//        for (int i = 1; i < 8; i++) {
//            int value = Check_Clicked_prefs.getInt(String.valueOf(i), -1);
//
//            if (value != -1){
//                dayButtons.get(i).setBackgroundColor(value == 1 ? Color.parseColor(onColor) : Color.parseColor(offColor));
//
//
//            }
//        }
//    }


    // 요가 선택 버튼 눌렀을 때
    private void setSetYogaButtonListener() {

        button_Set_yoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), YogaSellectActivity.class);
                startActivity(intent);


            }
        });
    }

    private void setDayButtonListeners() {
        for (Map.Entry<Integer, Button> entry : dayButtons.entrySet()) {

            Integer day = entry.getKey();
            Log.d("day", String.valueOf(day));
            Button dayButton = entry.getValue();
            dayButton.setOnClickListener(v -> {// 이거 Check_Clicked_prefs 안쓰고 day_of_week_request_code의 days에 해당 요일이 존제하는가로 해도 될듯
                // day_of_week_request_code의.getStringSet("days", new HashSet<>()).contains(String.valueof(day));로 확인 하면 될듯
                // 클릭된 요일 저장

                if (Check_Clicked_prefs.getString(String.valueOf(day),"-1").equals("0")){
                    Check_Clicked_prefs.edit().putString(String.valueOf(day), "1").apply();
                    Log.d(String.valueOf(day) + "상태", Check_Clicked_prefs.getString(String.valueOf(day),"-1"));
                    dayButtons.get(day).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(onColor)));

                    HashSet tmpset = new HashSet(day_of_weeks_request_code.getStringSet("days", new HashSet<>()));
                    tmpset.add(String.valueOf(day));
                    day_of_weeks_request_code.edit().putStringSet("days", tmpset).apply();
                    Log.d("알람 설정할 요일", String.valueOf(day_of_weeks_request_code.getStringSet("days", new HashSet<>())));


                }else{// 이거 Check_Clicked_prefs 안쓰고 day_of_week_request_code의 days에 해당 요일이 존제하는가로 해도 될듯
                    Check_Clicked_prefs.edit().putString(String.valueOf(day), "0").apply();
                    Log.d(String.valueOf(day) + "상태", Check_Clicked_prefs.getString(String.valueOf(day),"-1"));
                    dayButtons.get(day).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(offColor)));

                    HashSet tmpset = new HashSet(day_of_weeks_request_code.getStringSet("days", new HashSet<>()));
                    tmpset.remove(String.valueOf(day));
                    day_of_weeks_request_code.edit().putStringSet("days", tmpset).apply();

                    Log.d("알람 설정할 요일", String.valueOf(day_of_weeks_request_code.getStringSet("days", new HashSet<>())));
                }

            });
        }
    }


    private void setAlarm(String day) {
        int requestcode = prefs.getInt("next_request_code", 1000);// 알람마다 requestcode를 따로 설정해야 여러 알람 가능

        prefs.edit().putInt("next_request_code", requestcode+1).apply();// 기존 resquestcode에 + 1을 해서 다음 알람 설정할떄 사용

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();


        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.)
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.d("AlarmDebug", "알람 설정 시간: " + hour + ":" + minute);
        AlarmManager alarmManager = null;
        if (getActivity() != null) {
            alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(!alarmManager.canScheduleExactAlarms()){
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.putExtra("Request_code", requestcode);
        intent.putExtra("NotificationID", requestcode);
        if (alarmManager != null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), requestcode, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("AlarmDebug", "알람 설정 완료");
            Toast.makeText(getActivity(), "Alarm set at " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
            // 요일별 requestcode 저장 -> 나중에 알람 제거에 사용
            HashSet tmpset = new HashSet<>(day_of_weeks_request_code.getStringSet(day, new HashSet<>()));
            tmpset.add(String.valueOf(requestcode));
            day_of_weeks_request_code.edit().putStringSet(day, tmpset).apply();

            Log.d("day_of_weeks_request_code", day + "요일" + String.valueOf(day_of_weeks_request_code.getStringSet(String.valueOf(day), new HashSet<>())))        ;
        }
    }
}