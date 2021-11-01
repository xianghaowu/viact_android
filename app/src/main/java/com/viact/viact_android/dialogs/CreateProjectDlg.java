package com.viact.viact_android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateProjectDlg extends Dialog {

    Context context;
    EventListener listener;
    DatabaseHelper  dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_txt_name)    TextInputEditText       txt_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_txt_address) TextInputEditText       txt_address;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_txt_desc)    TextInputEditText       txt_desc;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_tl_name)     TextInputLayout         tl_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_tl_address)  TextInputLayout         tl_address;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_tl_desc)     TextInputLayout         tl_desc;

    public CreateProjectDlg(@NonNull Context context, EventListener listener) {
        super(context, R.style.AppTheme);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_create_project);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ButterKnife.bind(this);
        dbHelper = DatabaseHelper.getInstance(context);

        setCancelable(false);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.dlg_btn_create) void onClickCreate(){
        String name = Objects.requireNonNull(txt_name.getText()).toString().trim();
        String addr = Objects.requireNonNull(txt_address.getText()).toString().trim();
        String desc = Objects.requireNonNull(txt_desc.getText()).toString().trim();

        if (name.isEmpty()) {
            tl_name.setError("Wrong! Please enter the project name");
            return;
        }
        if (addr.isEmpty()) {
            tl_address.setError("Wrong! Please enter the address");
            return;
        }
        Project one = new Project();
        one.name = name;
        one.address = addr;
        one.note = desc;
        one.sync = "false";
        one.create_time = (long)System.currentTimeMillis()/1000 + "";
        one.update_time = one.create_time;
        dbHelper.addProject(one);
        if (listener != null) {
            listener.onClickCreate();
            dismiss();
        }
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

        txt_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    tl_address.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public interface EventListener {
        void onClickCreate();
    }
}
