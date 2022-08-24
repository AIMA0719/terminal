package com.example.ex;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.UUID;

public class bluetooth extends AppCompatActivity {

    TextView bluetooth_status;
    Button bluetooth_on, bluetooth_off, bluetooth_scan;
    final String TAG = "bluetooth_activity";
    Intent btEnableIntent;
    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;

    ArrayAdapter<String> pairingAdapter, scanAdapter;
    RecyclerView listView_pairing, listView_scan;
    ArrayList<String> pairingList, scanList;
    Button bt_cancel, bt_scan;
    BluetoothAdapter myBluetoothAdapter;
    protected static UUID MY_UUID;




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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetooth_scan = findViewById(R.id.bluetooth_scan);
        listView_pairing = findViewById(R.id.pairing_list);
        listView_scan = findViewById(R.id.scan_list);

        pairingList = new ArrayList<>(); // 페어링 목록
        scanList = new ArrayList<>(); // 스캔목록

        MY_UUID = UUID.randomUUID(); //????????????
        Log.d(TAG,MY_UUID.toString());


        bluetooth_on.setOnClickListener(v ->{ // 블루투스 on 클릭 이벤트 메소드
            if (mBluetoothAdapter != null) { //블루투스 지원안하면.. 근데 그럴일은 요즘 없지않나

                if (!mBluetoothAdapter.isEnabled()) { // 꺼져있으면
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(blue,REQUEST_ENABLE_BT);
                        bluetooth_status.setText("블루투스 연결 상태입니다.");

                    }
                }
                else {
                    Toast.makeText(this, "이미 켜져있습니다!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "블루투스 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Bluetoothadapter is null");
            }

        });


        bluetooth_off.setOnClickListener(v -> { //블루투스 off 클릭 이벤트 메소드
            if (mBluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { //권한 체크해주고
                    mBluetoothAdapter.disable(); //블루투스 비활성화
                    Toast.makeText(this, "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                    bluetooth_status.setText("블루투스 비연결 상태입니다.");
                }
                else {
                    Toast.makeText(this, "오류났습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bluetooth_scan.setOnClickListener(v ->{
            if(mBluetoothAdapter.isEnabled()){
                if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN)!= PackageManager.PERMISSION_GRANTED){

                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){ // 뒤로가기 버튼 만들고 누르면 작동하는 함수..
        switch (item.getItemId()){
            case android.R.id.home:
            {
                Intent intent = new Intent(this,MainActivity.class);
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
                //블루투스가 활성화 되었다.
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                //블루투스 켜는것을 취소했다.
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
