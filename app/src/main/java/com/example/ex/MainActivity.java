package com.example.ex;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> resultLauncher; // 클래스내에 선언 해주래  startactivityForresult 대신 쓰는거..
    public BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "activity_main";

    TextView bluetooth_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //  화면 특정방향 고정?

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetooth_status = (TextView) findViewById(R.id.mbluetooth_status);

        Toolbar toolbar = findViewById(R.id.toolbar); //툴바 아이디 가져오기
        setSupportActionBar(toolbar); //툴바 소환
        getSupportActionBar().setDisplayShowTitleEnabled(false); // title 가시 여부
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 타이틀 왼쪽에 버튼 일단 false 로
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); // 버튼 아이콘 변경
//      getSupportActionBar().setTitle("Mini Terminal program"); // toolbar.setTitle("이거아니라네유");

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION); // Manifest 에서 권한ID 가져오기
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int permission4 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);

        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED ||
                permission3 == PackageManager.PERMISSION_DENIED || permission4 == PackageManager.PERMISSION_DENIED) {  // 권한이 열려있는지 확인
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 빌드 버젼이 마쉬멜로우 이상부터 권한 물어본다.
                    requestPermissions(new String[]{ //위치권한요청
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 1);// 이거 권한요청 뜨긴했는데 coarse랑 fine 다 된건지 모르겠습니다..
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 블루투스 권한 S이상부터 물어본다...
                    requestPermissions(new String[]{ //블루투스 요청
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN}, 2);
                }
            } catch (Exception e) {
                Log.d(TAG, "모르겠다~" + e);
                return;
            }

        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) { // 권한 요청 이후 로직 앱 시작하면 권한 체크 한다
//        if (requestCode == 1) {
//            boolean check_result = true;  //  location 권한 체크 결과를 불러온다.
//
//            for (int result : grandResults) { // 모든 퍼미션을 허용했는지 체크
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    check_result = false; // check_result 왜 안쓰는지 모르겠다..
//                    break;
//                }
//            }
//            if (check_result = true) {
//                Toast.makeText(this, "위치 권한 확인 되었습니다.", Toast.LENGTH_SHORT).show();
//            } else
//                Toast.makeText(this, "설정에서 권한을 허용 해주세요 ㅠㅠ ", Toast.LENGTH_SHORT).show();// 권한 허용 해줘
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grandResults); // 이건 왜있는지 모르겠음
//    }

    @Override // Toolbar 에 menu.xml을 inflate 함 =  메뉴에있는 UI 들 객체화해서 쓸 수 있게한다? 로 이해함
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // 메뉴가 선택되면 동작하는 로직들
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "환경설정 클릭됨", Toast.LENGTH_SHORT).show(); // 환경설정 일단 만들어놓음
                return true;
//            case R.id.bluetooth_connection:  // 블루투스 연결 클릭 listen
//                if (bluetoothAdapter != null) { // 블루투스가 사용 가능 유무 체크... 근데 안된다 ㅠ
//
//                    if (!bluetoothAdapter.isEnabled()) {
//                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                            Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//
//                            bluetooth_status.setText("연결됨");
//                        }
//                    }
//                }
//                else {
//                    Log.d(TAG,"null?");
//                }
//                return true;

            case R.id.bluetooth_disconnection: //블루투스 해제 클릭  listen
                Toast.makeText(getApplicationContext(), "블루투스 해제", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return onOptionsItemSelected(item);
        }
    }

//    void bluetoothOn() {
//        if (bluetoothAdapter == null) { //장치가 블루투스 지원x
//            Toast.makeText(this, "블루투스 지원기기가 아닙니다.", Toast.LENGTH_SHORT).show();
//        } else {
//            if (bluetoothAdapter.isEnabled()) { //불루투스 지원하지만 비활성화면 활성으로 바꾸기위해 동의 요청
//                Toast.makeText(this, "블루투스 활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
//                bluetooth_status.setText("connected");
//            } else { // 지원하고 활성 상태이면 기기 목록 보여주기기                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { // 아마 블루투스 권한 체크해주는 코드 같다..
//                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                }
//            }
//        }
//
}
