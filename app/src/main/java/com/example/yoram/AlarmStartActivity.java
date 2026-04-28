package com.example.yoram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;

public class AlarmStartActivity extends AppCompatActivity {
    Button start_yoga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        start_yoga = findViewById(R.id.start_yoga);



        start_yoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset_Fail_Alarm(getIntent());
                Intent intent = new Intent(AlarmStartActivity.this, YogaActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        reset_Fail_Alarm(intent);
    }

    private void reset_Fail_Alarm(Intent intent){
        int requestcode = intent.getIntExtra("Request_code", 1000);
        Log.d("Alarm_Start_intent", intent.getExtras().toString());
        Calendar resetFail_Calendar = Calendar.getInstance();

        resetFail_Calendar.set(Calendar.DAY_OF_WEEK, intent.getIntExtra("weekday", -1));
        resetFail_Calendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("hour", 0));
        resetFail_Calendar.set(Calendar.MINUTE, intent.getIntExtra("minute", 0));//1분 추가 된거 받음
        resetFail_Calendar.set(Calendar.MILLISECOND, 0);
        resetFail_Calendar.set(Calendar.SECOND, 0);

        // 이게 말이 안되네 위에 받은건 1분 뒤 알람 정보인데 지금이랑 비교하면 당연히 false 나오지

        resetFail_Calendar.add(Calendar.DAY_OF_MONTH, 7);



        Intent reset_Fail_intent = new Intent(this, FailReceiver.class);
        reset_Fail_intent.putExtra("weekday", intent.getIntExtra("weekday", -1));
        reset_Fail_intent.putExtra("hour", intent.getIntExtra("hour", 0));
        reset_Fail_intent.putExtra("minute", intent.getIntExtra("minute", 0));
        reset_Fail_intent.putExtra("Request_code", requestcode);
        reset_Fail_intent.putExtra("NotificationID", requestcode);
        reset_Fail_intent.putExtra("poses", intent.getStringExtra("poses"));
        reset_Fail_intent.setAction("FAIL_ACTION");
        Log.d("reset_Fail_intent", reset_Fail_intent.getExtras().toString());
        PendingIntent reset_fail_pendingIntent = PendingIntent.getBroadcast(this, requestcode, reset_Fail_intent, PendingIntent.FLAG_IMMUTABLE| PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("Fail_Request_code", String.valueOf(requestcode));

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {// 알람을 취소하는게 아니라 덮어 씌기
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, resetFail_Calendar.getTimeInMillis(), reset_fail_pendingIntent);
            Log.d("resetting_Fail_Alarm", resetFail_Calendar.get(Calendar.DAY_OF_WEEK) + " " + resetFail_Calendar.get(Calendar.HOUR_OF_DAY) + " " + resetFail_Calendar.get(Calendar.MINUTE));
            Log.d("resetting_Fail_Alarm", "success");
        }
    }
}