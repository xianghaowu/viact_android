package com.viact.viact_android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Markup;
import com.viact.viact_android.models.PinPoint;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MarkupDlg extends Dialog {

    Context context;
    EventListener listener;
    DatabaseHelper dbHelper;
    Markup  markup;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_txt_name)    TextInputEditText       txt_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_txt_desc)    TextInputEditText       txt_desc;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_tl_name)     TextInputLayout         tl_name;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_tl_desc)     TextInputLayout         tl_desc;


    public MarkupDlg(@NonNull Context context, Markup mark, EventListener listener) {
        super(context, R.style.AppTheme);
        this.context = context;
        this.listener = listener;
        this.markup = mark;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_markup);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        ButterKnife.bind(this);

        dbHelper = DatabaseHelper.getInstance(context);
        setCancelable(false);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.dlg_btn_save) void onClickSave(){
        String name = Objects.requireNonNull(txt_name.getText()).toString().trim();
        String desc = Objects.requireNonNull(txt_desc.getText()).toString().trim();

        if (name.isEmpty()) {
            tl_name.setError("Wrong! Please enter the project name");
            return;
        }

        markup.name = txt_name.getText().toString().trim();
        markup.note = txt_desc.getText().toString().trim();
        long timestamp = System.currentTimeMillis()/1000;
        markup.create_time = timestamp + "";
        markup.update_time = timestamp + "";


        if (markup.id > -1){
            markup.update_time = timestamp + "";
            dbHelper.updateMarkup(markup);
        } else {
            markup.create_time = timestamp + "";
            markup.update_time = timestamp + "";
            dbHelper.addMarkup(markup);
        }
        if (listener != null) {
            listener.onClickCreate();
        }

        dismiss();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.dlg_btn_cancel) void onClickBack(){
        dismiss();
    }

    void initLayout(){
        txt_name.setText(markup.name);
        txt_desc.setText(markup.note);
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
