package com.nibm.techfix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.models.RepairService;
import com.nibm.techfix.utils.RepairInfoUtils;

import java.util.List;

public class RepairServiceAdapter extends RecyclerView.Adapter<RepairServiceAdapter.ServiceViewHolder> {

    public interface OnServiceClickListener {
        void onServiceClick(RepairService service);
    }

    private final List<RepairService> services;
    private final OnServiceClickListener listener;

    public RepairServiceAdapter(List<RepairService> services, OnServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_repair_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        RepairService service = services.get(position);
        holder.tvName.setText(service.getName());
        holder.tvDescription.setText(service.getDescription());
        holder.tvPrice.setText("Estimated cost: Rs. " + (int) service.getBasePrice() + "*");
        holder.tvEstimatedTime.setText("⏱ Estimated completion: " + RepairInfoUtils.estimatedTime(service.getName()));
        holder.itemView.setOnClickListener(v -> listener.onServiceClick(service));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice, tvEstimatedTime;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
            tvEstimatedTime = itemView.findViewById(R.id.tvEstimatedTime);
        }
    }
}
