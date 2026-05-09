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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

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
    SharedPreferences day_compledted;
    SharedPreferences ALARM_History;
    Decorator REDdecorator;
    Decorator ORANGEdecorator;
    Decorator GREENdecorator;
    Collection<CalendarDay> dates;

    TextView explain_tv1;
    TextView explain_tv2;
    TextView progress_tv2;

    Calendar calendar;
    String YEAR_MONTH;
    TextView trueday_count;
    JSONObject Alarm_time_obj;
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


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
//        decorator = new Decorator()

        calendar = Calendar.getInstance();
        ALARM_History = getContext().getSharedPreferences("ALARM_History", MODE_PRIVATE);
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

        super.onViewCreated(view, savedInstanceState);

        String currentTheme = ThemeUtil.modLoad(requireContext());
        explain_tv1 = view.findViewById(R.id.explain_tv1);
        explain_tv2 = view.findViewById(R.id.explain_tv2);
        progress_tv2 = view.findViewById(R.id.progress_tv2);
        calendarView = view.findViewById(R.id.calendarView);
        calendarView.setDateTextAppearance(android.R.style.TextAppearance_DeviceDefault_Small);
        if (currentTheme.equals(ThemeUtil.DARK_MODE)){
            explain_tv1.setTextColor(Color.WHITE);
            explain_tv2.setTextColor(Color.WHITE);

//            calendarView.setDateTextAppearance(R.style.CalendarDateDark);
        }else{
            explain_tv1.setTextColor(Color.BLACK);
            explain_tv2.setTextColor(Color.BLACK);

//            calendarView.setDateTextAppearance(R.style.CalendarDateLight);
        }




        day_of_week_prefs = getActivity().getSharedPreferences("day_of_weeks_request_code", MODE_PRIVATE);

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

        Days_Pop_up();
    }
    // 여기서 Set빨강, Set주황, Set초록 마다 날짜 분리해서 저장하고
    //
    private void setting_dates(String YEAR_MONTH) throws JSONException {

        String[] year_month_split = YEAR_MONTH.split("_");
        String YEAR = year_month_split[0];
        String MONTH = year_month_split[1];
        Calendar calendar1 = Calendar.getInstance();
        boolean thismonth = false;
        if (calendar1.get(Calendar.MONTH) ==  Integer.parseInt(MONTH)){
            thismonth = true;
        }
//        REDdecorator = new Decorator(); -> 요가 알람 하나도 완료 안된 날짜들 넣기
//        ORANGEdecorator = new Decorator();-> 요가알람이 일정부분 진행된 날짜만 넣기
//        GREENdecorator = new Decorator(); -> 요가알람이 모두 완료된 날짜만 넣기

        Collection<CalendarDay> REDdates = new HashSet<>();
        Collection<CalendarDay> ORANGEdates = new HashSet<>();
        Collection<CalendarDay> GREENdates = new HashSet<>();
        int GREENCount = 0;
        int thismonth_alarm_count = 0;
        int totaltrue_count = 0;
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
                    if (thismonth){
                        thismonth_alarm_count+=1;
                    }
                    String requestcode_key = requescode_keys.next();
                    if (checkobject.getBoolean(requestcode_key)) {
                        truecount+=1;
                        if (thismonth) {
                            totaltrue_count += 1;
                        }
                    }
                }

                if (truecount >= day_Alarm_count) {
                    GREENdates.add(CalendarDay.from(Integer.parseInt(YEAR), Integer.parseInt(MONTH) + 1, Integer.parseInt(daykey)));
                    Log.d("GREENdates", String.valueOf(GREENdates));
                    if (thismonth){
                        GREENCount+=1;
                    }

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
        trueday_count = (TextView) getActivity().findViewById(R.id.continuous_day);
        trueday_count.setText(String.valueOf(GREENCount));
        ProgressBar pb = getActivity().findViewById(R.id.progress_bar);
        TextView progressTextview = getActivity().findViewById(R.id.progress_tv);
        int progress = (thismonth_alarm_count == 0) ? 0: (totaltrue_count*100) / thismonth_alarm_count;

        progressTextview.setText(" 진행도: " + progress + "% ");
        progress_tv2.setText(thismonth_alarm_count + "개 중" +totaltrue_count + "개 수행 했습니다!");
        pb.setProgress(progress);
    }
    private void Days_Pop_up(){
//        day_of_week_prefs
//        day_compledted
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            
            //popUP을 생성할 때 day_of_week_requestcode를 참조하는데 해당 과거 날짜에 들어가 있는 알람을 삭제하면
            //Check_completed에 있는 requestcode로 day_of_week_requestcode를 검색하는데 없어져 있으니 오류 뜸
            //이게 삽입 삭제 할때 현재 이상의 날짜에만 삽입 삭제 하기 때문에 과거 정보는 안건들이는데
            //켈린더에서 과거 정보 보려고 클릭을 하면 시간 정보를 가져오기 위해 day_of_week_requestcode를 참조함
            //근데 day_of_week_requestcode서 해당 과거 날짜의 요일 알람이 삭제 되어 있으면 
            //Check_completed에는 있고 day_of_week_requestcode에는 없는 requestcode로 찾으려 하기 떄문에 오류가 발생
            //과거 정보를 관리하는 prefs 따로 하나 만들던가 해야할 듯(과거 정보는 절대 바뀌면 안되기 때문에)
            //requstcode: 시간, 분, true/false로
           
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                //일단 알람 완료 갯수 가져오기
                //이후에 알람별 완료 여부 표시하기
                String YEAR_MONTH = date.getYear() + "_" + (date.getMonth()-1);
                Log.d("Sellected_YEAR_MONTH: ", YEAR_MONTH);
                Log.d("day_compledted_keys", String.valueOf(day_compledted.getAll().keySet()));
                String Alarm_info ="";
                int True_Count = 0;
                int Alarm_count = 0;
                Log.d("day_of_week_requestcode", String.valueOf(day_of_week_prefs.getAll()));
                Log.d("day_compledted", String.valueOf(day_compledted.getAll()));
                Log.d("ALARM_History_row", String.valueOf(ALARM_History.getAll()));
                //여기에 삭제된 알람 들어 있는거 확인

                if(day_compledted.contains(YEAR_MONTH)){
                    Set<String> Alarm_days = day_compledted.getStringSet(YEAR_MONTH, new HashSet<>());
                    Log.d("Alarm_days", String.valueOf(Alarm_days));


                    for (String JSONString : Alarm_days){
                        try {
                            JSONObject Alarm_days_Object = new JSONObject(JSONString);
                            if (Alarm_days_Object.has(String.valueOf(date.getDay())) ){
                                JSONObject day_Object = (JSONObject) Alarm_days_Object.get(String.valueOf(date.getDay()));
                                Iterator<String> request_code_keys = day_Object.keys();
                                //Check_completed에 있는 requestcode로 history도 검색 가능
                                JSONObject day_Alarms_obj = get_Alarm_info(date);//
                                // 알람 시간 정보를 day_of_week_request하고 History 2곳에서 다 가져와야됌
                                if(day_Alarms_obj == null){
                                    Log.e("day_Alarms_obj null", "day_Alarms_obj null입니다.");
                                    day_Alarms_obj = new JSONObject();

                                }else{

                                }


                                while(request_code_keys.hasNext()){
                                    String requestcode_key = request_code_keys.next();
                                    // //
                                    //에초에 날짜선택 했을때 해당 날짜의 요일에 해당 하는 day_of_week_reqeustcode
                                    //따로 빼서 여기서 쓰는게 편할 듯

//                                    Log.d("")

                                    //이거 삭제된 알람 정보에 대해서는 검색할 수가 없기 때문에 No Value 오류뜸
                                    Log.d("ALARM_Historys: ", String.valueOf(ALARM_History.getAll()));
                                    if(day_Alarms_obj.has(requestcode_key)){
                                        Alarm_time_obj = (JSONObject) day_Alarms_obj.get(requestcode_key);
                                        Log.d("ALARM_History", "exist in day_of_week_request_code: "+ALARM_History.getString(requestcode_key,""));
                                        Alarm_count ++;
                                    }else if(ALARM_History.contains(requestcode_key)){

                                        Alarm_time_obj = new JSONObject(ALARM_History.getString(requestcode_key,""));
                                        Log.d("ALARM_History", "exis int ALARM_History: "+ALARM_History.getString(requestcode_key,""));
                                        //오늘이 안끝났는데 delete해 버리면 오늘은 아직 알람 삭제가 가능한 상태라
                                        //day_of_week_request_code 뿐만 아니라 Check_completed의 기록도 지워져서
                                        //삭제 했을때 날짜 눌러도 오늘 날짜에는 팝업 창 안뜸
                                        Alarm_count ++;
                                    }else{
                                        Log.d("Non exist", "알람 목록, 히스토리 두 곳다 없음");
                                        continue;
                                    }

                                    Log.d("Alarm_time_obj: ", String.valueOf(Alarm_time_obj));
                                    if (day_Object.get(requestcode_key).equals(true)){
                                        True_Count++;
                                        Alarm_info += Alarm_time_obj.optString("Hour") + "시" + Alarm_time_obj.optString("Minute") + "분" + " 알람" + "  V\n";
                                    }else{
                                        Alarm_info += Alarm_time_obj.optString("Hour") + "시" + Alarm_time_obj.optString("Minute") + "분" + " 알람" + "  X\n";
                                    }
                                }
                                Toast.makeText(getContext(), "진행도: " + True_Count + "/" + Alarm_count, Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), "해당 날짜에는 알람이 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Log.d("Alarm_info", Alarm_info);
                    Log.d("Alarm_count", String.valueOf(Alarm_count));
                    if (!Alarm_info.isEmpty() && Alarm_count !=0){
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                        builder.setTitle(date.getYear() + "년 " + date.getMonth() + "월 " + date.getDay() + "일 " + "알람 정보");
                        builder.setMessage(Alarm_info + " \n 진행도: " + True_Count + "/" + Alarm_count);
                        builder.setNegativeButton("닫기", null);
                        builder.show();

                    }

                }else{
                    Toast.makeText(getContext(), "해당 달에는 알람이 없습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private JSONObject get_Alarm_info(CalendarDay date) throws JSONException {
        Calendar calendar1 = Calendar.getInstance();

        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();

        calendar1.set(Calendar.YEAR, year);
        calendar1.set(Calendar.MONTH, month - 1);// Calendar의 Calendar.MONTH는 범위가 0~11 이고 CalendarDayd의 month는 1~12라 -1 해준거
        calendar1.set(Calendar.DAY_OF_MONTH, day);

        int day_of_week = calendar1.get(Calendar.DAY_OF_WEEK);

        Set<String> Date_Alarms_Set = day_of_week_prefs.getStringSet(String.valueOf(day_of_week), new HashSet<>());
        Log.d("Date_Alarms_Set", String.valueOf(Date_Alarms_Set));
        //day_of_week 이걸로 day_of_week_requestcode 검색
        //Check_completed의 request_code를 기반으로 해당 알람 설정 시간 분 알아내서
        //return
        //pop_up함수만들어서 해당 requestcode의 알람의 
        //true/false 여부와 알람 시간 표시해주기
        JSONObject requestcode_obj = null;
        if (Date_Alarms_Set.size() == 0){

            Log.d("해당 요일에 알람 없음", "해당 요일에 알람 없음");
            return null;
        }
        JSONObject new_Object = new JSONObject();

        for (String JSONString : Date_Alarms_Set){
            requestcode_obj = new JSONObject(JSONString);
            Iterator<String> keys = requestcode_obj.keys();
            while(keys.hasNext()){
                String key = keys.next();
                Log.d("requestcode_obj.keys()", key);
                new_Object.put(key, requestcode_obj.get(key));
            }
        }
        Log.d("new_Object", String.valueOf(new_Object));

        if(new_Object != null && new_Object.length() != 0){
            return new_Object;
        }else{
            Log.d("requestcode_obj_Error", "requestcode_obj가 초기화 되지 않았거나 내용물이 없습니다.");
            return null;
        }

    }
}