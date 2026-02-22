package com.example.diaryapp.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diaryapp.R;
import com.example.diaryapp.activities.CheckInItemEditActivity;
import com.example.diaryapp.adapters.CheckInAdapter;
import com.example.diaryapp.dao.CheckInRecordDao;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.CheckInItem;
import com.example.diaryapp.models.CheckInRecord;
import com.example.diaryapp.models.TimeCapsule;
import com.example.diaryapp.models.User;
import com.example.diaryapp.views.CheckInCalendarView;
import com.google.android.material.button.MaterialButton;

//import com.example.diaryapp.managers.UserManager;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class CheckInFragment extends Fragment {

    private RecyclerView checkInRecyclerView;
    private CheckInAdapter checkInAdapter;
    private List<CheckInItem> checkInItemList;
    private AppDatabase database;

    private View molingImage; // 修正：统一用View兼容所有ImageView类型
    private TextView molingMessage;
    private TextView checkInStatus;
    private MaterialButton checkInButton;
    private TextView streakDays;
    private TextView monthCheckIns;
    private TextView calendarMonth;
    private TextView checkInItemsCount;
    // 自定义日历控件（已导入正确包，无标红）
    private CheckInCalendarView checkInCalendar;

    private Set<Long> checkedInDates;
    private boolean isTodayCheckedIn = false;
    private int currentStreak = 0;
    private Handler updateHandler;
    private Runnable updateRunnable;
    private static final long UPDATE_DELAY_MS = 300;
    private static final long MAX_UPDATE_DELAY_MS = 1000;

    // 用于测试获取用户等级
//    private UserManager usermanager;

    private static final String[] MOLING_MESSAGES = {
            "今天也要加油哦！",
            "你是最棒的！",
            "坚持就是胜利！",
            "每一天都值得记录！",
            "相信自己，你可以的！",
            "小墨灵为你加油！",
            "今天的你也很优秀！",
            "保持好心情！"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ========== 直接打印具体打卡日期（一行都不用改） ==========
        new Thread(() -> {
            // 直接从数据库查已格式化的日期字符串
            List<String> dates = AppDatabase.getInstance(requireContext()).checkInRecordDao().getAllCheckInDates();
            Log.d("打卡具体日期", "===== 所有打卡日期 =====");
            for (String date : dates) {
                Log.d("打卡具体日期", date); // 直接打印：2026-02-20、2026-02-21...
            }
            AppDatabase db = AppDatabase.getInstance(requireContext());
            CheckInRecordDao dao = db.checkInRecordDao();

            dao.clearAllCheckInRecords();
        }).start();
        // ========== 结束 ==========

        View view = inflater.inflate(R.layout.fragment_check_in, container, false);

        checkInRecyclerView = view.findViewById(R.id.check_in_list);
        View addCheckInButton = view.findViewById(R.id.add_check_in_button);

        molingImage = view.findViewById(R.id.moling_image);
        molingMessage = view.findViewById(R.id.moling_message);
        checkInStatus = view.findViewById(R.id.check_in_status);
        // 逻辑层按钮和界面层按钮绑定
//        findViewById() 是 Android 系统提供的核心查找方法，作用就像：
//        你告诉它 “我要找界面上 ID 是 xxx 的控件”，它就去当前view对应的界面布局里，把这个控件 “找出来” 并返回给你。
        checkInButton = view.findViewById(R.id.check_in_button);
        streakDays = view.findViewById(R.id.streak_days);

        monthCheckIns = view.findViewById(R.id.month_check_ins);
//        capsuleCount = view.findViewById(R.id.capsule_count);
        calendarMonth = view.findViewById(R.id.calendar_month);
        checkInCalendar = view.findViewById(R.id.check_in_calendar);
        checkInItemsCount = view.findViewById(R.id.check_in_items_count);

        database = AppDatabase.getInstance(requireContext());
        checkInItemList = new ArrayList<>();
        checkedInDates = new HashSet<>();
        updateHandler = new Handler(Looper.getMainLooper());

        checkInAdapter = new CheckInAdapter(checkInItemList, requireContext(), this::deleteCheckInItem);
        checkInRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        checkInRecyclerView.setAdapter(checkInAdapter);

        setupMolingInteraction();
        setupCheckInButton();
        setupCalendar();

        addCheckInButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CheckInItemEditActivity.class);
            startActivity(intent);
        });

//        capsuleCount.setOnClickListener(v -> {
//            Toast.makeText(requireContext(), "查看时间胶囊", Toast.LENGTH_SHORT).show();
//        });

        loadAllData();

        return view;
    }

    private void setupMolingInteraction() {
        molingImage.setOnClickListener(v -> {
            animateMoling();
            showRandomMessage();
        });

        molingImage.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private float startX, startY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        v.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();

                        if (Math.abs(event.getRawX() - startX) > 10 || Math.abs(event.getRawY() - startY) > 10) {
                            isDragging = true;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            v.performClick();
                        } else {
                            snapToPosition(v);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void animateMoling() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(molingImage, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(molingImage, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void showRandomMessage() {
        Random random = new Random();
        String message = MOLING_MESSAGES[random.nextInt(MOLING_MESSAGES.length)];
        molingMessage.setText(message);
    }

    private void snapToPosition(View view) {
        Rect parentRect = new Rect();
        ((ViewGroup) view.getParent()).getHitRect(parentRect);

        float x = view.getX();
        float y = view.getY();

        float newX = Math.max(parentRect.left, Math.min(x, parentRect.right - view.getWidth()));
        float newY = Math.max(parentRect.top, Math.min(y, parentRect.bottom - view.getHeight()));

        view.animate()
                .x(newX)
                .y(newY)
                .setDuration(200)
                .start();
    }

//    v -> { ... }：Kotlin/Java8 的Lambda 表达式（简化匿名内部类），v是被点击的按钮对象，->后是点击后要做的事。
    private void setupCheckInButton() {
        checkInButton.setOnClickListener(v -> {
            if (isTodayCheckedIn) {
                Toast.makeText(requireContext(), "今天已经打卡了哦，明天再来吧", Toast.LENGTH_SHORT).show();
                return;
            }

            performCheckIn();
        });
    }

    // 执行打卡逻辑
    private void performCheckIn() {
        new Thread(() -> {
//            Date本质就是毫秒时间戳
            Date today = getTodayDate();
            CheckInRecord record = new CheckInRecord(today, new Date());
            database.checkInRecordDao().insert(record);
            // 获取当前fragment绑定的activity再开线程，{}都将并入界面，更新线程
            requireActivity().runOnUiThread(() -> {
                isTodayCheckedIn = true;
                updateCheckInUI();
                performCheckInSuccessAnimation();
                molingImage.setBackgroundResource(R.drawable.ic_moling_excited);
                molingMessage.setText("打卡成功！太棒了！");
                Toast.makeText(requireContext(), "今日已打卡！", Toast.LENGTH_SHORT).show();

                performImmediateUpdate();
            });
        }).start();
    }

    private void setupCalendar() {
        // 自定义日历无需setOnDateChangeListener（保留原生逻辑也可）
        updateCalendarMonth();
    }

    private void updateCalendarMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月", Locale.getDefault());
        calendarMonth.setText(sdf.format(calendar.getTime()));
    }

    private void loadAllData() {
        loadCheckInItems();
        loadCheckInStatistics();
//        loadCapsuleCount();
        loadCheckedInDates(); // 加载打卡日期到日历
    }

    private void loadCheckInItems() {
        new Thread(() -> {
            List<CheckInItem> items = database.checkInItemDao().getAllCheckInItems();
            requireActivity().runOnUiThread(() -> {
                updateCheckInItemsWithAnimation(items);
            });
        }).start();
    }

    private void updateCheckInItemsWithAnimation(List<CheckInItem> newItems) {
        if (newItems == null) return;
        
        int oldSize = checkInItemList.size();
        int newSize = newItems.size();
        
        checkInItemList.clear();
        checkInItemList.addAll(newItems);
        
        if (checkInItemsCount != null) {
            checkInItemsCount.setText(newSize + "项");
        }
        
        if (oldSize == 0 && newSize > 0) {
            checkInAdapter.notifyDataSetChanged();
        } else {
            checkInAdapter.updateData(checkInItemList);
        }
    }

    private void scheduleDataUpdate() {
        if (updateHandler != null) {
            if (updateRunnable != null) {
                updateHandler.removeCallbacks(updateRunnable);
            }
            updateRunnable = this::loadAllData;
            updateHandler.postDelayed(updateRunnable, UPDATE_DELAY_MS);
        }
    }

    private void performImmediateUpdate() {
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
        loadAllData();
    }

    private void loadCheckInStatistics() {
        new Thread(() -> {
            // 单一实例化
//            usermanager = UserManager.getInstance(getContext());

            List<CheckInRecord> allRecords = database.checkInRecordDao().getAllRecords();
            Date today = getTodayDate();
            isTodayCheckedIn = false;

            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long todayMillis = cal.getTimeInMillis();

            for (CheckInRecord record : allRecords) {
                if (record.getCheckInDate().getTime() == todayMillis) {
                    isTodayCheckedIn = true;
                    break;
                }
            }

            currentStreak = calculateStreak(allRecords);

            // 传入dao类的是毫秒时间戳
            // 计算本月打卡数（修正后的逻辑）
            Calendar monthCal = Calendar.getInstance();
            monthCal.set(Calendar.DAY_OF_MONTH, 1);
            monthCal.set(Calendar.HOUR_OF_DAY, 0);
            monthCal.set(Calendar.MINUTE, 0);
            monthCal.set(Calendar.SECOND, 0);
            monthCal.set(Calendar.MILLISECOND, 0);
            long startOfMonth = monthCal.getTimeInMillis();

            monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            monthCal.set(Calendar.HOUR_OF_DAY, 23);
            monthCal.set(Calendar.MINUTE, 59);
            monthCal.set(Calendar.SECOND, 59);
            monthCal.set(Calendar.MILLISECOND, 999);
            long endOfMonth = monthCal.getTimeInMillis();

            int monthCount = database.checkInRecordDao().getMonthCheckInDaysCount(startOfMonth, endOfMonth);
//            User user = usermanager.getUser();
//            int level = user.getLevel();

            requireActivity().runOnUiThread(() -> {
                streakDays.setText(currentStreak + "天");
                monthCheckIns.setText(monthCount + "天");
                updateCheckInUI();
            });
        }).start();
    }

    private int calculateStreak(List<CheckInRecord> records) {
        if (records == null || records.isEmpty()) return 0; // 增加null判断，更健壮

        Set<Long> dateSet = new HashSet<>();
        Calendar cal = Calendar.getInstance(); // 基于本地时区，避免UTC偏差

        for (CheckInRecord record : records) {
            if (record.getCheckInDate() == null) continue; // 空值防护

            // 关键：基于本地时区，把打卡时间转成「当天0点的毫秒数」
            cal.setTime(record.getCheckInDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dateSet.add(cal.getTimeInMillis());
        }

        int streak = 0;
        // 重新初始化Calendar，避免循环复用导致的问题
        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);

        // 从今天开始，往前逐天检查
        while (dateSet.contains(todayCal.getTimeInMillis())) {
            streak++;
            todayCal.add(Calendar.DAY_OF_MONTH, -1); // 往前推1天
        }

        return streak;
    }

//    private void loadCapsuleCount() {
//        new Thread(() -> {
//            List<TimeCapsule> capsules = database.timeCapsuleDao().getAllTimeCapsules();
//            int count = capsules.size();
//            requireActivity().runOnUiThread(() -> {
//                capsuleCount.setText(count + "个");
//            });
//        }).start();
//    }

    // 修正后的loadCheckedInDates（日志正常、变量无标红）
    private void loadCheckedInDates() {
        new Thread(() -> {
            // 1. 查询所有打卡记录
            List<CheckInRecord> allRecords = database.checkInRecordDao().getAllRecords();
            checkedInDates.clear();

//
//            // for test
//            checkedInDates.clear();
//            Calendar cal = Calendar.getInstance();
//
//            // test1
//            cal.set(2026,Calendar.FEBRUARY,10,0,0,0);

            // 日志1：打印数据库记录总数
            Log.d("日历全链路", "数据库打卡记录数：" + (allRecords == null ? 0 : allRecords.size()));

            // 2. 遍历记录，转换为0点毫秒数并打印日志
            if (allRecords != null && !allRecords.isEmpty()) {
                Calendar cal = Calendar.getInstance();
                for (int i = 0; i < allRecords.size(); i++) {
                    CheckInRecord record = allRecords.get(i);
                    if (record == null || record.getCheckInDate() == null) continue;

                    // 转换为0点毫秒数
                    cal.setTime(record.getCheckInDate());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long dateMillis = cal.getTimeInMillis();
                    checkedInDates.add(dateMillis);

                    // 日志2：打印单条记录的信息（临时变量定义正确）
                    Log.d("日历全链路", "第" + i + "条记录 - 毫秒数：" + dateMillis + "，日期：" + record.getCheckInDate());
                }
            }


            // ========== 只加这3行测试代码（核心！测试完删掉就恢复） ==========
            Calendar testCal = Calendar.getInstance();
            testCal.set(2026, Calendar.FEBRUARY, 10, 0, 0, 0); // 手动加2月10日打卡
            checkedInDates.add(testCal.getTimeInMillis());
            // 想加几天就多复制两行，比如再加2月18日：
            // testCal.set(2026, Calendar.FEBRUARY, 18, 0, 0, 0);
            // checkedInDates.add(testCal.getTimeInMillis());


            // 3. 主线程更新日历（确保控件非空）
            requireActivity().runOnUiThread(() -> {
                if (checkInCalendar == null) {
                    Log.e("日历全链路", "日历控件为空！绑定失败");
                    return;
                }
                // 日志3：打印传给日历的日期数
                Log.d("日历全链路", "传给日历的打卡日期数：" + checkedInDates.size());
                // 关键：传递日期并触发重绘
                checkInCalendar.setCheckedInDates(checkedInDates);
                checkInCalendar.setDate(System.currentTimeMillis(), false, true);
            });
        }).start();
    }

    private void updateCheckInUI() {
        if (isTodayCheckedIn) {
            checkInStatus.setText("今日已打卡");
            checkInButton.setEnabled(false);
            checkInButton.setText("已打卡");
            animateButtonToCheckedIn();
        } else {
            checkInStatus.setText("今日未打卡");
            checkInButton.setEnabled(true);
            checkInButton.setText("打卡");
            animateButtonToUnchecked();
        }
    }

    private void animateButtonToCheckedIn() {
        int successColor = ContextCompat.getColor(requireContext(), R.color.btn_plus_orange_yellow);
        int currentColor = checkInButton.getBackgroundTintList() != null
            ? checkInButton.getBackgroundTintList().getDefaultColor() 
            : Color.parseColor("#FF9800");

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(currentColor, successColor);
        colorAnimator.setDuration(400);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            checkInButton.setBackgroundTintList(ColorStateList.valueOf(color));
        });
        colorAnimator.start();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(checkInButton, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(checkInButton, "scaleY", 1f, 1.05f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void animateButtonToUnchecked() {
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.secondary);
        int currentColor = checkInButton.getBackgroundTintList() != null 
            ? checkInButton.getBackgroundTintList().getDefaultColor() 
            : Color.parseColor("#4CAF50");

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(currentColor, defaultColor);
        colorAnimator.setDuration(400);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            checkInButton.setBackgroundTintList(ColorStateList.valueOf(color));
        });
        colorAnimator.start();
    }

    private void performCheckInSuccessAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(checkInButton, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(checkInButton, "scaleY", 1f, 1.15f, 1f);
        scaleX.setDuration(350);
        scaleY.setDuration(350);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private Date getTodayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void deleteCheckInItem(CheckInItem checkInItem) {
        new Thread(() -> {
            database.checkInItemDao().delete(checkInItem);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                performImmediateUpdate();
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        performImmediateUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
            updateHandler = null;
        }
        updateRunnable = null;
    }
}
