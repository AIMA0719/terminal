package com.example.ex.MainActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ex.R;

import java.util.ArrayList;

public class AtCommandFragmentAdapter extends RecyclerView.Adapter<AtCommandFragmentAdapter.ViewHolder> {

    private ArrayList<PIDS> items;
    Context context;

    public AtCommandFragmentAdapter(ArrayList<PIDS> at_commands, Context context) {
        this.items = at_commands;
        this.context = context;
    }


    @NonNull
    @Override
    public AtCommandFragmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onbind(items.get(position),position);
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textView,blabla;

        public ViewHolder(@NonNull View view)
        {
            super(view);
            textView = view.findViewById(R.id.Commands);
            blabla = view.findViewById(R.id.blabla);
        }

        public void onbind(PIDS pids, int position) {
            String s = ""+ (position+1);
            textView.setText(pids.getCommand());
            blabla.setText(pids.getBlaBla());
        }
    }

    public static class PIDS{

        public String Command;
        public String BlaBla;

        public PIDS(String Command,String BlaBla){
            this.Command = Command;
            this.BlaBla = BlaBla;
        }

        public String getCommand(){
            return Command;
        }

        public String getBlaBla() {
            return BlaBla;
        }

        public void setCommand(String status) {
            this.Command = status;
        }

        public void setBlaBla(String device) {
            this.BlaBla = device;
        }

    }

}



