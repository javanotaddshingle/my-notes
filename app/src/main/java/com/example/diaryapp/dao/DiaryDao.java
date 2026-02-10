package com.example.diaryapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.diaryapp.models.Diary;

import java.util.List;

@Dao
public interface DiaryDao {
    @Insert
    long insert(Diary diary);

    @Update
    void update(Diary diary);

    @Delete
    void delete(Diary diary);

    @Query("SELECT * FROM diaries ORDER BY updatedAt DESC")
    List<Diary> getAllDiaries();

    @Query("SELECT * FROM diaries WHERE id = :id")
    Diary getDiaryById(long id);
}