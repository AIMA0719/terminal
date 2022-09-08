package com.example.ex;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import static com.example.ex.BluetoothFragment.BT_MESSAGE_READ;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;


public class ConnectedThread extends Thread implements Serializable {

    BluetoothSocket mBluetoothSocket;
    Handler mBluetoothHandler;
    InputStream mmInStream;
    OutputStream mmOutStream;
    private byte[] mmBuffer;
    public static String readMessage;

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
//                    SystemClock.sleep(100); // term 주기 -> 실시간 통신에는 부적합하다.. 보통 1초에 5~6번 하는데 슬립들어가면 x
                    byte temp = (byte) bytes; // length

                    try {
                        readMessage = new String(mmBuffer, 0, temp, StandardCharsets.UTF_8).trim();
//                        Log.d(TAG, "bytes 를 String으로 : "+readMessage);
                        //readMessage를 바꿔서 메세지로 보내면 이득일거같음
                        Message message = mBluetoothHandler.obtainMessage(bluetooth.BT_MESSAGE_READ, bytes, -1, mmBuffer);
                        message.sendToTarget();


                    } catch (Exception e) {
                        Log.d(TAG, "run: 오류남");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();

                break;
            }


            //                s = new String(buffer);
//                for(int i = 0; i < s.length(); i++){
//                    char x = s.charAt(i);
//                    msg = msg + x;
//                    if (x == 0x3e) {
//                        mBluetoothHandler.obtainMessage(BluetoothFragment.BT_MESSAGE_READ, buffer.length, -1, msg).sendToTarget();
//                        msg="";
//                    }
//                }
//                Log.d(TAG, "run: "+msg);
        }
    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            mmOutStream.write(bytes);
            Message writeMessage = mBluetoothHandler.obtainMessage(BT_MESSAGE_READ,-1,-1,mmBuffer);
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