package com.viact.viact_android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.viact.viact_android.R;
import com.viact.viact_android.models.SpotPhoto;

import java.util.ArrayList;
import java.util.List;

public class ItemPhotosAdapter extends RecyclerView.Adapter<ItemPhotosAdapter.ViewHolder> {

    Context context;
    public List<SpotPhoto> fields = new ArrayList<>();
    EventListener listener;

    public ItemPhotosAdapter(Context context, List<SpotPhoto> data, EventListener listener) {
        this.context = context;
        this.fields.addAll(data);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_photo_field, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        SpotPhoto spot = fields.get(position);
        Glide.with (context)
                .load (spot.path)
                .centerCrop()
                .into (viewHolder.item_image);
        viewHolder.item_image.setOnClickListener( view -> {
            if (listener != null){
                listener.onClickItem(position);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<SpotPhoto> data){
        fields.clear();
        fields.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_image;
        ConstraintLayout view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view        =     itemView.findViewById(R.id.item_parent);
            item_image   =       itemView.findViewById(R.id.item_photo);
        }
    }

    public interface EventListener {
        void onClickItem(int index);
    }
}
