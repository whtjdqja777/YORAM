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

public class HomeFragment extends Fragment {
    TimePicker timePicker;
    Button button_Set_yoga;
    String offColor = "#FF4469";
    String onColor = "#A2D5F2";
    int[] buttonIds = {R.id.Sun, R.id.Mon, R.id.Tue, R.id.Wed, R.id.Thu, R.id.Fri, R.id.Sat};
    Map<Integer, Button> dayButtons = new HashMap<>();
    String currentActiveDay = null;
    Button setAlarmbtn;
    Calendar calendar;
    HashSet<String> Clicked_button_info;
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
        Clicked_button_info = new HashSet<>();
//        dayofweeks = new HashSet<>();
        day_of_weeks_request_code = getContext().getSharedPreferences("day_of_weeks_request_code", MODE_PRIVATE);
//        day_of_weeks_request_code.edit().clear().apply();
        Log.d("day_of_weeks_request_code", day_of_weeks_request_code.toString());
        Check_Clicked_prefs = getContext().getSharedPreferences("Clicked", MODE_PRIVATE);

        for (int i = 1; i < 8; i++) {// 이부분이 prefs 참고해서 이전에 버튼 클릭여부에 따라 버튼 초기화 하는 부분인데 필요 없을듯
            dayButtons.put(i, view.findViewById(buttonIds[i - 1]));
            Log.d("daybuttons", String.valueOf(dayButtons));
            String key = String.valueOf(i);

//            String value = Check_Clicked_prefs.getString(key, "-1");
//            if (!"0".equals(value) &&
//                    !"1".equals(value)){
//                Check_Clicked_prefs.edit().putString(String.valueOf(i), "0").apply();
//                dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(offColor)));
//
//
//            } else if ("0".equals(value)) {
//                dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(offColor)));
//
//
//            }else{
//                dayButtons.get(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(onColor)));
//                HashSet tmpset = new HashSet(day_of_weeks_request_code.getStringSet("days", new HashSet<>()));
//                tmpset.add(String.valueOf(i));
//                Log.d("초기 알람 선택 요일", String.valueOf(tmpset));
//                day_of_weeks_request_code.edit().putStringSet("days", tmpset).apply();
//            }
        }

        prefs = getContext().getSharedPreferences("next_request_code", MODE_PRIVATE);
        prefs2 = getContext().getSharedPreferences("yoga", MODE_PRIVATE);
        setAlarmbtn = view.findViewById(R.id.setAlarmbtn);

        setAlarmbtn.setOnClickListener(v -> {

            if (!prefs2.getStringSet("pose", new HashSet<>()).isEmpty() && !Clicked_button_info.isEmpty()) {

                for (String i : Clicked_button_info) {

                    try {
                        setAlarm(i);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                Log.d("prefs pose", String.valueOf(prefs2.getStringSet("pose", new HashSet<>())));
            } else if (Clicked_button_info.isEmpty() && prefs2.getStringSet("pose", new HashSet<>()).isEmpty()) {
                Toast.makeText(getContext(), "요일과 자세를 선텍해주세요", Toast.LENGTH_SHORT).show();
            } else if(Clicked_button_info.isEmpty()){
                Toast.makeText(getContext(), "요일를 선택해 주세요", Toast.LENGTH_SHORT).show();
                Log.d("prefs pose", String.valueOf(prefs2.getStringSet("pose", new HashSet<>())));
            }else{
                Toast.makeText(getContext(), "자세를 선택해 주세요", Toast.LENGTH_SHORT).show();
            }
            Log.d("dayofweek_requestcode", String.valueOf(day_of_weeks_request_code.getAll()));

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
            Log.d("day", String.valueOf(day));
            dayButton.setOnClickListener(v -> {// 이거 Check_Clicked_prefs 안쓰고 day_of_week_request_code의 days에 해당 요일이 존제하는가로 해도 될듯
                // day_of_week_request_code의.getStringSet("days", new HashSet<>()).contains(String.valueof(day));로 확인 하면 될듯
                // 클릭된 요일 저장
                if (Clicked_button_info.contains(String.valueOf(day))) {
                    Clicked_button_info.remove(String.valueOf(day));
                    dayButtons.get(day).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(offColor)));
                } else {
                    dayButtons.get(day).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(onColor)));
                    Clicked_button_info.add(String.valueOf(day));
                }
                Log.d("Clicked_button_info", String.valueOf(Clicked_button_info));


            });
        }
    }


    private void setAlarm(String day) throws JSONException {// 여기서 JSONobject 호출해서 데이터 구조화 필요
        // 여기서 이전 시간에 알람을 설정하면 바로 울리게 되는데 이를 해결하는 로직 필요
        Boolean dupicated = false;
        JSONObject request_and_time_object = new JSONObject();
        JSONObject time_object = new JSONObject();
        int requestcode = prefs.getInt("next_request_code", 1000);// 알람마다 requestcode를 따로 설정해야 여러 알람 가능
//        request_object.put(String.valueOf(requestcode), )

        JSONArray poses = new JSONArray(); // day_of_weeks_request_code에 들어갈 포즈 리스트
        for (String pose : prefs2.getStringSet("pose", new HashSet<>())){
            poses.put(pose);
        }

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        Set<String> oldeset = day_of_weeks_request_code.getStringSet(day, new HashSet<>());
        Set<String> newset = new HashSet<>();
        //해당 날짜에
        if(day_of_weeks_request_code.getAll().keySet().contains(day)
        &&day_of_weeks_request_code.getStringSet(day, new HashSet<>()).size() != 0){
            for (String JSONString : oldeset) {
                JSONObject object = new JSONObject(JSONString);
                Log.d("JSONobject", String.valueOf(object));
                //해당 시간 분의 알람이 있으면

                Iterator<String> Keys = object.keys();
                while(Keys.hasNext()){
                    String currentkey = Keys.next();
                    JSONObject timeobject = (JSONObject) object.get(currentkey);
                    if (timeobject.getInt("Hour") == hour && timeobject.getInt("Minute") == minute
                    ) {
                        if (timeobject.getJSONArray("poses").equals(poses)){
                            Log.d("중복 알림", "완전히 중복된 알람입니다.");
                            Toast.makeText(getContext(), "중복된 알람입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            Log.d("중복 알람", day + "요일" + hour + "시" + " " + minute + "분");
                            dupicated = true;
                            requestcode = Integer.parseInt(currentkey);
                            Toast.makeText(getContext(), "기존 알람이 수정됩니다. " + timeobject.getJSONArray("poses")
                                     + " -> " + poses, Toast.LENGTH_SHORT).show();
                            //여기서 중복된 알람은 newset에 저장을 안하기 때문에 중복 제거됨
                        }

                    }
                    else{
                        JSONObject keep = new JSONObject();
                        keep.put(currentkey, timeobject);
                        newset.add(keep.toString());
                    }
                }

        }


        }
        if (dupicated){
            day_of_weeks_request_code.edit().remove(day).apply();// 기존 day에 해당하는 set 제거
            day_of_weeks_request_code.edit().putStringSet(day, newset).apply();
            //day에 대한 중복 알람이 제거된 새로운 newset을 저장

        }else{
            prefs.edit().putInt("next_request_code", requestcode + 1).apply();// 기존 resquestcode에 + 1을 해서 다음 알람 설정할떄 사용
        }
        time_object.put("Hour", hour);
        time_object.put("Minute", minute);

        time_object.put("poses", poses);
        request_and_time_object.put(String.valueOf(requestcode), time_object);
        HashSet tmp_hashset = new HashSet(day_of_weeks_request_code.getStringSet(String.valueOf(day), new HashSet<>()));
        tmp_hashset.add(request_and_time_object.toString());
        day_of_weeks_request_code.edit().putStringSet(String.valueOf(day), tmp_hashset).apply();

        Log.d("day_of_weeks_request_code", day + "요일" + String.valueOf(day_of_weeks_request_code.getStringSet(String.valueOf(day), new HashSet<>())));


        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.)
        calendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(day));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 7);
        }
        Log.d("AlarmDebug", "알람 설정 시간: " + hour + ":" + minute);
        AlarmManager alarmManager = null;
        if (getActivity() != null) {
            alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("weekday", Integer.parseInt(day));
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.putExtra("Request_code", requestcode);
        intent.putExtra("NotificationID", requestcode);
        intent.putExtra("poses", poses.toString());
        intent.setAction("ALARM_ACTION");

        Calendar Failcalendar = (Calendar) calendar.clone();
        Failcalendar.add(Calendar.MINUTE, 1);//alarmmanager에 전달할 실패시간

        Intent Fail_intent = new Intent(getActivity(), FailReceiver.class);
        Fail_intent.putExtra("weekday", Integer.parseInt(day));
        Fail_intent.putExtra("hour", hour);
        Fail_intent.putExtra("minute", minute+1);

        Fail_intent.putExtra("Request_code", requestcode);
        Fail_intent.putExtra("NotificationID", requestcode);
        Fail_intent.putExtra("poses", poses.toString());
        Fail_intent.setAction("FAIL_ACTION");
        Log.d("Fail_intent", Fail_intent.getExtras().toString());
        if (alarmManager != null) {
            Log.d("setAlarm requestcode", String.valueOf(requestcode));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), requestcode, intent, PendingIntent.FLAG_IMMUTABLE| PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent Fail_pendingIntent = PendingIntent.getBroadcast(getActivity(), requestcode, Fail_intent, PendingIntent.FLAG_IMMUTABLE| PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Failcalendar.getTimeInMillis(), Fail_pendingIntent);
            Log.d("AlarmDebug", "알람 설정 완료");
            Toast.makeText(getActivity(), "Alarm set at " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
            // 요일별 requestcode 저장 -> 나중에 알람 제거에 사용

        }
    }
}