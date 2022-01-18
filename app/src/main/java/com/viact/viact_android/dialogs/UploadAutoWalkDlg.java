package com.viact.viact_android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.arashivision.sdkmedia.player.listener.PlayerGestureListener;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.player.listener.VideoStatusListener;
import com.arashivision.sdkmedia.player.video.InstaVideoPlayerView;
import com.arashivision.sdkmedia.player.video.VideoParamsBuilder;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.RecVideo;
import com.viact.viact_android.utils.API;
import com.viact.viact_android.utils.APICallback;
import com.viact.viact_android.utils.NetworkManager;
import com.viact.viact_android.utils.TimeFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UploadAutoWalkDlg extends Dialog implements LifecycleOwner {

    Context context;
    EventListener listener;
    RecVideo    recVideo;
    DatabaseHelper dbHelper;

    WorkWrapper mWorkWrapper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.player_video)    InstaVideoPlayerView videoPlayerView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_mobile)       RadioButton rb_mobile;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_wifi)         RadioButton rb_wifi;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_mobile_net_desc)    View view_mobile_net;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_wifi_net_desc)      View view_wifi_net;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_bind_network)    ToggleButton mBtnBindNetwork;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_current)          TextView mTvCurrent;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_total)            TextView  mTvTotal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.group_progress)      Group mGroupProgress;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.seek_bar)            SeekBar mSeekBar;

    private KProgressHUD hud;


    public UploadAutoWalkDlg(@NonNull Context context, RecVideo recVideo, EventListener listener) {
        super(context, R.style.AppTheme);
        this.context = context;
        this.listener = listener;
        this.recVideo = recVideo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upload_autowalk);
        lifecycleRegistry = new LifecycleRegistry(this);

        ButterKnife.bind(this);
        dbHelper = DatabaseHelper.getInstance(context);

        setCancelable(false);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_bind_network) void onClickBind(){
        if (mBtnBindNetwork.isChecked()) {
            NetworkManager.getInstance().exchangeNetToMobile();
        } else {
            NetworkManager.getInstance().clearBindProcess();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_upload) void onClickUpload(){
        //call API
        hud = KProgressHUD.create(context).setLabel("Uploading");
        hud.show();
        API.uploadSLAMVideo(recVideo, new APICallback<Pair<String, String>>() {
            @Override
            public void onSuccess(Pair<String, String> response) {
                hud.dismiss();
                String v_id = response.first;
                String url = response.second;
                recVideo.cloud_id = v_id;
                recVideo.url = url;
                dbHelper.updateVideo(recVideo);
                if(listener != null){
                    listener.onUploadSuccess();
                    dismiss();
                }
            }

            @Override
            public void onFailure(String error) {
                hud.dismiss();
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_cancel) void onClickCancel(){
        if(listener != null){
            listener.onFailed();
        }
        dismiss();
    }

    void initLayout(){
        RadioGroup radioGroup = findViewById(R.id.rg_network_mode);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_mobile) {
                view_mobile_net.setVisibility(View.VISIBLE);
                view_wifi_net.setVisibility(View.GONE);
            } else if (checkedId == R.id.rb_wifi){
                view_mobile_net.setVisibility(View.GONE);
                view_wifi_net.setVisibility(View.VISIBLE);
            }
        });
        radioGroup.setVisibility(View.GONE);
        rb_mobile.setChecked(true);
        view_mobile_net.setVisibility(View.VISIBLE);
        view_wifi_net.setVisibility(View.VISIBLE);

        mBtnBindNetwork.setChecked(NetworkManager.getInstance().isBindingMobileNetwork());

        String[] filepaths = new String[] {recVideo.path};
        mWorkWrapper = new WorkWrapper(filepaths);

        mGroupProgress.setVisibility(View.VISIBLE);
        videoPlayerView.setLifecycle(getLifecycle());
        videoPlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(context, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
                videoPlayerView.refreshDrawableState();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = context.getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
            }
        });
        videoPlayerView.setVideoStatusListener(new VideoStatusListener() {
            @Override
            public void onProgressChanged(long position, long length) {
                mSeekBar.setMax((int) length);
                mSeekBar.setProgress((int) position);
                mTvCurrent.setText(TimeFormat.durationFormat(position));
                mTvTotal.setText(TimeFormat.durationFormat(length));
            }

            @Override
            public void onPlayStateChanged(boolean isPlaying) {
            }

            @Override
            public void onSeekComplete() {
                videoPlayerView.resume();
            }

            @Override
            public void onCompletion() {
            }
        });
        videoPlayerView.setGestureListener(new PlayerGestureListener() {
            @Override
            public boolean onTap(MotionEvent e) {
                if (videoPlayerView.isPlaying()) {
                    videoPlayerView.pause();
                } else if (!videoPlayerView.isLoading() && !videoPlayerView.isSeeking()) {
                    videoPlayerView.resume();
                }
                return false;
            }
        });
        VideoParamsBuilder builder = new VideoParamsBuilder();
        builder.setWithSwitchingAnimation(true);

        videoPlayerView.prepare(mWorkWrapper, builder);
        videoPlayerView.play();
    }

    private LifecycleRegistry lifecycleRegistry;
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onStart(){
        super.onStart();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    @Override
    public void onStop(){
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        super.onStop();
        if (videoPlayerView != null) {
            videoPlayerView.destroy();
        }
    }

    public interface EventListener {
        void onUploadSuccess();
        void onFailed();
    }
}
