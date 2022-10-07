package com.example.ex.Bluetooth;


import static com.example.ex.Bluetooth.BluetoothFragment.BT_MESSAGE_WRITE;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConnectedThread extends Thread  {

    public BluetoothSocket mBluetoothSocket;
    public InputStream mmInStream;
    public OutputStream mmOutStream;
    private byte[] mmBuffer;
    public static String readMessage;
    public static String Data = "";
    public final String TAG = "ConnectedThread";
    public static boolean first_connection;
    public final String[] DefaultATCommandArray = new String[]{"ATE1","ATD0","ATSP0","ATH1","ATM0","ATS0","ATAT1","ATST64"};


    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mBluetoothSocket = socket;
        BluetoothFragment.mBluetoothHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        if(mBluetoothSocket != null){
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) {
                Log.e(TAG, "ConnectedThread: 소켓 이 닫혀있습니다!" );
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

    }

    @Override
    public void run() {
        if(mBluetoothSocket != null){
            mmBuffer = new byte[1024];
            int bytes;
            int index = 0;

            while (true) {
                try {
                    bytes = mmInStream.read(mmBuffer,0,mmBuffer.length);
                    if(bytes != 0) {
                        byte temp = (byte) bytes; // length
                        try {
                            readMessage = new String(mmBuffer, 0, temp, StandardCharsets.UTF_8).trim();
                            Data += readMessage;
                        } catch (Exception e) {
                            Log.d(TAG, "Data read 실패!");
                        }

                        if (Data.contains(">")) { // > 뒤에 계속 추가되는거 방지용 초기화

                            if(first_connection){ // 처음 디폴트 AT 커맨드 설정

                                if(index<DefaultATCommandArray.length){
                                    write(DefaultATCommandArray[index] + "\r");  // 하나씩 호출하고 받으면 또 다음 인덱스 호출하고..
                                    index += 1;
                                }else {
                                    write("0105\r"); // 이걸 보내는 이유는 AT 세팅 완료 후 처음에만 Request 할 때
                                    // 0105SEARCHING...7E8034105C47E9034105C4> 이 SEARCHING...이 뜨고 Response 속도가 1초정도 느려서 더미데이터처럼 없얘기 위함...
                                    first_connection = false; // 다 설정하면 flag false로..
                                }
                                Log.e(TAG, "세팅한 AT 커맨드 : " + Data);

                            }else { // 처음 연결이 아닐 때
                                if(Data.contains("NO DATA")){  // 데이터가 없으면
                                    Message message = BluetoothFragment.mBluetoothHandler.obtainMessage(BluetoothFragment.BT_MESSAGE_READ, mmBuffer.length, -1, Data); //
                                    message.sendToTarget();
                                }
                                else if (Data.contains("at")||(Data.contains("OBD")||(Data.contains("AT")))){
                                    Message message = BluetoothFragment.mBluetoothHandler.obtainMessage(BluetoothFragment.BT_MESSAGE_READ, mmBuffer.length, -1, Data); //
                                    message.sendToTarget();

                                }
                                else { // 일반 명령어 입력시
                                    Log.d(TAG, "Request 메세지 전달 받음");
                                    Message message = BluetoothFragment.mBluetoothHandler.obtainMessage(BluetoothFragment.BT_MESSAGE_READ, mmBuffer.length, -1, Data); //
                                    message.sendToTarget();
                                    Log.d(TAG, "핸드폰으로 Response 메세지 "+Data +" 전달");
                                }

                            }
                            Data = ""; // Data 마지막이니까 초기화해줌 안해주면 계속 쌓인다
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                //------------------------ 아래 코드는 바이트 단위로 보냄 ex) 01234 를 0 1 2 3 4 이렇게
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

    }

    public void write(String input) {
        byte[] bytes = input.getBytes();
        try {
            if(mBluetoothSocket != null){
                mmOutStream.write(bytes);
                Message writeMessage = BluetoothFragment.mBluetoothHandler.obtainMessage(BT_MESSAGE_WRITE,-1,-1,bytes);
                writeMessage.sendToTarget();
            }
            else {
                Log.e(TAG, "소켓 연결 실패!" );
            }
        } catch (IOException e) {
            Log.e(TAG, "데이터 보내기 실패!", e);
        }

    }

    public void cancel() {
        try {
            if(mmInStream != null){
                mmInStream.close();
                mmInStream = null;
            }
            if(mmOutStream != null){
                mmOutStream.close();
                mmOutStream = null;
            }
            if(BluetoothFragment.mBluetoothSocket != null){
                BluetoothFragment.mBluetoothSocket = null;
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}