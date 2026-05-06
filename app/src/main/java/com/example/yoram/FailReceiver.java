package com.example.yoram;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class FailReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "1000";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            Log.d("get_Fail_intent", intent.getExtras().toString());
        }else{
            Log.d("FailReceiver", "intent가 null입니다.");

            return;
        }

        int requestcode = intent.getIntExtra("Request_code", 1000);
        int NotificationID = intent.getIntExtra("NotificationID", -1);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, requestcode, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                // Notification Channel 생성
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Test Notification", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);
                notificationChannel.setDescription("Notification from Mascot");
                notificationManager.createNotificationChannel(notificationChannel);

                // Notification을 만들어 NotificationManager를 통해 표시
                NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle("Yoram")
                        .setContentText("금일" + requestcode + ":" + intent.getIntExtra("hour", 0) +"시 " + intent.getIntExtra("minute", 0) + "분"
                        + " 알람: 일정 시간이 지나 요가 수행 실패로 기록됩니다.")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(notificationPendingIntent)
                        .setAutoCancel(true);
                notificationManager.notify(NotificationID, notifyBuilder.build());


            }
        }
        Intent go_back_to_Main_intent = new Intent(context, MainActivity.class);
        go_back_to_Main_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(go_back_to_Main_intent);

        reset_Fail_Alarm(intent, context);
    }

    private void reset_Fail_Alarm(Intent intent, Context context){
        // 해당 엑티비티에서 시작하기 클릭시 실패알람을 실행하지않고 다음주 실패알람으로 덮어 씌우는 함수
        // 그냥 알람, 실패 알람 따로 관리하고 있음
        // 시작하기 안누르면 기존의 실패 알람 실행되고 Receiver에 있는 resetAlram로직이 다음주로 알람 설정함
        int requestcode = intent.getIntExtra("Request_code", 1000);
        Log.d("ResetFailAlarm", String.valueOf(intent.getExtras()));
        Calendar resetFail_Calendar = Calendar.getInstance();
        //여기는 YEAR, MONTH 추가하면 안됌 꼬임
        resetFail_Calendar.set(Calendar.DAY_OF_WEEK, intent.getIntExtra("weekday", -1));
        resetFail_Calendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("hour", 0));
        resetFail_Calendar.set(Calendar.MINUTE, intent.getIntExtra("minute", 0));//1분 추가 된거 받음
        resetFail_Calendar.set(Calendar.MILLISECOND, 0);
        resetFail_Calendar.set(Calendar.SECOND, 0);
        // 이게 말이 안되네 위에 받은건 1분 뒤 알람 정보인데 지금이랑 비교하면 당연히 false 나오지
        resetFail_Calendar.add(Calendar.DAY_OF_MONTH, 7);



        Intent reset_Fail_intent = new Intent(context, FailReceiver.class);
        reset_Fail_intent.putExtras(intent);
        reset_Fail_intent.setAction("FAIL_ACTION");
        Log.d("reset_Fail_intent", reset_Fail_intent.getExtras().toString());
        PendingIntent reset_fail_pendingIntent = PendingIntent.getBroadcast(context, requestcode, reset_Fail_intent, PendingIntent.FLAG_IMMUTABLE| PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("Fail_Request_code", String.valueOf(requestcode));

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {// 알람을 취소하는게 아니라 덮어 씌기
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, resetFail_Calendar.getTimeInMillis(), reset_fail_pendingIntent);
            Log.d("resetting_Fail_Alarm", resetFail_Calendar.get(Calendar.DAY_OF_WEEK) + " " + resetFail_Calendar.get(Calendar.HOUR_OF_DAY) + " " + resetFail_Calendar.get(Calendar.MINUTE));
            Log.d("resetting_Fail_Alarm", "success");
        }

    }

}