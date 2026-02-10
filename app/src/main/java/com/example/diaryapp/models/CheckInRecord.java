package com.example.diaryapp.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "check_in_records")
public class CheckInRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long itemId;
    private Date checkInDate;
    private Date completedAt;

    public CheckInRecord(long itemId, Date checkInDate, Date completedAt) {
        this.itemId = itemId;
        this.checkInDate = checkInDate;
        this.completedAt = completedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
}