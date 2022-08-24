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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> resultLauncher; // 클래스내에 선언 해주래  startactivityForresult 대신 쓰는거..
    public BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "activity_main";

    // 선언 Area
    EditText editText;
    Button btAdd, btReset;
    RecyclerView recyclerView;
    List<MainData> dataList = new ArrayList<>();
    RoomDB database;
    MainAdapter adapter;
    TextView bluetooth_status;
    // 선언 Area

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 아디 묶어주는 Area
        editText = findViewById(R.id.command_write);
        btAdd = findViewById(R.id.send_message);
        btReset = findViewById(R.id.clear_message);
        recyclerView = findViewById(R.id.recycler_view);
        bluetooth_status = (TextView) findViewById(R.id.mbluetooth_status);
        // 아디 묶어주는 Area


        database = RoomDB.getInstance(this); // 룸디비 가져옴
        dataList = database.mainDao().getAll(); //리스트 만듦 리스트 디비에 있는거 얘가 보여주는거
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); //?
        adapter = new MainAdapter(MainActivity.this, dataList); //어댑터 만듦
        recyclerView.setAdapter(adapter); // 어댑터 설정

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //  화면 특정방향 고정?
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 블루투스 어댑터

        // -------- 툴바 관려된 설정
        Toolbar toolbar = findViewById(R.id.toolbar); //툴바 아이디 가져오기
        setSupportActionBar(toolbar); //툴바 소환
        getSupportActionBar().setDisplayShowTitleEnabled(false); // title 가시 여부
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // 타이틀 왼쪽 3단 메뉴 버튼일단 false 로
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); // 버튼 아이콘 변경
        // --------

        // -------- 권한 설정
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION); // Manifest 에서 권한ID 가져오기
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int permission4 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        // --------

        final int[] count = {0}; // 뷰에 저장된 명령어 개수

        // -------- 리스트 데이터베이스 리셋하고, 리스트 클리어하고 갱신 해야 빈 화면 볼 수 있음
        database.mainDao().reset(dataList); //리스트 DB 삭제
        dataList.clear(); // 리스트 클리어
        adapter.notifyDataSetChanged(); //갱신
        // --------

        // -------- 보내기 버튼 클릭 이벤트
        btAdd.setOnClickListener(v -> {
            String sText = editText.getText().toString().trim();
            if (!sText.equals(""))
            {
                MainData data = new MainData();
                data.setText(sText);
                database.mainDao().insert(data);

                editText.setText("");

                dataList.clear(); //리스트 초기화
                dataList.addAll(database.mainDao().getAll()); //add
                adapter.notifyDataSetChanged(); //갱신
                count[0] = count[0] + 1;
            }
            Log.d(TAG,"명령어를 입력한 횟수" + count[0]);
        });
        // --------

        // -------- Window clear 버튼 클릭이벤트
        btReset.setOnClickListener(v -> { // Terminal clear 버튼눌렀을때 동작
            database.mainDao().reset(dataList);

            dataList.clear();
            dataList.addAll(database.mainDao().getAll());
            adapter.notifyDataSetChanged(); // 리사이클러뷰의 리스트를 업데이트 하는 함수중 하난데 리스트의 크기와 아이템이 둘 다 변경되는 경우 사용
            //초보들이 젤 쓰기 편해서 많이 쓰는데 퍼포먼스적으론 최적화 못할 가능성 높다
        });
        // --------

        // -------- 메뉴 (Three dots) 버튼 클릭 이벤트
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
                Log.d(TAG, "모르겠다~" , e);
                return;
            }

        }
    }

    // -------- 앱 시작하면 권한이 있는지 없는지 체크하는 메소드 ( 현재 위치권한 확인 체크된다)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) { // 권한 요청 이후 로직 앱 시작하면 권한 체크 한다
        if (requestCode == 1) {
            boolean check_result = true;  //  location 권한 체크 결과를 불러온다.

            for (int result : grandResults) { // 모든 퍼미션을 허용했는지 체크
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false; // check_result 왜 안쓰는지 모르겠다..
                    break;
                }
            }
            if (check_result = true) {
                //Toast.makeText(this, "위치 권한 확인 되었습니다.", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "설정에서 권한을 허용 해주세요 ㅠㅠ ", Toast.LENGTH_SHORT).show();// 권한 허용 해줘
        }
        super.onRequestPermissionsResult(requestCode, permissions, grandResults); // 이건 왜있는지 모르겠음
    }
    // --------

    // -------- Toolbar 에 menu.xml을 inflate 함 =  메뉴에있는 UI 들 객체화해서 쓸 수 있게한다? 로 이해함
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    // --------

    // -------- 메뉴 (Three dots) 버튼 클릭 이벤트
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.blutooth_connect:
                Toast.makeText(this, "블루투스 페이지 선택", Toast.LENGTH_SHORT).show(); // 환경설정 일단 만들어놓음
                return true;

            case R.id.log_load:  // 블루투스 연결 클릭 listen
                if (bluetoothAdapter != null) {

                    if (!bluetoothAdapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                            bluetooth_status.setText("연결됨");
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "블루투스 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Bluetoothadapter is null");
                }
                return true;

            case R.id.dashboard: //블루투스 해제 클릭  listen
                Toast.makeText(getApplicationContext(), "대쉬보드", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return onOptionsItemSelected(item);
        }
    }
    // --------




}
