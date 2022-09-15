package com.example.ex.RoomDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MainData.class}, version = 1, exportSchema = false) //각각 Entity 클래스 배열, 버전 정수 값, 스키마를 폴더에 내보낼지 bool값 의미
public abstract class RoomDB extends RoomDatabase // 룸을 초기화할때 필요한 RoomDB 클래스 정의 , 구글 검색으로 찾아냈다 자세한 분석 필요
{// RoomDatabase는 DAO를 통해 SQLite DB에 접속하며, 데이터를 제어할 수 있는 권한을 갖습니다.
    private static RoomDB database;

    private static String DATABASE_NAME = "database";

    public synchronized static RoomDB getInstance(Context context)
    {
        if (database == null)
        {
            database = Room.databaseBuilder(context.getApplicationContext(), RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract MainDao mainDao();

}
