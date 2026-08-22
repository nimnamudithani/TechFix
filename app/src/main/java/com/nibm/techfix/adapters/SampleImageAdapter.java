package com.nibm.techfix.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nibm.techfix.R;

import java.util.List;

/**
 * Displays sample repaired-device images. Each entry in `images` is
 * [id, imagePath, caption] as returned by SampleImageDao.getAllSampleImages().
 * Pass a non-null onDelete listener to show delete buttons (admin mode);
 * pass null for the read-only customer-facing gallery.
 */
public class SampleImageAdapter extends RecyclerView.Adapter<SampleImageAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(String id);
    }

    private final List<String[]> images;
    private final OnDeleteListener onDeleteListener; // null = read-only

    public SampleImageAdapter(List<String[]> images, OnDeleteListener onDeleteListener) {
        this.images = images;
        this.onDeleteListener = onDeleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sample_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] entry = images.get(position);
        String id = entry[0];
        String path = entry[1];
        String caption = entry[2];

        holder.tvCaption.setText(caption);
        try {
            holder.ivImage.setImageURI(Uri.parse(path));
        } catch (Exception ignored) { }

        if (onDeleteListener != null) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> onDeleteListener.onDelete(id));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvCaption;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivSampleImage);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
