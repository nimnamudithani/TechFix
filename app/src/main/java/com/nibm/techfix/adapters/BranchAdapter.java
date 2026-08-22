package com.nibm.techfix.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;
import com.nibm.techfix.models.Branch;

import java.util.List;

public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

    public interface OnBranchClickListener {
        void onBranchClick(Branch branch);
    }

    private final List<Branch> branchList;
    private final OnBranchClickListener listener;

    public BranchAdapter(List<Branch> branchList, OnBranchClickListener listener) {
        this.branchList = branchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branchList.get(position);
        holder.tvName.setText(branch.getName());
        holder.tvAddress.setText(branch.getAddress());
        holder.tvContact.setText("☎ " + branch.getContactNumber() + " • Tap to call");
        holder.tvContact.setOnClickListener(v -> {
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + branch.getContactNumber().replace("-", "")));
            v.getContext().startActivity(dial);
        });
        holder.itemView.setOnClickListener(v -> listener.onBranchClick(branch));
    }

    @Override
    public int getItemCount() {
        return branchList.size();
    }

    static class BranchViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvContact;

        BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBranchName);
            tvAddress = itemView.findViewById(R.id.tvBranchAddress);
            tvContact = itemView.findViewById(R.id.tvBranchContact);
        }
    }
}
