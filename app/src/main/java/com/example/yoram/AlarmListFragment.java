package com.example.yoram;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<AlarmItem> alarmList;
    private AlarmAdapter adapter;
    private RecyclerView recyclerView;

    private SharedPreferences day_of_weeks_request_code;

    public AlarmListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlarmList.
     */
    // TODO: Rename and change types and number of parameters
    public static AlarmListFragment newInstance(String param1, String param2) {
        AlarmListFragment fragment = new AlarmListFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm_list, container, false);
        day_of_weeks_request_code = getContext().getSharedPreferences("day_of_weeks_request_code", MODE_PRIVATE);

        recyclerView = view.findViewById(R.id.alarmRecyclerView);
        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmList, alarmItem -> {
            // 알람 삭제 로직 구현

        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        try {
            loadAlarmList();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void loadAlarmList() throws JSONException {
        alarmList.clear();
        Map<String, ?> getAll = day_of_weeks_request_code.getAll();
        for(String Day_key : getAll.keySet()){

            Set<String> dayset = day_of_weeks_request_code.getStringSet(Day_key, new HashSet<>());
            for (String day : dayset){
                JSONObject object = new JSONObject(day);
                Iterator<String> key = object.keys();
                while (key.hasNext()){
                    String requestCode = key.next();
                    HashSet<String> pose = new HashSet<>((Collection) object.getJSONObject("poses"));

                    alarmList.add(new AlarmItem(Day_key,requestCode, object.getInt("Hour"),object.getInt("Minute"), pose));

                }

            }

        }
        adapter.notifyDataSetChanged();
    }

    private void deleteAlarm(AlarmItem alarmItem){

    }
}