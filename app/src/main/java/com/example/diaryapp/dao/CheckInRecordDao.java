package com.example.diaryapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diaryapp.models.CheckInRecord;

import java.util.Date;
import java.util.List;

@Dao
public interface CheckInRecordDao {
    @Insert
    long insert(CheckInRecord checkInRecord);

    @Update
    void update(CheckInRecord checkInRecord);

    @Delete
    void delete(CheckInRecord checkInRecord);

    @Query("SELECT * FROM check_in_records WHERE itemId = :itemId AND checkInDate = :date")
    CheckInRecord getCheckInRecordByItemAndDate(long itemId, Date date);

    @Query("SELECT * FROM check_in_records WHERE checkInDate = :date ORDER BY completedAt DESC")
    List<CheckInRecord> getCheckInRecordsByDate(Date date);

    @Query("SELECT * FROM check_in_records WHERE itemId = :itemId ORDER BY checkInDate DESC")
    List<CheckInRecord> getCheckInRecordsByItemId(long itemId);

    @Query("SELECT * FROM check_in_records ORDER BY checkInDate DESC")
    List<CheckInRecord> getAllRecords();


//    之前的getAllRecords()方法没有对打卡记录的日记进行去重，新增对日期去重的计算逻辑
    @Query("SELECT COUNT(DISTINCT date(checkInDate / 1000, 'unixepoch')) " +
            "FROM check_in_records " +
            "WHERE checkInDate >= :startOfMonth AND checkInDate <= :endOfMonth")
    int getMonthCheckInDaysCount(long startOfMonth, long endOfMonth);
}
