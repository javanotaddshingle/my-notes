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

    // ========== 1. 修复：本月打卡天数统计（加+8小时偏移） ==========
    @Query("SELECT COUNT(DISTINCT date(checkInDate / 1000, 'unixepoch', '+8 hours')) " +
            "FROM check_in_records " +
            "WHERE checkInDate >= :startOfMonth AND checkInDate <= :endOfMonth")
    int getMonthCheckInDaysCount(long startOfMonth, long endOfMonth);

    // ========== 2. 修复：查询去重日期（加+8小时偏移） ==========
    @Query("SELECT DISTINCT date(checkInDate / 1000, 'unixepoch', '+8 hours') AS check_date FROM check_in_records")
    List<String> getAllCheckInDates();

    // 新增：清空打卡记录表
    @Query("DELETE FROM check_in_records")
    void clearAllCheckInRecords();
}