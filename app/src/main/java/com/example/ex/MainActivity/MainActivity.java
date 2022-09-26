package com.example.ex.MainActivity;

import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothHandler;
import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothSocket;
import static com.example.ex.Bluetooth.BluetoothFragment.mConnectedThread;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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

import com.example.ex.Bluetooth.BluetoothFragment;
import com.example.ex.Bluetooth.ConnectedThread;
import com.example.ex.Bluetooth.MyDialogFragment;
import com.example.ex.DashBoard.DashBoard;
import com.example.ex.RoomDB.*;
import com.example.ex.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "activity_main";

    public EditText editText;
    Button btAdd, btReset;
    RecyclerView recyclerView;
    List<MainData> dataList = new ArrayList<>();
    RoomDB database;
    MainAdapter adapter;
    TextView bluetooth_status, list_item;
    BluetoothFragment bluetoothFragment;
    TextFileManager mTextFileManager = new TextFileManager(this);

    String readmessage = ""; // 핸들러로 받은 메세지 저장
    String sText = "";
    boolean flag = false;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "메인 엑티비티에 들어옴" );

        list_item = findViewById(R.id.text_view);
        editText = findViewById(R.id.command_write);
        btAdd = findViewById(R.id.send_message);
        btReset = findViewById(R.id.clear_message);
        recyclerView = findViewById(R.id.recycler_view);
        bluetooth_status = findViewById(R.id.mbluetooth_status);

        database = RoomDB.getInstance(this); // 룸디비 가져옴
        dataList = database.mainDao().getAll(); //리스트 만듦 리스트 디비에 있는거 얘가 보여주는거
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAdapter(MainActivity.this, dataList); //어댑터 만듦
        recyclerView.setAdapter(adapter);

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

        Intent intent = getIntent(); // device.getName 가져옴
        if (intent != null) {
            String data = intent.getStringExtra("데이터");
            if (data != null) {
                String [] data2 = data.split("\n");
                Log.d(TAG, "연결된 블루투스 기기 : " + data2[0]);
                bluetooth_status.setText(data2[0] + " 기기랑 연결 상태입니다.");

            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void onStart() {
        super.onStart();

        // -------- 초기화
        database.mainDao().reset(database.mainDao().getAll()); //어플 시작할때마다 DB 초기화
        dataList.clear(); // 어플 시작할때마다 리스트 초기화
        adapter.notifyDataSetChanged(); //갱신
        // --------


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
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void onResume() {
        super.onResume();

        new Thread(() -> {
            mBluetoothHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) { // 메시지 종류에 따라서

                    if (msg.what == BluetoothFragment.BT_MESSAGE_READ) {
                        if (msg.obj == null) {
                            Log.d(TAG, "메인엑티비티 에서 받은 데이터가 없습니다.");
                        }

                        readmessage += msg.obj;
                        editText.setText("");

                        if (readmessage.contains(">")) {
                            Log.d(TAG, "Response 메세지 전달 받음");
                            String[] slicing_data = readmessage.split(">");
                            Log.d(TAG, "Response 메세지 : " + slicing_data[0]);

                            try {
                                mTextFileManager.save(slicing_data[0] + "::"); // File에 add , :: 는 구분 용
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if((readmessage.contains("at"))||(readmessage.contains("OBD"))){
                                Log.d(TAG, "AT command 를 입력 했습니다.");
                                Toast.makeText(MainActivity.this, "AT command 를 입력 했습니다.", Toast.LENGTH_SHORT).show();
                            }else if(readmessage.equals(editText.getText()+"?")){
                                Toast.makeText(MainActivity.this, "유효하지 않는 명령어 입니다!", Toast.LENGTH_SHORT).show();
                            }else if(readmessage.contains("NO")){
                                Toast.makeText(MainActivity.this, "데이터가 존재하지 않습니다!", Toast.LENGTH_SHORT).show();
                            }else if(readmessage.contains("ok")||(readmessage.contains("OK"))){
                                Log.d(TAG, "AT Commands setting중");
                            }
                            else {
                                if(!flag) {
                                    MainData data1 = new MainData();
                                    data1.setText(slicing_data[0]);
                                    database.mainDao().insert(data1);
                                    dataList.add(data1);
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "이거로됨?", Toast.LENGTH_SHORT).show();
                                }
                            }

//                        Log.e(TAG, "핸들 메세지 받은 후 dataList : "+dataList);
//                        Log.e(TAG, "핸들 메세지 받은 후 DB 데이터 : "+database.mainDao().getAll());

                        } else {
                            Log.d(TAG, "마지막 데이터가 아닙니다.");
                        }

                        adapter.notifyDataSetChanged();

                        readmessage = ""; // 초기화 시켜줌
                        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    }

                    if (msg.what == MyDialogFragment.BT_CONNECTING_STATUS) {
                        if (msg.arg1 == 1) {
                            String[] name = msg.obj.toString().split("\n");
                            Toast.makeText(getApplicationContext(), name[0] + " 와 연결 되었습니다.", Toast.LENGTH_SHORT).show();

//                        mConnectedThread.write("atz>");
//                        mBluetoothHandler.obtainMessage(MainActivity.BT_SETTINGS,1,-1).sendToTarget(); 메인에서 보냈고, 받을 Activity 에서 핸들러만들어서 받으면 된다.
                        } else {
                            Toast.makeText(MainActivity.this, "블루투스 연결에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (msg.what == BluetoothFragment.BT_MESSAGE_WRITE) {
                        Log.d(TAG, "ECU 로 Request 메세지 전달");
                    }

                }
            }; // 핸들러 ( What에 따라 동작 )
        }).start();

        btAdd.setOnClickListener(v -> {
            sText = editText.getText().toString().trim();
            flag = false; // 대시보드 데이터를 창에 안 띄워주기 위함
            if (!sText.equals("")) {
                if (BluetoothFragment.device != null) { //연결 되어있는 상태라면
                    BluetoothFragment.mConnectedThread.write(sText+"\r");
                    Log.d(TAG, "Request 메세지 : " + sText);

                    MainData data = new MainData();
                    data.setText(sText);
                    database.mainDao().insert(data); //DB에 add
                    dataList.add(data); //List에 add
//                    dataList.addAll(database.mainDao().getAll());

                    try {
                        mTextFileManager.save(sText + ">>"); // File에 add , >> 는 구분 용
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    editText.setText("");// editText 초기화
                    adapter.notifyDataSetChanged(); //갱신
//
//                    Log.e(TAG, "보내기 버튼 누른 후 dataList : "+dataList);
//                    Log.e(TAG, "보내기 버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());

                    Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌

                } else { //기기랑 연결 안 되어있는 상태
                    MainData data = new MainData();
                    data.setText(sText);
                    database.mainDao().insert(data);
                    Log.d(TAG, "Request 메세지 : " + sText);
                    dataList.add(data);
                    adapter.notifyDataSetChanged(); //갱신

                    editText.setText("");
//                    dataList.clear(); //리스트 초기화
//                    dataList.addAll(database.mainDao().getAll()); // DB에 add

                    Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    Log.d(TAG, "블루투스 기기랑 연결이 안 되어있는 상태입니다.");
                }
            }
            //Log.d(TAG,"현재 포커스 : " + getCurrentFocus());
            Log.d(TAG, "명령어를 입력한 횟수 " + dataList.size());
        }); // -------- 보내기 버튼 클릭 이벤트

        btReset.setOnClickListener(v -> { // Terminal clear 버튼눌렀을때 동작
            database.mainDao().reset(database.mainDao().getAll()); // DB 삭제
            dataList.clear(); // List 삭제
            mTextFileManager.delete(); // File 삭제
//
//            Log.e(TAG, "리셋버튼 누른 후 MainRecyclerview : "+dataList);
//            Log.e(TAG, "리셋버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());
//            Log.e(TAG,"리셋버튼 누른 후 File : "+ mTextFileManager.load());

            adapter.notifyDataSetChanged(); // 리사이클러뷰의 리스트를 업데이트 하는 함수중 하난데 리스트의 크기와 아이템이 둘 다 변경되는 경우 사용
            Toast.makeText(this, "창을 클리어 했습니다.", Toast.LENGTH_SHORT).show();
        }); // -------- Window clear 버튼 클릭이벤트

    }

    @Override
    public void onPause(){
        super.onPause();
        flag = true;

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void onRestart(){
        super.onRestart();
        mConnectedThread = new ConnectedThread(mBluetoothSocket,mBluetoothHandler);
        mConnectedThread.start();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mConnectedThread.write("ATZ"+"\r");
        if( mConnectedThread.isAlive()){
            mConnectedThread.interrupt();
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        finish();
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

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    } // -------- Toolbar 에 menu.xml을 inflate 함 =  메뉴에있는 UI 들 객체화해서 쓸 수 있게한다? 로 이해함

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.blutooth_connect:
                try {
                    FragmentView(); // 프래그먼트로 이동하는 코드

                    FragmentManager fm1 = getSupportFragmentManager();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.FirstFragment, bluetoothFragment);
                    ft1.commit();
                } catch (Exception e) {
                    Log.d(TAG, "Error :" + e);
                }
                return true;

            case R.id.log_load:  // log 버튼 클릭
                if (!editText.getText().equals("")) {
                    String a = mTextFileManager.load();

                    if (a == null) {
                        Toast.makeText(this, "저장할 Text가 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            mTextFileManager.save(a);
                            Toast.makeText(this, "Text 파일로 저장 되었습니다.", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return true;

            case R.id.dashboard: //대쉬보드 클릭
                Intent intent = getIntent();
                String data = intent.getStringExtra("데이터");
                Intent intent1 = new Intent(this, DashBoard.class);
                intent1.putExtra("기기이름",data);
                startActivity(intent1);

                return true;

            default:
                return onOptionsItemSelected(item);
        }
    } // -------- 메뉴 (Three dots) 버튼 클릭 이벤트

    public static class TextFileManager{ // 파일 관리
        private static final String FILE_NAME = "Memo.txt";

        Context context = null;

        public TextFileManager(Context context){
            this.context = context;
        }

        public void save(String strData) throws IOException { // 파일 쓰기
            if(strData == null||strData.equals("")){
                return;
            }

            FileOutputStream fosMemo = null;

            try {
                fosMemo = context.openFileOutput(FILE_NAME,Context.MODE_PRIVATE);
                fosMemo.write(strData.getBytes(StandardCharsets.UTF_8));
                fosMemo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public String load(){ // 파일 불러 오기
            try {
                FileInputStream fisMemo = context.openFileInput(FILE_NAME);
                byte[] memoData = new byte[fisMemo.available()];

                while (fisMemo.read(memoData)!= -1){}
                return new String(memoData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void delete(){
            context.deleteFile(FILE_NAME);
        } // 파일 삭제

    }

    private void FragmentView() {

        bluetoothFragment = new BluetoothFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.FirstFragment, bluetoothFragment);
        fragmentTransaction.commit();
    }

}
