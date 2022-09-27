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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ex.MainActivity.MainActivity;
import com.example.ex.R;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class CancelDialogFragment extends Fragment {

    public TextView name;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothSocket mBluetoothSocket;

    private static final UUID MY_UUID = UUID.fromString("0001101-0000-1000-8000-00805f9b34fb");
    final String TAG = "Dialog_Fragment";

    public CancelDialogFragment() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cancel_dialog, container, false);
        Button cancel_ok = view.findViewById(R.id.cancel_ok);
        Button cancel_no = view.findViewById(R.id.cancel_no);
        name = view.findViewById(R.id.cancel_bluetooth_name);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (getArguments() != null) {
            String bluetooth_name = getArguments().getString("이름");
            String[] slicing_name = bluetooth_name.split("\n");
            Log.e(TAG, "onCreateView: "+slicing_name[0] );

            name.setText(slicing_name[0] +"\n"+ "기기와 연결 취소 하시겠습니까?");

        }

        cancel_ok.setOnClickListener(v -> {

        });

        cancel_no.setOnClickListener(v ->{

        });


//        setCancelable(false); // 유저가 화면 밖 검은 곳 터치하면 취소되게 하는거 방지용용

        return view;
    }

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