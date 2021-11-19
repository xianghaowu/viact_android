package com.viact.viact_android.adapters;

import static com.viact.viact_android.utils.Const.def_categories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.viact.viact_android.R;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    Context context;
    public String filters = "";
    EventListener listener;
    public CategoriesAdapter(Context context, String data, EventListener listener) {
        this.context = context;
        this.filters = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_category_field, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        viewHolder.item_name.setText(def_categories[position]);

        if (filters.contains(def_categories[position])){
            viewHolder.item_check.setChecked(true);
        } else {
            viewHolder.item_check.setChecked(false);
        }

        viewHolder.view.setOnClickListener(view -> {
            if (listener != null) {
                listener.onClickItem(position);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(String data){
        filters = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return def_categories.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_name;
        RelativeLayout view;
        CheckBox    item_check;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view        =     itemView.findViewById(R.id.item_parent);
            item_name   =       itemView.findViewById(R.id.item_tv_name);
            item_check   =       itemView.findViewById(R.id.item_cb_active);
        }
    }

    public interface EventListener {
        void onClickItem(int index);
    }
}
