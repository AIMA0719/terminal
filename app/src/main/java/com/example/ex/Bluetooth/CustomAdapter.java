package com.example.ex.Bluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ex.R;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    Context context;
    private final List<Customer> items; // items 라는 리스트 선언

    public CustomAdapter(bluetooth context, List<Customer> items) {
        this.context = context;
        this.items = items;
    }



    public interface OnItemClickListener{ //온 아이템 리스너 인터페이스 선언
        void onItemClicked(int position,View view);
    }

    public static OnItemClickListener itemClickListener = null;  // 참조 변수 선언

    public void setOnItemClickListener(OnItemClickListener listener){  // OnItemClickListener 전달 메소드
        this.itemClickListener = listener;
    }

    //--------------------------------------------------------

    public class ViewHolder extends RecyclerView.ViewHolder { //뷰홀더 클래스
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.device);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if(pos != RecyclerView.NO_POSITION){
                    itemClickListener.onItemClicked(pos,v);
                }
            });

        }
    }

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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Customer item = items.get(position);

        holder.textView.setText(item.getName());
    }

//    public void removeItemView(int position) {
//        items.remove(position);
//        notifyItemRemoved(position);
//        notifyItemRangeChanged(position,items.size());
//    } //삭제하는 메소드드
}

