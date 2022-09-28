package com.example.ex.Bluetooth;

import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ex.R;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private final List<Customer2> items;
    private static final String TAG = "adapter";
    public static boolean Connection_flag = true;

    public interface OnItemClickListener{ //온 아이템 리스너 인터페이스 선언
        void onItemClicked(int position,View view);
    }

    public OnItemClickListener itemClickListener = null;  // 참조 변수 선언

    public void setOnItemClickListener(OnItemClickListener listener){  // OnItemClickListener 전달 메소드
        this.itemClickListener = listener;
    }

    public MyItemRecyclerViewAdapter(List<Customer2> items,Context context) {
        this.items = items;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listview = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device,parent,false);
        ViewHolder holder = new ViewHolder(listview);
        return  holder;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Customer2 item = items.get(position);

        String content = item.getStatus(); // 바꾸고 싶은 부분의 Text 가져와서
        SpannableString spannableString = new SpannableString(content);  // SpannableString 객체에 넣어줌

        int start = content.indexOf(item.getStatus()); //바꾸고 싶은 부분 시작
        int end = start + item.getStatus().length(); //바꾸고 싶은 부분 끝

        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#438bff")),start,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 색깔 바꿔주고
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),start,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 굵은 글씨로 바꿔 줌

        holder.textView.setText(item.getDevice());

        if(Connection_flag){
            if(item.getStatus().equals("연결 안 됨")){
                holder.status.setText(item.getStatus()); // 넣어줌
            }else {
                holder.status.setText(spannableString);
            }
        }else {
            holder.status.setText("연결 안 됨");
        }


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        TextView status;

        public ViewHolder(View view) {
            super(view);

            textView = view.findViewById(R.id.device);
            status = view.findViewById(R.id.bluetooth_boolean);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION){
                    if(itemClickListener!=null){
                        itemClickListener.onItemClicked(pos,v);
                    }
                }
            });

        }
    }

    public static class Customer2 {
        public String device;
        public String status;

        public Customer2(String name,String status){
            this.device = name;
            this.status = status;
        }

        public String getDevice(){
            return device;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setDevice(String device) {
            this.device = device;
        }
    }
}

