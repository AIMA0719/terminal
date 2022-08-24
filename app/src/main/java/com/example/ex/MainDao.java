package com.example.ex;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MainDao // Direct access object의 약자 = 데이터베이스에서 데이터가 엑세스 하기위한 객체를 의미
{
    @Insert(onConflict = REPLACE)
    void insert(MainData mainData); // 삽입

    @Delete
    void delete(MainData mainData); //데이터 삭제는 쓰지않고있다...

    @Delete
    void reset(List<MainData> mainData); //삭제?

    @Query("SELECT * FROM `table name`") //getAll() 메소드는 Contact 리스트를 LiveData 형태로 반환합니다. LiveData는 데이터의 상태를 감시하며, 데이터가 변경되면 연결된 Listener에 알림을 보내 UI가 변경될 수 있도록 하는 기능을 제공한다.
    List<MainData> getAll(); //조회
}