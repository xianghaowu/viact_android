package com.viact.viact_android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;

import java.util.ArrayList;
import java.util.List;

public class ListSheetsAdapter extends RecyclerView.Adapter<ListSheetsAdapter.ViewHolder> {

    Context context;
    public List<Sheet> fields = new ArrayList<>();
    EventListener listener;
    DatabaseHelper dbHelper;

    public ListSheetsAdapter(Context context, List<Sheet> data, EventListener listener) {
        this.context = context;
        this.fields.addAll(data);
        this.listener = listener;
        dbHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // create a new view
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sheet_field, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        Sheet sh = fields.get(position);
        viewHolder.item_name.setText(sh.name);
        String s_count = getCount(sh);
        if (s_count.isEmpty()){
            viewHolder.item_count.setVisibility(View.GONE);
        } else {
            viewHolder.item_count.setVisibility(View.VISIBLE);
            viewHolder.item_count.setText(getCount(sh));
        }

        setMarkTxt(viewHolder.item_mark, sh);

        viewHolder.item_parent.setOnClickListener( view -> {
            if (listener != null){
                listener.onClickItem(position);
            }
        });

        viewHolder.item_menu.setOnClickListener( view -> {
            if (listener != null){
                listener.onClickMenu(position);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setMarkTxt(TextView txt_v, Sheet sh){
        List<PinPoint> pp_list = dbHelper.getPinsForSheet(sh.id);
        int cnt = 0;
        for (int i = 0; i < pp_list.size(); i++){
            List<SpotPhoto> sp_list = dbHelper.getAllSpots(pp_list.get(i).id);
            if (sp_list.size() == 0){
                cnt ++;
            }
        }
        if (cnt > 0) {
            txt_v.setVisibility(View.VISIBLE);
            txt_v.setText(cnt + "");
        } else{
            txt_v.setVisibility(View.GONE);
        }
    }

    private String getCount(Sheet sh){
        int count = 0;
        List<PinPoint> pt_list = dbHelper.getPinsForSheet(sh.id);
        for (int i = 0; i < pt_list.size(); i++){
            List<SpotPhoto> sp_list = dbHelper.getAllSpots(pt_list.get(i).id);
            count += sp_list.size();
        }

        String str_count = "";
        if (count == 1){
            str_count = "1 Photo";
        } else if (count > 1){
            str_count = count + " Photos";
        }
        return str_count;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<Sheet> data){
        fields.clear();
        fields.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView item_name, item_count, item_mark;
        ImageView  item_menu;
        View    item_parent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_parent = itemView.findViewById(R.id.item_parent);
            item_name   =       itemView.findViewById(R.id.item_tv_name);
            item_count   =       itemView.findViewById(R.id.item_tv_count);
            item_menu   =       itemView.findViewById(R.id.item_iv_more);
            item_mark   =       itemView.findViewById(R.id.item_txt_mark);
        }
    }

    public interface EventListener {
        void onClickItem(int index);
        void onClickMenu(int index);
    }
}
