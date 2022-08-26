package com.example.ex;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class bluetooth extends AppCompatActivity {

    TextView bluetooth_status, listtxt;
    Button bluetooth_on, bluetooth_off, bluetooth_scan;
    final String TAG = "bluetooth_activity";

    //----- 블루투스 위한
    protected static UUID MY_UUID; // 블루투스 뭐 아이디?
    int REQUEST_ENABLE_BT = 1; // 요청 코드

    RecyclerView listView_pairing, listView_scan; //
    private BluetoothAdapter mBluetoothAdapter; //블루투스 어댑터 선언
    private Set<BluetoothDevice> pairedDevice;
    //-----


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_connect);

        Toolbar toolbar = findViewById(R.id.toolbar); // 묶어주자... 진짜 피곤하니까 슬슬 짜증난다
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

        if(mBluetoothAdapter.isEnabled()){
            bluetooth_status.setText("블루투스 연결 상태입니다.");
        }
        else {
            bluetooth_status.setText("블루투스 비연결 상태입니다.");
        }

        //-------------------------

        //------------------------- 등록된 디바이스 보여주는 Area

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false); // list_scan 사용하기 위한 부분
        listView_pairing.setLayoutManager(linearLayoutManager2);

        CustomAdapter adapter2 = new CustomAdapter(getApplicationContext());
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                pairedDevice = mBluetoothAdapter.getBondedDevices();
                if (pairedDevice.size() > 0) { // 디바이스가 있으면
                    for (BluetoothDevice bt : pairedDevice) { // 체크해서 list에 add 해줘가지고 listview에 나타냄..
                        adapter2.addItem(new Customer(bt.getName(), bt.getAddress())); // 이름이랑 주소 나타냄
                        listView_pairing.setAdapter(adapter2);
                    }

                }
            }
        }catch (Exception e ) { Log.d(TAG,"error" + e);}
        //-------------------------


        //------------------------- 버튼 클릭 이벤트

        bluetooth_on.setOnClickListener(v -> { // 블루투스 on 클릭 이벤트
            if (mBluetoothAdapter != null) { //블루투스 지원안하면.. 근데 그럴일은 요즘 없지않나
                if (!mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // 정신병 걸릴뻔 했다 샤오미는 랑 z플립 반대로 작동해서 이것도 버젼대로 해야함 P sdk 이하면 퍼미션 없이 작동하고 이상이면 퍼미션 필요
                        Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //OFF도 똑같은 방식으로 sdk에 따라 나눠야함
                        startActivityForResult(blue, REQUEST_ENABLE_BT);  //이거 고쳐야함 일단 패스
                        bluetooth_status.setText("블루투스 연결 상태입니다.");
                    }
                    else{
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        } else {
                            Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(blue, REQUEST_ENABLE_BT);  //이거 고쳐야함 일단 패스
                            bluetooth_status.setText("블루투스 연결 상태입니다.");
                        }
                    }
                }
                else{
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
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                    mBluetoothAdapter.disable(); //블루투스 비활성화
                    Toast.makeText(this, "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                    bluetooth_status.setText("블루투스 비연결 상태입니다.");
                }
                else {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { //권한 체크해주고
                        Toast.makeText(this, "오류났습니다", Toast.LENGTH_SHORT).show();

                    } else {
                        mBluetoothAdapter.disable(); //블루투스 비활성화
                        Toast.makeText(this, "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                        bluetooth_status.setText("블루투스 비연결 상태입니다.");
                    }
                }
            }
            else {

                Toast.makeText(this, "이미 비활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //-----------------------

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false); // list_scan 사용하기 위한 부분
        listView_scan.setLayoutManager(linearLayoutManager);


        CustomAdapter adapter = new CustomAdapter(getApplicationContext());

        bluetooth_scan.setOnClickListener(v ->{
            adapter.addItem(new Customer("getName","getAddress"));
            listView_scan.setAdapter(adapter);
        });

        //-----------------------

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

