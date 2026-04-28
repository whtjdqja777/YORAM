package com.example.yoram;

import android.os.Parcel;

import androidx.annotation.NonNull;

//import com.google.android.material.datepicker.DayViewDecorator;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

public class Decorator implements DayViewDecorator {
    private int color;
    private final HashSet<CalendarDay> dates;

    public Decorator(int color, Collection<CalendarDay> dates){
        this.color = color;
        this.dates = new HashSet<>(dates);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, color));
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {

        return dates.contains(day);
    }
}
