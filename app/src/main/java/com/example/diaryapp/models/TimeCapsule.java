package com.example.diaryapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "time_capsules")
public class TimeCapsule {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String content;
    private Date createdAt;
    private Date openAt;
    private boolean isOpened;
    private int experiencePoints;

    public TimeCapsule(String title, String content, Date createdAt, Date openAt, boolean isOpened, int experiencePoints) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.openAt = openAt;
        this.isOpened = isOpened;
        this.experiencePoints = experiencePoints;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getOpenAt() {
        return openAt;
    }

    public void setOpenAt(Date openAt) {
        this.openAt = openAt;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }
}