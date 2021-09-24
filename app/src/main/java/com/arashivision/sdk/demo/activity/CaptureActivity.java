package com.arashivision.sdk.demo.activity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.arashivision.insta360.basecamera.camera.CameraType;
import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.models.UploadedData;
import com.arashivision.sdk.demo.util.API;
import com.arashivision.sdk.demo.util.APICallback;
import com.arashivision.sdk.demo.util.SaveSharedPrefrence;
import com.arashivision.sdk.demo.util.TimeFormat;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.Nullable;

public class CaptureActivity extends BaseObserveCameraActivity implements ICaptureStatusListener {

    private TextView mTvCaptureStatus;
    private TextView mTvCaptureTime;
    private TextView mTvCaptureCount;
    private Button mBtnPlayCameraFile;
    private Button mBtnPlayLocalFile;
    private Button mBtnUploadFile;

    private TextView mTvUploadedUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        setTitle(R.string.capture_toolbar_title);
        bindViews();

        sharedPref = new SaveSharedPrefrence();

        if (InstaCameraManager.getInstance().getCameraConnectedType() == InstaCameraManager.CONNECT_TYPE_NONE) {
            finish();
            return;
        }

        findViewById(R.id.btn_normal_capture).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                InstaCameraManager.getInstance().startNormalCapture(false);
            }
        });

        findViewById(R.id.btn_normal_pano_capture).setVisibility(isOneX2() ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_normal_pano_capture).setEnabled(supportInstaPanoCapture());
        findViewById(R.id.btn_normal_pano_capture).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                InstaCameraManager.getInstance().startNormalPanoCapture(InstaCameraManager.FOCUS_SENSOR_REAR, false);
            }
        });

        findViewById(R.id.btn_hdr_capture).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                int funcMode = InstaCameraManager.FUNCTION_MODE_HDR_CAPTURE;
                InstaCameraManager.getInstance().setAEBCaptureNum(funcMode, 3);
                InstaCameraManager.getInstance().setExposureEV(funcMode, 2f);
                InstaCameraManager.getInstance().startHDRCapture(false);
            }
        });

        findViewById(R.id.btn_hdr_pano_capture).setVisibility(isOneX2() ? View.VISIBLE : View.GONE);
        findViewById(R.id.btn_hdr_pano_capture).setEnabled(supportInstaPanoCapture());
        findViewById(R.id.btn_hdr_pano_capture).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                int funcMode = InstaCameraManager.FUNCTION_MODE_HDR_PANO_CAPTURE;
                InstaCameraManager.getInstance().setAEBCaptureNum(funcMode, 3);
                InstaCameraManager.getInstance().setExposureEV(funcMode, 2f);
                InstaCameraManager.getInstance().startHDRPanoCapture(InstaCameraManager.FOCUS_SENSOR_FRONT, false);
            }
        });

        findViewById(R.id.btn_interval_shooting_start).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                InstaCameraManager.getInstance().setIntervalShootingTime(3000);
                InstaCameraManager.getInstance().startIntervalShooting();
            }
        });

        findViewById(R.id.btn_interval_shooting_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopIntervalShooting();
        });

        findViewById(R.id.btn_normal_record_start).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                InstaCameraManager.getInstance().startNormalRecord();
            }
        });

        findViewById(R.id.btn_normal_record_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopNormalRecord();
        });

        findViewById(R.id.btn_hdr_record_start).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                InstaCameraManager.getInstance().startHDRRecord();
            }
        });

        findViewById(R.id.btn_hdr_record_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopHDRRecord();
        });

        findViewById(R.id.btn_timelapse_start).setOnClickListener(v -> {
            if (checkSdCardEnabled()) {
                InstaCameraManager.getInstance().setTimeLapseInterval(500);
                InstaCameraManager.getInstance().startTimeLapse();
            }
        });

        findViewById(R.id.btn_timelapse_stop).setOnClickListener(v -> {
            InstaCameraManager.getInstance().stopTimeLapse();
        });

        // Capture Status Callback
        InstaCameraManager.getInstance().setCaptureStatusListener(this);
    }

    private void bindViews() {
        mTvCaptureStatus = findViewById(R.id.tv_capture_status);
        mTvCaptureTime = findViewById(R.id.tv_capture_time);
        mTvCaptureCount = findViewById(R.id.tv_capture_count);
        mBtnPlayCameraFile = findViewById(R.id.btn_play_camera_file);
        mBtnPlayLocalFile = findViewById(R.id.btn_play_local_file);
        mBtnUploadFile = findViewById(R.id.btn_upload_file);
        mTvUploadedUrl = findViewById(R.id.tv_uploaded_url);
    }

    private boolean isOneX2() {
        return CameraType.getForType(InstaCameraManager.getInstance().getCameraType()) == CameraType.ONEX2;
    }

    private boolean supportInstaPanoCapture() {
        return isOneX2() && InstaCameraManager.getInstance().getCurrentCameraMode() == InstaCameraManager.CAMERA_MODE_PANORAMA;
    }

    private boolean checkSdCardEnabled() {
        if (!InstaCameraManager.getInstance().isSdCardEnabled()) {
            Toast.makeText(this, R.string.capture_toast_sd_card_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        if (!enabled) {
            finish();
        }
    }

    @Override
    public void onCameraSensorModeChanged(int cameraSensorMode) {
        super.onCameraSensorModeChanged(cameraSensorMode);
        findViewById(R.id.btn_normal_pano_capture).setEnabled(supportInstaPanoCapture());
        findViewById(R.id.btn_hdr_pano_capture).setEnabled(supportInstaPanoCapture());
    }

    @Override
    public void onCaptureStarting() {
        mTvCaptureStatus.setText(R.string.capture_capture_starting);
        mBtnPlayCameraFile.setVisibility(View.GONE);
        mBtnPlayLocalFile.setVisibility(View.GONE);
        mBtnUploadFile.setVisibility(View.GONE);
        mTvUploadedUrl.setVisibility(View.GONE);
    }

    @Override
    public void onCaptureWorking() {
        mTvCaptureStatus.setText(R.string.capture_capture_working);
    }

    @Override
    public void onCaptureStopping() {
        mTvCaptureStatus.setText(R.string.capture_capture_stopping);
    }

    @Override
    public void onCaptureFinish(String[] filePaths) {
        mTvCaptureStatus.setText(R.string.capture_capture_finished);
        mTvCaptureTime.setVisibility(View.GONE);
        mTvCaptureCount.setVisibility(View.GONE);
        mTvUploadedUrl.setVisibility(View.GONE);
        if (filePaths != null && filePaths.length > 0) {
            mBtnPlayCameraFile.setVisibility(View.VISIBLE);
            mBtnPlayCameraFile.setOnClickListener(v -> {
                PlayAndExportActivity.launchActivity(this, filePaths);
            });
            mBtnPlayLocalFile.setVisibility(View.VISIBLE);
            mBtnUploadFile.setVisibility(View.VISIBLE);
            mBtnPlayLocalFile.setOnClickListener(v -> {
                downloadFilesAndPlay(filePaths);
            });
            mBtnUploadFile.setOnClickListener(v -> {
                uploadFiles(filePaths);
            });
        } else {
            mBtnPlayCameraFile.setVisibility(View.GONE);
            mBtnPlayCameraFile.setOnClickListener(null);
            mBtnPlayLocalFile.setVisibility(View.GONE);
            mBtnPlayLocalFile.setOnClickListener(null);
            mBtnUploadFile.setVisibility(View.GONE);
            mBtnUploadFile.setOnClickListener(null);
        }
    }

    @Override
    public void onCaptureTimeChanged(long captureTime) {
        mTvCaptureTime.setVisibility(View.VISIBLE);
        mTvCaptureTime.setText(getString(R.string.capture_capture_time, TimeFormat.durationFormat(captureTime)));
    }

    @Override
    public void onCaptureCountChanged(int captureCount) {
        mTvCaptureCount.setVisibility(View.VISIBLE);
        mTvCaptureCount.setText(getString(R.string.capture_capture_count, captureCount));
    }

    private void downloadFilesAndPlay(String[] urls) {
        if (urls == null || urls.length == 0) {
            return;
        }

        String localFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/SDK_DEMO_CAPTURE";
        File folder = new File(localFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String[] fileNames = new String[urls.length];
        String[] localPaths = new String[urls.length];
        boolean needDownload = false;
        for (int i = 0; i < localPaths.length; i++) {
            fileNames[i] = urls[i].substring(urls[i].lastIndexOf("/") + 1);
            localPaths[i] = localFolder + "/" + fileNames[i];
            if (!new File(localPaths[i]).exists()) {
                needDownload = true;
            }
        }

        if (!needDownload) {
            PlayAndExportActivity.launchActivity(CaptureActivity.this, localPaths);
            return;
        }

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.osc_dialog_title_downloading)
                .content(getString(R.string.osc_dialog_msg_downloading, urls.length, 0, 0))
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        AtomicInteger successfulCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        for (int i = 0; i < localPaths.length; i++) {
            String url = urls[i];
            AndroidNetworking.download(url,localFolder,fileNames[i])
                    .setTag("download")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .setDownloadProgressListener(new DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDownloaded, long totalBytes) {
                            // do anything with progress
                        }
                    })
                    .startDownload(new DownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            successfulCount.incrementAndGet();
                            checkDownloadCount();
                        }
                        @Override
                        public void onError(ANError error) {
                            errorCount.incrementAndGet();
                            checkDownloadCount();
                        }
                        private void checkDownloadCount() {
                            dialog.setContent(getString(R.string.osc_dialog_msg_downloading, urls.length, successfulCount.intValue(), errorCount.intValue()));
                            if (successfulCount.intValue() + errorCount.intValue() >= urls.length) {
                                PlayAndExportActivity.launchActivity(CaptureActivity.this, localPaths);
                                dialog.dismiss();
                            }
                        }
                    });
//            OkGo.<File>get(url)
//                    .execute(new FileCallback(localFolder, fileNames[i]) {
//
//                        @Override
//                        public void onError(Response<File> response) {
//                            super.onError(response);
//                            errorCount.incrementAndGet();
//                            checkDownloadCount();
//                        }
//
//                        @Override
//                        public void onSuccess(Response<File> response) {
//                            successfulCount.incrementAndGet();
//                            checkDownloadCount();
//                        }
//
//                        private void checkDownloadCount() {
//                            dialog.setContent(getString(R.string.osc_dialog_msg_downloading, urls.length, successfulCount.intValue(), errorCount.intValue()));
//                            if (successfulCount.intValue() + errorCount.intValue() >= urls.length) {
//                                PlayAndExportActivity.launchActivity(CaptureActivity.this, localPaths);
//                                dialog.dismiss();
//                            }
//                        }
//                    });
        }
    }

    private void uploadFiles(String[] urls){
        if (urls == null || urls.length == 0) {
            return;
        }

        String localFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/SDK_DEMO_CAPTURE";
        File folder = new File(localFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String[] fileNames = new String[urls.length];
        String[] localPaths = new String[urls.length];
        boolean needDownload = false;
        for (int i = 0; i < localPaths.length; i++) {
            fileNames[i] = urls[i].substring(urls[i].lastIndexOf("/") + 1);
            localPaths[i] = localFolder + "/" + fileNames[i];
            if (!new File(localPaths[i]).exists()) {
                needDownload = true;
            }
        }

        if (!needDownload) {
            uploadFilesToServer(localPaths);
            return;
        }

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.osc_dialog_title_downloading)
                .content(getString(R.string.osc_dialog_msg_downloading, urls.length, 0, 0))
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .show();

        AtomicInteger successfulCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        for (int i = 0; i < localPaths.length; i++) {
            String url = urls[i];
            OkGo.<File>get(url)
                    .execute(new FileCallback(localFolder, fileNames[i]) {

                        @Override
                        public void onError(Response<File> response) {
                            super.onError(response);
                            errorCount.incrementAndGet();
                            checkDownloadCount();
                        }

                        @Override
                        public void onSuccess(Response<File> response) {
                            successfulCount.incrementAndGet();
                            checkDownloadCount();
                        }

                        private void checkDownloadCount() {
                            dialog.setContent(getString(R.string.osc_dialog_msg_downloading, urls.length, successfulCount.intValue(), errorCount.intValue()));
                            if (successfulCount.intValue() + errorCount.intValue() >= urls.length) {
                                uploadFilesToServer(localPaths);
                                dialog.dismiss();
                            }
                        }
                    });
        }
    }

    void uploadFilesToServer(String[] local_paths){
        String token = sharedPref.getString(this, SaveSharedPrefrence.PREFS_AUTH_TOKEN);
        if (token.isEmpty()) {
            Toast.makeText(this, "Autherntication failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        AtomicInteger successfulCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        String[] urlPaths = new String[local_paths.length];
        for (int i = 0; i < local_paths.length; i++) {
            String one_path = local_paths[i];
            File file = new File(one_path);
            if (file.exists()){
                showProgress("Uploading...");
                API.uploadCaptureFile(token, file, new APICallback<UploadedData>() {
                    @Override
                    public void onSuccess(UploadedData response) {
                        urlPaths[successfulCount.intValue()] = response.data;
                        Toast.makeText(CaptureActivity.this, "Uploaded file : " + response.data, Toast.LENGTH_SHORT).show();
                        successfulCount.incrementAndGet();
                        checkUploadCount();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(CaptureActivity.this, "Uploading Error : " + error, Toast.LENGTH_SHORT).show();
                        errorCount.incrementAndGet();
                        checkUploadCount();
                    }

                    private void checkUploadCount() {
                        String msg = getString(R.string.osc_dialog_msg_downloading, local_paths.length, successfulCount.intValue(), errorCount.intValue());
                        Toast.makeText(CaptureActivity.this, msg, Toast.LENGTH_SHORT).show();
                        if (successfulCount.intValue() + errorCount.intValue() >= local_paths.length) {
                            dismissProgress();
                            showS3Paths(urlPaths);
                        }
                    }
                });
            }
        }
    }

    public void showS3Paths(String[] uploaded_paths){
        mTvUploadedUrl.setVisibility(View.VISIBLE);
        String total_url = "";
        for(int i = 0; i < uploaded_paths.length; i++){
            total_url = uploaded_paths[i] + "\n";
        }
        mTvUploadedUrl.setText(total_url);
    }

    SaveSharedPrefrence sharedPref;
    private KProgressHUD hud;

    public void showProgress(String message) {
        hud = KProgressHUD.create(this).setLabel(message);
        hud.show();
    }

    public void dismissProgress() {
        if (hud != null) {
            hud.dismiss();
        }
    }
}
