package com.example.diaryapp.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.example.diaryapp.R;

public class ThemeDialog {

    private static final String PREF_NAME = "theme_preferences";
    private static final String THEME_KEY = "app_theme";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_FOLLOW_SYSTEM = 2;

    public interface ThemeChangeListener {
        void onThemeChanged(int newTheme);
    }

    public static void showThemeDialog(Context context, ThemeChangeListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择主题");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_theme, null);
        builder.setView(view);

        RadioGroup themeRadioGroup = view.findViewById(R.id.theme_radio_group);
        RadioButton lightThemeRadio = view.findViewById(R.id.light_theme);
        RadioButton darkThemeRadio = view.findViewById(R.id.dark_theme);
        RadioButton systemThemeRadio = view.findViewById(R.id.system_theme);

        // 获取当前主题设置
        int currentTheme = getCurrentTheme(context);
        switch (currentTheme) {
            case THEME_LIGHT:
                lightThemeRadio.setChecked(true);
                break;
            case THEME_DARK:
                darkThemeRadio.setChecked(true);
                break;
            case THEME_FOLLOW_SYSTEM:
                systemThemeRadio.setChecked(true);
                break;
        }

        builder.setPositiveButton("确定", (dialog, which) -> {
            int selectedTheme = THEME_FOLLOW_SYSTEM;
            int checkedId = themeRadioGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.light_theme) {
                selectedTheme = THEME_LIGHT;
            } else if (checkedId == R.id.dark_theme) {
                selectedTheme = THEME_DARK;
            }

            saveTheme(context, selectedTheme);
            if (listener != null) {
                listener.onThemeChanged(selectedTheme);
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public static int getCurrentTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(THEME_KEY, THEME_FOLLOW_SYSTEM);
    }

    private static void saveTheme(Context context, int theme) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().putInt(THEME_KEY, theme).apply();
    }

    public static void applyTheme(Context context) {
        int theme = getCurrentTheme(context);
        int nightMode = android.app.UiModeManager.MODE_NIGHT_AUTO;

        switch (theme) {
            case THEME_LIGHT:
                nightMode = android.app.UiModeManager.MODE_NIGHT_NO;
                break;
            case THEME_DARK:
                nightMode = android.app.UiModeManager.MODE_NIGHT_YES;
                break;
            case THEME_FOLLOW_SYSTEM:
                nightMode = android.app.UiModeManager.MODE_NIGHT_AUTO;
                break;
        }

        android.app.UiModeManager uiModeManager = (android.app.UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null) {
            uiModeManager.setNightMode(nightMode);
        }
    }
}
