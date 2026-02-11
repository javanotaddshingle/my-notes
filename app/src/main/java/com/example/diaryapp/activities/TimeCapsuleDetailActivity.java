/*
时间胶囊功能
 */

package com.example.diaryapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.TimeCapsule;

import java.util.Date;

public class TimeCapsuleDetailActivity extends AppCompatActivity {

    private TextView titleTextView;
    private TextView contentTextView;
    private TextView createTimeTextView;
    private TextView openTimeTextView;
    private TextView statusTextView;
    private TextView unlockButton;
    private long timeCapsuleId;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_capsule_detail);

        titleTextView = findViewById(R.id.capsule_title);
        contentTextView = findViewById(R.id.capsule_content);
        createTimeTextView = findViewById(R.id.create_time);
        openTimeTextView = findViewById(R.id.open_time);
        statusTextView = findViewById(R.id.capsule_status);
        unlockButton = findViewById(R.id.unlock_button);

        database = AppDatabase.getInstance(this);

        if (getIntent().hasExtra("time_capsule_id")) {
            timeCapsuleId = getIntent().getLongExtra("time_capsule_id", -1);
            loadTimeCapsule(timeCapsuleId);
        }

        unlockButton.setOnClickListener(v -> unlockTimeCapsule());
    }

    private void loadTimeCapsule(long id) {
        new Thread(() -> {
            TimeCapsule timeCapsule = database.timeCapsuleDao().getTimeCapsuleById(id);
            runOnUiThread(() -> {
                if (timeCapsule != null) {
                    titleTextView.setText(timeCapsule.getTitle());
                    createTimeTextView.setText(formatDate(timeCapsule.getCreatedAt()));
                    openTimeTextView.setText(formatDate(timeCapsule.getOpenAt()));

                    if (timeCapsule.isOpened()) {
                        contentTextView.setText(timeCapsule.getContent());
                        statusTextView.setText("已解锁");
                        unlockButton.setVisibility(android.view.View.GONE);
                    } else {
                        Date now = new Date();
                        if (now.after(timeCapsule.getOpenAt())) {
                            // 可以解锁
                            statusTextView.setText("可以解锁");
                            unlockButton.setVisibility(android.view.View.VISIBLE);
                        } else {
                            // 未到解锁时间
                            statusTextView.setText("未到解锁时间");
                            unlockButton.setVisibility(android.view.View.GONE);
                        }
                        contentTextView.setText("🔒 时间胶囊尚未解锁");
                    }
                }
            });
        }).start();
    }

    private void unlockTimeCapsule() {
        new Thread(() -> {
            TimeCapsule timeCapsule = database.timeCapsuleDao().getTimeCapsuleById(timeCapsuleId);
            if (timeCapsule != null) {
                timeCapsule.setOpened(true);
                database.timeCapsuleDao().update(timeCapsule);
                
                runOnUiThread(() -> {
                    contentTextView.setText(timeCapsule.getContent());
                    statusTextView.setText("已解锁");
                    unlockButton.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "时间胶囊已解锁", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String formatDate(Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        return sdf.format(date);
    }
}
