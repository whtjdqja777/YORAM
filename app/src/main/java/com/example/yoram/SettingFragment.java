package com.example.yoram;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    Button mod_btn;
    String themeColor;

    Button alarmsound;
    Button developer;

    Button onepage;

    Button twopage;
    Button threepage;

    RadioButton Light_Theme_button;
    RadioButton Dark_Theme_button;

    String ThemeMod = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeColor = ThemeUtil.modLoad(getActivity().getApplicationContext());
        ThemeUtil.applyTheme(themeColor);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_yoram_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mod_btn = view.findViewById(R.id.theme_button);
        mod_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_moddialog, null); // 여기서 moddialog.xml을 인플레이션합니다.
                Light_Theme_button = dialogView.findViewById(R.id.r_btn_light);
                Light_Theme_button.setOnClickListener(v1 ->{
                    ThemeMod = "light";
                });
                Dark_Theme_button = dialogView.findViewById(R.id.r_btn_dark);
                Dark_Theme_button.setOnClickListener(v1 ->{
                    ThemeMod = "dark";
                });
                builder.setView(dialogView)
                        .setTitle("테마")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // 확인 버튼을 눌렀을 때 수행할 동작
                                ThemeUtil.toggleTheme(getActivity().getApplicationContext(), ThemeMod);
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        alarmsound = view.findViewById(R.id.alarm_sound_button);
        alarmsound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View dialogView = getLayoutInflater().inflate(R.layout.activity_soundselect, null);
                builder.setView(dialogView);

                // ListView 찾기
                ListView listView = dialogView.findViewById(R.id.listView);

                // 노래 파일 리소스 ID 리스트 생성 (여기서 R.raw.song_name은 노래 파일의 리소스 ID입니다)
                ArrayList<Integer> songResources = new ArrayList<>();
                songResources.add(R.raw.dreamers);
                songResources.add(R.raw.blueming);
                songResources.add(R.raw.knaan);

                // 노래 파일 이름 리스트 생성
                ArrayList<String> songList = new ArrayList<>();
                songList.add("노래 1");
                songList.add("노래 2");
                songList.add("노래 3");

                // 어댑터 생성
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, songList);

                // ListView에 어댑터 설정
                listView.setAdapter(adapter);

                // 팝업 다이얼로그 생성 및 보이기
                AlertDialog dialog = builder.create();
                dialog.show();

                // 리스트뷰 아이템 클릭 이벤트 처리
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // 선택된 노래 플레이
                        int selectedSongResource = songResources.get(position);
                        MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(), selectedSongResource);
                        mediaPlayer.start();

                        // 선택된 노래 텍스트로 표시
                        String selectedSong = songList.get(position);
                        Toast.makeText(getActivity(), selectedSong + " 노래가 선택되었습니다", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // 팝업 닫기

                        // 이동을 위한 Intent 생성
                        Intent intent = new Intent(getActivity(), MainActivity.class);

                        // 선택된 노래의 리소스 ID를 Move1Activity로 전달
                        intent.putExtra("selectedSongResource", selectedSongResource);

                        // 다른 화면으로 이동
                        startActivity(intent);
                    }
                });
            }
        });
        developer = view.findViewById(R.id.developer);
        developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("개발자 정보");
                builder.setMessage("여기에 개발자의 이름, 연락처 또는 기타 정보를 입력하세요.");

                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 확인 버튼을 눌렀을 때 수행할 동작
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}