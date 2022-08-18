package com.example.ex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int BLUETOOTH_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //  화면 특정방향 고정?


        Toolbar toolbar = findViewById(R.id.toolbar); //툴바 아이디 가져오기
        setSupportActionBar(toolbar); //툴바 소환
        getSupportActionBar().setDisplayShowTitleEnabled(false); // title 가시 여부
//      getSupportActionBar().setTitle("Mini Terminal program"); // toolbar.setTitle("이거아니라네유");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 타이틀 왼쪽에 버튼 일단 false 로
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); // 버튼 아이콘 변경

//------------------------------------------------------------------------------------------------------------------------------권한 요청 좀 뻘짓 한거같다. 일단 냅둠

//        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION); // Manifest 에서 권한ID 가져오기
//        int permission2 = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
//        int permission3 = ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_ADMIN);
//
//        int permission4 = ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH);
//
//        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED){  // 권한이 열려있는지 확인
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ // 빌드 버젼이 마쉬멜로우 이상부터 권한 물어본다.
//                requestPermissions(new String[]{
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.ACCESS_FINE_LOCATION},1000);// 이거 권한요청 뜨긴했는데 coarse랑 fine 다 된건지 모르겠습니다..
//                }
//                return;
//            }
//
//
//
//        }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) { // 권한 요청 이후 로직
//        if(requestCode == 1000){
//            boolean check_result = true;  //  location 권한 체크 결과를 불러온다.
//
//            for ( int result : grandResults){ // 모든 퍼미션을 허용했는지 체크
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    check_result = false;
//                    break;
//                }
//            }
//            if(check_result = true){
//                Toast.makeText(this, "권한 확인 되었습니다.", Toast.LENGTH_SHORT).show();
//            }
//            else Toast.makeText(this, "설정에서 권한을 허용 해주세요 ㅠㅠ ", Toast.LENGTH_SHORT).show();// 권한 허용 해줘
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grandResults); // 이건 왜있는지 모르겠음
    }
    //---------------------------------------------------------------------------------------------------------------

    public void checkPermission(String permission,int requestCode){
        if(ContextCompat.checkSelfPermission(MainActivity.this,permission)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{permission},requestCode);
        }
        else{
            Toast.makeText(this, "Permission already Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);

        if(requestCode == 100){
            if(grandResults.length>0 && grandResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "bluetooth permission ok bro", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "nope bro", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 101){
            if(grandResults.length>0 && grandResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "bluetooth permission ok bro", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "nope bro", Toast.LENGTH_SHORT).show();
            }
        }
    }
//------------------------------------------------------------------------------------------------------------------------------권한 요청 좀 뻘짓 한거같다. 일단 냅둠
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
                Toast.makeText(getApplicationContext(), "블루투스 연결", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.bluetooth_disconnection:
                Toast.makeText(getApplicationContext(), "블루투스 해제", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return onOptionsItemSelected(item);
        }
    }
}