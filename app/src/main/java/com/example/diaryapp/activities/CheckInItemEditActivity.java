package com.example.diaryapp.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.CheckInItem;

import java.util.Date;

public class CheckInItemEditActivity extends AppCompatActivity {

    private EditText nameEditText;
    private long checkInItemId = -1;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_item_edit);

        nameEditText = findViewById(R.id.check_in_item_name);
        TextView saveButton = findViewById(R.id.save_button);
        TextView cancelButton = findViewById(R.id.cancel_button);

        database = AppDatabase.getInstance(this);

        // 检查是否是编辑模式
        if (getIntent().hasExtra("check_in_item_id")) {
            checkInItemId = getIntent().getLongExtra("check_in_item_id", -1);
            loadCheckInItem(checkInItemId);
        }

        saveButton.setOnClickListener(v -> saveCheckInItem());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadCheckInItem(long id) {
        new Thread(() -> {
            CheckInItem checkInItem = database.checkInItemDao().getCheckInItemById(id);
            runOnUiThread(() -> {
                if (checkInItem != null) {
                    nameEditText.setText(checkInItem.getName());
                }
            });
        }).start();
    }

    private void saveCheckInItem() {
        String name = nameEditText.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "请输入打卡项名称", Toast.LENGTH_SHORT).show();
            return;
        }

        Date now = new Date();
        int experiencePoints = 5; // 每次打卡获得5点经验值

        new Thread(() -> {
            if (checkInItemId == -1) {
                // 创建新打卡项
                CheckInItem checkInItem = new CheckInItem(name, now, experiencePoints);
                database.checkInItemDao().insert(checkInItem);
            } else {
                // 更新现有打卡项
                CheckInItem checkInItem = database.checkInItemDao().getCheckInItemById(checkInItemId);
                if (checkInItem != null) {
                    checkInItem.setName(name);
                    database.checkInItemDao().update(checkInItem);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
