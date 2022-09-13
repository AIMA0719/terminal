package com.example.ex;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import static com.example.ex.BluetoothFragment.BT_MESSAGE_READ;
import static com.example.ex.BluetoothFragment.BT_MESSAGE_WRITE;
import static com.example.ex.BluetoothFragment.mConnectedThread;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.room.Database;

import com.example.ex.DB.MainData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ConnectedThread extends Thread implements Serializable {

    BluetoothSocket mBluetoothSocket;
    Handler mBluetoothHandler;
    InputStream mmInStream;
    OutputStream mmOutStream;
    private byte[] mmBuffer;
    public static String readMessage;
    String Data = "";
    String TAG = "ConnectedThread";

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mBluetoothSocket = socket;
        mBluetoothHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;


        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        mmBuffer = new byte[1024];
        int bytes;
        while (true) {
            try {
                bytes = mmInStream.read(mmBuffer,0,mmBuffer.length);
                if(bytes != 0) {
                    byte temp = (byte) bytes; // length
                    try {
                        readMessage = new String(mmBuffer, 0, temp, StandardCharsets.UTF_8).trim();
                        Data += readMessage;
                    } catch (Exception e) {
                        Log.d(TAG, "run: 오류남");
                    }

                    Message message = mBluetoothHandler.obtainMessage(bluetooth.BT_MESSAGE_READ, mmBuffer.length, -1, Data); //
                    message.sendToTarget();

                    Log.d(TAG, "쓰레드에서 보낸 데이터 : " + Data);
                    if (Objects.equals(readMessage, ">")) { // > 뒤에 계속 추가되는거 방지용 초기화
                        Data = "";
                        Log.d(TAG, "더이상 쓰레드에서 보낼 데이터가 없습니다.");
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();

                break;
            }
            //try{
            //    msg = "";
            //    byte[] buffer = new byte[1];
            //    bytes = mmInStream.read(buffer,0,buffer.length);
            //    s = new String(buffer);
//
            //    for (int i = 0;i<s.length();i++){
            //        char x = s.charAt(i);
            //        msg = msg +x;
//
            //        if ( x== 0x3e){
            //            Message message = mBluetoothHandler.obtainMessage(bluetooth.BT_MESSAGE_READ, buffer.length, -1, msg);
            //            message.sendToTarget();
            //        }
            //    }
            //    Log.d(TAG, "run: "+msg);
//
            //} catch (IOException e) {
            //    e.printStackTrace();
            //}

        }
    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
            Message writeMessage = mBluetoothHandler.obtainMessage(BT_MESSAGE_WRITE,-1,-1,mmBuffer);
            writeMessage.sendToTarget();
        } catch (IOException e) { }
//
//        try {
//            mmOutStream.write(bytes);
//            Message writeMessage = mBluetoothHandler.obtainMessage(BT_MESSAGE_READ,-1,-1,mmBuffer);
//            writeMessage.sendToTarget();
//
//        } catch (IOException e) {
//            Log.e(TAG, "Error occurred when sending data", e);
//
//            // Send a failure message back to the activity.
//            Message writeErrorMsg =
//                    mBluetoothHandler.obtainMessage(BT_MESSAGE_READ);
//            Bundle bundle = new Bundle();
//            bundle.putString("toast",
//                    "Couldn't send data to the other device");
//            writeErrorMsg.setData(bundle);
//            mBluetoothHandler.sendMessage(writeErrorMsg);
//        }
    }

    public void cancel() {
        try {
            mBluetoothSocket.close();
        } catch (IOException ignored) { }
    }

} // 연결하기 위한 스레드