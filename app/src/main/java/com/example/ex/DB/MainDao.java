package com.example.ex.DB;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface MainDao // Direct access object의 약자 = 데이터베이스에서 데이터가 엑세스 하기위한 객체를 의미
{
    @Insert(onConflict = REPLACE)
    void insert(MainData mainData); // 삽입

    @Delete
    void delete(ArrayList<MainData> mainData); //데이터 삭제는 쓰지않고있다...

    @Delete
    void reset(List<MainData> mainData); //삭제

    @Query("SELECT * FROM `table name`")
    List<MainData> getAll(); //조회
}