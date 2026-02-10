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
import com.example.diaryapp.activities.CheckInItemEditActivity;
import com.example.diaryapp.adapters.CheckInAdapter;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.CheckInItem;

import java.util.ArrayList;
import java.util.List;

public class CheckInFragment extends Fragment {

    private RecyclerView checkInRecyclerView;
    private CheckInAdapter checkInAdapter;
    private List<CheckInItem> checkInItemList;
    private AppDatabase database;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_in, container, false);

        checkInRecyclerView = view.findViewById(R.id.check_in_list);
        View addCheckInButton = view.findViewById(R.id.add_check_in_button);

        database = AppDatabase.getInstance(requireContext());
        checkInItemList = new ArrayList<>();

        checkInAdapter = new CheckInAdapter(checkInItemList, requireContext(), this::deleteCheckInItem);
        checkInRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        checkInRecyclerView.setAdapter(checkInAdapter);

        addCheckInButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CheckInItemEditActivity.class);
            startActivity(intent);
        });

        loadCheckInItems();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCheckInItems(); // 每次返回页面时重新加载数据
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

    private void deleteCheckInItem(CheckInItem checkInItem) {
        new Thread(() -> {
            database.checkInItemDao().delete(checkInItem);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show();
                loadCheckInItems(); // 重新加载数据
            });
        }).start();
    }
}
