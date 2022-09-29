package com.example.ex.MainActivity;

import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothHandler;
import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothSocket;
import static com.example.ex.Bluetooth.BluetoothFragment.mConnectedThread;

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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ex.Bluetooth.BluetoothFragment;
import com.example.ex.Bluetooth.ConnectedThread;
import com.example.ex.Bluetooth.MyDialogFragment;
import com.example.ex.Bluetooth.MyItemRecyclerViewAdapter;
import com.example.ex.DashBoard.DashBoardActivity;
import com.example.ex.R;
import com.example.ex.RoomDB.MainAdapter;
import com.example.ex.RoomDB.MainData;
import com.example.ex.RoomDB.RoomDB;

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
    public Button btAdd, btReset,AT,OBD;
    public RecyclerView recyclerView;
    public List<MainData> dataList = new ArrayList<>();
    public RoomDB database;
    public MainAdapter adapter;
    public TextView list_item;
    public BluetoothFragment bluetoothFragment;
    public TextFileManager mTextFileManager = new TextFileManager(this);
    public String readdress = ""; // 핸들러로 받은 메세지 저장
    public String sText = ""; // editText 창에 입력한 메세지
    public boolean flag = false;
    public boolean bluetooth_flag = false;
    private Fragment AtCommandsFragment; // AT 커맨드 프래그먼트
    private Fragment ObdPidsFragment; // OBD PIDS 프래그먼트
    public static int screenflag = 0; // Activity,Fragment 별 screen flag 구분위해 만들었는데 아직 쓸모없다

    long backKeyPressedTime = 0;


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
        AT = findViewById(R.id.AT_Commands);
        OBD = findViewById(R.id.OBD_PIDS);

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

        CheckPermission(); // 권한 체크
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

                        readdress += msg.obj;
                        editText.setText("");

                        if (readdress.contains(">")) {
                            Log.d(TAG, "Response 메세지 전달 받음");
                            String[] slicing_data = readdress.split(">");
                            Log.d(TAG, "Response 메세지 : " + slicing_data[0]); // slicing_data[0] = ex) 0105 41 05 09 41 05 90

                            if(readdress.equals(editText.getText()+"?")){ // 명령어 제외하고 입력
                                Toast.makeText(MainActivity.this, "유효하지 않는 명령어 입니다!", Toast.LENGTH_SHORT).show();
                            }else if(readdress.contains("NO")){ // 데이터 없는 명령어 입력
                                Toast.makeText(MainActivity.this, "데이터가 존재하지 않습니다!", Toast.LENGTH_SHORT).show();
                            }else if(readdress.contains("ok")||(readdress.contains("OK"))){ // 초기 세팅
                                Log.d(TAG, "AT Commands setting중");
                            }
                            else { // 명령어 제대로 된거 입력 하면
                                if(!flag) {
                                    MainData data1 = new MainData();
                                    data1.setText("RX : "+slicing_data[0]);
                                    database.mainDao().insert(data1);
                                    dataList.add(data1);
                                    try {
                                        mTextFileManager.save("RX : "+ slicing_data[0]+"\n"); // File에 add , :: 는 구분 용
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

//                        Log.e(TAG, "핸들 메세지 받은 후 dataList : "+dataList);
//                        Log.e(TAG, "핸들 메세지 받은 후 DB 데이터 : "+database.mainDao().getAll());

                        } else {
                            Log.d(TAG, "마지막 데이터가 아닙니다.");
                        }

                        adapter.notifyDataSetChanged(); // 갱신

                        readdress = ""; // 초기화 시켜줌
                        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    }

                    if (msg.what == MyDialogFragment.BT_CONNECTING_STATUS) {
                        if (msg.arg1 == 1) {
                            String[] name = msg.obj.toString().split("\n");
                            Toast.makeText(getApplicationContext(), name[0] + " 기기와 연결 되었습니다.", Toast.LENGTH_SHORT).show();

                        } else if (msg.arg1==2){
                            String[] name = msg.obj.toString().split("\n");
                            Toast.makeText(getApplicationContext(), name[0] + " 기기와 연결 취소 했습니다", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            MyItemRecyclerViewAdapter.Connection_flag = false; // 이거 안 해주면 실패해도 연결 취소하시겠냐 뜸
                            Toast.makeText(MainActivity.this, "블루투스 연결에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (msg.what == BluetoothFragment.BT_MESSAGE_WRITE) {
                        Log.d(TAG, "ECU 로 Request 메세지 전달");
                    }

                }
            }; // 핸들러
        }).start();

        btAdd.setOnClickListener(v -> {
            sText = editText.getText().toString().trim();
            flag = false; // 대시보드 데이터를 창에 안 띄워주기 위함
            if (!sText.equals("")) {
                if (BluetoothFragment.device != null) { // 시뮬레이터랑 연결 되어있는 상태라면
                    if(mBluetoothSocket != null){
                        BluetoothFragment.mConnectedThread.write(sText+"\r"); // write 함
                        Log.d(TAG, "Request 메세지 : " + sText);

                        MainData data = new MainData();
                        data.setText("TX : "+sText);
                        database.mainDao().insert(data); //DB에 add
                        dataList.add(data); //List에 add
//                    dataList.addAll(database.mainDao().getAll());

                        try {
                            mTextFileManager.save("TX : "+sText+"\n" ); // 내부 저장소에 add
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        editText.setText("");// editText 초기화
                        adapter.notifyDataSetChanged(); //갱신
//
//                    Log.e(TAG, "보내기 버튼 누른 후 dataList : "+dataList);
//                    Log.e(TAG, "보내기 버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());

                        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    }


                } else { //기기랑 연결 안 되어있는 상태
                    MainData data = new MainData();
                    data.setText(sText);

                    dataList.clear(); //리스트 초기화
                    dataList.add(data); // 리스트에 추가
                    adapter.notifyDataSetChanged(); //갱신
                    editText.setText("");
                    Log.d(TAG, "Request 메세지 : " + sText);

//                    database.mainDao().insert(data); // 연결 안 됐으면 DB에 넣을필욘 없다

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

//            Log.e(TAG, "리셋버튼 누른 후 MainRecyclerview : "+dataList);
//            Log.e(TAG, "리셋버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());
//            Log.e(TAG,"리셋버튼 누른 후 File : "+ mTextFileManager.load()); 이건 따로 기능으로 냅두려고 여기에 안 넣었다.

            adapter.notifyDataSetChanged(); // 리사이클러뷰의 리스트를 업데이트 하는 함수중 하난데 리스트의 크기와 아이템이 둘 다 변경되는 경우 사용
            Toast.makeText(this, "창을 클리어 했습니다.", Toast.LENGTH_SHORT).show();
        }); // -------- Window clear 버튼 클릭이벤트

        AT.setOnClickListener(view -> {
            try {
                AtCommands(); // 프래그먼트로 이동하는 코드

                FragmentManager fm1 = getSupportFragmentManager();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.FirstFragment, AtCommandsFragment);
                ft1.commit();
            } catch (Exception e) {
                Log.d(TAG, "Error :" + e);
            }
        });

        OBD.setOnClickListener(view ->{
            try {
                ObdPids(); // 프래그먼트로 이동하는 코드

                FragmentManager fm1 = getSupportFragmentManager();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.FirstFragment, ObdPidsFragment);
                ft1.commit();
            } catch (Exception e) {
                Log.d(TAG, "Error :" + e);
            }
        });

    }

    @Override
    public void onPause(){
        super.onPause();
        flag = true;
        Log.e(TAG, "MainActivity 퍼즈" );
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
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void CheckPermission(){
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permission3 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int permission4 = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        int permission5 = ContextCompat.checkSelfPermission(this,Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        int permission6 = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE);
        int permission7 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED ||
                permission3 == PackageManager.PERMISSION_DENIED || permission4 == PackageManager.PERMISSION_DENIED||
                permission5 == PackageManager.PERMISSION_DENIED|| permission6 == PackageManager.PERMISSION_DENIED||
                permission7 == PackageManager.PERMISSION_DENIED) {  // 권한이 열려있는지 확인
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){  // SDK가 31 미만이면
                    requestPermissions(new String[]{ //안 열려있으면 권한 요청
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH
                    }, 1);
                }else if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.S){ //SDK가 31 이상이면
                    requestPermissions(
                            new String[]{
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_ADVERTISE,
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.ACCESS_FINE_LOCATION, // 정확한 위치
                                    Manifest.permission.ACCESS_COARSE_LOCATION, // 대략적인 위치
                                    Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE, // 스토리지 쓰기
                                    Manifest.permission.READ_EXTERNAL_STORAGE // 스토리지(저장소) 읽기
                            },
                            1);
                }
            } catch (Exception e) {
                Log.d(TAG, "권한 오류", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) { // 권한 요청 이후 로직 앱 시작하면 권한 체크 한다
        if (requestCode == 1) {
            boolean check_result = false;  //  권한 체크 했니?

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) { //SDK 31 미만이면 블루투스 스캔 권한 없어도 됨.
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

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.blutooth_connect:
                try {
                    BluetoothFragment(); // 프래그먼트로 이동하는 코드

                    FragmentManager fm1 = getSupportFragmentManager();
                    FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.FirstFragment, bluetoothFragment);
                    ft1.commit();
                    screenflag = 1;
                } catch (Exception e) {
                    Log.d(TAG, "Error :" + e);
                }
                return true;

            case R.id.log_load:  // 내부 저장소에 저장된 log 불러오기

                String a = mTextFileManager.load();

                if(a != null){
                    MainData data1 = new MainData();
                    data1.setText(a);
                    database.mainDao().insert(data1); // 디비에 넣고
                    dataList.add(data1); // 리스트에도 넣고
                    adapter.notifyDataSetChanged(); //갱신

                    editText.setText("");

                    Toast.makeText(this, "파일 Load 완료!", Toast.LENGTH_SHORT).show();

                    Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1);
                }else {
                    Toast.makeText(this, "불러올 Log가 없습니다!", Toast.LENGTH_SHORT).show();
                }

                return true;
                
            case R.id.delete_load:

                if(mTextFileManager.load() == null){
                    Toast.makeText(this, "삭제할 Log가 없습니다!", Toast.LENGTH_SHORT).show();
                }
                else {
                    database.mainDao().reset(database.mainDao().getAll()); // DB 삭제
                    dataList.clear(); // List 삭제
                    mTextFileManager.delete();

                    adapter.notifyDataSetChanged(); //갱신

                    editText.setText("");
                    Toast.makeText(this, "저장된 Log 전부 삭제 했습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.dashboard: //대쉬보드 클릭
                Intent intent = getIntent();
                String data = intent.getStringExtra("데이터");
                Intent intent1 = new Intent(this, DashBoardActivity.class);
                intent1.putExtra("기기이름",data);
                if(bluetooth_flag){
                    intent1.putExtra("블루투스연결","연결됨");
                    Log.e(TAG, "onOptionsItemSelected: 넘어감?" );
                }
                startActivity(intent1);

                return true;

            default:
                return onOptionsItemSelected(item);
        }
    } // -------- 메뉴 (Three dots) 버튼 클릭 이벤트

    public static class TextFileManager{ // 내부 저장소 파일 관리 클래스
        private static final String FILE_NAME = "Memo.txt";

        Context context = null;

        public TextFileManager(Context context){
            this.context = context;
        }

        public void save(String strData) throws IOException { // 파일을 저장
            if(strData == null||strData.equals("")){
                return;
            }

            try {
                FileOutputStream fosMemo = context.openFileOutput(FILE_NAME,Context.MODE_APPEND); // FileOutputStream 객체 생성 , PRIVATE = 덮어쓰기, APPEND = 새로 쓰기
                fosMemo.write(strData.getBytes(StandardCharsets.UTF_8));
                fosMemo.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public String load(){ // 파일 읽어서 불러오기
            String strTmp;
            try {
                FileInputStream fis = context.openFileInput(FILE_NAME);
                StringBuilder buffer = new StringBuilder();
                byte[] dataBuffer = new byte[1024];
                int n = 0;
                
                while ((n=fis.read(dataBuffer))!=-1){
                    buffer.append(new String(dataBuffer));
                }
                strTmp = buffer.toString();
                fis.close();
                
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return strTmp;
        }

        public void delete(){
            context.deleteFile(FILE_NAME);
        } // 파일 삭제

    }

    private void BluetoothFragment() {

        bluetoothFragment = new BluetoothFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.FirstFragment, bluetoothFragment);
        fragmentTransaction.commit();
    }

    private void AtCommands() {

        bluetoothFragment = new BluetoothFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.FirstFragment, AtCommandsFragment);
        fragmentTransaction.commit();
    }

    private void ObdPids() {

        bluetoothFragment = new BluetoothFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.FirstFragment, bluetoothFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 기존 뒤로 가기 버튼의 기능을 막기 위해 주석 처리 또는 삭제

        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지났으면 Toast 출력
        // 2500 milliseconds = 2.5 seconds

        if(screenflag == 0){
            if (System.currentTimeMillis() > backKeyPressedTime + 1000) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG).show();
                return;
            }
            // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
            // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 종료
            if (System.currentTimeMillis() <= backKeyPressedTime + 1000) {
                finish();
                Log.e(TAG, "어플 종료!" );

            }
        }else if(screenflag == 1){

        }


    }
}
