package com.example.yoram;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.w3c.dom.Text;

import java.util.HashSet;

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
    SharedPreferences day_of_week;
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


        day_of_week = getActivity().getSharedPreferences("day_of_weeks_request_code", MODE_PRIVATE);
        for (int i = 1; i < 8; i++){
            Log.d("day_of_week", String.valueOf(day_of_week.getStringSet(String.valueOf(i), new HashSet<>()).size()));
            //요가 완료시 해당 요일의 count+=1 저장 형태는 23(날짜): count
            //날짜는 요가 시작시 prefs에 23(날짜): 0으로 저장
            //처음 부터 세팅을 1 ~ 31(이 중 알람이 저장된 요일만) : prefs.getStringset(요일).size() 으로 할지 아니면 요가 시작시 해당 날짜: size()으로 할지
            //1번째 방법은 초기에 다 저장된 요일의 날짜가 빨간색으로 세팅되고
            //2번째 방법은 요가를 시작해야 색깔이 세팅됨
            //1번째 방법으로 ㄱㄱ
            //그럼 설정된 알람 요일들 가져오고 해당 달의 해당 요일이 몇일이 있는지를 가져와서 요일 별로 저장
            //저장된 요일 날짜에 23: 4(설정된 알람수, prefs.getStringSet(요일).size()) 부여하고
            // 23: 4 이면 빨간색 23: 0이 아니고 4보다 작으면 주황색 23: 0 이면 초록색으로 세팅
        }

        calendarView = view.findViewById(R.id.calendarView);
//            calendarView.setSelectedDate(CalendarDay.today());
    }
}