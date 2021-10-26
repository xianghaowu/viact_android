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

import com.arashivision.sdkmedia.player.image.ImageParamsBuilder;
import com.arashivision.sdkmedia.player.image.InstaImagePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerGestureListener;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.player.listener.VideoStatusListener;
import com.arashivision.sdkmedia.player.video.InstaVideoPlayerView;
import com.arashivision.sdkmedia.player.video.VideoParamsBuilder;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.SpotPhoto;
import com.viact.viact_android.utils.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RoomViewActivity extends BaseObserveCameraActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_photoview)           PhotoView               photoView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_txt_date)       TextView                    txt_date;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_txt_time)       TextView                    txt_time;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_current)          TextView                    mTvCurrent;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_total)            TextView                    mTvTotal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.group_progress)      Group                       mGroupProgress;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.seek_bar)            SeekBar                     mSeekBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_iv_back)        ImageView                   iv_back;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_iv_forward)     ImageView                   iv_forward;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_play_view)    RelativeLayout player_container;
    InstaImagePlayerView    mImagePlayerView;
    InstaVideoPlayerView    mVideoPlayerView;

    String pin_id;
    DatabaseHelper dbHelper;
    List<SpotPhoto> photo_list = new ArrayList<>();
    WorkWrapper mWorkWrapper;
    private int sel_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_view);
        ButterKnife.bind(this);
        String title = getIntent().getStringExtra("name");
        setTitle(title);
        pin_id = getIntent().getIntExtra("pin_id", 0) + "";

        dbHelper = DatabaseHelper.getInstance(this);
        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_delete) void onClickDelete(){
        confirmDeletePhoto();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_back) void onClickBack(){
        if (sel_index > 0){
            sel_index--;
        }
        playPhoto();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_forward) void onClickForward(){
        if (sel_index < photo_list.size() - 1){
            sel_index++;
        }
        playPhoto();
    }

    void initLayout(){
        txt_date.setText("");
        txt_time.setText("");
        photo_list = dbHelper.getAllSpots(pin_id);
        if (photo_list.size() == 0) {
            finish();
        } else {
            playPhoto();
        }
    }

    void playPhoto(){
        if (sel_index == 0) {
            iv_back.setVisibility(View.GONE);
        } else {
            iv_back.setVisibility(View.VISIBLE);
        }

        if (sel_index == photo_list.size() - 1) {
            iv_forward.setVisibility(View.GONE);
        } else {
            iv_forward.setVisibility(View.VISIBLE);
        }
        mGroupProgress.setVisibility(View.GONE);
        photoView.setVisibility(View.GONE);
        releasePlayerViews();
        if (player_container.getChildCount() > 0) {
            player_container.removeAllViews();
        }
        player_container.setVisibility(View.GONE);

        SpotPhoto photo = photo_list.get(sel_index);
        String filename = photo.path;
        long ctime = Long.parseLong(photo.create_time);
        showDateTime(ctime);
        String[] filepaths = new String[] {filename};
        mWorkWrapper = new WorkWrapper(filepaths);
        if (filename.contains("exp_360_")){
            if (mWorkWrapper.isVideo()){
                playVideo(mWorkWrapper);
            } else {
                playImage(mWorkWrapper);
            }
        } else {
            if (filename.contains(".png")){
                photoView.setVisibility(View.VISIBLE);
                Glide.with (this)
                        .load (filename)
                        .into (photoView);
            } else {
                playVideo(mWorkWrapper);
            }
        }
    }

    private void showDateTime(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String sdate = DateFormat.format("MMM dd", cal).toString();
        String stime = DateFormat.format("hh:mm", cal).toString();
        txt_date.setText(sdate);
        txt_time.setText(stime);
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
                Toast.makeText(RoomViewActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
                mVideoPlayerView.refreshDrawableState();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(RoomViewActivity.this, toast, Toast.LENGTH_LONG).show();
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

    private void playImage(WorkWrapper mWrapper) {
        player_container.setVisibility(View.VISIBLE);
        mImagePlayerView = new InstaImagePlayerView(this);
        mImagePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        player_container.addView(mImagePlayerView);

        mImagePlayerView.setLifecycle(getLifecycle());
        mImagePlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(RoomViewActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
                mImagePlayerView.refreshDrawableState();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(RoomViewActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        ImageParamsBuilder builder = new ImageParamsBuilder();
        builder.setWithSwitchingAnimation(true);
        builder.setImageFusion(mWrapper.isPanoramaFile());
//        if (isPlaneMode) {
//            builder.setRenderModelType(ImageParamsBuilder.RENDER_MODE_PLANE_STITCH);
//            builder.setScreenRatio(2, 1);
//        }

        mImagePlayerView.prepare(mWrapper, builder);
        mImagePlayerView.play();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_close) void onClickClose(){
        finish();
    }

    void confirmDeletePhoto(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_delete_spot);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (sel_index >= 0 && sel_index < photo_list.size()){
                SpotPhoto proc = photo_list.get(sel_index);
                dbHelper.deleteSpot(proc.id + "");
                photo_list.clear();
                photo_list = dbHelper.getAllSpots(pin_id);
                if (sel_index == photo_list.size()){
                    sel_index--;
                }
                playPhoto();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayerViews();
    }

    void releasePlayerViews(){
        if (mImagePlayerView != null) {
            mImagePlayerView.destroy();
        }
        if (mVideoPlayerView != null) {
            mVideoPlayerView.destroy();
        }
    }
}