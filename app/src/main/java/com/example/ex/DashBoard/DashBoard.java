package com.example.ex.DashBoard;


import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothHandler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.example.ex.Bluetooth.BluetoothFragment;
import android.Manifest;
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
import com.example.ex.MainActivity.MainActivity;
import com.example.ex.R;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

public class DashBoard extends AppCompatActivity {

    public final String TAG = "DashBoard_Activity";
    public int value = 0;
    public int i;
    public static int mMaxPercentage = 100;
    public static int mPercentage = 0;
    public static int mAngle = 0;
    public static final int CIRCLE_DEGREES = 360;
    public BluetoothSocket mBluetoothSocket;
    public BluetoothDevice device;
    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805f9b34fb");
    public final String [] DashBoard_Data = {"0105","010c","010d","0142","0110"};
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

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == BluetoothFragment.BT_MESSAGE_READ){
                    if(msg.obj != null){
                        Log.e(TAG, "handleMessage123: "+msg.obj );
                    }
                }

            }
        };

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

        new Thread(() -> {
            while (true){
                BluetoothFragment.mConnectedThread.write(DashBoard_Data[i]+"\r");
                value += 1;
                if (value>100){
                    value = 1;
                }
                mBluetoothHandler.post(() -> {
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

}