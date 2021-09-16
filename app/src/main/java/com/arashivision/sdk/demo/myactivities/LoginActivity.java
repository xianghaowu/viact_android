package com.arashivision.sdk.demo.myactivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.activity.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    final int REQUEST_CUSTOM_PERMISSION = 9999;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_txt_username)    TextInputEditText         txt_username;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_txt_password)    TextInputEditText         txt_password;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_tl_username)     TextInputLayout           tl_username;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_tl_password)     TextInputLayout           tl_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        ButterKnife.bind(this);

        initLayout();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.login_btn_login) void onClickLogin(){
        String uname = Objects.requireNonNull(txt_username.getText()).toString().trim();
        String pword = Objects.requireNonNull(txt_password.getText()).toString().trim();
        if (uname.length() > 0){
            if (pword.length() > 0) {
                if (uname.equals("user")){
                    if (pword.equals("password")){
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        tl_password.setError("Wrong! Invalid password");
                    }
                } else {
                    tl_username.setError("Wrong! Invalid username");
                }
            } else {
                tl_password.setError("Wrong! Please type the password");
            }
        } else {
            tl_username.setError("Wrong! Please type the username");
        }
    }

    void initLayout(){
        txt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    tl_username.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0){
                    tl_password.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CUSTOM_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please check the permission on setting", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_CUSTOM_PERMISSION);
        }
    }
}