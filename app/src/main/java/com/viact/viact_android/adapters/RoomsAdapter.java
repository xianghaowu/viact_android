package com.viact.viact_android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.viact.viact_android.R;
import com.viact.viact_android.models.Room;
import com.viact.viact_android.models.SpotPhoto;

import java.util.ArrayList;
import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.ViewHolder> {

    Context context;
    public List<Room> fields = new ArrayList<>();
    EventListener listener;
    public RoomsAdapter(Context context, List<Room> data, EventListener listener) {
        this.context = context;
        this.fields.addAll(data);  //cloneList(data);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_room_field, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        Room one = fields.get(position);
        viewHolder.item_name.setText(one.ppt.name);

        viewHolder.view.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClickItem(position);
            }
        });
        viewHolder.photosAdapter = new ItemPhotosAdapter(context, one.photos, index -> {
            if (listener != null) {
                listener.onClickItem(position);
            }
        });
        viewHolder.room_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        viewHolder.room_recycler.setAdapter(viewHolder.photosAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<Room> data){
        fields.clear();
        fields.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_name;
        RecyclerView room_recycler;
        LinearLayout view;
        ItemPhotosAdapter photosAdapter;
//        List<SpotPhoto> spot_list = new ArrayList<>();
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view        =     itemView.findViewById(R.id.item_parent);
            item_name   =       itemView.findViewById(R.id.item_room_name);
            room_recycler =     itemView.findViewById(R.id.item_recycler);
        }
    }

    public interface EventListener {
        void onClickItem(int index);
    }
}
