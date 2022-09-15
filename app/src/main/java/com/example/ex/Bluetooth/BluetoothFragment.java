package com.example.ex.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ex.MainActivity.MainActivity;
import com.example.ex.MainActivity.MyItemRecyclerViewAdapter;
import com.example.ex.R;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothFragment extends Fragment implements Serializable {

    public static final int BT_MESSAGE_READ = 3;
    public static final int BT_MESSAGE_WRITE = 3;
    private static final int RESULT_OK = 1111;
    private static final int RESULT_CANCELED = 1112;
    public TextView bluetooth_status;
    public Button bluetooth_on, bluetooth_off, bluetooth_scan;
    final String TAG = "bluetooth_activity";
    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805f9b34fb");
    public final String[] DefaultATCommandArray = new String[]{"ATZ","ATE0","ATD0","ATSP0","ATH1","ATM0","ATS0","ATAT1","ATST64"};

    public static final int REQUEST_ENABLE_BT = 1; // 요청 코드
    public static final int REQUEST_LOACTION = 2;
    public static final int BT_CONNECTING_STATUS = 4;

    public BluetoothAdapter mBluetoothAdapter; //블루투스 어댑터 선언
    public static Handler mBluetoothHandler;
    public static BluetoothSocket mBluetoothSocket;
    public static ConnectedThread mConnectedThread;
    public static BluetoothDevice device;
    public EditText editText;

    public RecyclerView listView_pairing, listView_scan;

    private final List<MyItemRecyclerViewAdapter.Customer2> paired_list = new ArrayList<>(); //리사이클러뷰 리스트 생성
    private final List<MyItemRecyclerViewAdapter.Customer2> scan_list = new ArrayList<>();

    private final MyItemRecyclerViewAdapter adapter = new MyItemRecyclerViewAdapter(paired_list, getContext()); //어댑터 생성
    private final MyItemRecyclerViewAdapter adapter2 = new MyItemRecyclerViewAdapter(scan_list, getContext());

    Set<android.bluetooth.BluetoothDevice> pairedDevice; // 등록된 디바이스 받는 Set

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        Context context = view.getContext();

        bluetooth_status = view.findViewById(R.id.bluetooth_status2);
        bluetooth_on = view.findViewById(R.id.bluetooth_on);
        bluetooth_off = view.findViewById(R.id.bluetooth_off);
        bluetooth_scan = view.findViewById(R.id.bluetooth_scan);
        editText = view.findViewById(R.id.command_write);
        listView_scan = view.findViewById(R.id.scan_list);
        listView_pairing = view.findViewById(R.id.pairing_list);

        //------------------------------------------------------- 리사이클러뷰 세팅

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        listView_pairing.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView_pairing.setLayoutManager(layoutManager);

        listView_scan.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        listView_scan.setLayoutManager(layoutManager1);

        listView_scan.setAdapter(adapter2);

        //-------------------------------------------------------


        //------------------------ 인플레이터 정의 ( ex)액션 파운드 같은경우 기기 찾으면 앱에서 받는다 )
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(mDeviceDiscoverReceiver, filter);

        //------------------------

        CheckPairedDevice(); // 등록된 디바이스 출력


        bluetooth_on.setOnClickListener(v -> {
            if (mBluetoothAdapter != null) { //블루투스 지원안하면.. 근데 그럴일은 요즘 없지않나
                if (!mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {// 정신병 걸릴뻔 했다 샤오미는 랑 z플립 반대로 작동해서 이것도 버젼대로 해야함 P sdk 이하면 퍼미션 없이 작동하고 이상이면 퍼미션 필요
                        Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //OFF도 똑같은 방식으로 sdk에 따라 나눠야함
                        startActivityForResult(blue, REQUEST_ENABLE_BT);  //이거 고쳐야함 일단 패스

                    } else {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getContext(), "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent blue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(blue, REQUEST_ENABLE_BT);  //이거 고쳐야함 일단 패스

                        }
                    }
                } else {
                    Toast.makeText(getContext(), "이미 활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "블루투스 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Bluetoothadapter is null");
            }
        }); // Bluetooth on 클릭

        bluetooth_off.setOnClickListener(v -> {

            if (mBluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    mBluetoothAdapter.disable(); //블루투스 비활성화
                    Toast.makeText(getContext(), "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { //권한 체크해주고
                        Toast.makeText(getContext(), "블루투스 권한이 없습니다.", Toast.LENGTH_SHORT).show();

                    } else {
                        mBluetoothAdapter.disable(); //블루투스 비활성화
                        Toast.makeText(getContext(), "블루투스를 비활성화 하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getContext(), "이미 비활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
            }
        }); // Bluetooth off 클릭

        bluetooth_scan.setOnClickListener(v -> { // 연결 가능한 디바이스 검색 버튼 클릭 이벤트 처리 부분

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent); // 300초동안 검색 가능상태로 만들거냐?

            try {
                if (mBluetoothAdapter.isDiscovering()) { // 검색 중인가?
                    mBluetoothAdapter.cancelDiscovery(); //검색 상태였으면 취소
                } else {
                    mBluetoothAdapter.startDiscovery(); //검색 시작
                    //Log.d(TAG, "디바이스 검색 했습니다.");
                    adapter2.notifyDataSetChanged();
                }
            } catch (Exception e) {
                Log.d(TAG, "Discover error" + e);
            }
        }); // Bluetooth scan 클릭

        adapter.setOnItemClickListener((position, view2) -> { // 등록된 디바이스  클릭

            String name1 = paired_list.get(position).getName();
            String[] address2 = name1.split("\n");

            Log.d(TAG, "연결 시도한 블루투스 기기 : " + address2[0]);

            new Thread() {
                @Override
                public void run() {
                    boolean fail = false;

                    device = mBluetoothAdapter.getRemoteDevice(address2[1]);

                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                        Log.d(TAG, "소켓 생성 완료!");
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mBluetoothSocket.connect();
                        Log.d(TAG, "소켓 연결 완료!");
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBluetoothSocket.close();
                            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!fail) {
                        mConnectedThread = new ConnectedThread(mBluetoothSocket, mBluetoothHandler);
                        mConnectedThread.start(); // 시작

                        //for (int i=0;i< DefaultATCommandArray.length;i++){
                        //    mConnectedThread.write(DefaultATCommandArray[i]);
                        //}

                        if(isConnected(device)){ //연결 되면 메인 엑티비티로 이동
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("데이터",device.getName());
                            startActivity(intent);
                        }

                        mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1, name1)
                                .sendToTarget();
                    }
                }
            }.start();

        }); // 등록된 디바이스  클릭

        adapter2.setOnItemClickListener((position, view2) -> { // 연결 가능한 디바이스 클릭

            String name = scan_list.get(position).getName();

            String[] address = name.split("\n");

            new Thread() {
                @Override
                public void run() {
                    boolean fail = false;

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "run: 권한업음");
                    }
                    mBluetoothAdapter.cancelDiscovery();
                    device = mBluetoothAdapter.getRemoteDevice(address[1]);

                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                        Log.d(TAG, "run: 소켓 생성 완료!");
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "run: 소켓 생성 실패!");
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(context, "권한 없음", Toast.LENGTH_SHORT).show();
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
                            Log.d(TAG, "run: 소켓 연결 실패!");
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!fail) {
                        mConnectedThread = new ConnectedThread(mBluetoothSocket, mBluetoothHandler);
                        mConnectedThread.start();



                        if(isConnected(device)){
                            Intent intent = new Intent(getContext(),MainActivity.class);
                            intent.putExtra("데이터",device.getName());
                            startActivity(intent);
                        }

                        mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();

        }); // 연결 가능한 디바이스 클릭 이벤트

        return view;
    }

    public void CheckPairedDevice(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_DENIED) {
            pairedDevice = mBluetoothAdapter.getBondedDevices();
            if (pairedDevice.size() > 0) { // 디바이스가 있으면 작동함
                for (android.bluetooth.BluetoothDevice bt : pairedDevice) { // 체크해서 list에 add 해줘가지고 listview에 나타냄..
                    paired_list.add(new MyItemRecyclerViewAdapter.Customer2(bt.getName() + "\n" + bt.getAddress()));
                    adapter.notifyDataSetChanged(); // 어댑터 항목에 변화가 있음을 알려줌
                }
            } else {
            }
        } else { //샤오미에선 여기 돔
            pairedDevice = mBluetoothAdapter.getBondedDevices();
            if (pairedDevice.size() > 0) { // 디바이스가 있으면
                for (android.bluetooth.BluetoothDevice bt : pairedDevice) { // 체크해서 list에 add 해줘가지고 listview에 나타냄..
                    paired_list.add(new MyItemRecyclerViewAdapter.Customer2(bt.getName() + "\n" + bt.getAddress())); // 저 new Customer을 그냥 Device로 바꿔야함.
                    adapter.notifyDataSetChanged();
                }
            } else {
            }
        }
        listView_pairing.setAdapter(adapter);
    } // 등록된 디바이스 출력

    private final BroadcastReceiver mDeviceDiscoverReceiver = new BroadcastReceiver() {
        int cnt = 0;

        @SuppressLint("NotifyDataSetChanged")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // SDK가 31 이상일때
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "권한이 없습니다..", Toast.LENGTH_SHORT).show();
                    } else {
                        if (device.getName() != null) {
                            scan_list.add(new MyItemRecyclerViewAdapter.Customer2(device.getName()+"\n"+device.getAddress()));
                            adapter2.notifyDataSetChanged(); //갱신
                            cnt += 1;
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.v(TAG, "총 찾은 디바이스 개수 : " + cnt); // 블루투스가 꺼질때 동작한다.
                }

            } else {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if (device.getName() != null) {
                        scan_list.add(new MyItemRecyclerViewAdapter.Customer2(device.getName()+"\n"+device.getAddress()));
                        adapter2.notifyDataSetChanged(); //갱신
                        cnt += 1;
                    }
                }
            }
        }
    }; //----------------------- 브로드캐스트 리시버 정의 // device 스캔 작동 부분

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) { //툴바랑 연결된 메뉴
        inflater.inflate(R.menu.fragment_menu, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // 툴바 소환

        Toolbar toolbar = view.findViewById(R.id.bluetooth_toolbar);
        toolbar.inflateMenu(R.menu.fragment_menu);
//        toolbar.setTitle("블루투스 연결 설정");

        toolbar.setNavigationIcon(R.drawable.ic_baseline_keyboard_backspace_24);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(getContext(),MainActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_reset:  {
                Log.d(TAG, "onOptionsItemSelected: 아직 기능 구현 안함 새로고침");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                //블루투스가 활성화 되었다
                Toast.makeText(getContext(), "블루투스가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                //블루투스 켜는것을 취소했다.
                Toast.makeText(getContext(), "블루투스가 활성화 되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    } //startActivityForResult 실행 후 결과를 처리하는 함수

    @Override
    public void onDestroy(){
        super.onDestroy();
        requireContext().unregisterReceiver(mDeviceDiscoverReceiver);
//        mConnectedThread.interrupt();
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // 생명주기

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //권한 요청 하는 부분
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOACTION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {   // 권한요청이 허용된 경우
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();    // 권한요청이 거절된 경우
                }
            }
        }
    } // 권한 요청하는 함수

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
    } // 소켓 만들기 위한 메소드

    public boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    } // 블루투스 연결 됐는지 체크하는 함수 브로드캐스트로도 할 수 있지만 이거 사용해봤다

}