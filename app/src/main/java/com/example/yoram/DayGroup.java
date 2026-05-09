package com.example.yoram;

import java.util.ArrayList;

public class DayGroup {
    private String day;
    private ArrayList<AlarmItem> alarms;

    public DayGroup(String day, ArrayList<AlarmItem> alarms) {
        this.day = day;
        this.alarms = alarms;
    }

    public String getDay() {
        return day;
    }

    public ArrayList<AlarmItem> getAlarms() {
        return alarms;
    }
}
