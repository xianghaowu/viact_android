package com.viact.viact_android.dialogs;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_SPOT_PATH;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.models.SpotPhoto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateSpotDlg extends Dialog {

    Context context;
    String bm;
    String proc_id;
    EventListener listener;
    DatabaseHelper dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_txt_name)    TextInputEditText txt_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_txt_desc)    TextInputEditText       txt_desc;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_tl_name)    TextInputLayout tl_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_tl_desc)     TextInputLayout         tl_desc;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_iv_spot)    ImageView iv_spot;

    public CreateSpotDlg(@NonNull Context context, String bm, String proc_id, EventListener listener) {
        super(context, R.style.AppTheme);
        this.context = context;
        this.bm = bm;
        this.proc_id = proc_id;
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_spot);
        ButterKnife.bind(this);

        dbHelper = DatabaseHelper.getInstance(context);
        setCancelable(false);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.dlg_btn_create) void onClickCreate(){
        String name = Objects.requireNonNull(txt_name.getText()).toString().trim();
        String desc = Objects.requireNonNull(txt_desc.getText()).toString().trim();

        if (name.isEmpty()) {
            tl_name.setError("Wrong! Please enter the project name");
            return;
        }

        SpotPhoto one = new SpotPhoto();
        one.p_id = proc_id;
        one.name = name;
        one.desc = desc;
        one.path = bm;
        dbHelper.addSpot(one);

//        File folder = new File(EXT_STORAGE_SPOT_PATH);
//        if (!folder.exists()) {
//            folder.mkdirs();
//        }
//        String filename = EXT_STORAGE_SPOT_PATH + "spot_" + (long)(System.currentTimeMillis()/1000) + ".png";
//        try (FileOutputStream out = new FileOutputStream(filename)) {
//            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
//            out.flush();
//            SpotPhoto one = new SpotPhoto();
//            one.p_id = proc_id;
//            one.name = name;
//            one.desc = desc;
//            one.path = filename;
//            dbHelper.addSpot(one);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(context, "File save failed", Toast.LENGTH_SHORT).show();
//        }

        dismiss();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.dlg_iv_back) void onClickBack(){
        dismiss();
    }

    void initLayout(){
        txt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    tl_name.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
//        iv_spot.setImageBitmap(bm);
        Glide.with (context)
                .load (bm)
                .into (iv_spot);
    }

    public interface EventListener {
        void onClickCreate(SpotPhoto spot);
    }

}
