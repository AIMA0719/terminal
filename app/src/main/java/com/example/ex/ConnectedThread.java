package com.example.ex;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import static com.example.ex.BluetoothFragment.BT_MESSAGE_READ;

import android.bluetooth.BluetoothSocket;
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

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mBluetoothSocket = socket;
        mBluetoothHandler = handler;
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

    @Override
    public void run() {
        mmBuffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = mmInStream.read(mmBuffer,0,mmBuffer.length);
                Log.d(TAG, "처음 read한 bytes : "+ bytes);

                if(bytes != 0) {
                    SystemClock.sleep(100); // term 주기
//                    bytes = mmInStream.available(); // how many bytes are ready to be read?
//                    bytes = mmInStream.read(buffer, 0, buffer.length);// record how many bytes we actually read
                    byte temp = (byte)bytes;
                    Log.d(TAG, "처음 read한 bytes를 형변환 : "+temp);
                    try {
                        String b = new String(mmBuffer,0,temp, StandardCharsets.UTF_8);
                        Log.d(TAG, "bytes 를 String으로 : "+b);
//
                        Message message = mBluetoothHandler.obtainMessage(bluetooth.BT_MESSAGE_READ, bytes, -1, mmBuffer);
                        Log.d(TAG, "메세지는 : "+message);
                        message.sendToTarget();
                    }catch (Exception e){

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

    /* Call this from the main activity to send data to the remote device */
    public void write(String input) {
        byte[] bytes = input.getBytes();           //converts entered String into bytes
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

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mBluetoothSocket.close();
        } catch (IOException ignored) { }
    }

} // 연결하기 위한 스레드