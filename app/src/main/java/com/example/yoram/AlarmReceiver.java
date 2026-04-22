package com.example.yoram;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.startActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "채널 ID를 입력하세요";
    private static final int NOTIFICATION_ID = 1;
    SharedPreferences prefs;
    @Override
    public void onReceive(Context context, Intent intent) {
        prefs = context.getSharedPreferences("next_request_code", Context.MODE_PRIVATE);
        int requestcode = intent.getIntExtra("Request_code", 1000);
        int NotificationID = intent.getIntExtra("NotificationID", -1);
        MediaPlayer player = MediaPlayer.create(context, R.raw.blueming);
        player.start();
        Log.d("리시버", "리스브 완료");
        Toast.makeText(context, "Alarm! Wake up!", Toast.LENGTH_SHORT).show();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, AlarmStartActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, requestcode, notificationIntent,PendingIntent.FLAG_IMMUTABLE);

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
                        .setContentText("일어나세요!!!!!.")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(notificationPendingIntent)
                        .setAutoCancel(true);
                notificationManager.notify(NotificationID, notifyBuilder.build());
            }
        }

        Log.d("리시버","리시브 완료");
        resetAlarm(context, intent);


    }
    private void resetAlarm(Context context, Intent intent){
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        int requestcode = prefs.getInt("next_request_code", 1000);
        prefs.edit().putInt("next_request_code", requestcode+1).apply();

        target.set(Calendar.DAY_OF_WEEK, intent.getIntExtra("weekday", -1));
        target.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("hour", 0));
        target.set(Calendar.MINUTE, intent.getIntExtra("minute", 0));
        target.set(Calendar.MILLISECOND, 0);
        target.set(Calendar.SECOND, 0);

        int nextweekday = (target.get(Calendar.DAY_OF_WEEK) - now.get(Calendar.DAY_OF_WEEK) + 7) % 7;

        if(nextweekday == 0 && target.before(now)){
            nextweekday = 7;
        }
        target.add(Calendar.DAY_OF_MONTH, nextweekday);// 7일을 추가

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()){
            return;
        }
        Intent Alarm_intent = new Intent(context, AlarmReceiver.class);
        Alarm_intent.putExtra("weekday", target.get(Calendar.DAY_OF_WEEK));
        Alarm_intent.putExtra("hour", target.get(Calendar.HOUR_OF_DAY));
        Alarm_intent.putExtra("minute", target.get(Calendar.MINUTE));
        Alarm_intent.putExtra("Request_code", requestcode);
        Alarm_intent.putExtra("NotificationID", requestcode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestcode, Alarm_intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);
        }




    }
}