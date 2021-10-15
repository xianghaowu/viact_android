package com.viact.viact_android.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.SaveSharedPrefrence;
import com.viact.viact_android.models.User;
import com.viact.viact_android.utils.API;
import com.viact.viact_android.utils.APICallback;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    final int REQUEST_CUSTOM_PERMISSION = 9999;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_txt_username)    TextInputEditText txt_username;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_txt_password)    TextInputEditText         txt_password;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_tl_username)    TextInputLayout tl_username;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.login_tl_password)     TextInputLayout           tl_password;

    SaveSharedPrefrence sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.app_name);
        ButterKnife.bind(this);

        initLayout();
        sharedPref = new SaveSharedPrefrence();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.login_btn_login) void onClickLogin(){
        String uname = Objects.requireNonNull(txt_username.getText()).toString().trim();
        String pword = Objects.requireNonNull(txt_password.getText()).toString().trim();
        if (uname.length() > 0){
            if (pword.length() > 0) {
                showProgress();
                API.authLogin(uname, pword, new APICallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        dismissProgress();
                        sharedPref.putString(LoginActivity.this, SaveSharedPrefrence.PREFS_AUTH_TOKEN, response);
                        getSignedUser(response);
                    }

                    @Override
                    public void onFailure(String error) {
                        dismissProgress();
                        showToast(error);
                    }
                });
            } else {
                tl_password.setError("Wrong! Please type the password");
            }
        } else {
            tl_username.setError("Wrong! Please type the username");
        }
    }

    void getSignedUser(String token){
        showProgress("Signed User...");
        API.getLoginUser(token, new APICallback<User>() {
            @Override
            public void onSuccess(User response) {
                dismissProgress();
                if (response.company_code != null && !response.company_code.isEmpty()){
                    sharedPref.putString(LoginActivity.this, SaveSharedPrefrence.PREFS_COMPANY_CODE, response.company_code);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    showToast("Can not get the company code");
                }
            }

            @Override
            public void onFailure(String error) {
                dismissProgress();
                showToast(error);
            }
        });
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
        if (requestCode == REQUEST_CUSTOM_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please check the permission on setting", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CUSTOM_PERMISSION);
        }
    }
}