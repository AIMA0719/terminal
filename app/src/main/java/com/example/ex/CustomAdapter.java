package com.example.ex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    Context context;
    private ArrayList<Customer> items = new ArrayList<Customer>();

    public CustomAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getItemCount() { // 리스트에 있는거 개수센다
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // View 홀더 객체를 생성하여 리턴한다
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.bluetooth_device,parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) { // 뷰홀더안에 내용으로 position에 해당하는 데이터로 교체
        Customer item = items.get(position);
        holder.setItem(item);
    }

    public void addItem(Customer item){  //중복체크 하는법 ex)
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        TextView tv_address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.txtName);
            tv_address = itemView.findViewById(R.id.txtAddress);

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
