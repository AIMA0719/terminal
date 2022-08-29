package com.example.ex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class bluetooth extends AppCompatActivity {

    TextView bluetooth_status, listtxt;
    Button bluetooth_on, bluetooth_off, bluetooth_scan;
    final String TAG = "bluetooth_activity";

    //----- 블루투스 위한
    protected static UUID MY_UUID; // UUID는 Universally Unique IDentifier로 SDP에서 서비스의 종류를구분하기 위한 128비트 포맷의 표준화 된 문자열 ID입니다.
    int REQUEST_ENABLE_BT = 1; // 요청 코드
    int REQUEST_LOACTION = 2;

    RecyclerView listView_pairing, listView_scan; //
    private BluetoothAdapter mBluetoothAdapter; //블루투스 어댑터 선언
    private Set<BluetoothDevice> pairedDevice;

    //-----


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_connect);

        Toolbar toolbar = findViewById(R.id.toolbar); // 묶어주자... 이거때매 자꾸 팅겼다..
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("블루투스 연결 페이지");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 개같은뒤로가기 이거 툴바 아이디 안묶어주면 계속 오류났었다

        bluetooth_on = findViewById(R.id.bluetooth_on);
        bluetooth_off = findViewById(R.id.bluetooth_off);
        bluetooth_scan = findViewById(R.id.bluetooth_scan);
        bluetooth_status = findViewById(R.id.bluetooth_status2);
        listView_pairing = findViewById(R.id.pairing_list);
        listView_scan = findViewById(R.id.scan_list);
        listtxt = findViewById(R.id.txtName);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터
        MY_UUID = UUID.randomUUID(); //????????????

        //------------------------- 블루투스 연결 상태 보여주는 Text 설정

        if (mBluetoothAdapter.isEnabled()) {
            bluetooth_status.setText("블루투스 연결 상태입니다.");
        } else {
            bluetooth_status.setText("블루투스 비연결 상태입니다.");
        }

        //-------------------------

        ActivityCompat.requestPermissions(bluetooth.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOACTION); // 위치권한 사용자에게 요청

        //------------------------- 등록된 디바이스 보여주는 Area

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); // list_scan부분 레이아웃 선언
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); // list_scan 사용하기 위한 부분
        listView_pairing.setLayoutManager(linearLayoutManager);
        listView_scan.setLayoutManager(linearLayoutManager2);

        CustomAdapter adapter = new CustomAdapter(getApplicationContext()); //어댑터 만듦 , 얘가 스캔
        CustomAdapter adapter2 = new CustomAdapter(getApplicationContext()); //얘가 등록된 디바이스


        //list_paired 부분 등록된 디바이스 있는지 체크하고 뷰에 보여주는 함수

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED) {
            pairedDevice = mBluetoothAdapter.getBondedDevices();
            if (pairedDevice.size() > 0) { // 디바이스가 있으면
                for (BluetoothDevice bt : pairedDevice) { // 체크해서 list에 add 해줘가지고 listview에 나타냄..
                    adapter2.addItem(new Customer(bt.getName(), bt.getAddress())); // 이름이랑 주소 나타냄
                    listView_pairing.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged(); // 어댑터 항목에 변화가 있음을 알려줌
                }
            } else {
                Toast.makeText(this, "등록된 디바이스가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            pairedDevice = mBluetoothAdapter.getBondedDevices();
            if (pairedDevice.size() > 0) { // 디바이스가 있으면
                for (BluetoothDevice bt : pairedDevice) { // 체크해서 list에 add 해줘가지고 listview에 나타냄..
                    adapter2.addItem(new Customer(bt.getName(), bt.getAddress())); // 이름이랑 주소 나타냄
                    listView_pairing.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(this, "등록된 디바이스 보여주기 오류남", Toast.LENGTH_SHORT).show();
            }
        }


        //------------------------- 버튼 클릭 이벤트

        bluetooth_on.setOnClickListener(v -> { // 블루투스 on 클릭 이벤트
            if (mBluetoothAdapter != null) { //블루투스 지원안하면.. 근데 그럴일은 요즘 없지않나
                if (!mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // 정신병 걸릴뻔 했다 샤오미는 랑 z플립 반대로 작동해서 이것도 버젼대로 해야함 P sdk 이하면 퍼미션 없이 작동하고 이상이면 퍼미션 필요
                        Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //OFF도 똑같은 방식으로 sdk에 따라 나눠야함
                        startActivityForResult(blue, REQUEST_ENABLE_BT);  //이거 고쳐야함 일단 패스
                        bluetooth_status.setText("블루투스 연결 상태입니다.");
                    } else {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        } else {
                            Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(blue, REQUEST_ENABLE_BT);  //이거 고쳐야함 일단 패스
                            bluetooth_status.setText("블루투스 연결 상태입니다.");
                        }
                    }
                } else {
                    Toast.makeText(this, "이미 활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "블루투스 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Bluetoothadapter is null");
            }
        });

        //-------------------------

        bluetooth_off.setOnClickListener(v -> { //블루투스 off 클릭 이벤트
            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    mBluetoothAdapter.disable(); //블루투스 비활성화
                    Toast.makeText(this, "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                    bluetooth_status.setText("블루투스 비연결 상태입니다.");
                } else {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { //권한 체크해주고
                        Toast.makeText(this, "블루투스 권한이 없습니다.", Toast.LENGTH_SHORT).show();

                    } else {
                        mBluetoothAdapter.disable(); //블루투스 비활성화
                        Toast.makeText(this, "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                        bluetooth_status.setText("블루투스 비연결 상태입니다.");
                    }
                }
            } else {
                Toast.makeText(this, "이미 비활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //-----------------------

        //----------------------- 브로드캐스트 리시버 정의 // device 스캔 작동 부분

        final BroadcastReceiver mDeviceDiscoverReceiver = new BroadcastReceiver() {
            int cnt = 0;

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // SDK가 31 이상일때
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(context, "권한이 없습니다..", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            if (device.getName() != null) {
                                adapter.addItem(new Customer(device.getName(), device.getAddress())); // 리사이클러뷰에 이름이랑 맥주소 추가
                                adapter.notifyDataSetChanged(); //갱신
                                cnt += 1;
                            }
                        }
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        Log.v(TAG, "총 찾은 디바이스 개수 : " + cnt); // 블루투스가 꺼질때 동작한다.
                    } else {
                        //Log.d(TAG, "onReceive: "+action);
                    }
                } else {
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        if (device.getName() != null) {
                            adapter.addItem(new Customer(device.getName(), device.getAddress())); // 기기찾고... 블루투스 권한까지 허용했다.. 근데 왜 안되냐?
                            adapter.notifyDataSetChanged(); //갱신
                            cnt += 1;
                        }
                    }
                }
            }
        };

        //---------------------- 앱에서 받는다..

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND); // 블루투스 장치 발견시 앱에서 받음
        registerReceiver(mDeviceDiscoverReceiver, filter);

        //------------------------

        bluetooth_scan.setOnClickListener(v -> { // 연결 가능한 디바이스 검색 버튼 클릭 이벤트 처리 부분

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent); // 300초동안 검색 가능상태로 만들거냐?


            try {
                if (mBluetoothAdapter.isDiscovering()) // 검색 중이냐?
                {
                    mBluetoothAdapter.cancelDiscovery(); //검색 상태였으면 취소
                } else {
                    mBluetoothAdapter.startDiscovery(); //검색 시작
                    //Log.d(TAG, "디바이스 검색 했습니다.");
                    listView_scan.setAdapter(adapter);  // list_scan에 어댑터에 들어간 데이터 넣어줌
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                Log.d(TAG, "오류남 ㅠ.." + e);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //권한 요청 하는 부분
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOACTION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {   // 권한요청이 허용된 경우
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();    // 권한요청이 거절된 경우
                    return;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 뒤로가기 버튼 만들고 누르면 작동하는 함수..
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  //startActivityForResult 실행 후 결과를 처리하는 부분
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //블루투스가 활성화 되었다
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                //블루투스 켜는것을 취소했다.
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

