package com.arashivision.sdk.demo.activity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.osc.OscManager;
import com.arashivision.sdk.demo.osc.callback.IOscCallback;
import com.arashivision.sdk.demo.osc.delegate.OscRequestDelegate;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;

import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.Nullable;

public class OscActivity extends BaseObserveCameraActivity implements IOscCallback {

    private MaterialDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_osc);
        setTitle(R.string.osc_toolbar_title);

        mDialog = new MaterialDialog.Builder(this)
                .title("")
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .positiveText(android.R.string.ok)
                .build();

        // 配置网络请求代理
        // Setting up a network request proxy
        OscManager.getInstance().setOscRequestDelegate(new OscRequestDelegate());

        // 查看相机基本信息与支持的osc命令
        // Get the basic information about the camera and functionality it supports
        findViewById(R.id.btn_info).setOnClickListener(v -> {
            // 先检查相机是否已连接
            // First determine if the camera is connected
            if (isCameraConnected()) {
                OscManager.getInstance().customRequest("/osc/info", null, this);
            } else {
                promptToConnectCamera();
            }
        });

        // 获取相机属性状态
        // Get the attributes of the camera
        findViewById(R.id.btn_state).setOnClickListener(v -> {
            if (isCameraConnected()) {
                OscManager.getInstance().customRequest("/osc/state", "", this);
            } else {
                promptToConnectCamera();
            }
        });

        // 获取相机支持的Options参数
        // Get camera supported options
        findViewById(R.id.btn_get_support_options).setOnClickListener(v -> {
            if (isCameraConnected()) {
                String content = "{\"name\":\"camera.getOptions\"," +
                        "  \"parameters\": {\n" +
                        "      \"optionNames\": [\n" +
                        "          \"_batteryCapacity\",\n" +
                        "          \"remainingSpace\",\n" +
                        "          \"totalSpace\",\n" +
                        "          \"captureModeSupport\",\n" +
                        "          \"captureIntervalSupport\",\n" +
                        "          \"exposureProgramSupport\",\n" +
                        "          \"isoSupport\",\n" +
                        "          \"shutterSpeedSupport\",\n" +
                        "          \"whiteBalanceSupport\",\n" +
                        "          \"hdrSupport\",\n" +
                        "          \"exposureBracketSupport\"\n" +
                        "      ]\n" +
                        "  }\n" +
                        "}}";
                OscManager.getInstance().customRequest("/osc/commands/execute", content, this);
            } else {
                promptToConnectCamera();
            }
        });

        // 拍照，此处为HDR拍照
        // Take Picture. Here is HDR Capture.
        findViewById(R.id.btn_take_picture).setOnClickListener(v -> {
            if (isCameraConnected()) {
                String options = "\"captureMode\":\"image\"," +
                        "\"hdr\":\"hdr\"," +
                        "\"exposureBracket\":{" +
                        "   \"shots\":3," +
                        "   \"increment\":2" +
                        "}";
                OscManager.getInstance().takePicture(options, this);
            } else {
                promptToConnectCamera();
            }
        });

        // 开始录像
        // Start Record
        findViewById(R.id.btn_start_record).setOnClickListener(v -> {
            if (isCameraConnected()) {
                String options = "\"captureMode\":\"video\"";
                OscManager.getInstance().startRecord(options, this);
            } else {
                promptToConnectCamera();
            }
        });

        // 停止录像
        // Stop Record
        findViewById(R.id.btn_stop_record).setOnClickListener(v -> {
            if (isCameraConnected()) {
                OscManager.getInstance().stopRecord(this);
            } else {
                promptToConnectCamera();
            }
        });
    }

    private boolean isCameraConnected() {
        return InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE;
    }

    @Override
    public void onStartRequest() {
        mDialog.setTitle(R.string.osc_dialog_title_send_request);
        mDialog.setContent(R.string.osc_dialog_msg_send_request);
        mDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.GONE);
        mDialog.show();
    }

    @Override
    public void onSuccessful(Object object) {
        mDialog.setTitle(R.string.osc_dialog_title_success);
        if (object == null) {
            // setOptions()、startRecord() return null
            mDialog.setContent(R.string.osc_dialog_msg_no_data);
        } else {
            try {
                // customRequest() will callback the original content returned by OSC
                JSONObject jsonObject = new JSONObject((String) object);
                mDialog.setContent(jsonObject.toString(2));
            } catch (Exception e) {
                // takePicture()、stopRecord() will return file address (String[] urls), could be downloaded to local
                StringBuilder message = new StringBuilder();
                if (object.getClass().isArray()) {
                    for (Object obj : (Object[]) object) {
                        message.append(obj).append("\n");
                    }
                    downloadFiles((String[]) object);
                } else {
                    message.append(object.toString());
                }
                mDialog.setContent(message.toString());
            }
        }
        mDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(String message) {
        mDialog.setTitle(R.string.osc_dialog_title_failed);
        mDialog.setContent(message);
        mDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
    }

    private void promptToConnectCamera() {
        Toast.makeText(this, R.string.osc_toast_connect_camera, Toast.LENGTH_SHORT).show();
    }

    private void downloadFiles(String[] urls) {
        if (urls == null || urls.length == 0) {
            return;
        }

        String localFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SDK_DEMO_OSC";
        String[] fileNames = new String[urls.length];
        String[] localPaths = new String[urls.length];
        for (int i = 0; i < localPaths.length; i++) {
            fileNames[i] = urls[i].substring(urls[i].lastIndexOf("/") + 1);
            localPaths[i] = localFolder + "/" + fileNames[i];
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
                                // Demo直接将filePaths传参打开播放页
                                // This demo directly transfers the file paths to the play page for playback
                                PlayAndExportActivity.launchActivity(OscActivity.this, localPaths);
                                dialog.dismiss();
                            }
                        }
                    });
        }
    }
}
