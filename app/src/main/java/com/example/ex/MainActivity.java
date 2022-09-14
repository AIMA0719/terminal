package com.example.ex;

import static com.example.ex.BluetoothFragment.mBluetoothHandler;
import static com.example.ex.BluetoothFragment.mBluetoothSocket;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ex.DB.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "activity_main";

    public static EditText editText;
    Button btAdd, btReset;
    RecyclerView recyclerView;
    List<MainData> dataList = new ArrayList<>();
    RoomDB database;
    MainAdapter adapter;
    TextView bluetooth_status, list_item;
    ConnectedThread mConnectedThread;
    BluetoothFragment bluetoothFragment;

    String readmessage = ""; // 핸들러로 받은 메세지 저장

    // --------

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // findViewByID 부분
        list_item = findViewById(R.id.text_view);
        editText = findViewById(R.id.command_write);
        btAdd = findViewById(R.id.send_message);
        btReset = findViewById(R.id.clear_message);
        recyclerView = findViewById(R.id.recycler_view);
        bluetooth_status = findViewById(R.id.mbluetooth_status);
        // findViewByID 부분

        database = RoomDB.getInstance(this); // 룸디비 가져옴
        //dataList = database.mainDao().getAll(); //리스트 만듦 리스트 디비에 있는거 얘가 보여주는거
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAdapter(MainActivity.this, dataList); //어댑터 만듦
        recyclerView.setAdapter(adapter); // 어댑터 설정

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); //  화면 특정방향 고정

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//장치가 블루투스 기능을 지원하는지 알아옴

        // -------- 툴바 관려된 설정
        Toolbar toolbar = findViewById(R.id.toolbar); //툴바 아이디 가져오기
        setSupportActionBar(toolbar); //툴바 소환
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false); // title 가시 여부
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // 타이틀 왼쪽 3단 메뉴 버튼일단 false 로
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24); // 버튼 아이콘 변경
        Objects.requireNonNull(toolbar.getOverflowIcon()).setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP); // three dots 색상 변경
        // --------

        // -------- 리스트 데이터베이스 리셋하고, 리스트 클리어하고 갱신 해야 빈 화면 볼 수 있음
        //database.mainDao().reset(dataList); //리스트 DB 삭제
        //dataList.clear(); // 리스트 클리어
        //adapter.notifyDataSetChanged(); //갱신

        // --------

        Intent intent = getIntent(); // device.getName 가져옴
        if (intent != null) {
            String data = intent.getStringExtra("데이터");
            Log.d(TAG, "onCreate: " + data);
            if ( data != null){
                bluetooth_status.setText(data+" 기기랑 연결 상태입니다.");
            }
            else{
                bluetooth_status.setText("기기랑 연결되어 있지 않습니다.");
            }
        }

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int permission4 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);

        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED ||
                permission3 == PackageManager.PERMISSION_DENIED || permission4 == PackageManager.PERMISSION_DENIED) {  // 권한이 열려있는지 확인
            try {
                requestPermissions(new String[]{ //위치권한요청
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN}, 1);

            } catch (Exception e) {
                Log.d(TAG, "권한 오류", e);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.BLUETOOTH_CONNECT

                    },
                    1);
        } else {
            requestPermissions(
                    new String[]{
                            Manifest.permission.BLUETOOTH
                    },
                    1);
        }

        mBluetoothHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) { // 메시지 종류에 따라서
                if (msg.what == BluetoothFragment.BT_MESSAGE_READ) {

                    //String a = new String((byte[]) msg.obj, StandardCharsets.UTF_8).trim();
                    //a = a.replace("null","");
                    //a = a.replaceAll("\n","");

                    Log.d(TAG, "메인엑티비티 에서 받은 데이터 : "+msg.obj);
                    readmessage += msg.obj;
                    editText.setText("");

                    Log.d(TAG, "받은 메세지 : " + readmessage);

                    if(readmessage.contains(">")){
                        MainData data1 = new MainData();
                        data1.setText(readmessage); // readmessage = 010d41 0D 5E 41 0D5E 41 0D 5E> 이여서 > 일때 여기로 들어오긴 함
                        Log.d(TAG, "요기로 들어왔나요?");
                        dataList.add(data1); //근데 리스트뷰에 안 나옴
                        adapter.notifyDataSetChanged();
                    }else {
                        Log.d(TAG, "안 들어왔나요?");
                    }
                    Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌

                    //dataList.clear(); //리스트 초기화
                    //dataList.addAll(database.mainDao().getAll()); // database.mainDao().getAll() = DB안에 있는 모든 정보를 List 형태로 불러온다.
                    readmessage = ""; // 초기화 시켜줌
                }

                if (msg.what == BluetoothFragment.BT_CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        String[] name = msg.obj.toString().split("\n");
                        Toast.makeText(getApplicationContext(), name[0]+" 와 연결 되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "블루투스 연결에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                if(msg.what== BluetoothFragment.BT_MESSAGE_WRITE){
                    Log.d(TAG, "write 함");

                }
            }
        }; // 핸들러 ( What에 따라 동작 )


        btAdd.setOnClickListener(v -> {
            String sText = editText.getText().toString().trim();
            if (!sText.equals("")) {
                if (BluetoothFragment.device != null) { //연결 되어있는 상태라면
                    sText = sText+"\r";
                    BluetoothFragment.mConnectedThread.write(sText);
//
                    MainData data = new MainData();
                    data.setText(sText);
//                    database.mainDao().insert(data);

                    editText.setText("");

                    Log.d(TAG, "Request 메세지 : " + sText);

//                    dataList.clear(); //리스트 초기화
                    dataList.add(data);
                    adapter.notifyDataSetChanged(); //갱신

                    Log.d(TAG, "보내기 버튼 누른 후 MainRecyclerview : "+dataList);
                    Log.d(TAG, "보내기 버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());

                    Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌

                } else { //기기랑 연결 안 되어있는 상태
                    MainData data = new MainData();
                    data.setText(sText);
//                    database.mainDao().insert(data);

                    editText.setText("");

                    Log.d(TAG, "TX : " + sText);

//                    dataList.clear(); //리스트 초기화
//                    dataList.addAll(database.mainDao().getAll()); //add
                    dataList.add(data);
                    adapter.notifyDataSetChanged(); //갱신

                    Log.d(TAG, "보내기 버튼 누른 후 MainRecyclerview : "+dataList);
                    Log.d(TAG, "보내기 버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());

                    Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    Log.d(TAG, "onCreate: 블루투스 기기랑 연결이 안 되어있는 상태입니다.");
                }

            }
            //Log.d(TAG,"현재 포커스 : " + getCurrentFocus());
            Log.d(TAG, "명령어를 입력한 횟수 " + dataList.size());
        }); // -------- 보내기 버튼 클릭 이벤트

        btReset.setOnClickListener(v -> { // Terminal clear 버튼눌렀을때 동작
            database.mainDao().reset(dataList);
            database.mainDao().delete((ArrayList<MainData>) dataList);
            dataList.removeAll(dataList);
            dataList.clear();

            Log.d(TAG, "리셋버튼 누른 후 MainRecyclerview : "+dataList);
            Log.d(TAG, "리셋버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());

            //dataList.addAll(database.mainDao().getAll());
            adapter.notifyDataSetChanged(); // 리사이클러뷰의 리스트를 업데이트 하는 함수중 하난데 리스트의 크기와 아이템이 둘 다 변경되는 경우 사용
            //초보들이 젤 쓰기 편해서 많이 쓰는데 퍼포먼스적으론 최적화 못할 가능성 높다
            Toast.makeText(this, "창을 클리어 했습니다.", Toast.LENGTH_SHORT).show();
        }); // -------- Window clear 버튼 클릭이벤트

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) { // 권한 요청 이후 로직 앱 시작하면 권한 체크 한다
        if (requestCode == 1) {
            boolean check_result = false;  //  권한 체크 했니?


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) { //SDK 31 미만이면 블루투스 스캔 권한 없어도 됨.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    check_result = true;
                }
            } else { //31 이상이면
                for (int result : grandResults) { // 모든 퍼미션을 허용했는지 체크
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        check_result = true; // 체크 됐으면 false
                        break;
                    }
                }
            }

            if (check_result) { // 권한 체크 됐을때
                //Toast.makeText(this, "위치 권한 확인 되었습니다.", Toast.LENGTH_SHORT).show();
            } else //권한 체크 안 됐을때
                Toast.makeText(this, "설정에서 권한을 허용 해주세요. ", Toast.LENGTH_SHORT).show();// 권한 허용 해줘
        }
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
    }// -------- 앱 시작하면 권한이 있는지 없는지 체크하는 메소드 ( 현재 위치권한 확인 체크된다)

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    } // -------- Toolbar 에 menu.xml을 inflate 함 =  메뉴에있는 UI 들 객체화해서 쓸 수 있게한다? 로 이해함

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.blutooth_connect:
                try {
//                        Intent intent = new Intent(this, bluetooth.class);
//                        startActivity(intent);
                    FragmentView(); // 프래그먼트로 이동하는 코드

                    FragmentManager fm1 = getSupportFragmentManager();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.FirstFragment, bluetoothFragment);
                    ft1.commit();
                } catch (Exception e) {
                    Log.d(TAG, "Error :" + e);
                }
            case R.id.log_load:  //
                if (bluetoothAdapter != null) {
                    if (!bluetoothAdapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            // 로그버튼 클릭
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "블루투스 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Bluetooth's is null");
                }
                return true;

            case R.id.dashboard: //대쉬보드 클리
                Toast.makeText(getApplicationContext(), "대쉬보드", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return onOptionsItemSelected(item);
        }
    } // -------- 메뉴 (Three dots) 버튼 클릭 이벤트

    private void FragmentView() {

        bluetoothFragment = new BluetoothFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.FirstFragment, bluetoothFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
