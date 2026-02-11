/*
写日记功能
 */

package com.example.diaryapp.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.Diary;

import java.util.Date;

public class DiaryEditActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText contentEditText;
    private long diaryId = -1;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        titleEditText = findViewById(R.id.diary_title);
        contentEditText = findViewById(R.id.diary_content);
        TextView saveButton = findViewById(R.id.save_button);
        TextView cancelButton = findViewById(R.id.cancel_button);

        database = AppDatabase.getInstance(this);

        // 检查是否是编辑模式
        if (getIntent().hasExtra("diary_id")) {
            diaryId = getIntent().getLongExtra("diary_id", -1);
            loadDiary(diaryId);
        }

        saveButton.setOnClickListener(v -> saveDiary());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadDiary(long id) {
        new Thread(() -> {
            Diary diary = database.diaryDao().getDiaryById(id);
            runOnUiThread(() -> {
                if (diary != null) {
                    titleEditText.setText(diary.getTitle());
                    contentEditText.setText(diary.getContent());
                }
            });
        }).start();
    }

    private void saveDiary() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        Date now = new Date();
        int experiencePoints = 10; // 每次写日记获得10点经验值

        new Thread(() -> {
            if (diaryId == -1) {
                // 创建新日记
                Diary diary = new Diary(title, content, now, now, experiencePoints);
                database.diaryDao().insert(diary);
            } else {
                // 更新现有日记
                Diary diary = database.diaryDao().getDiaryById(diaryId);
                if (diary != null) {
                    diary.setTitle(title);
                    diary.setContent(content);
                    diary.setUpdatedAt(now);
                    database.diaryDao().update(diary);
                }
            }

            // 更新用户经验值
            updateUserExperience(experiencePoints);

            runOnUiThread(() -> {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void updateUserExperience(int points) {
        com.example.diaryapp.managers.UserManager.getInstance(this).addExperiencePoints(points);
    }
}
