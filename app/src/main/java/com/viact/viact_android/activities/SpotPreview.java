package com.viact.viact_android.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SpotPreview extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.spot_preview_photo)    PhotoView   photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_preview);
        ButterKnife.bind(this);
        String title = getIntent().getStringExtra("name");
        setTitle(title);
        String path = getIntent().getStringExtra("path");
        Glide.with (this)
                .load (path)
                .into (photoView);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.spot_preview_iv_close) void onClickClose(){
        finish();
    }
}