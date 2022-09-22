package com.example.ex.DashBoard;


import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothHandler;
import static com.example.ex.Bluetooth.BluetoothFragment.mConnectedThread;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.ex.Bluetooth.BluetoothFragment;
import com.example.ex.Bluetooth.ConnectedThread;
import com.example.ex.Bluetooth.MyDialogFragment;
import com.example.ex.MainActivity.MainActivity;
import com.example.ex.R;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;


public class DashBoard extends AppCompatActivity {

    public final String TAG = "DashBoard_Activity";
    public int value = 0;
    public static int mMaxPercentage = 100;
    public static int mPercentage = 0;
    public static int mAngle = 0;
    public static final int CIRCLE_DEGREES = 360;
    public static DashBoardThread mDashBoardThread;
    public BluetoothSocket mBluetoothSocket;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothDevice device;
    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805f9b34fb");
    public String device_name;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        //------------------- Toolbar setting
        Toolbar toolbar = findViewById(R.id.DashBoard_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false); // title 가시 여부
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //-------------------

        Intent intent = getIntent();
        device_name = intent.getStringExtra("기기이름").split("\n")[1];
        Log.e(TAG, "대쉬보드에 들어옴");

    }

    @Override
    public void onStart(){
        super.onStart();

        boolean fail = false;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        device = mBluetoothAdapter.getRemoteDevice(device_name);

        try {
            mBluetoothSocket = createBluetoothSocket(device);
            Log.d(TAG, "소켓 생성 완료!");
        } catch (IOException e) {
            fail = true;
            Toast.makeText(getApplicationContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
        }
        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mBluetoothSocket.connect();
            Log.d(TAG, "소켓 연결 완료!");
        } catch (IOException e) {
            try {
                fail = true;
                mBluetoothSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "소켓 생성 실패!");
            }
        }
        if (!fail) {
            DashBoardThread mDashBoardThread = new DashBoardThread(mBluetoothSocket);
            mDashBoardThread.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //----------------------pieChart setting

        CustomPieChart SpeedPie = findViewById(R.id.pieView_speed);
        CustomPieChart RpmPie = findViewById(R.id.pieView_rpm);

        SpeedPie.setPercentageBackgroundColor(R.color.purple_200);
        RpmPie.setPercentageBackgroundColor(R.color.purple_200);

        SpeedPie.setPieInnerPadding(30);
        RpmPie.setPieInnerPadding(30);
        // Update the visibility of the widget text
        SpeedPie.setInnerTextVisibility(View.VISIBLE); // 안에 텍스트 보여주기
        SpeedPie.setPercentageTextSize(35); //안에 텍스트 사이즈

        RpmPie.setInnerTextVisibility(View.VISIBLE); // 안에 텍스트 보여주기
        RpmPie.setPercentageTextSize(35); //안에 텍스트 사이즈

        //----------------------pieChart control

        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == DashBoardThread.DATA_SEND){
                    Log.e(TAG, "handleMessage: "+msg.obj );
                }

            }
        };

        new Thread(() -> {
            while (true){
                value += 1;
                if (value>100){
                    value = 1;
                }
                handler.post(() -> {
                    SpeedPie.setInnerText(String.valueOf(value)); // 안에 값 변경
                    SpeedPie.setPercentage(value); // 퍼센트 변경

                    RpmPie.setInnerText(String.valueOf(value));
                    RpmPie.setPercentage(value*2);

                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onDestroy(){
        super.onDestroy();
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 뒤로가기 버튼 만들고 누르면 작동하는 함수..
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // 뒤로가기 버튼 만들고 누르면 작동하는 함수


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
    } // 소켓 만들기 위한 메소드
}