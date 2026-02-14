package com.example.diaryapp.fragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diaryapp.R;
import com.example.diaryapp.activities.CheckInItemEditActivity;
import com.example.diaryapp.adapters.CheckInAdapter;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.CheckInItem;
import com.example.diaryapp.models.CheckInRecord;
import com.example.diaryapp.models.TimeCapsule;
import com.google.android.material.button.MaterialButton;

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

    private ImageView molingImage;
    private TextView molingMessage;
    private TextView checkInStatus;
    private MaterialButton checkInButton;
    private TextView streakDays;
    private TextView monthCheckIns;
    private TextView capsuleCount;
    private TextView calendarMonth;
    private CalendarView checkInCalendar;

    private Set<Long> checkedInDates;
    private boolean isTodayCheckedIn = false;
    private int currentStreak = 0;

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
        View view = inflater.inflate(R.layout.fragment_check_in, container, false);

        checkInRecyclerView = view.findViewById(R.id.check_in_list);
        View addCheckInButton = view.findViewById(R.id.add_check_in_button);

        molingImage = view.findViewById(R.id.moling_image);
        molingMessage = view.findViewById(R.id.moling_message);
        checkInStatus = view.findViewById(R.id.check_in_status);
        checkInButton = view.findViewById(R.id.check_in_button);
        streakDays = view.findViewById(R.id.streak_days);

//        fragment文件中将布局文件的month_check_ins与本文件的一个变量绑定
        monthCheckIns = view.findViewById(R.id.month_check_ins);
        capsuleCount = view.findViewById(R.id.capsule_count);
        calendarMonth = view.findViewById(R.id.calendar_month);
        checkInCalendar = view.findViewById(R.id.check_in_calendar);

        database = AppDatabase.getInstance(requireContext());
        checkInItemList = new ArrayList<>();
        checkedInDates = new HashSet<>();

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

        capsuleCount.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "查看时间胶囊", Toast.LENGTH_SHORT).show();
        });

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

    private void setupCheckInButton() {
        checkInButton.setOnClickListener(v -> {
            if (isTodayCheckedIn) {
                Toast.makeText(requireContext(), "今天已经打卡啦！", Toast.LENGTH_SHORT).show();
                return;
            }

            performCheckIn();
        });
    }

    private void performCheckIn() {
        new Thread(() -> {
            Date today = getTodayDate();
            CheckInRecord record = new CheckInRecord(today, new Date());
            database.checkInRecordDao().insert(record);

            requireActivity().runOnUiThread(() -> {
                isTodayCheckedIn = true;
                updateCheckInUI();
                molingImage.setImageResource(R.drawable.ic_moling_excited);
                molingMessage.setText("打卡成功！太棒了！");
                Toast.makeText(requireContext(), "打卡成功！", Toast.LENGTH_SHORT).show();

                loadCheckInStatistics();
            });
        }).start();
    }

    private void setupCalendar() {
        checkInCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long dateMillis = calendar.getTimeInMillis();

            if (checkedInDates.contains(dateMillis)) {
                Toast.makeText(requireContext(), "这天已经打卡了", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "这天未打卡", Toast.LENGTH_SHORT).show();
            }
        });

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
        loadCapsuleCount();
        loadCheckedInDates();
    }

    private void loadCheckInItems() {
        new Thread(() -> {
            List<CheckInItem> items = database.checkInItemDao().getAllCheckInItems();
            requireActivity().runOnUiThread(() -> {
                checkInItemList.clear();
                checkInItemList.addAll(items);
                checkInAdapter.updateData(checkInItemList);
            });
        }).start();
    }

    private void loadCheckInStatistics() {
        new Thread(() -> {
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

            // ========== 新增/修改的核心代码 start ==========
            // 1. 计算本月第一天 00:00:00 的毫秒数
            Calendar monthCal = Calendar.getInstance();
            monthCal.set(Calendar.DAY_OF_MONTH, 1); // 本月第一天
            monthCal.set(Calendar.HOUR_OF_DAY, 0);
            monthCal.set(Calendar.MINUTE, 0);
            monthCal.set(Calendar.SECOND, 0);
            monthCal.set(Calendar.MILLISECOND, 0);
            long startOfMonth = monthCal.getTimeInMillis();

            // 2. 计算本月最后一天 23:59:59 的毫秒数
            monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)); // 本月最后一天
            monthCal.set(Calendar.HOUR_OF_DAY, 23);
            monthCal.set(Calendar.MINUTE, 59);
            monthCal.set(Calendar.SECOND, 59);
            monthCal.set(Calendar.MILLISECOND, 999);
            long endOfMonth = monthCal.getTimeInMillis();

            // 3. 调用修正后的Dao方法，传入本月起止毫秒数
            int monthCount = database.checkInRecordDao().getMonthCheckInDaysCount(startOfMonth, endOfMonth);
            // ========== 新增/修改的核心代码 end ==========

            requireActivity().runOnUiThread(() -> {
                streakDays.setText(currentStreak + "天");
                monthCheckIns.setText(monthCount + "天");
                updateCheckInUI();
            });
        }).start();
    }

    private int calculateStreak(List<CheckInRecord> records) {
        if (records.isEmpty()) return 0;

        Set<Long> dateSet = new HashSet<>();
        Calendar cal = Calendar.getInstance();
        for (CheckInRecord record : records) {
            cal.setTime(record.getCheckInDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dateSet.add(cal.getTimeInMillis());
        }

        int streak = 0;
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        while (dateSet.contains(cal.getTimeInMillis())) {
            streak++;
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        return streak;
    }

//    private int calculateMonthCount(List<CheckInRecord> records) {
//        Calendar now = Calendar.getInstance();
//        int currentMonth = now.get(Calendar.MONTH);
//        int currentYear = now.get(Calendar.YEAR);
//
//        int count = 0;
//        Calendar cal = Calendar.getInstance();
//        for (CheckInRecord record : records) {
//            cal.setTime(record.getCheckInDate());
//            if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
//                count++;
//            }
//        }
//        return count;
//    }

    private void loadCapsuleCount() {
        new Thread(() -> {
            List<TimeCapsule> capsules = database.timeCapsuleDao().getAllTimeCapsules();
            int count = capsules.size();
            requireActivity().runOnUiThread(() -> {
                capsuleCount.setText(count + "个");
            });
        }).start();
    }

    private void loadCheckedInDates() {
        new Thread(() -> {
            List<CheckInRecord> allRecords = database.checkInRecordDao().getAllRecords();
            checkedInDates.clear();

            Calendar cal = Calendar.getInstance();
            for (CheckInRecord record : allRecords) {
                cal.setTime(record.getCheckInDate());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                checkedInDates.add(cal.getTimeInMillis());
            }

            requireActivity().runOnUiThread(() -> {
                updateCalendarView();
            });
        }).start();
    }

    private void updateCalendarView() {
        CalendarView calendarView = checkInCalendar;
        calendarView.setDate(System.currentTimeMillis(), false, true);
    }

    private void updateCheckInUI() {
        if (isTodayCheckedIn) {
            checkInStatus.setText("今日已打卡");
            checkInButton.setEnabled(false);
            checkInButton.setText("已打卡");
        } else {
            checkInStatus.setText("今日未打卡");
            checkInButton.setEnabled(true);
            checkInButton.setText("打卡");
        }
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
                loadCheckInItems();
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllData();
    }
}
