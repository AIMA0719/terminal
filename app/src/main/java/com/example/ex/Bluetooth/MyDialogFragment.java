package com.example.ex.Bluetooth;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.ex.R;

public class MyDialogFragment extends DialogFragment {
    public static final String TAG_EVENT_DIALOG = "dialog_event";

    public MyDialogFragment () {}

    public static MyDialogFragment getInstance(){
        MyDialogFragment e = new MyDialogFragment();
        return e;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog, container, false);
        Button btn_ok = view.findViewById(R.id.ok);
        Button btn_no = view.findViewById(R.id.no);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "확인버튼 클릭함", Toast.LENGTH_SHORT).show();

            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        setCancelable(false); // 유저가 화면 밖 검은 곳 터치하면 취소되게 하는거 방지용용

        return view;
    }

}