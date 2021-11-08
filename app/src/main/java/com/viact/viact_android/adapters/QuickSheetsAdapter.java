package com.viact.viact_android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class QuickSheetsAdapter extends RecyclerView.Adapter<QuickSheetsAdapter.ViewHolder> {

    Context context;
    public List<Sheet> fields = new ArrayList<>();
    EventListener listener;
    DatabaseHelper dbHelper;

    public QuickSheetsAdapter(Context context, List<Sheet> data, EventListener listener) {
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
                .inflate(R.layout.item_sheet_quick_field, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {
        Sheet sh = fields.get(position);
        Glide.with (context)
                .load (sh.path)
                .centerCrop()
                .into (viewHolder.item_image);
        viewHolder.item_name.setText(sh.name);
        viewHolder.item_date.setText(getDate(sh.create_time));
        setMarkTxt(viewHolder.item_mark, sh);
        viewHolder.view.setOnClickListener( view -> {
            if (listener != null){
                listener.onClickItem(position);
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

    private String getDate(String time) {
        long ts = Long.parseLong(time);
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(ts * 1000);
        return DateFormat.format("MMM dd, yyyy hh:mm", cal).toString();
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
        ImageView item_image;
        TextView  item_name, item_date, item_mark;
        CardView view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view        =     itemView.findViewById(R.id.item_parent);
            item_image   =       itemView.findViewById(R.id.item_iv_photo);
            item_name   =       itemView.findViewById(R.id.item_tv_name);
            item_date   =       itemView.findViewById(R.id.item_tv_date);
            item_mark   =       itemView.findViewById(R.id.item_tv_mark);
        }
    }

    public interface EventListener {
        void onClickItem(int index);
    }
}
