package com.example.diaryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.diaryapp.R;
import com.example.diaryapp.activities.DiaryEditActivity;
import com.example.diaryapp.adapters.DiaryAdapter;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.Diary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DiaryFragment extends Fragment {

    private RecyclerView diaryRecyclerView;
    private DiaryAdapter diaryAdapter;
    private List<Diary> diaryList;
    private AppDatabase database;

    private TextView totalDiaries;
    private TextView monthDiaries;
    private TextView yearDiaries;
    private TextView continuousDays;
    private TextView avgWeeklyDiaries;
    private TextView completionRate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        diaryRecyclerView = view.findViewById(R.id.diary_list);
        View addDiaryButton = view.findViewById(R.id.add_diary_button);

        totalDiaries = view.findViewById(R.id.total_diaries);
        monthDiaries = view.findViewById(R.id.month_diaries);
        yearDiaries = view.findViewById(R.id.year_diaries);
        continuousDays = view.findViewById(R.id.continuous_days);
        avgWeeklyDiaries = view.findViewById(R.id.avg_weekly_diaries);
        completionRate = view.findViewById(R.id.completion_rate);

        database = AppDatabase.getInstance(requireContext());
        diaryList = new ArrayList<>();

        diaryAdapter = new DiaryAdapter(diaryList, requireContext(), this::deleteDiary);
        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        diaryRecyclerView.setAdapter(diaryAdapter);

        addDiaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DiaryEditActivity.class);
            startActivity(intent);
        });

        loadAllData();

        return view;
    }

    private void loadAllData() {
        loadDiaries();
        loadDiaryStatistics();
    }

    private void loadDiaries() {
        new Thread(() -> {
            List<Diary> diaries = database.diaryDao().getAllDiaries();
            requireActivity().runOnUiThread(() -> {
                diaryList.clear();
                diaryList.addAll(diaries);
                diaryAdapter.updateData(diaryList);
            });
        }).start();
    }

    private void loadDiaryStatistics() {
        new Thread(() -> {
            List<Diary> allDiaries = database.diaryDao().getAllDiaries();

            int totalCount = allDiaries.size();
            int monthCount = calculateMonthCount(allDiaries);
            int yearCount = calculateYearCount(allDiaries);
            int continuous = calculateContinuousDays(allDiaries);
            double avgWeekly = calculateAvgWeekly(allDiaries);
            int completion = calculateCompletionRate(allDiaries);

            requireActivity().runOnUiThread(() -> {
                totalDiaries.setText(totalCount + "篇");
                monthDiaries.setText(monthCount + "篇");
                yearDiaries.setText(yearCount + "篇");
                continuousDays.setText(continuous + "天");
                avgWeeklyDiaries.setText(String.format(Locale.getDefault(), "%.1f篇", avgWeekly));
                completionRate.setText(completion + "%");
            });
        }).start();
    }

    private int calculateMonthCount(List<Diary> diaries) {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        int count = 0;
        Calendar cal = Calendar.getInstance();
        for (Diary diary : diaries) {
            cal.setTime(diary.getCreatedAt());
            if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                count++;
            }
        }
        return count;
    }

    private int calculateYearCount(List<Diary> diaries) {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);

        int count = 0;
        Calendar cal = Calendar.getInstance();
        for (Diary diary : diaries) {
            cal.setTime(diary.getCreatedAt());
            if (cal.get(Calendar.YEAR) == currentYear) {
                count++;
            }
        }
        return count;
    }

    private int calculateContinuousDays(List<Diary> diaries) {
        if (diaries.isEmpty()) return 0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int continuous = 0;
        boolean hasToday = false;

        for (int i = 0; i < 365; i++) {
            boolean hasDiaryOnDay = false;
            for (Diary diary : diaries) {
                Calendar diaryCal = Calendar.getInstance();
                diaryCal.setTime(diary.getCreatedAt());
                diaryCal.set(Calendar.HOUR_OF_DAY, 0);
                diaryCal.set(Calendar.MINUTE, 0);
                diaryCal.set(Calendar.SECOND, 0);
                diaryCal.set(Calendar.MILLISECOND, 0);

                if (diaryCal.getTimeInMillis() == cal.getTimeInMillis()) {
                    hasDiaryOnDay = true;
                    break;
                }
            }

            if (hasDiaryOnDay) {
                continuous++;
                if (i == 0) hasToday = true;
            } else if (i > 0) {
                break;
            }

            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        return hasToday ? continuous : 0;
    }

    private double calculateAvgWeekly(List<Diary> diaries) {
        if (diaries.isEmpty()) return 0;

        Calendar firstDiary = Calendar.getInstance();
        firstDiary.setTime(diaries.get(diaries.size() - 1).getCreatedAt());

        Calendar now = Calendar.getInstance();
        long diffInMillis = now.getTimeInMillis() - firstDiary.getTimeInMillis();
        long weeks = diffInMillis / (7 * 24 * 60 * 60 * 1000);

        if (weeks < 1) weeks = 1;

        return (double) diaries.size() / weeks;
    }

    private int calculateCompletionRate(List<Diary> diaries) {
        if (diaries.isEmpty()) return 0;

        Calendar firstDiary = Calendar.getInstance();
        firstDiary.setTime(diaries.get(diaries.size() - 1).getCreatedAt());

        Calendar now = Calendar.getInstance();
        long diffInMillis = now.getTimeInMillis() - firstDiary.getTimeInMillis();
        long days = diffInMillis / (24 * 60 * 60 * 1000);

        if (days < 1) days = 1;

        int targetDays = (int) Math.min(days, 30);
        int completedDays = calculateCompletedDays(diaries, targetDays);

        return (int) ((double) completedDays / targetDays * 100);
    }

    private int calculateCompletedDays(List<Diary> diaries, int targetDays) {
        Set<Long> dateSet = new HashSet<>();
        Calendar cal = Calendar.getInstance();
        for (Diary diary : diaries) {
            cal.setTime(diary.getCreatedAt());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            dateSet.add(cal.getTimeInMillis());
        }
        return dateSet.size();
    }

    private void deleteDiary(Diary diary) {
        new Thread(() -> {
            database.diaryDao().delete(diary);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                loadAllData();
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllData();
    }
}
