package com.example.location_requester1.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location_requester1.R;
import com.example.location_requester1.models.CallsModel;

import java.util.ArrayList;
import java.util.List;

public class CallRecordsAdapter extends RecyclerView.Adapter<CallRecordsAdapter.ViewHolder> {

    Context context;
    List<CallsModel> dataModel;
    OnClickListener listener;
    List<CallsModel>  selectedModel = new ArrayList<>(1);
    int selectedPosition = -1;
    public CallRecordsAdapter(Context context, List<CallsModel> dataModel, OnClickListener listener) {
        this.context = context;
        this.dataModel = dataModel;
        this.listener = listener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.calls_info_layout, parent, false));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CallsModel model = dataModel.get(position);


        String name = model.getName();
        if (name != null) {
            if (name.length() == 0) {
                holder.callerName.setText(model.getPhoneNo());
            } else {
                holder.callerName.setText(model.getName());
            }
        } else {
            holder.callerName.setText(model.getPhoneNo());
        }
        holder.callDateTime.setText(model.getDate());

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            listener.onClick(position);
            if (selectedModel.contains( model)) {
                selectedModel.remove(model);
            } else {
                selectedModel.clear();
                selectedModel.add(model);
            }
            Log.d("selectedModel", "onBindViewHolder: " + selectedModel.size());
        });

        if (position == selectedPosition) {
//            holder.cb.setVisibility(View.VISIBLE);
//            holder.cb.setChecked(true);
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.grey));
        } else {
//            holder.cb.setVisibility(View.GONE);
//            holder.cb.setChecked(false);
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));

        }
    }

    @Override
    public int getItemCount() {
        if (dataModel != null) {
            return dataModel.size();
        } return  0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView callerName, callDateTime;


        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            callerName = itemView.findViewById(R.id.callerName);
            callDateTime = itemView.findViewById(R.id.callDateTime);

            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}