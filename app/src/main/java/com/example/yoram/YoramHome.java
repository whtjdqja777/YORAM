package com.example.yoram;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class YoramHome extends AppCompatActivity {
    TimePicker timePicker;
    Button buttonSetyoga;
    Map<Button, Boolean> dayButtonStates = new HashMap<>();
    Map<String, Calendar> dayTimeMap = new HashMap<>();
    String offColor = "#FF4469";
    String onColor = "#A2D5F2";
    String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    Button setAlarmbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_yoram_home);
        buttonSetyoga = (Button)findViewById(R.id.buttonSetyoga);
        timePicker = findViewById(R.id.timePicker);

        Calendar now = Calendar.getInstance();
        Log.d("HOME", "HOME");
        timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(now.get(Calendar.MINUTE));

        setAlarmbtn = findViewById(R.id.setAlarmbtn);

        setAlarmbtn.setOnClickListener(v -> {
            // 일단 시간 정보 알람 설정




        });
        buttonSetyoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisible(false);
            }
        });

        // 모든 요일 버튼을 맵에 추가하고 초기 상태를 false로 설정
        int[] buttonIds = {R.id.Mon, R.id.Tue, R.id.Wed, R.id.Thu, R.id.Fri, R.id.Sat, R.id.Sun};
        for (int id : buttonIds) {
            Button dayButton = findViewById(id);
//            dayButton.setOnClickListener(dayButtonClickListener);
//            dayButtonStates.put(dayButton, false);
        }

        // 초기에는 모든 요일에 대해 시간을 null로 설정
//        for (String day : days) {
//            dayTimeMap.put(day, null);
//        }
    }



    // 공통 클릭 리스너
    private View.OnClickListener dayButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button dayButton = (Button) v;
            String day = dayButton.getText().toString();
            boolean isOn = !dayButtonStates.get(dayButton);
            dayButtonStates.put(dayButton, isOn);

            dayButton.setBackground(new ColorDrawable(isOn ? Color.parseColor(onColor) : Color.parseColor(offColor)));
//            timePicker.setEnabled(isOn);

            if (isOn) {
                Calendar calendar = dayTimeMap.get(day);
                if (calendar != null) {
                    // 이전에 설정된 시간이 있다면 TimePicker에 반영
                    timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
                    timePicker.setMinute(calendar.get(Calendar.MINUTE));
                } else {
                    // 시간이 설정되지 않았다면 현재 시간으로 설정
                    Calendar now = Calendar.getInstance();
                    timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
                    timePicker.setMinute(now.get(Calendar.MINUTE));
                }
                timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
                    // TimePicker에서 시간이 변경될 때마다 이를 저장
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    dayTimeMap.put(day, selectedTime);
                });
            }
        }
    };
}
