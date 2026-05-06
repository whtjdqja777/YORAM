package com.example.yoram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class BootedReceiver extends BroadcastReceiver {
    private SharedPreferences day_of_week_requestcode;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootedReceiver", "BootedReceiver");
        // 부팅이 되면 알람 했던 정보 다 사리지기 때문에
        // 다시 알람 해줘야됨
        // 알람Receiver로 가는 intent 만들어 줘야되서 기존 정보 다 가져와 댜욈
//       //Intent intent = new Intent(getActivity(), AlarmReceiver.class);
//        intent.putExtra("YEAR", calendar.get(Calendar.YEAR));
//        intent.putExtra("MONTH", calendar.get(Calendar.MONTH));
//        intent.putExtra("weekday", day);
//        intent.putExtra("hour", hour);
//        intent.putExtra("minute", minute);
//        intent.putExtra("Request_code", requestcode);
//        intent.putExtra("NotificationID", requestcode);
//        intent.putExtra("poses", poses.toString());
//        intent.setAction("ALARM_ACTION");
        // 이거랑 같이 다 넣어줘야 다른 Activity들도 정산 작동함
        //YEAR_MONTH는 알람 설정 했을때의 YEAR_MONTH로 유지 해야되는데...
        //일단 Check_completed도 필요할거 같고(년 월 구분)
        //알람 설정하는거는 당일 이상에만 하면됨
        //YEAR, MONTH 는 YogaActvity에서 Check_Completed에서 사용함
        //근데 이 YEAR, MONTH는 ex) 요가 알람 진행중
        // 2026-04-31 애서 2026-05-01로 넘어갔을때
        //2026-04를 검색하기 위해서 쓰는거고 날짜는 AlamReceicer가 실행되었을 때 계산함
        // 알람 재등록할때는 무조건 미래에 대한 알람만 재등록 하는 거고
        //저건 Receive를 받고 요가알람(YogaActivity의 종료 함수 부분)
        // 을 수행할때 발생하는 문제이기 때문에
        //현 코드에서는 calender.get(Calendar.YEAR, MONTH)하면 될듯
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);


        day_of_week_requestcode = context.getSharedPreferences("day_of_weeks_request_code", Context.MODE_PRIVATE);
        //Map으로 할지 1~7 돌면서 있으면 빼서 requestcode 추출하고 알람 할지

        Map<String, ?> day_of_week_Map = day_of_week_requestcode.getAll();

        Set<String> day_of_week_keys = day_of_week_Map.keySet();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for(String day_key : day_of_week_keys){
            Log.d("day_key", day_key);
            //requestcode 기준으로 알람 하나씩 다시 세팅해야됨
            Object tmp_obj = day_of_week_Map.get(day_key);

            for(String JSONString : (Set<String>) tmp_obj){
                try {
                    JSONObject object = new JSONObject(JSONString);
                    Log.d("requestcode_object", object.keys().toString());
                    Iterator<String> request_keys = object.keys();
                    while (request_keys.hasNext()){
                        String request_code = request_keys.next();
                        JSONObject request_object = (JSONObject) object.get(request_code);
                        Log.d("request_object", request_object.toString());

                        int weekday = Integer.parseInt(day_key);
                        int hour = request_object.getInt("Hour");
                        int minute = request_object.getInt("Minute");
                        int requestcode = Integer.parseInt(request_code);


                        Intent Re_Register_Intent = new Intent(context, AlarmReceiver.class);// AlarmReceiver에 보낼 intent 생성

                        Re_Register_Intent.putExtra("YEAR", YEAR);
                        Re_Register_Intent.putExtra("MONTH", MONTH);
                        Re_Register_Intent.putExtra("weekday",weekday);
                        Re_Register_Intent.putExtra("hour", hour);
                        Re_Register_Intent.putExtra("minute", minute);
                        Re_Register_Intent.putExtra("Request_code",requestcode);
                        Re_Register_Intent.putExtra("NotificationID", requestcode);
                        Re_Register_Intent.putExtra("poses", String.valueOf(request_object.getJSONArray("poses")));
                        Re_Register_Intent.setAction("ALARM_ACTION");

                        calendar.set(Calendar.YEAR, YEAR);
                        calendar.set(Calendar.MONTH, MONTH);
                        calendar.set(Calendar.DAY_OF_WEEK, weekday);
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        Log.d("Re_Register_Intent", Re_Register_Intent.getExtras().toString());

                        if (alarmManager != null){
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(request_code), Re_Register_Intent, PendingIntent.FLAG_IMMUTABLE| PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                        }


                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }


        }

    }
}
