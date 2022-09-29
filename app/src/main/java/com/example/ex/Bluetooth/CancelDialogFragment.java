package com.example.ex.Bluetooth;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.ex.R;

public class CancelDialogFragment extends DialogFragment {

    public TextView name;
    public BluetoothAdapter mBluetoothAdapter;

    final String TAG = "Dialog_Fragment";

    public CancelDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cancel_dialog, container, false);
        name = view.findViewById(R.id.cancel_bluetooth_name);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (getArguments() != null) {
            String bluetooth_name = getArguments().getString("이름");
            String[] slicing_name = bluetooth_name.split("\n");
            Log.e(TAG, "onCreateView: "+slicing_name[0] );

            name.setText(slicing_name[0] +"\n"+ "기기와 연결 취소 하시겠습니까?");

        }
//
//        cancel_ok.setOnClickListener(v -> {
//
//        });
//
//        cancel_no.setOnClickListener(v ->{
//
//        });
//        setCancelable(false); // 유저가 화면 밖 검은 곳 터치하면 취소되게 하는거 방지용용

        return view;
    }

    public void onDestroy(){
        super.onDestroy();

    }

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.fragment_cancel_dialog,null))
                .setTitle("블루투스 연결 설정")
                .setPositiveButton(R.string.cancel_ok, (dialog, which) -> {
                    Log.e(TAG, "onCreateDialog: dsf" );
                })
                .setNegativeButton("취소",(dialog, which) -> {
                    dialog.dismiss();
                });
        return builder.create();
    }

}