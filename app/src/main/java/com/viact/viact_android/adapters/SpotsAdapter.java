package com.viact.viact_android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.viact.viact_android.R;
import com.viact.viact_android.models.SpotPhoto;

import java.util.ArrayList;
import java.util.List;

public class SpotsAdapter extends RecyclerView.Adapter<SpotsAdapter.ViewHolder> {

    Context context;
    public List<SpotPhoto> fields = new ArrayList<>();
    EventListener listener;
    public SpotsAdapter(Context context, List<SpotPhoto> data, EventListener listener) {
        this.context = context;
        this.fields.addAll(data);  //cloneList(data);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_spot_field, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        SpotPhoto oneItem = fields.get(position);

        viewHolder.tv_title.setText(oneItem.name);
        viewHolder.tv_desc.setText(oneItem.desc);
        if (!oneItem.path.isEmpty()){
            Glide.with (context)
                    .load (oneItem.path)
                    .into (viewHolder.iv_spot);
        }

        viewHolder.view.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClickItem(position);
            }
        });
        viewHolder.iv_del.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClickDelete(position);
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
        ImageView iv_spot, iv_del;
        TextView tv_title;
        TextView tv_desc;
        LinearLayout view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view        =     itemView.findViewById(R.id.item_parent);
            iv_spot     =     itemView.findViewById(R.id.item_iv_spot);
            iv_del     =     itemView.findViewById(R.id.item_iv_delete);
            tv_title    =     itemView.findViewById(R.id.item_txt_title);
            tv_desc     =     itemView.findViewById(R.id.item_txt_desc);
        }
    }

    public interface EventListener {
        void onClickItem(int index);
        void onClickDelete(int index);
    }
}
