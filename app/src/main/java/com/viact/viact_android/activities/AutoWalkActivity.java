package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_SPOT_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_VIDEO_PATH;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MAX_LEN;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MIN_LEN;
import static com.viact.viact_android.utils.Const.SITE_MAX_SCALE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ICameraChangedCallback;
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener;
import com.arashivision.sdkcamera.camera.callback.IPreviewStatusListener;
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
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.dialogs.UploadAutoWalkDlg;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.RecVideo;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;
import com.viact.viact_android.utils.FileUtils;
import com.viact.viact_android.utils.TimeFormat;

import java.io.File;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AutoWalkActivity extends BaseObserveCameraActivity implements IPreviewStatusListener, ICaptureStatusListener, IExportCallback {

    Sheet cur_sheet;
    DatabaseHelper dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_mode_photoview)       PhotoView photo_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pinsContainer)               RelativeLayout   pinsContainer;
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

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_mode_status_view)      View mStatusView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_mode_guide_view)      View mGuideView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.record_mode_guide)           TextView mTvGuide;

//    private boolean mNeedToRestartPreview;
    private boolean mIsCapture;
//    private int mCurPreviewType = -1;
    private PreviewStreamResolution mCurPreviewResolution = null;

    final int STEP_INIT                 = 0;
    final int STEP_FIRST_POINT          = 1;
    final int STEP_START_RECORD         = 2;
    final int STEP_SECOND_POINT         = 3;
    final int STEP_STOP_RECORD          = 4;

    private int nStep = STEP_INIT;

    private PinPoint    firstPin, secondPin;
    private long        ts_first, ts_second;
    private Matrix      mat_site = new Matrix();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_walk);
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

        photo_view.setOnMatrixChangeListener(rect -> {
            drawPins();
        });

        photo_view.setMaximumScale(SITE_MAX_SCALE);
        photo_view.setOnPhotoTapListener(new PhotoTapListener());

        Glide.with (this)
                .load (cur_sheet.path)
                .into (photo_view);

        tv_title.setText("AutoWalk - " + cur_sheet.name);

        nStep = STEP_INIT;
        refreshLayout();

        InstaCameraManager cameraManager = InstaCameraManager.getInstance();
        if (isCameraConnected()) {
            setCameraStatusCallback();
        } else {
            connectCameraWifi();
        }
        cameraManager.registerCameraChangedCallback(this);

    }

    void connectCameraWifi() {
        InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);
    }

    void setCameraStatusCallback(){
        InstaCameraManager cameraManager = InstaCameraManager.getInstance();
        onCameraStatusChanged(true);
        onCameraBatteryUpdate(cameraManager.getCameraCurrentBatteryLevel(), cameraManager.isCameraCharging());
        onCameraSDCardStateChanged(cameraManager.isSdCardEnabled());
        onCameraStorageChanged(cameraManager.getCameraStorageFreeSpace(), cameraManager.getCameraStorageTotalSpace());
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.record_mode_ib_back) void onClickBack(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.record_mode_btn_record) void onClickRecord(){
        if (isCameraConnected() && mLayoutLoading.getVisibility() == View.GONE){
            if(nStep == STEP_FIRST_POINT){
                mIsCapture = true;
                if (!checkToRestartCameraPreviewStream()) {
                    nStep = STEP_START_RECORD;
                    refreshLayout();
                    doCameraWork();
                }
            } else if (nStep == STEP_SECOND_POINT){
                nStep = STEP_STOP_RECORD;
                refreshLayout();
                stopCameraWork();
            }
        } else {
            Toast.makeText(this, "Please check the connection with the 360 camera", Toast.LENGTH_SHORT).show();
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
            exp_filename = "exp_walk_" + (long)(System.currentTimeMillis()/1000);
            if (mWorkWrapper.isVideo()) {
                File folder = new File(EXT_STORAGE_VIDEO_PATH);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                EXPORT_DIR_PATH = EXT_STORAGE_VIDEO_PATH;
                exp_filename = exp_filename + ".mp4";
                exportVideoOriginal();
            } else {
                nStep = STEP_INIT;
                refreshLayout();
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
        if (strings.length > 0){
            nStep = STEP_STOP_RECORD;
            refreshLayout();
            captured_files = strings;
            onClickConfirm();
        } else {
            Toast.makeText(this, "Capture failed!", Toast.LENGTH_SHORT).show();
            nStep = STEP_INIT;
            refreshLayout();
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
        super.onCameraSDCardStateChanged(enabled);
        mLayoutLoading.setVisibility(View.GONE);
        resetState();
        nStep = STEP_INIT;
        refreshLayout();

        // 连接相机后自动开启预览、注册拍照监听
        // After connecting the camera, open preview stream and register listeners
        if (enabled) {
            Toast.makeText(this, R.string.main_toast_camera_connected, Toast.LENGTH_SHORT).show();
            InstaCameraManager.getInstance().setCaptureStatusListener(this);
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(this);
        } else {
            Toast.makeText(this, R.string.main_toast_camera_disconnected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCameraConnectError() {
        Toast.makeText(this, "Please check the connection with the 360 camera", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.main_toast_camera_disconnected, Toast.LENGTH_SHORT).show();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.auto_walk_connect_disp);
        alertDialogBuilder.setPositiveButton("Yes", (arg0, arg1) -> {
            InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);
        });
        alertDialogBuilder.setNegativeButton("Finish", (dialogInterface, i) -> {
            finish();
        });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
            if (nStep == STEP_START_RECORD){
                nStep = STEP_FIRST_POINT;
                refreshLayout();
            } else {
                stopCameraWork();
                nStep = STEP_STOP_RECORD;
                refreshLayout();
            }
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
                photo_view.setSuppMatrix(mat_site);
                drawPins();
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
        if (mIsCapture) {
            mIsCapture = false;
            nStep = STEP_START_RECORD;
            refreshLayout();

            doCameraWork();
        }
    }

    private CaptureParamsBuilder createCaptureParams() {
        return new CaptureParamsBuilder()
                .setCameraType(InstaCameraManager.getInstance().getCameraType())
                .setMediaOffset(InstaCameraManager.getInstance().getMediaOffset())
                .setCameraSelfie(InstaCameraManager.getInstance().isCameraSelfie())
                .setLive(false)  // 是否为直播模式
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

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.record_mode_ib_restart) void onClickMore(){
        if (isCameraConnected()){
            nStep = STEP_INIT;
            refreshLayout();
        } else{
            connectCameraWifi();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            // 退出页面时销毁预览
            // Destroy the preview when exiting the page
            stopCameraWork();
            InstaCameraManager.getInstance().setPreviewStatusChangedListener(null);
            InstaCameraManager.getInstance().closePreviewStream();
            mCapturePlayerView.destroy();
            InstaCameraManager.getInstance().closeCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InstaCameraManager.getInstance().unregisterCameraChangedCallback(this);
    }

    private boolean checkToRestartCameraPreviewStream() {
        if (isCameraConnected()) {
            PreviewStreamResolution newResolution = getPreviewResolution(InstaCameraManager.PREVIEW_TYPE_RECORD);
//            if (mCurPreviewResolution != newResolution) {
                mCurPreviewResolution = newResolution;
                InstaCameraManager.getInstance().closePreviewStream();
                InstaCameraManager.getInstance().startPreviewStream(newResolution, InstaCameraManager.PREVIEW_TYPE_RECORD);
                return true;
//            }
        }
        return false;
    }

    // 获取预览的分辨率
    // 此处为，录像选择5.7k，其他从支持列表中选择默认
    // Get preview resolution
    // Here, select 5.7k for recording, and select default from the support list for others
    private PreviewStreamResolution getPreviewResolution(int previewType) {
        // Optional resolution (as long as you feel the effect is OK)
        if (previewType == InstaCameraManager.PREVIEW_TYPE_RECORD) {
            return PreviewStreamResolution.STREAM_5760_2880_15FPS; //STREAM_5760_2880_30FPS
        }
        // Or choose one of the supported shooting modes of the current camera
        return InstaCameraManager.getInstance().getSupportedPreviewStreamResolution(previewType).get(0);
    }

    private void doCameraWork() {
        if (!InstaCameraManager.getInstance().isSdCardEnabled()) {
            Toast.makeText(this, R.string.capture_toast_sd_card_error, Toast.LENGTH_SHORT).show();
            mIsCapture = false;
            nStep = STEP_INIT;
            refreshLayout();
            return;
        }
        InstaCameraManager.getInstance().startNormalRecord(); //startHDRRecord();
        ts_first = System.currentTimeMillis();
    }
    private void stopCameraWork() {
        InstaCameraManager.getInstance().stopNormalRecord();
    }

    private void resetState() {
        mTvRecordDuration.setText(null);
        mCurPreviewResolution = null;
        mIsCapture = false;

        int captureType = InstaCameraManager.getInstance().getCurrentCaptureType();
//        if (captureType == InstaCameraManager.CAPTURE_TYPE_NORMAL_RECORD) {
//
//        } else if (captureType == InstaCameraManager.CAPTURE_TYPE_NORMAL_CAPTURE) {
//            mLayoutLoading.setVisibility(View.VISIBLE);
//        }
    }

    private boolean isCameraConnected() {
        return InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE;
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
                .setFps(15)
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

            nStep = STEP_INIT;
            refreshLayout();
        }
    }

    @Override
    public void onSuccess() {
        mExportDialog.dismiss();
        mCurrentExportId = -1;

        RecVideo rec_video =  new RecVideo();
        rec_video.path = EXPORT_DIR_PATH + "/" + exp_filename;
        rec_video.sh_id = cur_sheet.id + "";
        rec_video.first_pos = firstPin.x + "," + firstPin.y;
        rec_video.second_pos = secondPin.x + "," + secondPin.y;
        rec_video.gap_time = (ts_second - ts_first) + "";
        rec_video.create_time = System.currentTimeMillis()/1000 + "";
        dbHelper.addVideo(rec_video);

        InstaCameraManager.getInstance().closePreviewStream();

        showUploadDlg(rec_video);
    }

    @Override
    public void onFail(int i, String s) {
        mExportDialog.setContent(R.string.export_dialog_msg_export_failed, i);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
        Toast.makeText(this, "Export failed!", Toast.LENGTH_SHORT).show();

        nStep = STEP_INIT;
        refreshLayout();
    }

    @Override
    public void onCancel() {
        mExportDialog.setContent(R.string.export_dialog_msg_export_stopped);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
        Toast.makeText(this, "Export failed!", Toast.LENGTH_SHORT).show();

        nStep = STEP_INIT;
        refreshLayout();
    }

    @Override
    public void onProgress(float progress) {
        mExportDialog.setContent(getString(R.string.export_dialog_msg_export_progress, String.format(Locale.CHINA, "%.1f", progress * 100) + "%"));
    }

    void refreshLayout(){
        switch (nStep){
            case STEP_FIRST_POINT:
                mTvGuide.setText(R.string.auto_walk_guide_2);
                mBtnView.setVisibility(View.GONE);
                mRecBtn.setVisibility(View.VISIBLE);
                mRecBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_btn_play_bg));
                mStatusView.setVisibility(View.VISIBLE);
                photo_view.getSuppMatrix(mat_site);
                checkToRestartCameraPreviewStream();
                break;
            case STEP_START_RECORD:
                mTvGuide.setText(R.string.auto_walk_guide_3);
                mRecBtn.setVisibility(View.GONE);
                break;
            case STEP_SECOND_POINT:
                mTvGuide.setText(R.string.auto_walk_guide_4);
                mRecBtn.setVisibility(View.VISIBLE);
                mRecBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_btn_stop_bg));
                break;
            case STEP_STOP_RECORD:
                mTvGuide.setText(R.string.auto_walk_guide_5);
                mRecBtn.setVisibility(View.GONE);
                mRecBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_btn_play_bg));
                break;
            default:
                mTvGuide.setText(R.string.auto_walk_guide_1);
                mBtnView.setVisibility(View.GONE);
                mRecBtn.setVisibility(View.GONE);
                mStatusView.setVisibility(View.GONE);

                firstPin = null;
                secondPin = null;
                pinsContainer.removeAllViews();
                InstaCameraManager.getInstance().closePreviewStream();
                break;
        }
    }

    void drawPins(){
        if (firstPin != null){
            drawPin(firstPin);
        }
        if (secondPin != null){
            drawPin(secondPin);
        }
    }

    void drawPin(PinPoint p_pin){
        // calculate Pin position
        float img_scale = photo_view.getScale();
        int pin_wh = (int)((PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale);
        RectF rc = photo_view.getDisplayRect();
        float site_width = rc.width();
        float site_height = rc.height();
        int tempX = (int) (site_width * p_pin.x + rc.left) - pin_wh / 2;
        int tempY = (int) (site_height * p_pin.y + rc.top) - pin_wh;

        if (p_pin.iv_mark == null){
            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.ic_pin);
            iv.setOnClickListener(view -> {

            });

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(tempX, tempY, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            pinsContainer.addView(iv, lp);
            iv.getLayoutParams().height = pin_wh;
            iv.getLayoutParams().width = pin_wh;
            iv.requestLayout();
            p_pin.iv_mark = iv;
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pin_wh, pin_wh);
            layoutParams.leftMargin = tempX;
            layoutParams.topMargin = tempY;
            p_pin.iv_mark.setLayoutParams(layoutParams);
            p_pin.iv_mark.requestLayout();
        }

        int xMax = photo_view.getWidth();
        int yMax = photo_view.getHeight();

        if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
            p_pin.iv_mark.setVisibility(View.INVISIBLE);
        } else{
            p_pin.iv_mark.setVisibility(View.VISIBLE);
        }

    }

    void showUploadDlg(RecVideo recv){
        UploadAutoWalkDlg uploadDlg = new UploadAutoWalkDlg(this, recv, uploadListener);

        View decorView = uploadDlg.getWindow().getDecorView();
        decorView.setBackgroundResource(android.R.color.transparent);
        uploadDlg.show();
    }

    UploadAutoWalkDlg.EventListener uploadListener = new UploadAutoWalkDlg.EventListener() {
        @Override
        public void onUploadSuccess() {
            Toast.makeText(AutoWalkActivity.this, "360 Video was uploaded successfully.", Toast.LENGTH_SHORT).show();
            nStep = STEP_INIT;
            refreshLayout();
        }

        @Override
        public void onFailed() {
            nStep = STEP_INIT;
            refreshLayout();
        }
    };

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            if (isCameraConnected()){
                if (nStep == STEP_INIT){
                    firstPin = new PinPoint();
                    firstPin.x = x;
                    firstPin.y = y;
                    nStep = STEP_FIRST_POINT;
                } else if (nStep == STEP_START_RECORD){
                    secondPin = new PinPoint();
                    secondPin.x = x;
                    secondPin.y = y;
                    nStep = STEP_SECOND_POINT;
                    ts_second = System.currentTimeMillis();
                }

                drawPins();
                refreshLayout();
            }
        }
    }
}