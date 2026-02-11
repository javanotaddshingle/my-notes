package com.example.diaryapp.activities; // 包名，和你项目结构一致

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {
    private static final String SP_NAME = "app_settings";
    private static final String KEY_NIGHT_MODE = "night_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 优先读取保存的模式，设置主题（必须在super.onCreate前）
        SharedPreferences sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        boolean isNightMode = sp.getBoolean(KEY_NIGHT_MODE, false);

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
    }
}