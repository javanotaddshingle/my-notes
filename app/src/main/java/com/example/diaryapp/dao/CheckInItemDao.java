package com.example.diaryapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diaryapp.models.CheckInItem;

import java.util.List;

@Dao
public interface CheckInItemDao {
    @Insert
    long insert(CheckInItem checkInItem);

    @Update
    void update(CheckInItem checkInItem);

    @Delete
    void delete(CheckInItem checkInItem);

    @Query("SELECT * FROM check_in_items ORDER BY createdAt DESC")
    List<CheckInItem> getAllCheckInItems();

    @Query("SELECT * FROM check_in_items WHERE id = :id")
    CheckInItem getCheckInItemById(long id);
}