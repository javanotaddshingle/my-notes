package com.example.diaryapp.managers;

import android.content.Context;
import com.example.diaryapp.database.AppDatabase;
import com.example.diaryapp.models.User;

public class UserManager {

    private static UserManager instance;
    private AppDatabase database;

    private UserManager(Context context) {
        database = AppDatabase.getInstance(context);
        initializeUser();
    }

    public static synchronized UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context.getApplicationContext());
        }
        return instance;
    }

    private void initializeUser() {
        // 异步执行初始化，避免在主线程中执行数据库操作
        new Thread(() -> {
            User user = database.userDao().getUser();
            if (user == null) {
                // 创建默认用户
                User newUser = new User("用户", 1, 0, 100);
                database.userDao().insert(newUser);
            }
        }).start();
    }

    public void addExperiencePoints(int points) {
        new Thread(() -> {
            User user = database.userDao().getUser();
            if (user != null) {
                int newExperience = user.getExperiencePoints() + points;
                user.setExperiencePoints(newExperience);

                // 检查是否升级
                while (newExperience >= user.getExperienceToNextLevel()) {
                    newExperience -= user.getExperienceToNextLevel();
                    user.setLevel(user.getLevel() + 1);
                    user.setExperiencePoints(newExperience);
                    user.setExperienceToNextLevel(calculateExperienceToNextLevel(user.getLevel()));
                }

                database.userDao().update(user);
            }
        }).start();
    }

    public User getUser() {
        return database.userDao().getUser();
    }

    public boolean isFeatureUnlocked(int featureLevel) {
        User user = database.userDao().getUser();
        return user != null && user.getLevel() >= featureLevel;
    }

    public void updateUser(User user) {
        new Thread(() -> {
            database.userDao().update(user);
        }).start();
    }

    public void updateUserName(String name) {
        new Thread(() -> {
            User user = database.userDao().getUser();
            if (user != null) {
                user.setName(name);
                database.userDao().update(user);
            }
        }).start();
    }

    public void updateUserSignature(String signature) {
        new Thread(() -> {
            User user = database.userDao().getUser();
            if (user != null) {
                user.setSignature(signature);
                database.userDao().update(user);
            }
        }).start();
    }

    public void updateUserAvatar(String avatarPath) {
        new Thread(() -> {
            User user = database.userDao().getUser();
            if (user != null) {
                user.setAvatarPath(avatarPath);
                database.userDao().update(user);
            }
        }).start();
    }

    public void updateUserBackground(String backgroundPath) {
        new Thread(() -> {
            User user = database.userDao().getUser();
            if (user != null) {
                user.setBackgroundPath(backgroundPath);
                database.userDao().update(user);
            }
        }).start();
    }

    private int calculateExperienceToNextLevel(int currentLevel) {
        // 简单的经验值计算公式：每级所需经验值 = 基础值 * 等级系数
        return 100 * (currentLevel + 1);
    }

    // 功能解锁等级常量
    public static final int FEATURE_AI_BEAUTIFY = 5; // AI美化功能解锁等级
    public static final int FEATURE_ADVANCED_THEMES = 10; // 高级主题解锁等级
    public static final int FEATURE_CUSTOM_CHECK_IN = 3; // 自定义打卡项解锁等级
}
