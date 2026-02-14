package com.example.diaryapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import com.example.diaryapp.R;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class CheckInCalendarView extends CalendarView {
    private Set<Long> checkedInDates = new HashSet<>();
    private Paint bgPaint;
    private Paint textPaint;
    private long todayMillis;
    private Context mContext;

    // 构造方法
    public CheckInCalendarView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public CheckInCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public CheckInCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    // 初始化画笔和今日毫秒数
    private void init() {
        // 背景画笔（橙色）
        bgPaint = new Paint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bgPaint.setColor(mContext.getResources().getColor(R.color.btn_plus_orange_yellow, mContext.getTheme()));
        } else {
            bgPaint.setColor(mContext.getResources().getColor(R.color.btn_plus_orange_yellow));
        }
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);

        // 文字画笔（白色）
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 计算今日0点毫秒数（和Fragment的日期格式完全对齐）
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        todayMillis = cal.getTimeInMillis();
        Log.d("日历绘制", "今日0点毫秒数：" + todayMillis); // 打印今日毫秒数，方便对比

//        // ========== 新增这1行（核心！禁用原生选中） ==========
//        setSelectedDateVerticalBar(R.color.transparent); // 把原生选中的竖线设为透明
//        setSelectionMode(CalendarView.SELECTION_MODE_NONE); // 完全禁用选中模式
    }

    // 接收打卡日期（核心：延迟重绘，避开原生绘制）
    public void setCheckedInDates(Set<Long> dates) {
        this.checkedInDates = dates;
        Log.d("日历绘制", "日历收到的打卡日期：" + dates); // 打印收到的日期
        postInvalidateDelayed(300); // 延迟300ms重绘，确保原生绘制完成后再画自定义样式
    }

    // 核心：重写onDraw，先画自定义样式，后画原生（避免被覆盖）
    @Override
    protected void onDraw(Canvas canvas) {
        // 第一步：先画自定义样式（关键！原先是先画原生，自定义被覆盖）
        drawCustomStyles(canvas);
        // 第二步：再画原生样式（如果自定义被遮挡，可注释这行测试）
//        super.onDraw(canvas);
    }

    // 抽离自定义绘制逻辑，便于调试
    private void drawCustomStyles(Canvas canvas) {
        Log.d("日历绘制", "开始自定义绘制，打卡日期数：" + checkedInDates.size());
        Log.d("日历绘制", "今日毫秒数：" + todayMillis + "，打卡日期包含今日：" + checkedInDates.contains(todayMillis));

        // 遍历所有单元格
        for (int i = 0; i < getChildCount(); i++) {
            View cell = getChildAt(i);
            if (cell == null) continue;

            // 获取单元格的日期毫秒数
            long cellDateMillis = getCellDateMillis(cell);
            String dayText = getCellDayText(cell);

            // 打印单元格信息，方便匹配
            Log.d("日历绘制", "单元格" + i + "：文字=" + dayText + "，毫秒数=" + cellDateMillis);

            // 匹配今日：画橙色背景+“今”字
            if (cellDateMillis == todayMillis) {
                Log.d("日历绘制", "匹配到今日，绘制样式");
                RectF cellRect = new RectF(cell.getLeft(), cell.getTop(), cell.getRight(), cell.getBottom());
                // 放大矩形，确保能看到（避免单元格边界问题）
                cellRect.inset(-10, -10); // 向外扩展10px
                canvas.drawRoundRect(cellRect, 16, 16, bgPaint);
                float textY = cellRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2;
                canvas.drawText("今", cellRect.centerX(), textY, textPaint);
            }
            // 匹配打卡日期：画橙色背景+数字
            else if (checkedInDates.contains(cellDateMillis)) {
                Log.d("日历绘制", "匹配到打卡日期，绘制样式");
                RectF cellRect = new RectF(cell.getLeft(), cell.getTop(), cell.getRight(), cell.getBottom());
                cellRect.inset(-10, -10);
                canvas.drawRoundRect(cellRect, 16, 16, bgPaint);
                float textY = cellRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2;
                canvas.drawText(dayText, cellRect.centerX(), textY, textPaint);
            }
        }
    }

    // 精准获取单元格日期毫秒数（和Fragment完全对齐）
    private long getCellDateMillis(View cell) {
        try {
            String dayText = getCellDayText(cell);
            if (dayText.isEmpty() || !dayText.matches("\\d+")) return -1;
            int day = Integer.parseInt(dayText);

            // 获取日历当前显示的年月（关键：匹配当前显示的月份）
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(getDate()); // 日历当前显示的日期
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);

            // 拼接成0点毫秒数（和Fragment的转换逻辑完全一致）
            cal.set(year, month, day, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // 递归遍历，确保拿到日期文字
    private String getCellDayText(View cell) {
        final String[] dayText = {""};
        traverseView(cell, textView -> {
            String text = textView.getText().toString().trim();
            if (text.matches("\\d+") && text.length() <= 2) { // 只取1-31的数字
                dayText[0] = text;
            }
        });
        return dayText[0];
    }

    // 递归遍历所有子View
    private void traverseView(View view, ViewCallback callback) {
        if (view instanceof TextView) {
            callback.onFindTextView((TextView) view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                traverseView(group.getChildAt(i), callback);
            }
        }
    }

    // 回调接口
    interface ViewCallback {
        void onFindTextView(TextView textView);
    }
}