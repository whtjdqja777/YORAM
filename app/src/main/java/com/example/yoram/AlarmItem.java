package com.example.yoram;

import java.util.HashSet;

public class AlarmItem {
    private String Day;
    private String Request_Code;
    private int Hour;
    private int Minute;
    private HashSet<String> poses;

    public AlarmItem(String Day, String Request_Code, int Hour, int Minute, HashSet<String> poses){
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
    public HashSet<String> getPoses(){
        return poses;
    }
}
