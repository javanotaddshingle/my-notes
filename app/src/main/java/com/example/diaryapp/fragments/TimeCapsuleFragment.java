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
import com.example.diaryapp.activities.TimeCapsuleEditActivity;
import com.example.diaryapp.adapters.TimeCapsuleAdapter;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.TimeCapsule;

import java.util.ArrayList;
import java.util.List;

public class TimeCapsuleFragment extends Fragment {

    private RecyclerView timeCapsuleRecyclerView;
    private TimeCapsuleAdapter timeCapsuleAdapter;
    private List<TimeCapsule> timeCapsuleList;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_capsule, container, false);

        timeCapsuleRecyclerView = view.findViewById(R.id.time_capsule_list);
        View addTimeCapsuleButton = view.findViewById(R.id.add_time_capsule_button);

        database = AppDatabase.getInstance(requireContext());
        timeCapsuleList = new ArrayList<>();

        timeCapsuleAdapter = new TimeCapsuleAdapter(timeCapsuleList, requireContext(), this::deleteTimeCapsule);
        timeCapsuleRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        timeCapsuleRecyclerView.setAdapter(timeCapsuleAdapter);

        addTimeCapsuleButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TimeCapsuleEditActivity.class);
            startActivity(intent);
        });

        loadTimeCapsules();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTimeCapsules(); // 每次返回页面时重新加载数据
    }

    private void loadTimeCapsules() {
        new Thread(() -> {
            List<TimeCapsule> capsules = database.timeCapsuleDao().getAllTimeCapsules();
            requireActivity().runOnUiThread(() -> {
                timeCapsuleList.clear();
                timeCapsuleList.addAll(capsules);
                timeCapsuleAdapter.updateData(timeCapsuleList);
            });
        }).start();
    }

    private void deleteTimeCapsule(TimeCapsule timeCapsule) {
        new Thread(() -> {
            database.timeCapsuleDao().delete(timeCapsule);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                loadTimeCapsules(); // 重新加载数据
            });
        }).start();
    }
}
