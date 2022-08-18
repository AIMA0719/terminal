package com.example.ex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //  화면 특정방향 고정?

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); //툴바 소환
        getSupportActionBar().setDisplayShowTitleEnabled(false); // title 가시 여부
//        getSupportActionBar().setTitle("Mini Terminal program"); // toolbar.setTitle("이거아니라네유");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 타이틀 왼쪽에 버튼 일단 false 로
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); // 버튼 아이콘 변경

    }
    @Override // Toolbar 에 menu.xml을 inflate 함
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "환경설정 클릭됨", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.bluetooth_connection:
                Toast.makeText(this, "블루투스 연결", Toast.LENGTH_SHORT).show();
                return onOptionsItemSelected(item);
            case R.id.bluetooth_disconnection:
                Toast.makeText(this, "블루투스 해제", Toast.LENGTH_SHORT).show();
                return onOptionsItemSelected(item);
            default:
                return onOptionsItemSelected(item);
        }
    }
}