package com.viact.viact_android.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.Group;

import com.arashivision.sdkmedia.player.listener.PlayerGestureListener;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.player.listener.VideoStatusListener;
import com.arashivision.sdkmedia.player.video.InstaVideoPlayerView;
import com.arashivision.sdkmedia.player.video.VideoParamsBuilder;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.RecVideo;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.utils.TimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideosViewActivity extends BaseObserveCameraActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.video_txt_date)          TextView            txt_date;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_current)              TextView            mTvCurrent;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_total)                TextView            mTvTotal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.video_tv_status)          TextView                tv_status;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.group_progress)          Group               mGroupProgress;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.seek_bar)                SeekBar             mSeekBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.video_iv_back)           ImageView           iv_back;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.video_iv_forward)        ImageView           iv_forward;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.video_play_view)         RelativeLayout      player_container;

    //toolbar
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.video_tv_title)          TextView            tv_title;

    InstaVideoPlayerView mVideoPlayerView;

    Sheet cur_sheet;
    DatabaseHelper dbHelper;
    List<RecVideo> video_list = new ArrayList<>();
    WorkWrapper mWorkWrapper;
    private int sel_index = 0;
    private RecVideo    sel_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_view);
        ButterKnife.bind(this);

        int sh_id = getIntent().getIntExtra("sheet", -1);
        dbHelper = DatabaseHelper.getInstance(this);
        if (sh_id == -1) {finish();}
        cur_sheet = dbHelper.getSheet(sh_id);
        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.video_iv_delete) void onClickDelete(){
        confirmDeletePhoto();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.video_iv_back) void onClickBack(){
        if (sel_index > 0){
            sel_index--;
        }
        initPlay();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.video_iv_forward) void onClickForward(){
        if (sel_index < video_list.size() - 1){
            sel_index++;
        }
        initPlay();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.video_ib_back) void onClickClose(){
        finish();
    }

    void initLayout(){
        tv_title.setText(cur_sheet.name);
        video_list = dbHelper.getVideos(cur_sheet.id);
        if (video_list.size() == 0) {
            finish();
        } else {
            initPlay();
        }
    }

    void initPlay(){
        tv_status.setVisibility(View.GONE);
        if (sel_index == 0) {
            iv_back.setVisibility(View.GONE);
        } else {
            iv_back.setVisibility(View.VISIBLE);
        }

        if (sel_index == video_list.size() - 1) {
            iv_forward.setVisibility(View.GONE);
        } else {
            iv_forward.setVisibility(View.VISIBLE);
        }
        mGroupProgress.setVisibility(View.GONE);

        if (mVideoPlayerView != null) {
            mVideoPlayerView.destroy();
        }

        player_container.setVisibility(View.GONE);

        sel_video = video_list.get(sel_index);
        long ctime = Long.parseLong(sel_video.create_time);
        showDateTime(ctime);

        String filename = sel_video.path;
        String[] filepaths = new String[] {filename};
        mWorkWrapper = new WorkWrapper(filepaths);
        if (mWorkWrapper.isVideo()){
            playVideo(mWorkWrapper);
        }
    }

    private void playVideo(WorkWrapper mWrapper) {
        mGroupProgress.setVisibility(View.VISIBLE);

        player_container.setVisibility(View.VISIBLE);
        mVideoPlayerView = new InstaVideoPlayerView(this);
        mVideoPlayerView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        player_container.addView(mVideoPlayerView);

        mVideoPlayerView.setLifecycle(getLifecycle());
        mVideoPlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(VideosViewActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
                mVideoPlayerView.refreshDrawableState();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(VideosViewActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        mVideoPlayerView.setVideoStatusListener(new VideoStatusListener() {
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
                mVideoPlayerView.resume();
            }

            @Override
            public void onCompletion() {
            }
        });
        mVideoPlayerView.setGestureListener(new PlayerGestureListener() {
            @Override
            public boolean onTap(MotionEvent e) {
                if (mVideoPlayerView.isPlaying()) {
                    mVideoPlayerView.pause();
                } else if (!mVideoPlayerView.isLoading() && !mVideoPlayerView.isSeeking()) {
                    mVideoPlayerView.resume();
                }
                return false;
            }
        });
        VideoParamsBuilder builder = new VideoParamsBuilder();
        builder.setWithSwitchingAnimation(true);
//        if (isPlaneMode) {
//            builder.setRenderModelType(VideoParamsBuilder.RENDER_MODE_PLANE_STITCH);
//            builder.setScreenRatio(2, 1);
//        }

        mVideoPlayerView.prepare(mWrapper, builder);
        mVideoPlayerView.play();
    }

    void confirmDeletePhoto(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_delete_spot);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (sel_index >= 0 && sel_index < video_list.size()){
                RecVideo sp = video_list.get(sel_index);
                File ff = new File(sp.path);
                ff.delete();
                video_list.clear();
                video_list = dbHelper.getVideos(cur_sheet.id);
                if (sel_index == video_list.size()){
                    sel_index--;
                }
                if (video_list.size() == 0){
                    finish();
                }
                initPlay();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDateTime(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String sdate = DateFormat.format("MMM dd, yyyy hh:mm", cal).toString();
        txt_date.setText(sdate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoPlayerView != null) {
            mVideoPlayerView.destroy();
        }
    }
}