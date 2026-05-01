package com.example.yoram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class YogaEndActivity extends AppCompatActivity {
    Button off_yoga;

    private void set_continuous_day() {
        SharedPreferences pref = YogaEndActivity.this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int c_day = pref.getInt("continuous_day", 0);
        String recent_yoga_day = pref.getString("recent_yoga_day", "");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Corrected date format
        LocalDate recent_date;
        if (!recent_yoga_day.equals("")) {
            recent_date = LocalDate.parse(recent_yoga_day, formatter);
        } else {
            // Handle the case when recent_yoga_day is empty
            // Example: Initialize recent_date to a default value or current date
            recent_date = LocalDate.now();
        }
        LocalDate current_date = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(recent_date, current_date);
        if (daysBetween == 1) {
            editor.putInt("continuous_day", c_day + 1);
        } else {
            editor.putInt("continuous_day", 0);
        }

        editor.putString("recent_yoga_day", current_date.format(formatter));
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_end);
        off_yoga = (Button) findViewById(R.id.off_yoga);
        off_yoga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_continuous_day();
                Intent intent = new Intent(YogaEndActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
//                finishAffinity();
            }
        });


    }
}