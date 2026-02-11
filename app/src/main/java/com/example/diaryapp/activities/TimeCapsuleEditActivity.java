/*
时间胶囊功能
 */

package com.example.diaryapp.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.TimeCapsule;

import java.util.Calendar;
import java.util.Date;

public class TimeCapsuleEditActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText contentEditText;
    private TextView timeTextView;
    private long timeCapsuleId = -1;
    private AppDatabase database;
    private Calendar openCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_capsule_edit);

        titleEditText = findViewById(R.id.capsule_title);
        contentEditText = findViewById(R.id.capsule_content);
        timeTextView = findViewById(R.id.open_time);
        TextView saveButton = findViewById(R.id.save_button);
        TextView cancelButton = findViewById(R.id.cancel_button);

        database = AppDatabase.getInstance(this);
        openCalendar = Calendar.getInstance();
        openCalendar.add(Calendar.DAY_OF_YEAR, 1); // 默认设置为明天

        updateTimeTextView();

        // 检查是否是编辑模式
        if (getIntent().hasExtra("time_capsule_id")) {
            timeCapsuleId = getIntent().getLongExtra("time_capsule_id", -1);
            loadTimeCapsule(timeCapsuleId);
        }

        timeTextView.setOnClickListener(v -> showDateTimePicker());
        saveButton.setOnClickListener(v -> saveTimeCapsule());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadTimeCapsule(long id) {
        new Thread(() -> {
            TimeCapsule timeCapsule = database.timeCapsuleDao().getTimeCapsuleById(id);
            runOnUiThread(() -> {
                if (timeCapsule != null) {
                    titleEditText.setText(timeCapsule.getTitle());
                    contentEditText.setText(timeCapsule.getContent());
                    openCalendar.setTime(timeCapsule.getOpenAt());
                    updateTimeTextView();
                }
            });
        }).start();
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            openCalendar.set(Calendar.YEAR, year);
            openCalendar.set(Calendar.MONTH, month);
            openCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                openCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                openCalendar.set(Calendar.MINUTE, minute);
                updateTimeTextView();
            }, openCalendar.get(Calendar.HOUR_OF_DAY), openCalendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, openCalendar.get(Calendar.YEAR), openCalendar.get(Calendar.MONTH), openCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateTimeTextView() {
        String timeString = String.format("%d年%d月%d日 %d:%02d",
                openCalendar.get(Calendar.YEAR),
                openCalendar.get(Calendar.MONTH) + 1,
                openCalendar.get(Calendar.DAY_OF_MONTH),
                openCalendar.get(Calendar.HOUR_OF_DAY),
                openCalendar.get(Calendar.MINUTE));
        timeTextView.setText(timeString);
    }

    private void saveTimeCapsule() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        Date now = new Date();
        Date openAt = openCalendar.getTime();
        int experiencePoints = 15; // 每次创建时间胶囊获得15点经验值

        if (openAt.before(now)) {
            Toast.makeText(this, "打开时间必须在当前时间之后", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (timeCapsuleId == -1) {
                // 创建新时间胶囊
                TimeCapsule timeCapsule = new TimeCapsule(title, content, now, openAt, false, experiencePoints);
                database.timeCapsuleDao().insert(timeCapsule);
            } else {
                // 更新现有时间胶囊
                TimeCapsule timeCapsule = database.timeCapsuleDao().getTimeCapsuleById(timeCapsuleId);
                if (timeCapsule != null) {
                    timeCapsule.setTitle(title);
                    timeCapsule.setContent(content);
                    timeCapsule.setOpenAt(openAt);
                    database.timeCapsuleDao().update(timeCapsule);
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
