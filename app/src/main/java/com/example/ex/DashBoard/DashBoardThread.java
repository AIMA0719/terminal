package com.example.ex.DashBoard;
import static com.example.ex.Bluetooth.BluetoothFragment.BT_MESSAGE_WRITE;
import static com.example.ex.Bluetooth.MyDialogFragment.BT_MESSAGE_READ;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.example.ex.Bluetooth.BluetoothFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DashBoardThread extends Thread {
    public static final int DATA_SEND = 11;
    boolean isRun = false;
    int value = 0;
    BluetoothSocket mBluetoothSocket;
    InputStream mmInStream;
    OutputStream mmOutStream;
    private byte[] mmBuffer;
    public static String readMessage;
    public static String Data = "";
    public final String TAG = "ConnectedThread";
    public static final String [] DashBoard_Data = {"0105","010c","010d","0142","0110"};
    public int index = 0;


    public DashBoardThread(BluetoothSocket socket) {
        mBluetoothSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException ignored) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }


    @Override
    public void run() {
        super.run();
        isRun = true;
        mmBuffer = new byte[1024];
        int bytes;
        Handler handler1 = new Handler();
        DashBoard.mDashBoardThread.write("010d"+"\r");
        while (isRun) {
            try {
                bytes = mmInStream.read(mmBuffer, 0, mmBuffer.length);
                if (bytes != 0) {
                    byte temp = (byte) bytes; // length
                    try {
                        readMessage = new String(mmBuffer, 0, temp, StandardCharsets.UTF_8).trim();
                        Data += readMessage;
                    } catch (Exception e) {
                        Log.d(TAG, "run: 오류남");
                    }

                    if (Data.contains(">")) { // > 뒤에 계속 추가되는거 방지용 초기화

                        while (true) {
                            DashBoard.mDashBoardThread.write(DashBoard_Data[index]+"\r");
                            sleep(100);
                            index += 1;
                            Message message = handler1.obtainMessage(DATA_SEND, mmBuffer.length, -1, Data); //
                            message.sendToTarget();
                        }
                    }
                    else {
                        Message message = handler1.obtainMessage(DATA_SEND, mmBuffer.length, -1, Data); //
                        message.sendToTarget();
                    }
                    Data = "";
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();

                break;
            }
        }
    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
            Message writeMessage = BluetoothFragment.mBluetoothHandler.obtainMessage(BT_MESSAGE_WRITE,-1,-1,mmBuffer);
            writeMessage.sendToTarget();
        } catch (IOException e) {

            Log.e(TAG, "데이터 보내기 오류!", e);
            Message writeErrorMsg = BluetoothFragment.mBluetoothHandler.obtainMessage(BT_MESSAGE_READ);
            Bundle bundle = new Bundle();
            bundle.putString("toast", "다른기기에 데이터를 보낼 수 없습니다.");
            writeErrorMsg.setData(bundle);
            BluetoothFragment.mBluetoothHandler.sendMessage(writeErrorMsg);
        }

    }


}