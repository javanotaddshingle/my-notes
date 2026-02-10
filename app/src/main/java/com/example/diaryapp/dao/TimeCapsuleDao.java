package com.example.diaryapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diaryapp.models.TimeCapsule;

import java.util.List;

@Dao
public interface TimeCapsuleDao {
    @Insert
    long insert(TimeCapsule timeCapsule);

    @Update
    void update(TimeCapsule timeCapsule);

    @Delete
    void delete(TimeCapsule timeCapsule);

    @Query("SELECT * FROM time_capsules ORDER BY createdAt DESC")
    List<TimeCapsule> getAllTimeCapsules();

    @Query("SELECT * FROM time_capsules WHERE id = :id")
    TimeCapsule getTimeCapsuleById(long id);

    @Query("SELECT * FROM time_capsules WHERE isOpened = 0 AND openAt <= CURRENT_TIMESTAMP")
    List<TimeCapsule> getReadyToOpenTimeCapsules();

    @Query("SELECT * FROM time_capsules WHERE isOpened = 1 ORDER BY openAt DESC")
    List<TimeCapsule> getOpenedTimeCapsules();
}