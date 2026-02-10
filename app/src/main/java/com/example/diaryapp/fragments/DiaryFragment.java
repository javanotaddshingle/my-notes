package com.example.diaryapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class DiaryFragment extends Fragment {

    private RecyclerView diaryRecyclerView;
    private DiaryAdapter diaryAdapter;
    private List<Diary> diaryList;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        diaryRecyclerView = view.findViewById(R.id.diary_list);
        View addDiaryButton = view.findViewById(R.id.add_diary_button);

        database = AppDatabase.getInstance(requireContext());
        diaryList = new ArrayList<>();

        diaryAdapter = new DiaryAdapter(diaryList, requireContext(), this::deleteDiary);
        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        diaryRecyclerView.setAdapter(diaryAdapter);

        addDiaryButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DiaryEditActivity.class);
            startActivity(intent);
        });

        loadDiaries();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDiaries(); // 每次返回页面时重新加载数据
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

    private void deleteDiary(Diary diary) {
        new Thread(() -> {
            database.diaryDao().delete(diary);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                loadDiaries(); // 重新加载数据
            });
        }).start();
    }
}
