package com.example.ex;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ex.placeholder.PlaceholderContent;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderContent.Customer2}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private final List<Customer2> items;
    private static final String TAG = "adapter";

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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Customer2 item = items.get(position);
        holder.textView.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View view) {
            super(view);

            textView = view.findViewById(R.id.device);

        }

    }
}

class Customer2 {
    public String device;

    public Customer2(String name){
        this.device = name;
    }

    public String getName(){
        return device;
    }
}