package com.example.ex.DashBoard;


import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothHandler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.ex.Bluetooth.BluetoothFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import com.example.ex.Bluetooth.*;


import com.example.ex.MainActivity.MainActivity;
import com.example.ex.R;
import java.io.IOException;
import java.util.Objects;

public class DashBoard extends AppCompatActivity {

    public final String TAG = "DashBoard_Activity";
    public static int mMaxPercentage = 100;
    public static int mPercentage = 0;
    public static int mAngle = 0;
    public static final int CIRCLE_DEGREES = 360;
    public BluetoothSocket mBluetoothSocket;
    public BluetoothDevice device;
    public String [] DashBoard_Data = {"0105","010c","010d","0142","0110"};
    public String Speed_data;
    public String Rpm_data;
    public String Tmp_data;
    public String Volt_data;
    public String Maf_data;
    public static Thread DashBoardThread;
    private boolean run;
    public ConnectedThread mConnectedThread;

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
    }

    @Override
    public void onStart(){
        super.onStart();
        setRun(true);

        Log.e(TAG, "대쉬보드에 들어옴");
    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothHandler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == BluetoothFragment.BT_MESSAGE_READ){
                    if(msg.obj != null){
                        String data = msg.obj.toString();
                        String [] a = data.split(">");

                        if(a[0].contains("010d")){ // 속도 = A
                            int A = Integer.parseInt(a[0].substring(11,13),16);
                            Speed_data = String.valueOf(A);

                        }else if(a[0].contains("0105")){ // 냉각수 = A-40
                            int A = Integer.parseInt(a[0].substring(11,13),16);
                            Tmp_data = String.valueOf(A-40);

                        }else if(a[0].contains("010c")){ // RPM = 256*A + B / 4
                            String A = a[0].substring(11,13);
                            String B = a[0].substring(14,16);
                            int real_real = (Integer.parseInt(A,16)*256 + Integer.parseInt(B,16)) / 4;
                            Rpm_data = String.valueOf(real_real);

                        }else if(a[0].contains("0110")){ // MAF = 256*A + B / 100
                            String A = a[0].substring(11,13);
                            String B = a[0].substring(14,16);
                            int real_real = (Integer.parseInt(A,16)*256 + Integer.parseInt(B,16) )/ 100;
                            Maf_data = String.valueOf(real_real);

                        }else if(a[0].contains("0142")){ // VOLT = 256*A + B / 1000
                            String A = a[0].substring(11,13);
                            String B = a[0].substring(14,16);
                            int real_real = (Integer.parseInt(A,16)*256 + Integer.parseInt(B,16)) / 1000;
                            Volt_data = String.valueOf(real_real);
                        }
                    }
                }
            }
        };

        DashBoardThread = new Thread(() ->{
            run = true;
            while (run){
                for (String dashBoard_datum : DashBoard_Data) {
                    BluetoothFragment.mConnectedThread.write(dashBoard_datum + "\r");
                    try {
                        Thread.sleep(180);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        DashBoardThread.start();

        //----------------------pieChart setting

        CustomPieChart SpeedPie = findViewById(R.id.pieView_speed);
        CustomPieChart RpmPie = findViewById(R.id.pieView_rpm);
        CustomPieChart VoltPie = findViewById(R.id.pieView_volt);
        CustomPieChart MafPie = findViewById(R.id.pieView_maf);
        CustomPieChart TmpPie = findViewById(R.id.pieView_tmp);

        SpeedPie.setPercentageBackgroundColor(R.color.purple_200); // 뒤에 컬러
        TmpPie.setPercentageBackgroundColor(R.color.purple_200);
        RpmPie.setPercentageBackgroundColor(R.color.purple_200);
        VoltPie.setPercentageBackgroundColor(R.color.purple_200);
        MafPie.setPercentageBackgroundColor(R.color.purple_200);

        SpeedPie.setPieInnerPadding(30); //안에 패딩 얼마나
        TmpPie.setPieInnerPadding(30);
        RpmPie.setPieInnerPadding(30);
        MafPie.setPieInnerPadding(30);
        VoltPie.setPieInnerPadding(30);

        SpeedPie.setInnerTextVisibility(View.VISIBLE); // 안에 텍스트 보여주기
        TmpPie.setInnerTextVisibility(View.VISIBLE);
        RpmPie.setInnerTextVisibility(View.VISIBLE);
        MafPie.setInnerTextVisibility(View.VISIBLE);
        VoltPie.setInnerTextVisibility(View.VISIBLE);

        TmpPie.setPercentageTextSize(25); //안에 텍스트 사이즈
        SpeedPie.setPercentageTextSize(25);
        RpmPie.setPercentageTextSize(25);
        MafPie.setPercentageTextSize(25);
        VoltPie.setPercentageTextSize(25);


        //----------------------pieChart control

        Thread pieChartThread =  new Thread(() -> {
            while (true){
                mBluetoothHandler.post(() -> {
                    if (Speed_data!=null){
                        SpeedPie.setInnerText(Speed_data+" km/h"); // 안에 값 변경
                        SpeedPie.setPercentage(Float.parseFloat(Speed_data)*mMaxPercentage/255); // 퍼센트 변경
                    }else {
                        SpeedPie.setInnerText("No data"); // 안에 값 변경
                    }
                    if(Tmp_data!=null){
                        TmpPie.setInnerText(Tmp_data+" °C");
                        TmpPie.setPercentage(Float.parseFloat(Tmp_data)*mMaxPercentage/215); // 퍼센트 변경
                    }else {
                        TmpPie.setInnerText("No data");
                    }

                    if(Rpm_data!=null){
                        RpmPie.setInnerText(Rpm_data+" rpm");
                        RpmPie.setPercentage((float) (Float.parseFloat(Rpm_data)*mMaxPercentage/16383.75)); // 퍼센트 변경
                    }else {
                        RpmPie.setInnerText("No data");
                    }

                    if(Maf_data!=null){
                        MafPie.setInnerText(Maf_data+" g/s");
                        MafPie.setPercentage((float) (Float.parseFloat(Maf_data)*mMaxPercentage/655.35)); // 퍼센트 변경
                    }else {
                        MafPie.setInnerText("No data");
                    }

                    if(Volt_data!=null){
                        VoltPie.setInnerText(Volt_data+" v");
                        VoltPie.setPercentage((float) (Float.parseFloat(Volt_data)*mMaxPercentage/65.535)); // 퍼센트 변경
                    }else {
                        VoltPie.setInnerText("No data");
                    }

                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        pieChartThread.start();
    }

    public void onPause(){
        super.onPause();
        Log.e(TAG, "DashBoard 퍼즈 됐나요?" );
        setRun(false);
        DashBoardThread.interrupt(); // 쓰레드 처리함

    }

    public void onDestroy(){
        super.onDestroy();
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRun(boolean run){
        this.run = run;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 뒤로가기 버튼 만들고 누르면 작동하는 함수..
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("thread",1);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // 뒤로가기 버튼 만들고 누르면 작동하는 함수

}