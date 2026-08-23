package com.nibm.techfix.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;

import java.util.List;

/** Each entry in `items` is [id, message, created_at, is_read]. */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<String[]> items;

    public NotificationAdapter(List<String[]> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] entry = items.get(position);
        holder.tvMessage.setText(entry[1]);
        holder.tvTime.setText(entry[2]);
        holder.unreadDot.setVisibility("0".equals(entry[3]) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        View unreadDot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }
}
