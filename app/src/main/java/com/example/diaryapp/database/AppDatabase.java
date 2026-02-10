package com.example.diaryapp.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Context;

import com.example.diaryapp.dao.CheckInItemDao;
import com.example.diaryapp.dao.CheckInRecordDao;
import com.example.diaryapp.dao.DiaryDao;
import com.example.diaryapp.dao.TimeCapsuleDao;
import com.example.diaryapp.dao.UserDao;
import com.example.diaryapp.models.CheckInItem;
import com.example.diaryapp.models.CheckInRecord;
import com.example.diaryapp.models.Diary;
import com.example.diaryapp.models.TimeCapsule;
import com.example.diaryapp.models.User;
import androidx.room.TypeConverters;

@Database(entities = {
        Diary.class,
        CheckInItem.class,
        CheckInRecord.class,
        TimeCapsule.class,
        User.class
}, version = 2, exportSchema = false)
@TypeConverters({DateTypeConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "diary_app_db";
    private static volatile AppDatabase instance;

    public abstract DiaryDao diaryDao();
    public abstract CheckInItemDao checkInItemDao();
    public abstract CheckInRecordDao checkInRecordDao();
    public abstract TimeCapsuleDao timeCapsuleDao();
    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}