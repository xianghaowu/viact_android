package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_APP_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_SPOT_PATH;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdkmedia.export.ExportImageParamsBuilder;
import com.arashivision.sdkmedia.export.ExportUtils;
import com.arashivision.sdkmedia.export.ExportVideoParamsBuilder;
import com.arashivision.sdkmedia.export.IExportCallback;
import com.arashivision.sdkmedia.player.image.ImageParamsBuilder;
import com.arashivision.sdkmedia.player.image.InstaImagePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerGestureListener;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.player.listener.VideoStatusListener;
import com.arashivision.sdkmedia.player.video.InstaVideoPlayerView;
import com.arashivision.sdkmedia.player.video.VideoParamsBuilder;
import com.arashivision.sdkmedia.stitch.StitchUtils;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.viact.viact_android.R;
import com.viact.viact_android.dialogs.CreateSpotDlg;
import com.viact.viact_android.utils.TimeFormat;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayAndExportActivity extends BaseObserveCameraActivity implements IExportCallback {

    private static final String WORK_URLS = "CAMERA_FILE_PATH";
    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String EXPORT_DIR_PATH = EXT_STORAGE_APP_PATH;

    private InstaImagePlayerView mImagePlayerView;
    private InstaVideoPlayerView mVideoPlayerView;
    private RadioButton mRbNormal;
    private RadioButton mRbFisheye;
    private RadioButton mRbPerspective;
    private RadioButton mRbPlane;
    private Group mGroupProgress;
    private TextView mTvCurrent;
    private TextView mTvTotal;
    private SeekBar mSeekBar;
    private ToggleButton mBtnHDR;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_pick_spot)    Button btn_pick_spot;

    private WorkWrapper mWorkWrapper;
    private MaterialDialog mExportDialog;
    private int mCurrentExportId = -1;
    private static String proc_id = "0";

    // HDR拼接
    private StitchTask mStitchTask;
    private String mHDROutputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SDK_DEMO_OSC/generate_hdr_" + System.currentTimeMillis() + ".jpg";
    private boolean mIsStitchHDRSuccessful;

    public static void launchActivity(Context context, String[] urls, String p_id) {
        Intent intent = new Intent(context, PlayAndExportActivity.class);
        intent.putExtra(WORK_URLS, urls);
        intent.putExtra(PROJECT_ID, p_id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_and_export);
        setTitle(R.string.main_preview_button);//R.string.play_toolbar_title
        ButterKnife.bind(this);

        proc_id = getIntent().getStringExtra(PROJECT_ID);

        String[] urls = getIntent().getStringArrayExtra(WORK_URLS);
        if (urls == null) {
            finish();
            Toast.makeText(this, R.string.play_toast_empty_path, Toast.LENGTH_SHORT).show();
            return;
        }

        mWorkWrapper = new WorkWrapper(urls);
        bindViews();
        if (mWorkWrapper.isVideo()) {
            playVideo(false);
        } else {
            playImage(false);
        }

        // HDR stitch
        mBtnHDR = findViewById(R.id.btn_hdr_stitch);
        mBtnHDR.setVisibility(mWorkWrapper.isHDRPhoto() ? View.VISIBLE : View.GONE);
        mBtnHDR.setOnClickListener(v -> {
            if (mWorkWrapper.isHDRPhoto()) {
                if (mBtnHDR.isChecked()) {
                    mStitchTask = new StitchTask(this);
                    mStitchTask.execute();
                } else {
                    mIsStitchHDRSuccessful = false;
                    playImage(mRbPlane.isChecked());
                }
            } else {
                Toast.makeText(this, R.string.play_toast_not_hdr, Toast.LENGTH_SHORT).show();
                mBtnHDR.setChecked(false);
            }
        });
    }

    // 导出全景    // Export Panorama
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_export_original) void onClickExportOriginal(){
        if (mWorkWrapper.isVideo()) {
            exportVideoOriginal();
        } else {
            exportImageOriginal();
        }
        showExportDialog();
    }

    // 导出平面缩略图
    // Export Thumbnail
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_export_thumbnail) void onClickExportThumbnail(){
        if (mWorkWrapper.isVideo()) {
            exportVideoThumbnail();
        } else {
            exportImageThumbnail();
        }
        showExportDialog();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_pick_spot) void onClickPickSpot() {
//        makeScrenshot();
//        Bitmap bm;
//        if (mWorkWrapper.isVideo() && mVideoPlayerView.isPlaying()){
//            bm = ImageUtils.loadBitmapFromView(this, mVideoPlayerView.〇o0〇o0.getRootView());
//        } else {
//            mImagePlayerView.〇o0〇o0.getRootView().setDrawingCacheEnabled(true);
//            bm = Bitmap.createBitmap(mImagePlayerView.〇o0〇o0.getRootView().getDrawingCache());
//            mImagePlayerView.〇o0〇o0.getRootView().setDrawingCacheEnabled(false);
//        }
//        if (bm == null) return;
        CreateSpotDlg createDlg = new CreateSpotDlg(this, mWorkWrapper.getUrls()[0], proc_id, null);//

        View decorView = createDlg.getWindow().getDecorView();
        decorView.setBackgroundResource(android.R.color.transparent);
        createDlg.show();
    }

    private void bindViews() {
        //spot button
//        if (mWorkWrapper.isVideo()){
//            btn_pick_spot.setVisibility(View.INVISIBLE);
//        } else {
//            btn_pick_spot.setVisibility(View.VISIBLE);
//        }

        mVideoPlayerView = findViewById(R.id.player_video);
        mImagePlayerView = findViewById(R.id.player_image);
        mGroupProgress = findViewById(R.id.group_progress);
        mTvCurrent = findViewById(R.id.tv_current);
        mTvTotal = findViewById(R.id.tv_total);
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mVideoPlayerView.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoPlayerView.seekTo(seekBar.getProgress());
            }
        });

        mRbNormal = findViewById(R.id.rb_normal);
        mRbFisheye = findViewById(R.id.rb_fisheye);
        mRbPerspective = findViewById(R.id.rb_perspective);
        mRbPlane = findViewById(R.id.rb_plane);
        RadioGroup radioGroup = findViewById(R.id.rg_image_mode);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Need to restart the preview stream when switching between normal and plane
            if (checkedId == R.id.rb_plane) {
                if (mWorkWrapper.isVideo()) {
                    playVideo(true);
                } else {
                    playImage(true);
                }
                mRbFisheye.setEnabled(false);
                mRbPerspective.setEnabled(false);
            } else if (checkedId == R.id.rb_normal) {
                if (!mRbFisheye.isEnabled() || !mRbPerspective.isEnabled()) {
                    if (mWorkWrapper.isVideo()) {
                        playVideo(false);
                    } else {
                        playImage(false);
                    }
                    mRbFisheye.setEnabled(true);
                    mRbPerspective.setEnabled(true);
                } else {
                    // Switch to Normal Mode
                    mImagePlayerView.switchNormalMode();
                    mVideoPlayerView.switchNormalMode();
                }
            } else if (checkedId == R.id.rb_fisheye) {
                // Switch to Fisheye Mode
                mImagePlayerView.switchFisheyeMode();
                mVideoPlayerView.switchFisheyeMode();
            } else if (checkedId == R.id.rb_perspective) {
                // Switch to Perspective Mode
                mImagePlayerView.switchPerspectiveMode();
                mVideoPlayerView.switchPerspectiveMode();
            }
        });
    }

    private void playVideo(boolean isPlaneMode) {
        mGroupProgress.setVisibility(View.VISIBLE);
        mVideoPlayerView.setVisibility(View.VISIBLE);
        mVideoPlayerView.setLifecycle(getLifecycle());
        mVideoPlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(PlayAndExportActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(PlayAndExportActivity.this, toast, Toast.LENGTH_LONG).show();
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
        if (isPlaneMode) {
            builder.setRenderModelType(VideoParamsBuilder.RENDER_MODE_PLANE_STITCH);
            builder.setScreenRatio(2, 1);
        }

        File folder = new File(EXT_STORAGE_SPOT_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        builder.setCacheCutSceneRootPath(EXT_STORAGE_SPOT_PATH);

        mVideoPlayerView.prepare(mWorkWrapper, builder);
        mVideoPlayerView.play();
    }

    private void playImage(boolean isPlaneMode) {
        mImagePlayerView.setVisibility(View.VISIBLE);
        mImagePlayerView.setLifecycle(getLifecycle());
        mImagePlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(PlayAndExportActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(PlayAndExportActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        ImageParamsBuilder builder = new ImageParamsBuilder();
        builder.setWithSwitchingAnimation(true);
        builder.setImageFusion(mWorkWrapper.isPanoramaFile());
        if (isPlaneMode) {
            builder.setRenderModelType(ImageParamsBuilder.RENDER_MODE_PLANE_STITCH);
            builder.setScreenRatio(2, 1);
        }
        if (mIsStitchHDRSuccessful) {
            builder.setUrlForPlay(mHDROutputPath);
        }
        File folder = new File(EXT_STORAGE_SPOT_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        builder.setCacheCutSceneRootPath(EXT_STORAGE_SPOT_PATH);

        mImagePlayerView.prepare(mWorkWrapper, builder);
        mImagePlayerView.play();
    }

    // 实际项目中导出时建议关闭播放器，否则容易出现oom
    // Recommended to close the player when exporting in the actual project, otherwise it is easy to appear oom
    private void exportVideoOriginal() {
        ExportVideoParamsBuilder builder = new ExportVideoParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".mp4")
                // 导出视频对手机性能要求较高，如导出5.7k时遇到oom或者app被系统强制杀掉的情况，请自行设置较小宽高
                // Exporting video requires high performance of mobile phones. For example, when exporting 5.7k,
                // you encounter oom or app being forcibly killed by the system, please set a smaller width and height by yourself
                .setWidth(2048)
                .setHeight(1024);
//                .setBitrate(20 * 1024 * 1024);
        mCurrentExportId = ExportUtils.exportVideo(mWorkWrapper, builder, this);
    }

    // 实际项目中导出时建议关闭播放器，否则容易出现oom
    // Recommended to close the player when exporting in the actual project, otherwise it is easy to appear oom
    private void exportImageOriginal() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setImageFusion(mWorkWrapper.isPanoramaFile())
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".jpg");
        if (mBtnHDR.isChecked()) {
            builder.setUrlForExport(mHDROutputPath);
        }
        mCurrentExportId = ExportUtils.exportImage(mWorkWrapper, builder, this);
    }

    private void exportVideoThumbnail() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.SPHERE)
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".jpg")
                .setWidth(512)
                .setHeight(512)
                .setFov(mVideoPlayerView.getFov())
                .setDistance(mVideoPlayerView.getDistance())
                .setYaw(mVideoPlayerView.getYaw())
                .setPitch(mVideoPlayerView.getPitch());
        mCurrentExportId = ExportUtils.exportVideoToImage(mWorkWrapper, builder, this);
    }

    private void exportImageThumbnail() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.SPHERE)
                .setTargetPath(EXPORT_DIR_PATH + System.currentTimeMillis() + ".jpg")
                .setWidth(512)
                .setHeight(512)
                .setFov(mImagePlayerView.getFov())
                .setDistance(mImagePlayerView.getDistance())
                .setYaw(mImagePlayerView.getYaw())
                .setPitch(mImagePlayerView.getPitch());
        if (mBtnHDR.isChecked()) {
            builder.setUrlForExport(mHDROutputPath);
        }
        mCurrentExportId = ExportUtils.exportImage(mWorkWrapper, builder, this);
    }

    private void stopExport() {
        if (mCurrentExportId != -1) {
            ExportUtils.stopExport(mCurrentExportId);
            mCurrentExportId = -1;
        }
    }

    private void showExportDialog() {
        if (mExportDialog == null) {
            mExportDialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .positiveText(R.string.export_dialog_ok)
                    .neutralText(R.string.export_dialog_stop)
                    .build();
        }
        mExportDialog.setContent(R.string.export_dialog_msg_exporting);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.GONE);
        mExportDialog.show();
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setOnClickListener(v -> stopExport());
    }

    @Override
    public void onSuccess() {
        mExportDialog.setContent(getString(R.string.export_dialog_msg_export_success, EXPORT_DIR_PATH));
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
    }

    @Override
    public void onFail(int errorCode, String errorMsg) {
        // if GPU not support, errorCode is -10003 or -10005 or -13020
        mExportDialog.setContent(R.string.export_dialog_msg_export_failed, errorCode);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
    }

    @Override
    public void onCancel() {
        mExportDialog.setContent(R.string.export_dialog_msg_export_stopped);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
    }

    @Override
    public void onProgress(float progress) {
        // 仅在导出视频时有进度回调
        // callback only when exporting video
        mExportDialog.setContent(getString(R.string.export_dialog_msg_export_progress, String.format(Locale.CHINA, "%.1f", progress * 100) + "%"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStitchTask != null) {
            mStitchTask.cancel(true);
        }
        if (mImagePlayerView != null) {
            mImagePlayerView.destroy();
        }
        if (mVideoPlayerView != null) {
            mVideoPlayerView.destroy();
        }

    }

    //make screenshots

    private static class StitchTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<PlayAndExportActivity> activityWeakReference;
        private MaterialDialog mStitchDialog;

        private StitchTask(PlayAndExportActivity activity) {
            super();
            activityWeakReference = new WeakReference<>(activity);
            mStitchDialog = new MaterialDialog.Builder(activity)
                    .content(R.string.export_dialog_msg_hdr_stitching)
                    .progress(true, 100)
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .build();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mStitchDialog.show();
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            PlayAndExportActivity stitchActivity = activityWeakReference.get();
            if (stitchActivity != null && !isCancelled()) {
                // Start HDR stitching
                stitchActivity.mIsStitchHDRSuccessful =
                        StitchUtils.generateHDR(stitchActivity.mWorkWrapper, stitchActivity.mHDROutputPath);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            PlayAndExportActivity stitchActivity = activityWeakReference.get();
            if (stitchActivity != null && !isCancelled()) {
                stitchActivity.playImage(stitchActivity.mRbPlane.isChecked());
            }
            mStitchDialog.dismiss();
        }
    }

}
