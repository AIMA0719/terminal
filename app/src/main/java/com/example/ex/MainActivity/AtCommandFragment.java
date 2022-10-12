package com.example.ex.MainActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ex.R;

import java.util.ArrayList;

public class AtCommandFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ATCommandsFragment";
    private static RecyclerView AT_Commands_ListView;

    private final ArrayList<AtCommandFragmentAdapter.PIDS> At_commands = new ArrayList<>(); //리사이클러뷰 리스트 생성
    private final AtCommandFragmentAdapter adapter = new AtCommandFragmentAdapter(At_commands,getContext()); //어댑터 생성
    private final String [] AT_COMMAND_LIST = {""};


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AtCommandFragment() {
        // Required empty public constructor
    }
    public static AtCommandFragment newInstance(String param1, String param2) {
        AtCommandFragment fragment = new AtCommandFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        MainActivity.screenflag = 1;

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) { //백버튼
            @Override
            public void handleOnBackPressed() {
                MainActivity.screenflag = 0;
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_at_command, container, false);

        AT_Commands_ListView = view.findViewById(R.id.AT_Commands);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        AT_Commands_ListView.setLayoutManager(layoutManager);
        AT_Commands_ListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // 툴바 소환

        Toolbar toolbar = view.findViewById(R.id.AT_Commands_toolbar);
        toolbar.inflateMenu(R.menu.fragment_menu);

        toolbar.setNavigationIcon(R.drawable.ic_baseline_keyboard_backspace_24); // 뒤로가기 버튼 누르면 동작
        toolbar.setNavigationOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Intent intent = new Intent(getContext(),MainActivity.class);
            startActivity(intent);
        });
    } // 뒤로가기 버튼
}