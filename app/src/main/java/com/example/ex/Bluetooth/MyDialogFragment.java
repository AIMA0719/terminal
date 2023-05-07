package com.example.ex.Bluetooth;

import static com.example.ex.Bluetooth.BluetoothFragment.device;
import static com.example.ex.Bluetooth.BluetoothFragment.mBluetoothHandler;
import static com.example.ex.Bluetooth.BluetoothFragment.mConnectedThread;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ex.MainActivity.MainActivity;
import com.example.ex.R;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MyDialogFragment extends DialogFragment {


    public static final int BT_MESSAGE_READ = 2;
    public TextView name;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket mBluetoothSocket;

    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805f9b34fb");
    final String TAG = "Dialog_Fragment";

    public MyDialogFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog, container, false);
        Button btn_ok = view.findViewById(R.id.ok);
        Button btn_no = view.findViewById(R.id.no);
        name = view.findViewById(R.id.bluetooth_name);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (getArguments() != null) {
            String bluetooth_name = getArguments().getString("이름");
            String[] slicing_name = bluetooth_name.split("\n");

            name.setText(slicing_name[0] +"\n"+ "기기와 연결 하시겠습니까?");

        }


        btn_ok.setOnClickListener(v -> {

            new Thread() {
                @Override
                public void run() {

                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (mBluetoothAdapter.isDiscovering()) { // 검색 중인가?
                        mBluetoothAdapter.cancelDiscovery(); //검색 상태였으면 취소
                    }

                    boolean fail = false;

                    assert getArguments() != null;
                    device = mBluetoothAdapter.getRemoteDevice(getArguments().getString("이름").split("\n")[1]);

                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                        Log.d(TAG, "소켓 생성 완료!");
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getContext(), "소켓 생성 실패!", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mBluetoothSocket.connect();
                        Log.d(TAG, "소켓 연결 완료!");
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBluetoothSocket.close();
                            mBluetoothHandler.obtainMessage(MainActivity.BT_CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getContext(), "소켓 생성 실패!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!fail) {
                        mConnectedThread = new ConnectedThread(mBluetoothSocket, mBluetoothHandler);
                        mConnectedThread.start(); // 시작

                        mBluetoothHandler.obtainMessage(MainActivity.BT_CONNECTING_STATUS, 1, -1, getArguments().getString("이름"))
                                .sendToTarget();


                        if(isConnected(device)){ //연결 되면 메인 엑티비티로 이동
                            mConnectedThread.write("atz"+"\r"); // 시작할때 AT 커맨드 설정위해 날려준다

                            if (mBluetoothAdapter.isDiscovering()) { // 검색 중인가?
                                mBluetoothAdapter.cancelDiscovery(); //검색 상태였으면 취소
                            }

                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("데이터",getArguments().getString("이름"));
                            startActivity(intent);
                        }
                    }
                }
            }.start();

        });

        btn_no.setOnClickListener(v -> {

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction(); // bluetooth fragment로 이동
            BluetoothFragment bluetoothFragment = new BluetoothFragment();
            transaction.replace(R.id.SecondFragment,bluetoothFragment);
            transaction.commit();

        });

        setCancelable(false); // 유저가 화면 밖 검은 곳 터치하면 취소되게 하는거 방지

        return view;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        return device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
    } // 소켓 만들기 위한 메소드

    public boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            return (boolean) m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    } // 블루투스 연결 됐는지 체크하는 함수 브로드캐스트로도 할 수 있지만 이거 사용해봤다

    public void onDestroy(){
        super.onDestroy();

    }



}