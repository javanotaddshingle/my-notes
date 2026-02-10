package com.example.diaryapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private int level;
    private int experiencePoints;
    private int experienceToNextLevel;
    private String avatarPath; // 用户头像路径，默认无
    private String backgroundPath; // 背景照片路径，默认无
    private String signature; // 个性签名，默认无

    public User(String name, int level, int experiencePoints, int experienceToNextLevel) {
        this.name = name;
        this.level = level;
        this.experiencePoints = experiencePoints;
        this.experienceToNextLevel = experienceToNextLevel;
        this.avatarPath = null;
        this.backgroundPath = null;
        this.signature = null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    public void setExperienceToNextLevel(int experienceToNextLevel) {
        this.experienceToNextLevel = experienceToNextLevel;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}