package com.viact.viact_android.dialogs;

import static com.viact.viact_android.utils.Const.SHEET_TYPE_NORMAL;
import static com.viact.viact_android.utils.Const.SHEET_TYPE_NO_FILE;
import static com.viact.viact_android.utils.Const.SITE_MAP_URL;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.viact.viact_android.R;
import com.viact.viact_android.activities.CreateSheetActivity;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.models.Sheet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfirmSheetDlg extends Dialog {

    Context context;
    EventListener listener;
    int type = SHEET_TYPE_NORMAL;
    String f_name;
    String sheet_name;
    int proc_id;
    DatabaseHelper dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.photo_view)    PhotoView     photo_view;


    public ConfirmSheetDlg(@NonNull Context context, String sh_name, int pid, int type, String f_name, EventListener listener) {
        super(context, R.style.AppTheme);
        this.context = context;
        this.listener = listener;
        this.f_name = f_name;
        this.type = type;
        this.sheet_name = sh_name;
        this.proc_id = pid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm_sheet);
        ButterKnife.bind(this);
        dbHelper = DatabaseHelper.getInstance(context);

        setCancelable(false);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_confirm) void onClickConfirm(){
        if (type != SHEET_TYPE_NO_FILE){
            Sheet one = new Sheet();
            one.pro_id = proc_id + "";
            one.name = sheet_name;
            one.path = f_name;
            one.type = type;
            one.create_time = (long)System.currentTimeMillis()/1000 + "";
            one.update_time = one.create_time;
            dbHelper.addSheet(one);
            if (listener != null) {
                listener.onClickOk();
                dismiss();
            }
        } else {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_empty);
            try (FileOutputStream out = new FileOutputStream(f_name)) {
                bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                Sheet one = new Sheet();
                one.pro_id = proc_id + "";
                one.name = sheet_name;
                one.path = f_name;
                one.type = type;
                one.create_time = (long)System.currentTimeMillis()/1000 + "";
                one.update_time = one.create_time;
                dbHelper.addSheet(one);
                if (listener != null) {
                    listener.onClickOk();
                    dismiss();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "File save error!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_cancel) void onClickCancel(){
        dismiss();
    }

    void initLayout(){
        if (type != SHEET_TYPE_NO_FILE){
            Glide.with (context)
                    .load (f_name)
                    .into (photo_view);
        } else {
            photo_view.setImageResource(R.drawable.ic_empty);
        }
    }

    public interface EventListener {
        void onClickOk();
    }
}
