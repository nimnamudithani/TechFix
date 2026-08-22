package com.nibm.techfix.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.database.CatalogDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.RepairAppointment;
import com.nibm.techfix.models.RepairService;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentClickListener {
        void onAppointmentClick(RepairAppointment appointment);
    }

    private final List<RepairAppointment> appointments;
    private final BranchDao branchDao;
    private final CatalogDao catalogDao;
    private final OnAppointmentClickListener listener;

    public AppointmentAdapter(List<RepairAppointment> appointments, Context context, OnAppointmentClickListener listener) {
        this.appointments = appointments;
        this.branchDao = new BranchDao(context);
        this.catalogDao = new CatalogDao(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        RepairAppointment appt = appointments.get(position);

        RepairService service = catalogDao.getServiceById(appt.getRepairServiceId());
        Branch branch = branchDao.getBranchById(appt.getBranchId());

        holder.tvServiceName.setText(service != null ? service.getName() : "Repair Service");
        holder.tvBranchName.setText(branch != null ? branch.getName() : "");
        holder.tvDate.setText(appt.getRequestedDate());
        holder.tvStatus.setText(appt.getStatus());

        int color;
        switch (appt.getStatus()) {
            case RepairAppointment.STATUS_COMPLETED:
            case RepairAppointment.STATUS_PAID:
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.success);
                break;
            case RepairAppointment.STATUS_IN_PROGRESS:
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.accent);
                break;
            default: // Pending, Assigned
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.primary);
        }
        GradientDrawable pill = (GradientDrawable) holder.tvStatus.getBackground().mutate();
        pill.setColor(color);

        holder.itemView.setOnClickListener(v -> listener.onAppointmentClick(appt));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvBranchName, tvDate, tvStatus;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
