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

public class FailReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "1000";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("get_Fail_intent", intent.getExtras().toString());
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
        context.startActivity(intent);
    }

}