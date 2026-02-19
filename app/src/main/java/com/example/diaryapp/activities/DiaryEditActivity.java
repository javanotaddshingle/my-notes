/*
写日记功能
 */

package com.example.diaryapp.activities;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.Diary;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Looper;
import androidx.core.os.HandlerCompat; // 新增这行

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiaryEditActivity extends AppCompatActivity {

    private EditText titleEditText;
    // AI调用的逻辑层按钮
    private Button aiButton;
    private EditText contentEditText;
    private long diaryId = -1;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        titleEditText = findViewById(R.id.diary_title);
        aiButton = findViewById(R.id.ai_btn);
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

        // lambda函数
        aiButton.setOnClickListener(v -> {
            String originalContent = contentEditText.getText().toString().trim();
            if(originalContent.isEmpty()) {
                Toast.makeText(this,"内容不能为空",Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this,"正在对标题为" + originalContent+ "的文章进行AI润色，请稍后",Toast.LENGTH_SHORT).show();
            // 强制转型：父类不能调用子类方法
            Button aiTempButton = (Button)v;
            aiTempButton.setText("润色中...");
            // 禁用防止重复点击
            aiTempButton.setEnabled(false);
            AtomicInteger token = new AtomicInteger(0);
            // 把原来的new Handler(Looper.getMainLooper())换成下面这行
//            aiPolishBtn.postDelayed是View类的通用方法，你传了两个东西：
//            第一个参数：() -> {...} → 要延时执行的 “代码块”（润色逻辑）；
//            第二个参数：500 → 延时时间（毫秒）；
//            系统会自动接管这个代码块，到时间就执行。
//            HandlerCompat.postDelayed(
//                    HandlerCompat.createAsync(Looper.getMainLooper()),
//                    () -> {
//                        String polishedTitle = mockAiPolish(originalTitle);
//                        titleEditText.setText(polishedTitle);
//                        titleEditText.setSelection(polishedTitle.length());
//                        aiTempButton.setText("AI润色");
//                        aiTempButton.setEnabled(true);
//                        Toast.makeText(this, "标题润色完成～", Toast.LENGTH_SHORT).show();
//                    }, token,500
//            );
            callRealAiPolish(originalContent);

        });
    }
    private void callRealAiPolish(String originalContent) {
        // 保持你原有的密钥、Bot ID、接口地址不变
        String ARK_API_KEY = "59627586-0ccc-4480-80de-2eb0eb1d42a1";
        String botId = "bot-20260219215051-8ql4l";
        String aiUrl = "https://ark.cn-beijing.volces.com/api/v3/bots/chat/completions";

        // 保持你原有的提示词不变
        String prompt = "帮我润色日记正文，保留原意，语句更流畅生动，语气更活泼可爱，标点规范，只返回润色后的正文内容，不要多余解释: " + originalContent;

        try {
            Log.d("ddd","在润色");

            // 构建请求体（修复400错误的核心：替换bot_id为model + 新增system角色）
            JSONObject requestData = new JSONObject();
            requestData.put("model", botId); // 替换bot_id为model，匹配官方格式
            JSONArray messages = new JSONArray();

            // 新增官方要求的system角色消息
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are a helpful assistant.");
            messages.put(systemMsg);

            // 保持你原有的user消息逻辑不变
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            requestData.put("messages", messages);

            // 核心修复超时：设置30秒超时时间
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // 连接超时30秒
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)    // 读取超时30秒
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)   // 写入超时30秒
                    .build();

            // 构建请求（修复MediaType兼容问题）
            Request request = new Request.Builder()
                    .url(aiUrl)
                    .addHeader("Authorization", "Bearer " + ARK_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), requestData.toString()))
                    .build();

            // 发送异步请求
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        // 打印超时详情，方便排查
                        Log.e("AI请求失败", "网络错误/超时：" + e.getMessage());
                        Toast.makeText(DiaryEditActivity.this, "网络错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        aiButton.setText("AI润色");
                        aiButton.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                // 解析AI返回结果
                                String aiResponse = response.body().string();
                                JSONObject responseJson = new JSONObject(aiResponse);
                                String polishedContent = responseJson.getJSONArray("choices")
                                        .getJSONObject(0)
                                        .getJSONObject("message")
                                        .getString("content")
                                        .trim();

                                // 回填润色结果到正文输入框
                                contentEditText.setText(polishedContent);
                                contentEditText.setSelection(polishedContent.length());
                                Toast.makeText(DiaryEditActivity.this, "正文润色完成！", Toast.LENGTH_SHORT).show();
                            } else {
                                // 打印400等错误的详细信息，方便定位问题
                                String errorDetail = response.body() != null ? response.body().string() : "无错误信息";
                                Log.e("AI接口错误", "状态码：" + response.code() + "，详情：" + errorDetail);
                                Toast.makeText(DiaryEditActivity.this, "AI接口返回错误：" + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("AI解析失败", "解析结果出错：" + e.getMessage());
                            Toast.makeText(DiaryEditActivity.this, "解析结果失败", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        } finally {
                            // 恢复按钮状态，防止卡死
                            aiButton.setText("AI润色");
                            aiButton.setEnabled(true);
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "请求格式错误", Toast.LENGTH_SHORT).show();
                aiButton.setText("AI润色");
                aiButton.setEnabled(true);
            });
        }
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

    // 模拟AI调用-用于AI调用前的测试
//    private String mockAiPolish(String originalTile) {
//        return "天天开心!" + originalTile;
//    }
}
