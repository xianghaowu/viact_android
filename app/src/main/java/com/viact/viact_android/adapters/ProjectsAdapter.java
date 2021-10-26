package com.viact.viact_android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.viact.viact_android.models.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    Context context;
    public List<Project> fields = new ArrayList<>();
    EventListener listener;
    public ProjectsAdapter(Context context, List<Project> data, EventListener listener) {
        this.context = context;
        this.fields.addAll(data);  //cloneList(data);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_project_field, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        Project oneItem = fields.get(position);

        viewHolder.tv_title.setText(oneItem.name);
        viewHolder.tv_addr.setText(oneItem.address);
        viewHolder.tv_desc.setText(oneItem.note);
        if (!oneItem.site_map.isEmpty()){
            Glide.with (context)
                    .load (oneItem.site_map)
                    .into (viewHolder.iv_site);
        }
        if (oneItem.sync.equals("true")){
            viewHolder.iv_sync.setVisibility(View.GONE);
        } else {
            viewHolder.iv_sync.setVisibility(View.VISIBLE);
        }
        viewHolder.view.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClickEdit(position);
            }
        });
        viewHolder.view.setOnLongClickListener(view -> {
            if (listener != null) {
                listener.onClickDelete(position);
            }
            return false;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<Project> data){
        fields.clear();
        fields.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_site, iv_sync;
        TextView tv_title;
        TextView tv_desc;
        TextView tv_addr;
        LinearLayout    view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view        =     itemView.findViewById(R.id.item_parent);
            iv_site     =     itemView.findViewById(R.id.item_iv_site);
            iv_sync     =     itemView.findViewById(R.id.item_iv_sync);
            tv_title    =     itemView.findViewById(R.id.item_txt_title);
            tv_addr     =     itemView.findViewById(R.id.item_txt_address);
            tv_desc     =     itemView.findViewById(R.id.item_txt_desc);
        }
    }

    public interface EventListener {
        void onClickEdit(int index);
        void onClickDelete(int index);
    }
}
