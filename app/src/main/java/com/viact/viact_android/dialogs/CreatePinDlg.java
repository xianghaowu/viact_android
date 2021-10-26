package com.viact.viact_android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePinDlg extends Dialog {

    Context context;
    PinPoint m_pin;
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
    @BindView(R.id.dlg_btn_create)    Button            btn_create;

    public CreatePinDlg(@NonNull Context context, PinPoint pin, EventListener listener) {
        super(context, R.style.AppTheme);
        this.context = context;
        this.m_pin = pin;
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

        if (m_pin.id > -1){
            m_pin.name = name;
            m_pin.note = desc;
            long timestamp = System.currentTimeMillis()/1000;
            m_pin.update_time = timestamp + "";
            dbHelper.updatePin(m_pin);
        } else {
            m_pin.name = name;
            m_pin.note = desc;
            long timestamp = System.currentTimeMillis()/1000;
            m_pin.create_time = timestamp + "";
            m_pin.update_time = timestamp + "";
            dbHelper.addPin(m_pin);

            if (listener != null) {
                listener.onClickCreate();
            }
        }

        dismiss();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.dlg_iv_back) void onClickBack(){
        dismiss();
    }

    void initLayout(){
        if (m_pin.id > -1){
            btn_create.setText(R.string.edit_sitemap_side_menu_update);
            txt_name.setText(m_pin.name);
            txt_desc.setText(m_pin.note);
        } else {
            btn_create.setText(R.string.btn_create_pin);
        }
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
    }

    public interface EventListener {
        void onClickCreate();
    }

}
