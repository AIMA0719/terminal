package com.example.ex;

import android.content.Context;
import android.graphics.Color;
import android.icu.text.Transliterator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    Context context;
    private static ArrayList<Customer> items = new ArrayList<>(); // items 라는 리스트 선언

    public CustomAdapter(Context context){
        this.context = context;
    } //생성자 선언

//    public CustomAdapter(Context context, ArrayList<Customer> arrayList){
//        this.context = context;
//        this.
//    }

    //-------------------------------------------------------- 리사이클러뷰 아이템 클릭동작 구현 위한 작업 -> 이건 뷰홀더안에 만드는게아니라 외부 엑티비티 혹은 프래그먼트에서 동학하기위해 인터페이스만든거

    public interface OnItemClickListener{ //온 아이템 리스너 인터페이스 선언
        void onItemClicked(int position,View view);
    }

    public static OnItemClickListener itemClickListener = null;  // 참조 변수 선언

    public void setOnItemClickListener(OnItemClickListener listener){  // OnItemClickListener 전달 메소드
        this.itemClickListener = listener;
    }

    //--------------------------------------------------------

    @Override
    public int getItemCount() { // 리스트에 있는거 개수센다
        return items.size();
    } // 개수 얻어오는 함수

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // ViewHolder 객체를 생성하여 리턴한다
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.bluetooth_device, parent, false);

        return new ViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) { // 뷰홀더안에 내용으로 position에 해당하는 데이터로 교체
        Customer item = items.get(position);
        holder.setItem(item);
    }

    public void addItem(Customer item){  //데이터 삽입 함수 + 중복체크 하는법 comparetor ? 도 있다
        boolean isExist = false;
        for (Customer customer : items){
            if (item.address.equals(customer.address)){
                isExist = true;
                return;
            }
        }
        if (!isExist){
            items.add(item);
        }
    }

     static class ViewHolder extends RecyclerView.ViewHolder { //뷰홀더 클래스
        TextView tv_name;
        TextView tv_address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.txtName);
            tv_address = itemView.findViewById(R.id.txtAddress);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION){
                    itemClickListener.onItemClicked(pos,v);
                }
            });
        }

        public void setItem(Customer item){
            tv_name.setText(item.name);
            tv_address.setText(item.address);
        }
    }
}

class Customer {
    public String name;
    public String address;

    public Customer(String name,String address){
        this.name = name;
        this.address = address;
    }
}
