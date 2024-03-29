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
    public final String[] DefaultATCommandArray = new String[]{"ATZ","ATE1","ATD0","ATSP0","ATH1","ATM0","ATS0","ATAT1","ATST64"};
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
    public String sText; // editText 창에 입력한 메세지
    public boolean flag = false;
    private Fragment AtCommandsFragment; // AT 커맨드 프래그먼트
    private Fragment ObdPidsFragment; // OBD PIDS 프래그먼트
    public static int screenflag = 0; // Activity,Fragment 별 screen flag 구분위해 만들었는데 아직 쓸모없다
    public static final int BT_CONNECTING_STATUS = 1;
    public String show_data = "";
    long backKeyPressedTime = 0; // 백버튼 변수

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n", "HandlerLeak", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_item = findViewById(R.id.text_view1);
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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //BluetoothAdapter 인스턴스 get

        // -------- 툴바 관려된 설정
        Toolbar toolbar = findViewById(R.id.toolbar); //툴바 아이디 가져오기
        setSupportActionBar(toolbar); //툴바
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

                @SuppressLint("DefaultLocale")
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void handleMessage(Message msg) { // 메시지 종류에 따라서

                    if (msg.what == BluetoothFragment.BT_MESSAGE_READ) {
                        if (msg.obj == null) {
                            Log.d(TAG, "메인엑티비티 에서 받은 데이터가 없습니다.");
                        }

                        readdress += msg.obj;
                        editText.setText("");

                            if(msg.arg2 == -1){
                                if (readdress.contains(">")) {
                                    Log.d(TAG, "Response 메세지 전달 받음");
                                    String[] slicing_data = readdress.split(">");

                                    String PIDS = slicing_data[0].substring(0,2);
                                    if(sText!=null){
                                        show_data = slicing_data[0].replaceFirst(sText,""); //0105 7E8410542942 마냥 앞에 0105하고 널 하나 없얘주기위함
                                    }

                                    switch (PIDS){ // 서비스 아이디에 따라 출력
                                        case "01":
                                            if(readdress.contains("?")){ // 명령어 제외하고 입력
                                                Toast.makeText(MainActivity.this, "유효하지 않는 명령어 입니다!", Toast.LENGTH_SHORT).show();
                                            }else if(readdress.contains("DATA")){ // 데이터 없는 명령어 입력
                                                Toast.makeText(MainActivity.this, "데이터가 존재하지 않습니다!", Toast.LENGTH_SHORT).show();
                                            }
                                            else { // 명령어 제대로 된거 입력 하면
                                                if(!flag) {
                                                    RecyclerView_Add(new StringBuilder(show_data));
                                                    Log.d(TAG, "출력된 데이터 :" + show_data);
                                                }
                                            }
                                            break;

                                        case "03":
                                            String [] SevenEchoEight_Data_03 = show_data.split("7E9"); // 7E8 첫번째 두번째 랑 7E9,7EA 이렇게 나눔
                                            String [] replaceData_03 = SevenEchoEight_Data_03[0].split("7E8"); // 7E8 첫번째랑 두번째 나눔

                                            List<String> SevenEchoEightPart_03 = new ArrayList<>();// 7E8 작업 리스트
                                            List<String> SevenEchoNinePart_03 = new ArrayList<>();// 7E9 작업 리스트
                                            List<String> MergeList_03 = new ArrayList<>();// 위 3개 합쳐서 맨앞꺼 바꾼뒤 다시 넣어주는 리스트

                                            //-----------7E8
                                            int Intindex = Integer.parseInt(replaceData_03[1].substring(2,4),16); // 유효 바이트가 어디까지 인지

                                            if(Intindex%2 == 1){ //유효 바이트가 홀 수라면
                                                for(int i = 1; i< replaceData_03.length; i++){
                                                    if(i==1){
                                                        for(int j = 8; j< replaceData_03[i].length(); j+=2){
                                                            if(j+2<= replaceData_03[i].length())
                                                                SevenEchoEightPart_03.add(replaceData_03[i].substring(j,j+2));
                                                        }
                                                    }else {
                                                        for(int j = 2; j< replaceData_03[i].length(); j+=2){
                                                            if(j+2<= replaceData_03[i].length())
                                                                SevenEchoEightPart_03.add(replaceData_03[i].substring(j,j+2));
                                                        }
                                                    }
                                                }
                                            }else {  // 유효 바이트가 짝 수라면
                                                for(int i = 1; i< replaceData_03.length; i++){
                                                    if(i==1){
                                                        for(int j = 8; j< replaceData_03[i].length(); j+=2){
                                                            if(j+2<= replaceData_03[i].length())
                                                                SevenEchoEightPart_03.add(replaceData_03[i].substring(j,j+2));
                                                        }
                                                    }else {
                                                        for(int j = 2; j< replaceData_03[i].length(); j+=2){
                                                            if(j+2<= replaceData_03[i].length())
                                                                SevenEchoEightPart_03.add(replaceData_03[i].substring(j,j+2));
                                                        }
                                                    }
                                                }
                                            }

                                            List<String> rawDataList = SevenEchoEightPart_03.subList(0,Intindex);

                                            for(int i=0;i+1<rawDataList.size();i+=2){
                                                if(!(rawDataList.get(i) + rawDataList.get(i + 1)).equals("0000"))
                                                    MergeList_03.add(rawDataList.get(i) +rawDataList.get(i+1));
                                            }
                                            //-----------7E8

                                            //------------7E9
                                            int Intindex1 = Integer.parseInt(SevenEchoEight_Data_03[1].substring(4,5),16); // 유효 바이트가 어디까지 인지
                                            // 064702 0102 D600

                                            if(Intindex1 % 2 == 1){
                                                for(int i =8;i<SevenEchoEight_Data_03[1].length();i+=2){
                                                    if(i+2<=SevenEchoEight_Data_03[1].length())
                                                        SevenEchoNinePart_03.add(SevenEchoEight_Data_03[1].substring(i,i+2));
                                                }
                                            }
                                            else {
                                                for(int i =6;i<SevenEchoEight_Data_03[1].length();i+=2){
                                                    if(i+2<=SevenEchoEight_Data_03[1].length())
                                                        SevenEchoNinePart_03.add(SevenEchoEight_Data_03[1].substring(i,i+2));
                                                }
                                            }

                                            for(int i=0;i+1<SevenEchoNinePart_03.size();i+=2){
                                                if(!(SevenEchoNinePart_03.get(i) + SevenEchoNinePart_03.get(i + 1)).equals("0000"))
                                                    MergeList_03.add(SevenEchoNinePart_03.get(i) +SevenEchoNinePart_03.get(i+1));
                                            }
                                            //------------7E9

                                            //MergeList_03 가공
                                            for(int i=0;i<MergeList_03.size();i++){
                                                int firstIndex = Integer.parseInt(MergeList_03.get(i).substring(0,1),16); // 앞에 따와서 16진수 -> 10진수
                                                String BinaryFirstIndex = String.format("%04d",Integer.parseInt(Integer.toBinaryString(firstIndex))); //10진수를 format함수로 0 채워서 4자리 맟줌

                                                String start = BinaryFirstIndex.substring(0,2);
                                                String end = BinaryFirstIndex.substring(2,4);

                                                String Chilepal_data = Listgagong(start,end) + MergeList_03.get(i).substring(1,4);
                                                RecyclerView_Add(new StringBuilder(Chilepal_data));
                                            }

                                            break;
                                        case "07":
                                            if(show_data.contains("?")){ // 명령어 제외하고 입력
                                                Toast.makeText(MainActivity.this, "유효하지 않는 명령어 입니다!", Toast.LENGTH_SHORT).show();
                                            }else if(show_data.contains("DATA")){ // 데이터 없는 명령어 입력
                                                Toast.makeText(MainActivity.this, "데이터가 존재하지 않습니다!", Toast.LENGTH_SHORT).show();
                                            }else {
                                                if (!flag) {
                                                    String [] sevenEchoEightData_07 = show_data.split("7E9"); // 7E8 첫번째 두번째 랑 7E9,7EA 이렇게 나눔
                                                    String [] replacedata_07 = sevenEchoEightData_07[0].split("7E8"); // 7E8 첫번째랑 두번째 나눔
                                                    String [] replacedata1_07 = sevenEchoEightData_07[1].split("7EA"); // 7E9랑 7EA랑 나눔

                                                    List<String> sevenEchoEightPart_07 = new ArrayList<>();// 7E8 작업 리스트
                                                    List<String> sevenEchoNinePart_07 = new ArrayList<>();// 7E9 작업 리스트
                                                    List<String> sevenEchoAPart_07 = new ArrayList<>();// 7EA 작업 리스트
                                                    List<String> mergelist_07 = new ArrayList<>();// 위 3개 합쳐서 맨앞꺼 바꾼뒤 다시 넣어주는 리스트

                                                    //-----------7E8
                                                    int intindex_07 = Integer.parseInt(replacedata_07[1].substring(2,4),16); // 유효 바이트가 어디까지 인지

                                                    if(intindex_07%2 == 1){ //유효 바이트가 홀 수라면
                                                        for(int i=1;i<replacedata_07.length;i++){
                                                            if(i==1){
                                                                for(int j=8;j<replacedata_07[i].length();j+=2){
                                                                    if(j+2<= replacedata_07[i].length())
                                                                        sevenEchoEightPart_07.add(replacedata_07[i].substring(j,j+2));
                                                                }
                                                            }else {
                                                                for(int j=2;j<replacedata_07[i].length();j+=2){
                                                                    if(j+2<= replacedata_07[i].length())
                                                                        sevenEchoEightPart_07.add(replacedata_07[i].substring(j,j+2));
                                                                }
                                                            }
                                                        }
                                                    }else {  // 유효 바이트가 짝 수라면
                                                        for(int i=1;i<replacedata_07.length;i++){
                                                            if(i==1){
                                                                for(int j=8;j<replacedata_07[i].length();j+=2){
                                                                    if(j+2<= replacedata_07[i].length())
                                                                        sevenEchoEightPart_07.add(replacedata_07[i].substring(j,j+2));
                                                                }
                                                            }else {
                                                                for(int j=2;j<replacedata_07[i].length();j+=2){
                                                                    if(j+2<= replacedata_07[i].length())
                                                                        sevenEchoEightPart_07.add(replacedata_07[i].substring(j,j+2));
                                                                }
                                                            }
                                                        }
                                                    }

                                                    List<String> rawdatalist_07 = sevenEchoEightPart_07.subList(0,intindex_07);

                                                    for(int i=0;i+1<rawdatalist_07.size();i+=2){
                                                        if(!(rawdatalist_07.get(i) + rawdatalist_07.get(i + 1)).equals("0000"))
                                                            mergelist_07.add(rawdatalist_07.get(i) +rawdatalist_07.get(i+1));
                                                    }
                                                    //-----------7E8

                                                    //------------7E9
                                                    int intindex1_07 = Integer.parseInt(replacedata1_07[0].substring(4,5),16); // 유효 바이트가 어디까지 인지
                                                    // 064702 0102 D600
                                                    if(intindex1_07 % 2 == 1){
                                                        for(int i =8;i<replacedata1_07[0].length();i+=2){
                                                            if(i+2<=replacedata1_07[0].length())
                                                                sevenEchoNinePart_07.add(replacedata1_07[0].substring(i,i+2));
                                                        }
                                                    }
                                                    else {
                                                        for(int i =6;i<replacedata1_07[0].length();i+=2){
                                                            if(i+2<=replacedata1_07[0].length())
                                                                sevenEchoNinePart_07.add(replacedata1_07[0].substring(i,i+2));
                                                        }
                                                    }

                                                    for(int i=0;i+1<sevenEchoNinePart_07.size();i+=2){
                                                        if(!(sevenEchoNinePart_07.get(i) + sevenEchoNinePart_07.get(i + 1)).equals("0000"))
                                                            mergelist_07.add(sevenEchoNinePart_07.get(i) +sevenEchoNinePart_07.get(i+1));
                                                    }
                                                    //------------7E9

                                                    //------------7EA
                                                    int Intindex2_07 = Integer.parseInt(replacedata1_07[1].substring(4,5),16); // 유효 바이트가 어디까지 인지

                                                    if(Intindex2_07 % 2 == 1){
                                                        for(int i =8;i<replacedata1_07[1].length();i+=4){
                                                            if(i+4<=replacedata1_07[1].length())
                                                                sevenEchoAPart_07.add(replacedata1_07[1].substring(i,i+4));
                                                        }
                                                    }
                                                    else {
                                                        for(int i =6;i<replacedata1_07[1].length();i+=4){
                                                            if(i+4<=replacedata1_07[1].length())
                                                                sevenEchoAPart_07.add(replacedata1_07[1].substring(i,i+4));
                                                        }
                                                    }
                                                    if(!(sevenEchoAPart_07.get(0).equals("0000")))
                                                        mergelist_07.add(sevenEchoAPart_07.get(0));
                                                    //------------7EA

                                                    //MergeList_03 가공
                                                    for(int i=0;i<mergelist_07.size();i++){
                                                        int firstIndex = Integer.parseInt(mergelist_07.get(i).substring(0,1),16); // 앞에 따와서 16진수 -> 10진수
                                                        String BinaryFirstIndex = String.format("%04d",Integer.parseInt(Integer.toBinaryString(firstIndex))); //10진수를 format함수로 0 채워서 4자리 맟줌

                                                        String start = BinaryFirstIndex.substring(0,2);
                                                        String end = BinaryFirstIndex.substring(2,4);


                                                        String Chilepal_data = Listgagong(start,end) + mergelist_07.get(i).substring(1,4);
                                                        RecyclerView_Add(new StringBuilder(Chilepal_data));

                                                    }
                                                }
                                            }

                                            break;

                                        case "09":
                                            if(slicing_data[0].startsWith("02", 2)){ // 0902 입력했을 때
                                                    if (!flag) {
                                                        List<String> vinRawData = new ArrayList<>();
                                                        List<Integer> vinIntRawData = new ArrayList<>();
                                                        List<Character> vinCharRawData = new ArrayList<>();
                                                        StringBuilder ASCII = new StringBuilder();

//                                                        showData = "09027E810144902014B4D487E821444734315542457E82255303133363536"; 법인차량 차대번호 rawData

                                                        String [] replacedata = show_data.split("7E8"); // 차대번호에서 7E9이런거 나오는건 배제하고 작업함...

                                                        for(int i=1;i<replacedata.length;i++){

                                                            if(i==1){
                                                                for(int j=10;j<replacedata[1].length();j+=2){ //앞에 유효 바이트 멀티라인 본인 0902 요청의 응답인 4902 다 빼면 인덱스 10부터시작
                                                                    if(j+2<= replacedata[i].length())
                                                                        vinRawData.add(replacedata[i].substring(j,j+2));
                                                                }
                                                            } else {
                                                                for(int j=2;j<replacedata[i].length();j+=2){
                                                                    if(j+2<= replacedata[i].length())
                                                                        vinRawData.add(replacedata[i].substring(j,j+2));
                                                                }
                                                            }
                                                        }

                                                        for(int i=0;i<vinRawData.size();i++){
                                                            vinIntRawData.add(Integer.parseInt(vinRawData.get(i),16));

                                                            char ch = (char) Integer.parseInt(String.valueOf(vinIntRawData.get(i)));
                                                            if(vinIntRawData.get(i)!=null){
                                                                vinCharRawData.add(ch);
                                                            }
                                                        }

                                                        for(int i=0;i<vinIntRawData.size();i++){
                                                            ASCII.append(vinCharRawData.get(i));
                                                        }

                                                        RecyclerView_Add(new StringBuilder(ASCII));
                                                        Log.d(TAG, "출력된 데이터 :" + ASCII);
                                                    }

                                            }else { // 0902 아닐때
                                                if(show_data.contains("?")){ // 명령어 제외하고 입력
                                                    Toast.makeText(MainActivity.this, "유효하지 않는 명령어 입니다!", Toast.LENGTH_SHORT).show();
                                                }else if(show_data.contains("DATA")){ // 데이터 없는 명령어 입력
                                                    Toast.makeText(MainActivity.this, "데이터가 존재하지 않습니다!", Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    if (!flag) {

                                                        RecyclerView_Add(new StringBuilder(show_data));
                                                        Log.d(TAG, "Response 메세지 : " + show_data);
                                                    }
                                                }
                                            }
                                            break;
                                        case "0A":
                                        case "0a":
                                            List<String> MergeList_0a = new ArrayList<>();
                                            MergeList_0a.add(show_data.substring(10)); // 1234 다

                                            for(int i=0;i<MergeList_0a.size();i++){
                                                int firstIndex = Integer.parseInt(MergeList_0a.get(i).substring(0,1),16); // 앞에 따와서 16진수 -> 10진수
                                                String BinaryFirstIndex = String.format("%04d",Integer.parseInt(Integer.toBinaryString(firstIndex))); //10진수를 format함수로 0 채워서 4자리 맟줌

                                                String start = BinaryFirstIndex.substring(0,2);
                                                String end = BinaryFirstIndex.substring(2,4);

                                                String Chilepal_data = Listgagong(start,end)  + MergeList_0a.get(i).substring(1,4);
                                                RecyclerView_Add(new StringBuilder(Chilepal_data));

                                            }


                                            break;
                                        case "AT":
                                        case "at":
                                            Toast.makeText(MainActivity.this, "AT 커맨드를 입력하셨습니다.", Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            if(show_data.contains("?")){ // 명령어 제외하고 입력
                                                Toast.makeText(MainActivity.this, "유효하지 않는 명령어 입니다!", Toast.LENGTH_SHORT).show();
                                            }else if(show_data.contains("DATA")){ // 데이터 없는 명령어 입력
                                                Toast.makeText(MainActivity.this, "데이터가 존재하지 않습니다!", Toast.LENGTH_SHORT).show();
                                            }else { // 명령어 제대로 된거 입력 하면
                                                if(!flag) {
                                                    RecyclerView_Add(new StringBuilder(show_data));
                                                    Log.d(TAG, "Response 메세지 : " + show_data);
                                                }
                                            }
                                            break;
                                    }
                                } else {
                                    Log.d(TAG, "마지막 데이터가 아닙니다.");
                                }
                        }

                        adapter.notifyDataSetChanged(); // 갱신

                        readdress = ""; // 초기화 시켜줌
                        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    }

                    if (msg.what == MainActivity.BT_CONNECTING_STATUS) {
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

                        RecyclerView_Add(new StringBuilder(sText));

                        editText.setText("");// editText 초기화
                        adapter.notifyDataSetChanged(); //갱신
//
//                    Log.e(TAG, "보내기 버튼 누른 후 dataList : "+dataList);
//                    Log.e(TAG, "보내기 버튼 누른 후 DB 데이터 : "+database.mainDao().getAll());

                        Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    }


                } else { //기기랑 연결 안 되어있는 상태
                    RecyclerView_Add(new StringBuilder(sText));
                    editText.setText("");
                    Log.d(TAG, "Request 메세지 : " + sText);

                    Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(dataList.size() - 1); // 리사이클러뷰의 focus 맨 마지막에 입력했던걸로 맞춰줌
                    Log.d(TAG, "블루투스 기기랑 연결이 안 되어있는 상태입니다.");
                    Toast.makeText(this, "스캐너를 먼저 연결 해주세요!", Toast.LENGTH_SHORT).show();
                }
            }
            //Log.d(TAG,"현재 포커스 : " + getCurrentFocus());
            Log.d(TAG, "명령어를 입력한 횟수 " + dataList.size());
        }); // -------- 보내기 버튼 클릭 이벤트

        btReset.setOnClickListener(v -> { // Terminal clear 버튼눌렀을때 동작
            database.mainDao().reset(database.mainDao().getAll()); // DB 삭제
            dataList.clear(); // List 삭제

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

    public void RecyclerView_Add(StringBuilder text) {
        MainData data = new MainData();
        data.setText("RX : " + text);
        database.mainDao().insert(data);
        dataList.add(data);
        try {
            mTextFileManager.save("RX :" + text + "\n"); // File에 add , :: 는 구분 용
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public String Listgagong(String start,String end){
        switch (start){
            case "00":
                start = "P";
                break;
            case "01":
                start = "C";
                break;
            case "10":
                start = "B";
                break;
            case "11":
                start = "U";
                break;
        }

        switch (end) {
            case "00":
                end = "0";
                break;
            case "01":
                end = "1";
                break;
            case "10":
                end = "2";
                break;
            case "11":
                end = "3";
                break;
        }
        return start + end;
    }

    @Override
    public void onPause(){
        super.onPause();
        flag = true;
        Log.e(TAG, "MainActivity 퍼즈" ); //화면이 꺼지면
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
        Log.e(TAG, "CheckPermission: true " );
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

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

        AtCommandsFragment = new AtCommandFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.FirstFragment, AtCommandsFragment);
        fragmentTransaction.commit();
    }

    private void ObdPids() {

        ObdPidsFragment = new ObdPidsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.FirstFragment, ObdPidsFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (screenflag == 1) { // BluetoothFragment 일 때
            super.onBackPressed();
        }
        else if(screenflag == 0){ //main 일 때
            // 1000 milliseconds = 1.0 seconds

            if (System.currentTimeMillis() > backKeyPressedTime + 1000) {
                backKeyPressedTime = System.currentTimeMillis();
                Toast.makeText(this, "한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG).show();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 1000) {
                finish();
                Log.e(TAG, "어플 종료!");

            }
        }
    }
}
