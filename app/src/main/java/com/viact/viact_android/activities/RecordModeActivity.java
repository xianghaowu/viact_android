package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_PHOTO_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_SPOT_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_VIDEO_PATH;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ICameraChangedCallback;
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener;
import com.arashivision.sdkcamera.camera.callback.IPreviewStatusListener;
import com.arashivision.sdkcamera.camera.live.LiveParamsBuilder;
import com.arashivision.sdkcamera.camera.preview.ExposureData;
import com.arashivision.sdkcamera.camera.preview.GyroData;
import com.arashivision.sdkcamera.camera.preview.VideoData;
import com.arashivision.sdkcamera.camera.resolution.PreviewStreamResolution;
import com.arashivision.sdkmedia.export.ExportUtils;
import com.arashivision.sdkmedia.export.ExportVideoParamsBuilder;
import com.arashivision.sdkmedia.export.IExportCallback;
import com.arashivision.sdkmedia.player.capture.CaptureParamsBuilder;
import com.arashivision.sdkmedia.player.capture.InstaCapturePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.RecVideo;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.utils.FileUtils;
import com.viact.viact_android.utils.NetworkManager;
import com.viact.viact_android.utils.TimeFormat;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecordModeActivity extends AppCompatActivity implements ICameraChangedCallback, IPreviewStatusListener, ICaptureStatusListener, IExportCallback {

    Sheet cur_sheet;
    DatabaseHelper dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_mode_photoview)       PhotoView photo_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_mode_tv_title)        TextView tv_title;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.player_capture)              InstaCapturePlayerView mCapturePlayerView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_sd_card_state)            TextView mTvSdCard;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_battery_level)            TextView mTvSdBattery;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_record_duration)          TextView mTvRecordDuration;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.layout_loading)              ViewGroup mLayoutLoading;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_mode_btn_record)      Button mRecBtn;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_btn_view)             View mBtnView;

    private boolean mNeedToRestartPreview;
    private boolean mIsCaptureButtonClicked;
    private int mCurPreviewType = -1;
    private PreviewStreamResolution mCurPreviewResolution = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        int sh_id = data.getInt("sheet_id", -1);
        dbHelper = DatabaseHelper.getInstance(this);

        if (sh_id == -1){
            finish();
        }
        cur_sheet = dbHelper.getSheet(sh_id);
        if (cur_sheet == null) finish();

        initLayout();
    }

    void initLayout(){
        File folder = new File(EXT_STORAGE_SPOT_PATH);
        folder.mkdirs();

        Glide.with (this)
                .load (cur_sheet.path)
                .into (photo_view);

        tv_title.setText(cur_sheet.name);
        mBtnView.setVisibility(View.GONE);

        InstaCameraManager cameraManager = InstaCameraManager.getInstance();
        if (isCameraConnected()) {
            onCameraStatusChanged(true);
            onCameraBatteryUpdate(cameraManager.getCameraCurrentBatteryLevel(), cameraManager.isCameraCharging());
            onCameraSDCardStateChanged(cameraManager.isSdCardEnabled());
            onCameraStorageChanged(cameraManager.getCameraStorageFreeSpace(), cameraManager.getCameraStorageTotalSpace());
        }
        cameraManager.registerCameraChangedCallback(this);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.record_mode_ib_back) void onClickBack(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.record_mode_btn_record) void onClickRecord(){
        mIsCaptureButtonClicked = !mIsCaptureButtonClicked;

        if(mIsCaptureButtonClicked){
            mRecBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_btn_stop_bg));
            if (!checkToRestartCameraPreviewStream()) {
                doCameraWork();
            }
        } else{
            mRecBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_btn_play_bg));
            stopCameraWork();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.record_mode_btn_again) void onClickAgain(){
        onClickRecord();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.record_mode_btn_confirm) void onClickConfirm() {
        if (captured_files != null && captured_files.length > 0) {
            //download captured file as panorama
            mWorkWrapper = new WorkWrapper(captured_files);
            exp_filename = "exp_360_" + (long)(System.currentTimeMillis()/1000);
            if (mWorkWrapper.isVideo()) {
                File folder = new File(EXT_STORAGE_VIDEO_PATH);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                EXPORT_DIR_PATH = EXT_STORAGE_VIDEO_PATH;
                exp_filename = exp_filename + ".mp4";
                exportVideoOriginal();
            } else {
                mRecBtn.setVisibility(View.VISIBLE);
                mBtnView.setVisibility(View.GONE);
            }
            showExportDialog();
        }
    }

    @Override
    public void onCaptureStarting() {
    }

    @Override
    public void onCaptureWorking() {

    }

    @Override
    public void onCaptureStopping() {
        mLayoutLoading.setVisibility(View.VISIBLE);
    }

    private String[] captured_files;
    @Override
    public void onCaptureFinish(String[] strings) {
        mLayoutLoading.setVisibility(View.GONE);
        mTvRecordDuration.setText(null);
        checkToRestartCameraPreviewStream();
        // 拍摄结束返回文件路径，可执行下载、播放、导出操作，任君选择
        // 如果是HDR拍照则必须从相机下载到本地才可进行HDR合成操作
        // After capture, the file paths will be returned. Then download, play and export operations can be performed
        // If it is HDR Capture, you must download images from the camera to the local to perform HDR stitching operation
        if (strings.length > 0){
            mBtnView.setVisibility(View.VISIBLE);
            mRecBtn.setVisibility(View.GONE);
            captured_files = strings;
        } else {
            Toast.makeText(this, "Capture failed!", Toast.LENGTH_SHORT).show();
            mRecBtn.setVisibility(View.VISIBLE);
            mRecBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_btn_play_bg));
        }
    }

    @Override
    public void onCaptureTimeChanged(long captureTime) {
        mTvRecordDuration.setText(TimeFormat.durationFormat(captureTime));
    }

    @Override
    public void onCaptureCountChanged(int captureCount) {

    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        mLayoutLoading.setVisibility(View.GONE);
        resetState();

        // 连接相机后自动开启预览、注册拍照监听
        // After connecting the camera, open preview stream and register listeners
        if (enabled) {
            InstaCameraManager.getInstance().setCaptureStatusListener(this);
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(this);
            checkToRestartCameraPreviewStream();
        }
    }

    @Override
    public void onCameraConnectError() {
        Toast.makeText(this, "Please check the connection with the 360 camera", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onCameraBatteryUpdate(int batteryLevel, boolean isCharging) {
        String text = getString(R.string.full_demo_battery_level, batteryLevel);
        if (isCharging) {
            text += "  " + getString(R.string.full_demo_charging);
        }
        mTvSdBattery.setText(text);
    }

    @Override
    public void onCameraSDCardStateChanged(boolean enabled) {
        if (!enabled) {
            mTvSdCard.setText(R.string.full_demo_sd_error);
        }
    }

    @Override
    public void onCameraStorageChanged(long freeSpace, long totalSpace) {
        if (!(freeSpace <= 0 && totalSpace <= 0)) {
            mTvSdCard.setText(getString(R.string.full_demo_sd_remaining_space, FileUtils.computeFileSize(freeSpace) + "/" + FileUtils.computeFileSize(totalSpace)));
        }
    }

    @Override
    public void onOpening() {
        mLayoutLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onOpened() {
        mLayoutLoading.setVisibility(View.GONE);

        InstaCameraManager.getInstance().setStreamEncode();
        mCapturePlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingFinish() {
                InstaCameraManager.getInstance().setPipeline(mCapturePlayerView.getPipeline());
            }

            @Override
            public void onReleaseCameraPipeline() {
                InstaCameraManager.getInstance().setPipeline(null);
            }
        });
        mCapturePlayerView.prepare(createCaptureParams());
        mCapturePlayerView.play();
        mCapturePlayerView.setKeepScreenOn(true);

        // 预览开启后再录像
        // Record after preview is opened
        if (mIsCaptureButtonClicked) {
            mIsCaptureButtonClicked = false;
            doCameraWork();
        }
    }

    private CaptureParamsBuilder createCaptureParams() {
        return new CaptureParamsBuilder()
                .setCameraType(InstaCameraManager.getInstance().getCameraType())
                .setMediaOffset(InstaCameraManager.getInstance().getMediaOffset())
                .setCameraSelfie(InstaCameraManager.getInstance().isCameraSelfie())
                .setLive(mCurPreviewType == InstaCameraManager.PREVIEW_TYPE_LIVE)  // 是否为直播模式
                .setResolutionParams(mCurPreviewResolution.width, mCurPreviewResolution.height, mCurPreviewResolution.fps);
    }

    @Override
    public void onIdle() {
        mCapturePlayerView.destroy();
        mCapturePlayerView.setKeepScreenOn(false);
    }

    @Override
    public void onVideoData(VideoData videoData) {

    }

    @Override
    public void onGyroData(List<GyroData> gyroList) {

    }

    @Override
    public void onExposureData(ExposureData exposureData) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            // 退出页面时销毁预览
            // Destroy the preview when exiting the page
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(null);
            InstaCameraManager.getInstance().closePreviewStream();
            mCapturePlayerView.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InstaCameraManager.getInstance().unregisterCameraChangedCallback(this);
    }

    private boolean checkToRestartCameraPreviewStream() {
        if (isCameraConnected()) {
            int newPreviewType = getNewPreviewType();
            PreviewStreamResolution newResolution = getPreviewResolution(newPreviewType);
            if (mCurPreviewType != newPreviewType || mCurPreviewResolution != newResolution) {
                mCurPreviewType = newPreviewType;
                mCurPreviewResolution = newResolution;
                InstaCameraManager.getInstance().closePreviewStream();
                InstaCameraManager.getInstance().startPreviewStream(newResolution, newPreviewType);
                return true;
            }
        }
        return false;
    }
    // Get the preview mode currently to be turned on
    private int getNewPreviewType() {
        if (mIsCaptureButtonClicked) {
            // 即将开启录像
            // About to start recording
            return InstaCameraManager.PREVIEW_TYPE_RECORD;
        } else {
            // 普通预览
            // Normal Mode
            return InstaCameraManager.PREVIEW_TYPE_NORMAL;
        }
    }

    // 获取预览的分辨率
    // 此处为，录像选择5.7k，其他从支持列表中选择默认
    // Get preview resolution
    // Here, select 5.7k for recording, and select default from the support list for others
    private PreviewStreamResolution getPreviewResolution(int previewType) {
        // Optional resolution (as long as you feel the effect is OK)
        if (previewType == InstaCameraManager.PREVIEW_TYPE_RECORD) {
            return PreviewStreamResolution.STREAM_5760_2880_30FPS;
        }

        // Or choose one of the supported shooting modes of the current camera
        return InstaCameraManager.getInstance().getSupportedPreviewStreamResolution(previewType).get(0);
    }

    private void doCameraWork() {
        if (!InstaCameraManager.getInstance().isSdCardEnabled()) {
            Toast.makeText(this, R.string.capture_toast_sd_card_error, Toast.LENGTH_SHORT).show();
            mIsCaptureButtonClicked = false;
            mRecBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_btn_play_bg));
            return;
        }
        switch (mCurPreviewType) {
            case InstaCameraManager.PREVIEW_TYPE_RECORD:
                InstaCameraManager.getInstance().startNormalRecord(); //startHDRRecord();
                break;
            case InstaCameraManager.PREVIEW_TYPE_NORMAL:
                mLayoutLoading.setVisibility(View.VISIBLE);
                InstaCameraManager.getInstance().startNormalCapture(false);
                break;
//            case InstaCameraManager.PREVIEW_TYPE_LIVE:
//                NetworkManager.getInstance().exchangeNetToMobile();
//                InstaCameraManager.getInstance().startLive(createLiveParams(), this);
//                break;
        }
    }
    private void stopCameraWork() {
        switch (mCurPreviewType) {
            case InstaCameraManager.PREVIEW_TYPE_RECORD:
                InstaCameraManager.getInstance().stopNormalRecord();
                break;
//            case InstaCameraManager.PREVIEW_TYPE_LIVE:
//                InstaCameraManager.getInstance().stopLive();
//                NetworkManager.getInstance().clearBindProcess();
//                break;
        }
    }

    private void resetState() {
        mTvRecordDuration.setText(null);
        mCurPreviewType = InstaCameraManager.PREVIEW_TYPE_NORMAL;
        mCurPreviewResolution = null;

        mNeedToRestartPreview = false;
        int captureType = InstaCameraManager.getInstance().getCurrentCaptureType();
        if (captureType == InstaCameraManager.CAPTURE_TYPE_NORMAL_RECORD) {

        } else if (captureType == InstaCameraManager.CAPTURE_TYPE_NORMAL_CAPTURE) {
            mLayoutLoading.setVisibility(View.VISIBLE);
        }
        mNeedToRestartPreview = true;
    }

    private boolean isCameraConnected() {
        return InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE;
    }

    // All live streaming parameters are arbitrarily filled in according to your product requirements
    private LiveParamsBuilder createLiveParams() {
        return new LiveParamsBuilder()
                .setRtmp("rtmp://txy.live-send.acg.tv/live-txy/?streamname=live_23968708_6785332&key=6abecd453e112c38f190f69fabc6d3da")
                .setWidth(1440)
                .setHeight(720)
                .setFps(30)
                .setBitrate(2 * 1024 * 1024)
                .setPanorama(true)
                // 设置网络ID即可在使用WIFI连接相机时使用4G网络推流
                // set NetId to use 4G to push live streaming when connecting camera by WIFI
                .setNetId(NetworkManager.getInstance().getMobileNetId());
    }

    //Export Video
    WorkWrapper mWorkWrapper;
    MaterialDialog mExportDialog;
    String exp_filename, EXPORT_DIR_PATH;
    int mCurrentExportId = -1;

    private void exportVideoOriginal() {
        ExportVideoParamsBuilder builder = new ExportVideoParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setTargetPath(EXPORT_DIR_PATH + "/" + exp_filename)
                .setWidth(2048)
                .setHeight(1024);
//                .setBitrate(20 * 1024 * 1024);
        mCurrentExportId = ExportUtils.exportVideo(mWorkWrapper, builder, this);
    }

    //Export callback
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

    private void stopExport() {
        if (mCurrentExportId != -1) {
            ExportUtils.stopExport(mCurrentExportId);
            mCurrentExportId = -1;
            Toast.makeText(this, "Export stopped!", Toast.LENGTH_SHORT).show();
            mRecBtn.setVisibility(View.VISIBLE);
            mBtnView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSuccess() {
        mExportDialog.dismiss();
        mCurrentExportId = -1;

        RecVideo rec_video =  new RecVideo();
        rec_video.path = EXPORT_DIR_PATH + "/" + exp_filename;
        rec_video.sh_id = cur_sheet.id + "";
        rec_video.create_time = System.currentTimeMillis()/1000 + "";
        dbHelper.addVideo(rec_video);

        mRecBtn.setVisibility(View.VISIBLE);
        mBtnView.setVisibility(View.GONE);
    }

    @Override
    public void onFail(int i, String s) {
        mExportDialog.setContent(R.string.export_dialog_msg_export_failed, i);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
        Toast.makeText(this, "Export failed!", Toast.LENGTH_SHORT).show();
        mRecBtn.setVisibility(View.VISIBLE);
        mBtnView.setVisibility(View.GONE);
    }

    @Override
    public void onCancel() {
        mExportDialog.setContent(R.string.export_dialog_msg_export_stopped);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
        Toast.makeText(this, "Export failed!", Toast.LENGTH_SHORT).show();
        mRecBtn.setVisibility(View.VISIBLE);
        mBtnView.setVisibility(View.GONE);
    }

    @Override
    public void onProgress(float progress) {
        mExportDialog.setContent(getString(R.string.export_dialog_msg_export_progress, String.format(Locale.CHINA, "%.1f", progress * 100) + "%"));
    }
}