package com.example.ex.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;

import com.example.ex.MainActivity.MainActivity;
import com.example.ex.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class bluetooth extends AppCompatActivity {


    public static final int BT_MESSAGE_READ = 3;
    TextView bluetooth_status, listtxt;
    Button bluetooth_on, bluetooth_off, bluetooth_scan;
    final String TAG = "bluetooth_activity";
    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805F9B34FB");

    private static final int REQUEST_ENABLE_BT = 1; // 요청 코드
    private static final int REQUEST_LOACTION = 2;
    private static final int BT_CONNECTING_STATUS = 4;


    Handler mBluetoothHandler;
    public BluetoothSocket mBluetoothSocket;
    public static ConnectedThread mConnectedThread;

    public RecyclerView listView_pairing, listView_scan;
    private BluetoothAdapter mBluetoothAdapter; //블루투스 어댑터 선언

    private final List<Customer> paired_list = new ArrayList<>();
    private final List<Customer> scan_list = new ArrayList<>();

    private final CustomAdapter adapter = new CustomAdapter((bluetooth) getApplicationContext(),paired_list);
    private final CustomAdapter adapter1 = new CustomAdapter((bluetooth) getApplicationContext(),scan_list);

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_connect);

        Toolbar toolbar = findViewById(R.id.toolbar); // 묶어주자... 이거때매 자꾸 팅겼다..
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("블루투스 연결 페이지");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 개같은뒤로가기 이거 툴바 아이디 안묶어주면 계속 오류났었다

        bluetooth_on = findViewById(R.id.bluetooth_on);
        bluetooth_off = findViewById(R.id.bluetooth_off);
        bluetooth_scan = findViewById(R.id.bluetooth_scan);
        bluetooth_status = findViewById(R.id.bluetooth_status2);
        listView_pairing = findViewById(R.id.pairing_list);
        listView_scan = findViewById(R.id.scan_list);
        listtxt = findViewById(R.id.device);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //블루투스 어댑터

        //------------------------- 블루투스 연결 상태 보여주는 Text 설정
        if (mBluetoothAdapter.isEnabled()) {
            bluetooth_status.setText("블루투스 연결 상태입니다.");
        } else {
            bluetooth_status.setText("블루투스 비연결 상태입니다.");
        }
        //-------------------------

        //-------------------------권한설정
        ActivityCompat.requestPermissions(bluetooth.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOACTION); // 위치권한 사용자에게 요청
        //-------------------------

        //-------------------------
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); // list_scan부분 레이아웃 선언
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); // list_scan 사용하기 위한 부분

        listView_pairing.setLayoutManager(linearLayoutManager); //레이아웃 설정
        listView_scan.setLayoutManager(linearLayoutManager2); //레이아웃 설정

        listView_scan.setAdapter(adapter1); //어댑터를 scan_list에 연결

        //------------------------ 인플레이터 정의 ( ex)액션 파운드 같은경우 기기 찾으면 앱에서 받는다 )
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDeviceDiscoverReceiver, filter);
        //------------------------

        Set<BluetoothDevice> pairedDevice;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED) {
            pairedDevice = mBluetoothAdapter.getBondedDevices();
            if (pairedDevice.size() > 0) { // 디바이스가 있으면 작동함
                for (BluetoothDevice bt : pairedDevice) { // 체크해서 list에 add 해줘가지고 listview에 나타냄..
                    paired_list.add(new Customer(bt.getName() + "\n" + bt.getAddress()));
                    adapter.notifyDataSetChanged(); // 어댑터 항목에 변화가 있음을 알려줌
                }
            } else {
                Toast.makeText(this, "등록된 디바이스가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else { //샤오미에선 여기 돔
            pairedDevice = mBluetoothAdapter.getBondedDevices();
            if (pairedDevice.size() > 0) { // 디바이스가 있으면
                for (BluetoothDevice bt : pairedDevice) { // 체크해서 list에 add 해줘가지고 listview에 나타냄..
                    paired_list.add(new Customer(bt.getName() + "\n" + bt.getAddress())); // 저 new Customer을 그냥 Device로 바꿔야함.
                    adapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(this, "등록된 디바이스 보여주기 오류남", Toast.LENGTH_SHORT).show();
            }
        }

        listView_pairing.setAdapter(adapter); //리사이클러뷰에 어댑터 설정

        bluetooth_on.setOnClickListener(v -> { // 블루투스 on 클릭 이벤트
            if (mBluetoothAdapter != null) { //블루투스 지원안하면.. 근데 그럴일은 요즘 없지않나
                if (!mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // 정신병 걸릴뻔 했다 샤오미는 랑 z플립 반대로 작동해서 이것도 버젼대로 해야함 P sdk 이하면 퍼미션 없이 작동하고 이상이면 퍼미션 필요
                        Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //OFF도 똑같은 방식으로 sdk에 따라 나눠야함
                        startActivityForResult(blue, REQUEST_ENABLE_BT);  //이거 고쳐야함 일단 패스
                        bluetooth_status.setText("블루투스 연결 상태입니다.");
                    } else {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
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
        }); // 블루투스 on 클릭 이벤트 처리 부분

        bluetooth_off.setOnClickListener(v -> { //블루투스 off 클릭 이벤트
            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    mBluetoothAdapter.disable(); //블루투스 비활성화
                    Toast.makeText(this, "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                    bluetooth_status.setText("블루투스 비연결 상태입니다.");
                } else {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { //권한 체크해주고
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
        }); // 블루투스 off 버튼 클릭 이벤트 처리 부분

        bluetooth_scan.setOnClickListener(v -> { // 연결 가능한 디바이스 검색 버튼 클릭 이벤트 처리 부분

            /*Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent); // 300초동안 검색 가능상태로 만들거냐?*/

            try {
                if (mBluetoothAdapter.isDiscovering()) { // 검색 중이냐?
                    mBluetoothAdapter.cancelDiscovery(); //검색 상태였으면 취소
                } else {
                    mBluetoothAdapter.startDiscovery(); //검색 시작
                    //Log.d(TAG, "디바이스 검색 했습니다.");
                    adapter1.notifyDataSetChanged();
                }
            } catch (Exception e) {
                Log.d(TAG, "Discover error" + e);
            }
        }); // 블루투스 scan 버튼 클릭 이벤트 처리 부분 z플립3는 되는데 샤오미에선 안된다..후

//        mBluetoothHandler = new Handler(Looper.getMainLooper()) {
//            @Override
//            public void handleMessage(Message msg) {
//                if (msg.what == BT_MESSAGE_READ) {
//                    String readMessage = null;
//                    try {
//                        readMessage = new String((byte[]) msg.obj, "UTF-8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                    bluetooth_status.setText(readMessage);
//                }
//
//                if (msg.what == BT_CONNECTING_STATUS) {
//                    if (msg.arg1 == 1) {
//                        String[] name = msg.obj.toString().split("\n");
//                        bluetooth_status.setText("Connected to Device: " + name[0]);
//                    } else {
//                        bluetooth_status.setText("Connection Failed");
//                    }
//                }
//            }
//        }; // 핸들러...

        adapter.setOnItemClickListener((position, view) -> {

            String name = paired_list.get(position).getName();
            String[] address = name.split("\n");

            Log.d(TAG, "onCreate: " + address[1]);

            new Thread() {
                @Override
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address[1]);

                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                        Log.d(TAG, "run: 소켓 생성 완료!");
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mBluetoothSocket.connect();
                        Log.d(TAG, "run: 소켓 연결 완료!");
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBluetoothSocket.close();
                            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!fail) {
                        mConnectedThread = new ConnectedThread(mBluetoothSocket, mBluetoothHandler);
                        mConnectedThread.start();

                        mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();

        }); // 등록된 디바이스  클릭

        adapter1.setOnItemClickListener((position, view) -> {

            String name = scan_list.get(position).getName();
            String[] address = name.split("\n");

            Log.d(TAG, "onCreate: " + address[1]);

            new Thread() {
                @Override
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address[1]);

                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                        Log.d(TAG, "run: 소켓 생성 완료!");
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mBluetoothSocket.connect();
                        Log.d(TAG, "run: 소켓 연결 완료!");
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBluetoothSocket.close();
                            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!fail) {
                        mConnectedThread = new ConnectedThread(mBluetoothSocket, mBluetoothHandler);
                        mConnectedThread.start();

//                        Intent intent = new Intent(bluetooth.this,MainActivity.class);
//                        intent.putExtra("BT",mConnectedThread);
//
//                        startActivity(intent);
//                        Log.d(TAG, "run: "+intent);

                        mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();

        }); // 연결 가능한 디바이스 클릭 이벤트

    }

    public static class ConnectedThread extends Thread implements Serializable {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Handler mHandler;

        public ConnectedThread(BluetoothSocket socket, Handler handler) {
            mmSocket = socket;
            mHandler = handler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public static ConnectedThread getInstance() {
            return mConnectedThread;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(bluetooth.BT_MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }

    } // 연결하기 위한 스레드

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
    } // 소켓 만들기 위한 메소드

    private final BroadcastReceiver mDeviceDiscoverReceiver = new BroadcastReceiver() {
        int cnt = 0;

        @SuppressLint("NotifyDataSetChanged")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // SDK가 31 이상일때
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "권한이 없습니다..", Toast.LENGTH_SHORT).show();
                    } else {
                        if (device.getName() != null) {
                            scan_list.add(new Customer(device.getName() + "\n" + device.getAddress()));
                            adapter1.notifyDataSetChanged(); //갱신
                            cnt += 1;
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.v(TAG, "총 찾은 디바이스 개수 : " + cnt); // 블루투스가 꺼질때 동작한다.
                }

            } else {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (device.getName() != null) {
                        scan_list.add(new Customer(device.getName() + "\n" + device.getAddress()));
                        adapter1.notifyDataSetChanged(); //갱신
                        cnt += 1;
                    }
                }
            }
        }
    }; //----------------------- 브로드캐스트 리시버 정의 // device 스캔 작동 부분

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mDeviceDiscoverReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //권한 요청 하는 부분
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOACTION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {   // 권한요청이 허용된 경우
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();    // 권한요청이 거절된 경우
                }
            }
        }
    } // 권한 요청하는 함수

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // 뒤로가기 버튼 만들고 누르면 작동하는 함수..
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    } // 뒤로가기 버튼 만들고 누르면 작동하는 함수

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    } //startActivityForResult 실행 후 결과를 처리하는 함수

}

