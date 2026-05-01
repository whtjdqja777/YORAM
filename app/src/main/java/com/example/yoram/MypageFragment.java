package com.example.yoram;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.security.Key;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MypageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MypageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    MaterialCalendarView calendarView;
    SharedPreferences day_of_week_prefs;
    Decorator REDdecorator;
    Decorator ORANGEdecorator;
    Decorator GREENdecorator;
    Collection<CalendarDay> dates;

    SharedPreferences day_compledted;
    Calendar calendar;
    String YEAR_MONTH;
    public MypageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MypageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MypageFragment newInstance(String param1, String param2) {
        MypageFragment fragment = new MypageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView continuous_day;

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
//        decorator = new Decorator()

        calendar = Calendar.getInstance();
        day_compledted = getContext().getSharedPreferences("Check_completed", MODE_PRIVATE);
//        YEAR_MONTH = String.valueOf(calendar.get(Calendar.YEAR)) + "_" + String.valueOf(calendar.get(Calendar.MONTH));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_yoram_mypage, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView continuous_day;
        super.onViewCreated(view, savedInstanceState);
        continuous_day = (TextView) getActivity().findViewById(R.id.continuous_day);
        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
        int c_day = pref.getInt("continuous_day", 0);
        continuous_day.setText(String.valueOf(c_day));


        day_of_week_prefs = getActivity().getSharedPreferences("day_of_weeks_request_code", MODE_PRIVATE);
        calendarView = view.findViewById(R.id.calendarView);
        try {
            if(calendarView != null)
                for(String YEAR_MONTH : day_compledted.getAll().keySet()){
                    if(!day_compledted.getStringSet(YEAR_MONTH, new HashSet<>()).isEmpty()){
                        setting_dates(YEAR_MONTH);
                    }

                }

            else Log.d("calendarView", "null");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        resetting_day_of_week_Alarm_completed_count();


//            calendarView.setSelectedDate(CalendarDay.today());
    }
    // 여기서 Set빨강, Set주황, Set초록 마다 날짜 분리해서 저장하고
    //
    private void setting_dates(String YEAR_MONTH) throws JSONException {

        String[] year_month_split = YEAR_MONTH.split("_");
        String YEAR = year_month_split[0];
        String MONTH = year_month_split[1];
//        REDdecorator = new Decorator(); -> 요가 알람 하나도 완료 안된 날짜들 넣기
//        ORANGEdecorator = new Decorator();-> 요가알람이 일정부분 진행된 날짜만 넣기
//        GREENdecorator = new Decorator(); -> 요가알람이 모두 완료된 날짜만 넣기

        Collection<CalendarDay> REDdates = new HashSet<>();
        Collection<CalendarDay> ORANGEdates = new HashSet<>();
        Collection<CalendarDay> GREENdates = new HashSet<>();
//        calendarView.invalidateDecorators(); -> 이거
        //여기서 빨간색 날짜, 주황색 날짜, 초록색 날짜 분리해서 저장
        Set<String> Check_days = day_compledted.getStringSet(YEAR_MONTH, new HashSet<>());
        Log.d("Check_days", String.valueOf(Check_days));
        if (Check_days.isEmpty()){
            return;
        }
        for (String JSONString : Check_days) {
            JSONObject object = new JSONObject(JSONString);
            Iterator<String> Keys = object.keys();
            while (Keys.hasNext()) {
                String daykey = Keys.next();
                Log.d("daykey", daykey);
                JSONObject checkobject = (JSONObject) object.get(daykey);
                Iterator<String> requescode_keys = checkobject.keys();

                int truecount = 0;

                int day_Alarm_count = checkobject.length();

                while (requescode_keys.hasNext()) {
                    String requestcode_key = requescode_keys.next();
                    if (checkobject.getBoolean(requestcode_key)) {
                        truecount+=1;


                    }
                }



                if (truecount >= day_Alarm_count) {
                    GREENdates.add(CalendarDay.from(Integer.parseInt(YEAR), Integer.parseInt(MONTH) + 1, Integer.parseInt(daykey)));
                    Log.d("GREENdates", String.valueOf(GREENdates));
                } else if (0 < truecount && truecount < day_Alarm_count) {
                    ORANGEdates.add(CalendarDay.from(Integer.parseInt(YEAR), Integer.parseInt(MONTH) + 1, Integer.parseInt(daykey)));
                    Log.d("ORANGEdates", String.valueOf(ORANGEdates));
                } else if (truecount == 0) {
                    REDdates.add(CalendarDay.from(Integer.parseInt(YEAR), Integer.parseInt(MONTH) + 1, Integer.parseInt(daykey)));
                    Log.d("REDdates", String.valueOf(REDdates));
                } else {
                    Log.e("오류", "오류");
                }

            }
        }
        if (!GREENdates.isEmpty()){
            calendarView.addDecorator(new Decorator(Color.GREEN, GREENdates));
        }
        if (!ORANGEdates.isEmpty()){
            calendarView.addDecorator(new Decorator(Color.YELLOW, ORANGEdates));
        }
        if (!REDdates.isEmpty()){
            calendarView.addDecorator(new Decorator(Color.RED, REDdates));
        }


    }
}