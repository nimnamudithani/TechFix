package com.nibm.techfix.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.database.BranchDao;
import com.nibm.techfix.models.Branch;
import com.nibm.techfix.models.SparePart;

import java.util.List;

public class AdminSparePartAdapter extends RecyclerView.Adapter<AdminSparePartAdapter.ViewHolder> {

    public interface Listener {
        void onEditStock(SparePart part);
        void onDelete(SparePart part);
    }

    private final List<SparePart> parts;
    private final BranchDao branchDao;
    private final Listener listener;

    public AdminSparePartAdapter(List<SparePart> parts, Context context, Listener listener) {
        this.parts = parts;
        this.branchDao = new BranchDao(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_sparepart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SparePart part = parts.get(position);
        Branch branch = branchDao.getBranchById(part.getBranchId());

        holder.tvName.setText(part.getName());
        holder.tvDetails.setText((branch != null ? branch.getName() : "") +
                " - Stock: " + part.getStockQty() + " - Rs. " + (int) part.getUnitPrice());

        holder.btnEditStock.setOnClickListener(v -> listener.onEditStock(part));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(part));
    }

    @Override
    public int getItemCount() {
        return parts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        Button btnEditStock, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            btnEditStock = itemView.findViewById(R.id.btnEditStock);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
