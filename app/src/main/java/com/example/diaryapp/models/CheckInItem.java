package com.example.diaryapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "check_in_items")
public class CheckInItem {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private Date createdAt;
    private int experiencePoints;

    public CheckInItem(String name, Date createdAt, int experiencePoints) {
        this.name = name;
        this.createdAt = createdAt;
        this.experiencePoints = experiencePoints;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }
}