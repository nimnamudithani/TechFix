package com.nibm.techfix.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.database.TechnicianDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.Technician;

import java.util.List;

public class AdminTechnicianAdapter extends RecyclerView.Adapter<AdminTechnicianAdapter.ViewHolder> {

    public interface Listener {
        void onDelete(Technician technician);
    }

    private final List<Technician> technicians;
    private final BranchDao branchDao;
    private final TechnicianDao technicianDao;
    private final Listener listener;

    public AdminTechnicianAdapter(List<Technician> technicians, Context context, Listener listener) {
        this.technicians = technicians;
        this.branchDao = new BranchDao(context);
        this.technicianDao = new TechnicianDao(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_technician, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Technician tech = technicians.get(position);
        Branch branch = branchDao.getBranchById(tech.getBranchId());

        holder.tvName.setText(tech.getName());
        holder.tvDetails.setText(tech.getSpecialization() + " - " + (branch != null ? branch.getName() : ""));

        holder.switchAvailable.setOnCheckedChangeListener(null);
        holder.switchAvailable.setChecked(tech.isAvailable());
        holder.switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            technicianDao.updateTechnicianAvailability(tech.getId(), isChecked);
            tech.setAvailable(isChecked);
        });

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(tech));
    }

    @Override
    public int getItemCount() {
        return technicians.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        Switch switchAvailable;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            switchAvailable = itemView.findViewById(R.id.switchAvailable);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
