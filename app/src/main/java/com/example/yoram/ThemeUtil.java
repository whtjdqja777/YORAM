package com.example.yoram;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtil {
    public static final String LIGHT_MODE = "light";
    public static final String DARK_MODE = "dark";
    public static final String DEFAULT_MODE = "default";
    private static final String TAG = "ThemeUtil";

    public static void applyTheme(String themeColor) {
        switch (themeColor) {
            case LIGHT_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                // 다크 모드 해제
                Log.d(TAG, "Light mode applied");
                break;
            case DARK_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                // 다크 모드 설정
                Log.d(TAG, "Dark mode applied");
                break;
        }
    }
    private static final String MOD_PREFERENCES = "mod";
    public static void modSave(Context context, String select_mod) {
        SharedPreferences sp = context.getSharedPreferences(MOD_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("mod", select_mod);
        editor.apply();
    }

    public static String modLoad(Context context) {
        SharedPreferences sp = context.getSharedPreferences(MOD_PREFERENCES, Context.MODE_PRIVATE);
        return sp.getString("mod", "light");
    }
    public static void toggleTheme(Context context, String Theme_mod) {
        // 현재 테마를 불러옴
        String savedMode = modLoad(context);

        // 현재 테마를 기반으로 토글
        if (!savedMode.equals(Theme_mod)) {
            savedMode = Theme_mod;
        }

        // 변경된 테마 적용
        applyTheme(savedMode);
        modSave(context, savedMode);
    }
}