package com.example.yoram;

import org.json.JSONArray;

import java.util.HashSet;

public class AlarmItem {
    private String Day;
    private String Request_Code;
    private int Hour;
    private int Minute;
    private JSONArray poses;

    public AlarmItem(String Day, String Request_Code, int Hour, int Minute, JSONArray poses){
        this.Day = Day;
        this.Request_Code = Request_Code;
        this.Hour = Hour;
        this.Minute = Minute;
        this.poses = poses;
    }

    public String getDay(){
        return Day;
    }
    public String getRequest_Code(){
        return Request_Code;
    }
    public int getHour(){
        return Hour;
    }
    public int getMinute(){
        return Minute;
    }
    public JSONArray getPoses(){
        return poses;
    }
}
