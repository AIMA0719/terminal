package com.example.ex.Bluetooth;

import static com.example.ex.Bluetooth.BluetoothFragment.BT_MESSAGE_READ;
import static com.example.ex.Bluetooth.BluetoothFragment.BT_MESSAGE_WRITE;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ConnectedThread extends Thread  {

    BluetoothSocket mBluetoothSocket;
    InputStream mmInStream;
    OutputStream mmOutStream;
    private byte[] mmBuffer;
    public static String readMessage;
    public static String Data = "";
    String TAG = "ConnectedThread";

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mBluetoothSocket = socket;
        BluetoothFragment.mBluetoothHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException ignored) { }

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

                        if (Data.contains(">")||Data.contains("at")) { // > 뒤에 계속 추가되는거 방지용 초기화
                            Log.d(TAG, "Request 메세지 전달 받음");
                            Message message = BluetoothFragment.mBluetoothHandler.obtainMessage(bluetooth.BT_MESSAGE_READ, mmBuffer.length, -1, Data); //
                            message.sendToTarget();
                            Log.d(TAG, "핸드폰으로 Response 메세지 전달");

                            Data = ""; // Data 마지막이니까 초기화해줌
//                            Log.d(TAG, "마지막 데이터 입니다.");
                        }

                }

            } catch (IOException e) {
                e.printStackTrace();

                break;
            }
            //------------------------ 바이트 단위로 보냄 ex) 01234 를 0 1 2 3 4 이렇게
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
            //------------------------

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
            bundle.putString("toast", "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            BluetoothFragment.mBluetoothHandler.sendMessage(writeErrorMsg);
        }

    }

    public void cancel() {
        try {
            BluetoothFragment.mBluetoothSocket.close();
        } catch (IOException ignored) { }
    }



} // 연결하기 위한 스레드