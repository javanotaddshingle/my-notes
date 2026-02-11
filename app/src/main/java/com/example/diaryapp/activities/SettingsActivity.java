// 替换成你项目的实际包名（比如和BaseActivity相同的包名，如com.example.diaryapp.activities）
package com.example.diaryapp.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatDelegate;

// 导入你项目的R类（必须和包名匹配，若Android Studio自动导入可忽略，保留此行更保险）
import com.example.diaryapp.R;

// 继承BaseActivity（确保BaseActivity在同一包下）
public class SettingsActivity extends BaseActivity {

    // 控件声明：和布局中switch_night_mode完全一致
    private Switch switch_night_mode;

    // SP常量：统一命名，避免混乱
    private static final String SP_NAME = "app_settings";
    private static final String KEY_NIGHT_MODE = "night_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化开关控件
        switch_night_mode = findViewById(R.id.switch_night_mode);

        // 读取保存的夜间模式状态，初始化开关勾选状态（修正MODE_PRIVATE为Context.MODE_PRIVATE）
        SharedPreferences sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        boolean isNightMode = sp.getBoolean(KEY_NIGHT_MODE, false);
        switch_night_mode.setChecked(isNightMode);

        // 开关点击事件：切换夜间模式+动效+保存状态
        switch_night_mode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存状态到SP
            sp.edit().putBoolean(KEY_NIGHT_MODE, isChecked).apply();
            // 切换模式并添加动效
            setNightMode(isChecked);
        });
    }

    /**
     * 切换夜间模式核心方法
     * @param isNightMode 是否开启夜间模式
     */
    private void setNightMode(boolean isNightMode) {
        // 1. 设置夜间模式（Android官方API）
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // 2. 添加淡入淡出动效（300ms，流畅无卡顿）
        Animation fadeAnimation = new AlphaAnimation(0.8f, 1.0f);
        fadeAnimation.setDuration(300);
        fadeAnimation.setFillAfter(true);
        getWindow().getDecorView().startAnimation(fadeAnimation);

        // 3. 重建Activity使主题生效，添加系统过渡动画
        recreate();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}