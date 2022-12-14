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
            String bluetooth_name = getArguments().getString("??????");
            String[] slicing_name = bluetooth_name.split("\n");

            name.setText(slicing_name[0] +"\n"+ "????????? ?????? ???????????????????");

        }


        btn_ok.setOnClickListener(v -> {

            new Thread() {
                @Override
                public void run() {

                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (mBluetoothAdapter.isDiscovering()) { // ?????? ??????????
                        mBluetoothAdapter.cancelDiscovery(); //?????? ??????????????? ??????
                    }

                    boolean fail = false;

                    assert getArguments() != null;
                    device = mBluetoothAdapter.getRemoteDevice(getArguments().getString("??????").split("\n")[1]);

                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                        Log.d(TAG, "?????? ?????? ??????!");
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getContext(), "?????? ?????? ??????!", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mBluetoothSocket.connect();
                        Log.d(TAG, "?????? ?????? ??????!");
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBluetoothSocket.close();
                            mBluetoothHandler.obtainMessage(MainActivity.BT_CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getContext(), "?????? ?????? ??????!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (!fail) {
                        mConnectedThread = new ConnectedThread(mBluetoothSocket, mBluetoothHandler);
                        mConnectedThread.start(); // ??????

                        mBluetoothHandler.obtainMessage(MainActivity.BT_CONNECTING_STATUS, 1, -1, getArguments().getString("??????"))
                                .sendToTarget();


                        if(isConnected(device)){ //?????? ?????? ?????? ??????????????? ??????
                            mConnectedThread.write("atz"+"\r"); // ???????????? AT ????????? ???????????? ????????????

                            if (mBluetoothAdapter.isDiscovering()) { // ?????? ??????????
                                mBluetoothAdapter.cancelDiscovery(); //?????? ??????????????? ??????
                            }

                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("?????????",getArguments().getString("??????"));
                            startActivity(intent);
                        }
                    }
                }
            }.start();

        });

        btn_no.setOnClickListener(v -> {

            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction(); // bluetooth fragment??? ??????
            BluetoothFragment bluetoothFragment = new BluetoothFragment();
            transaction.replace(R.id.SecondFragment,bluetoothFragment);
            transaction.commit();

        });

        setCancelable(false); // ????????? ?????? ??? ?????? ??? ???????????? ???????????? ????????? ????????????

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
    } // ?????? ????????? ?????? ?????????

    public boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            return (boolean) m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    } // ???????????? ?????? ????????? ???????????? ?????? ???????????????????????? ??? ??? ????????? ?????? ???????????????

    public void onDestroy(){
        super.onDestroy();

    }



}