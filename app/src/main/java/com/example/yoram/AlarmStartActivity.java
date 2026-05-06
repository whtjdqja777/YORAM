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
        Intent intent = getIntent();
//        reset_Fail_Alarm(intent);

        start_yoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset_Fail_Alarm(intent);

                Intent To_Yoga_intent = new Intent(AlarmStartActivity.this, YogaActivity.class);
                To_Yoga_intent.putExtras(intent);
                startActivity(To_Yoga_intent);
                finish();
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
//        reset_Fail_Alarm(intent);// 클릭 안해도 다음주 실패 알람은 설정해야 됨
    }

    private void reset_Fail_Alarm(Intent intent){
        // 해당 엑티비티에서 시작하기 클릭시 실패알람을 실행하지않고 다음주 실패알람으로 덮어 씌우는 함수
        // 그냥 알람, 실패 알람 따로 관리하고 있음
        // 시작하기 안누르면 기존의 실패 알람 실행되고 Receiver에 있는 resetAlram로직이 다음주로 알람 설정함
        int requestcode = intent.getIntExtra("Request_code", 1000);
        Log.d("Alarm_Start_intent", String.valueOf(intent.getExtras()));
        Calendar resetFail_Calendar = Calendar.getInstance();
        //여기는 YEAR, MONTH 추가하면 안됌 꼬임
        resetFail_Calendar.set(Calendar.DAY_OF_WEEK, intent.getIntExtra("weekday", -1));
        resetFail_Calendar.set(Calendar.HOUR_OF_DAY, intent.getIntExtra("hour", 0));
        resetFail_Calendar.set(Calendar.MINUTE, intent.getIntExtra("minute", 0));//1분 추가 된거 받음
        resetFail_Calendar.set(Calendar.MILLISECOND, 0);
        resetFail_Calendar.set(Calendar.SECOND, 0);
        resetFail_Calendar.add(Calendar.MINUTE, 1);
        // 이게 말이 안되네 위에 받은건 1분 뒤 알람 정보인데 지금이랑 비교하면 당연히 false 나오지

        resetFail_Calendar.add(Calendar.DAY_OF_MONTH, 7);



        Intent reset_Fail_intent = new Intent(this, FailReceiver.class);
        reset_Fail_intent.putExtras(intent);
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