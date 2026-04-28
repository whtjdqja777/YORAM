package com.example.yoram;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.startActivity;

import static java.security.AccessController.getContext;

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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "1000";
    private static final int NOTIFICATION_ID = 1;
    SharedPreferences prefs_set_poses;
    @Override
    public void onReceive(Context context, Intent intent) {
        prefs_set_poses = context.getSharedPreferences("yoga", MODE_PRIVATE);

        try {
            HashSet<String> Alarmpose = new HashSet<>();
            prefs_set_poses.edit().remove("pose").apply();
            JSONArray poses = new JSONArray(intent.getStringExtra("poses"));
            Log.d("recevierPoses", String.valueOf(poses));
            if (poses != null && poses.length() != 0) {
                Log.d("recevierPoses", String.valueOf(poses));
                for (int i = 0; i < poses.length(); i++) {
                    Alarmpose.add(poses.getString(i));

            }
                prefs_set_poses.edit().putStringSet("pose", Alarmpose).apply();
                Log.d("prefs_set_poses_from_receiver", String.valueOf(prefs_set_poses.getStringSet("pose", new HashSet<>())));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        int requestcode = intent.getIntExtra("Request_code", 1000);
        int NotificationID = intent.getIntExtra("NotificationID", -1);
        MediaPlayer player = MediaPlayer.create(context, R.raw.blueming);
        player.start();
        Log.d("리시버", "리스브 완료");
        Toast.makeText(context, "Alarm! Wake up!", Toast.LENGTH_SHORT).show();

        Calendar target = Calendar.getInstance();
        target.set(Calendar.DAY_OF_WEEK, intent.getIntExtra("weekday", -1));
        target.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("hour", 0));
        target.set(Calendar.MINUTE, intent.getIntExtra("minute", 0));
        target.set(Calendar.MILLISECOND, 0);
        target.set(Calendar.SECOND, 0);


        Calendar Fail_target = (Calendar) target.clone();
        Fail_target.add(Calendar.MINUTE, 1);

        Intent notificationIntent = new Intent(context, AlarmStartActivity.class);
        notificationIntent.putExtra("Request_code", requestcode);

        notificationIntent.putExtra("weekday", Fail_target.get(Calendar.DAY_OF_WEEK));
        notificationIntent.putExtra("hour", Fail_target.get(Calendar.HOUR_OF_DAY));
        notificationIntent.putExtra("minute", Fail_target.get(Calendar.MINUTE));
        notificationIntent.putExtra("Request_code", requestcode);
        notificationIntent.putExtra("NotificationID", requestcode);
        notificationIntent.putExtra("poses", intent.getStringExtra("poses"));
        notificationIntent.setAction("FAIL_ACTION");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


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
                        .setContentText("일어나세요!!!!!." + requestcode + ":" + intent.getIntExtra("hour", 0) + intent.getIntExtra("minute", 0))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(notificationPendingIntent)
                        .setAutoCancel(true);
                notificationManager.notify(NotificationID, notifyBuilder.build());
            }
        }

        Log.d("리시버","리시브 완료");
        resetAlarm(context, intent, requestcode, target);


    }
    private void resetAlarm(Context context, Intent intent, int target_requestcode, Calendar target){
        Calendar now = Calendar.getInstance();

        int requestcode = target_requestcode;

        if (target.before(Calendar.getInstance())) {
            target.add(Calendar.DAY_OF_MONTH, 7);
        }



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
        Alarm_intent.putExtra("poses", intent.getStringExtra("poses"));
        Alarm_intent.setAction("ALARM_ACTION");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestcode, Alarm_intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);





        if (alarmManager != null){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.getTimeInMillis(), pendingIntent);

        }
        Log.d("resetting_Alarm", "success");
        Log.d("target", String.valueOf(target.get(Calendar.DAY_OF_MONTH)) + " " + String.valueOf(target.get(Calendar.DAY_OF_WEEK))
         + " " + String.valueOf(target.get(Calendar.HOUR_OF_DAY)) + " " + String.valueOf(target.get(Calendar.MINUTE)));


        //리시브 받은 requescode를 재사용해서 기존 알람 requescode 제거 하고
        // resetAlarm에서 다시 알람하는데 사용해야됨 -> 이러면 prefs를 건드릴 필요 없음(삭제할 때도 그냥 해당 request_code로 제거하면됨)

    }
}