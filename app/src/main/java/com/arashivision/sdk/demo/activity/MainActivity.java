package com.arashivision.sdk.demo.activity;

import static com.arashivision.sdk.demo.util.Const.ACTIVE_MAIN_PAGE;
import static com.arashivision.sdk.demo.util.Const.ACTIVE_OTHER_PAGE;
import static com.arashivision.sdk.demo.util.Const.CONNECT_MODE_NONE;
import static com.arashivision.sdk.demo.util.Const.CONNECT_MODE_USB;
import static com.arashivision.sdk.demo.util.Const.CONNECT_MODE_WIFI;
import static com.arashivision.sdk.demo.util.Const.EXT_STORAGE_DOC_PATH;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.dialog.SiteMapDlg;
import com.arashivision.sdk.demo.models.UploadedData;
import com.arashivision.sdk.demo.util.API;
import com.arashivision.sdk.demo.util.APICallback;
import com.arashivision.sdk.demo.util.ImageFilePath;
import com.arashivision.sdk.demo.util.NetworkManager;
import com.arashivision.sdk.demo.util.SaveSharedPrefrence;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseObserveCameraActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_ll_home)     LinearLayout        view_main;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_ll_other)    LinearLayout        view_other;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_bg_connect)   View          view_connect_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_connect_option)   View      view_connect_option;
    //bottom bar
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_iv_main)    ImageView         iv_bt_main;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_iv_other)    ImageView        iv_bt_other;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_iv_center)    ImageView       iv_bt_center;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_txt_main)    TextView         txt_bt_main;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_txt_other)   TextView         txt_bt_other;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_built_in_preview)   Button         btn_built_in_preview;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_built_in_upload)    Button         btn_built_in_upload;

    int active_page = ACTIVE_MAIN_PAGE;
    int connect_mode = CONNECT_MODE_NONE;
    int temp_connect = CONNECT_MODE_NONE;

    String captured_file = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        ButterKnife.bind(this);
        initLayout();
    }

    void initLayout(){
        // checkStoragePermission();
        if (InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE) {
            onCameraStatusChanged(true);
        }

        findViewById(R.id.btn_full_demo).setOnClickListener(v -> {
//            uploadTest();
//            showSiteMapDlg();
            startActivity(new Intent(MainActivity.this, FullDemoActivity.class));
        });

        findViewById(R.id.btn_connect_by_wifi).setOnClickListener(v -> {
            InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);
        });

        findViewById(R.id.btn_connect_by_usb).setOnClickListener(v -> {
            InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_USB);
        });

        findViewById(R.id.btn_close_camera).setOnClickListener(v -> {
            InstaCameraManager.getInstance().closeCamera();
        });

        findViewById(R.id.btn_capture).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CaptureActivity.class));
        });

        findViewById(R.id.btn_preview).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PreviewActivity.class));
        });

        findViewById(R.id.btn_preview2).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Preview2Activity.class));
        });

        findViewById(R.id.btn_preview3).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Preview3Activity.class));
        });

        findViewById(R.id.btn_live).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LiveActivity.class));
        });

        findViewById(R.id.btn_osc).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, OscActivity.class));
        });

        findViewById(R.id.btn_list_camera_file).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CameraFilesActivity.class));
        });

        findViewById(R.id.btn_settings).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MoreSettingActivity.class));
        });

        findViewById(R.id.btn_play).setOnClickListener(v -> {
            PlayAndExportActivity.launchActivity(this, new String[]{
                    StitchActivity.COPY_DIR + "/img1.jpg",
                    StitchActivity.COPY_DIR + "/img2.jpg",
                    StitchActivity.COPY_DIR + "/img3.jpg"
            });
        });

        findViewById(R.id.btn_stitch).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StitchActivity.class));
        });

        findViewById(R.id.btn_firmware_upgrade).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FwUpgradeActivity.class));
        });

        refreshLayout();
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    private void checkStoragePermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onDenied(permissions -> {
                    if (AndPermission.hasAlwaysDeniedPermission(this, permissions)) {
                        AndPermission.with(this)
                                .runtime()
                                .setting()
                                .start(1000);
                    }
                    Toast.makeText(this, "Please check the permission on setting", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .onGranted(data -> {

                })
                .start();
    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        findViewById(R.id.btn_capture).setEnabled(enabled);
        findViewById(R.id.btn_preview).setEnabled(enabled);
        findViewById(R.id.btn_preview2).setEnabled(enabled);
        findViewById(R.id.btn_preview3).setEnabled(enabled);
        findViewById(R.id.btn_live).setEnabled(enabled);
        findViewById(R.id.btn_osc).setEnabled(enabled);
        findViewById(R.id.btn_list_camera_file).setEnabled(enabled);
        findViewById(R.id.btn_settings).setEnabled(enabled);
        findViewById(R.id.btn_firmware_upgrade).setEnabled(enabled);
        if (enabled) {
            Toast.makeText(this, R.string.main_toast_camera_connected, Toast.LENGTH_SHORT).show();
            connect_mode = temp_connect;
            temp_connect = CONNECT_MODE_NONE;
        } else {
            NetworkManager.getInstance().clearBindProcess();
            Toast.makeText(this, R.string.main_toast_camera_disconnected, Toast.LENGTH_SHORT).show();
            connect_mode = CONNECT_MODE_NONE;
        }
        refreshLayout();
    }

    @Override
    public void onCameraConnectError() {
        super.onCameraConnectError();
        Toast.makeText(this, R.string.main_toast_camera_connect_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraSDCardStateChanged(boolean enabled) {
        super.onCameraSDCardStateChanged(enabled);
        if (enabled) {
            Toast.makeText(this, R.string.main_toast_sd_enabled, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.main_toast_sd_disabled, Toast.LENGTH_SHORT).show();
        }
    }

    void refreshLayout(){
        view_connect_option.setVisibility(View.GONE);
        view_connect_bg.setVisibility(View.GONE);

        switch (active_page){
            case ACTIVE_OTHER_PAGE:
                view_main.setVisibility(View.GONE);
                view_other.setVisibility(View.VISIBLE);
                iv_bt_main.setColorFilter(ContextCompat.getColor(this, R.color.menu_disable), android.graphics.PorterDuff.Mode.SRC_IN);
                txt_bt_main.setTextColor(ContextCompat.getColor(this, R.color.menu_disable));
                iv_bt_other.setColorFilter(ContextCompat.getColor(this, R.color.menu_active), android.graphics.PorterDuff.Mode.SRC_IN);
                txt_bt_other.setTextColor(ContextCompat.getColor(this, R.color.menu_active));
                break;
            default:
                view_main.setVisibility(View.VISIBLE);
                view_other.setVisibility(View.GONE);
                iv_bt_main.setColorFilter(ContextCompat.getColor(this, R.color.menu_active), android.graphics.PorterDuff.Mode.SRC_IN);
                txt_bt_main.setTextColor(ContextCompat.getColor(this, R.color.menu_active));
                iv_bt_other.setColorFilter(ContextCompat.getColor(this, R.color.menu_disable), android.graphics.PorterDuff.Mode.SRC_IN);
                txt_bt_other.setTextColor(ContextCompat.getColor(this, R.color.menu_disable));
                break;
        }

        switch (connect_mode) {
            case CONNECT_MODE_WIFI:
            case CONNECT_MODE_USB:
                iv_bt_center.setImageResource(R.drawable.ic_connect);
                break;
            default:
                iv_bt_center.setImageResource(R.drawable.ic_disconnect);
                break;
        }

        if (captured_file.isEmpty()) {
            btn_built_in_preview.setEnabled(false);
            btn_built_in_upload.setEnabled(false);
        } else {
            btn_built_in_preview.setEnabled(true);
            btn_built_in_upload.setEnabled(true);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_built_in_upload) void onClickBuiltinUpload(){
        if (captured_file.isEmpty()) return;
        uploadFilesToServer(captured_file);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_built_in_preview) void onClickBuiltinPreview(){
        if (captured_file.isEmpty()) return;
        String[] filePaths = new String[]{captured_file};
        PlayAndExportActivity.launchActivity(this, filePaths);
    }

    @SuppressLint({"NonConstantResourceId", "QueryPermissionsNeeded"})
    @OnClick(R.id.btn_built_in_capture) void onClickBuiltinCamera(){
        captured_file = "";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_capture_camera);
        builder.setPositiveButton(R.string.main_capture_photo, (dialogInterface, i) -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            photoResultLauncher.launch(cameraIntent);
        });
        builder.setNegativeButton(R.string.main_capture_video, (dialogInterface, i) -> {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                videoResultLauncher.launch(takeVideoIntent);
            }
        });
        builder.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.bottom_ll_center) void onClickConnect(){
        if (connect_mode != CONNECT_MODE_NONE){
            InstaCameraManager.getInstance().closeCamera();
        } else if (view_connect_bg.getVisibility() != View.VISIBLE){
            showConnectOption();
        } else if (view_connect_bg.getVisibility() == View.VISIBLE){
            onClickConnectBack();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_bottom_home) void onClickHome(){
        active_page = ACTIVE_MAIN_PAGE;
        refreshLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_bottom_other) void onClickOther(){
        active_page = ACTIVE_OTHER_PAGE;
        refreshLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.main_btn_wifi) void onClickConnectWifi(){
        temp_connect = CONNECT_MODE_WIFI;
        InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);
        onClickConnectBack();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.main_btn_usb) void onClickConnectUsb(){
        temp_connect = CONNECT_MODE_USB;
        InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_USB);
        onClickConnectBack();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.main_view_bg_connect) void onClickConnectBack(){
        Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        view_connect_option.startAnimation(bottomDown);
        view_connect_option.setVisibility(View.GONE);
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        view_connect_bg.startAnimation(fadeout);
        view_connect_bg.setVisibility(View.GONE);
    }

    void showConnectOption(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view_connect_bg.startAnimation(fadein);
        view_connect_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);

        view_connect_option.startAnimation(bottomUp);
        view_connect_option.setVisibility(View.VISIBLE);
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> photoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    String dir_name = EXT_STORAGE_DOC_PATH + getString(R.string.app_name) + File.separator;
                    File dir = new File(dir_name);
                    if (!dir.exists()){
                        dir.mkdir();
                    }
                    String f_name = dir_name + "temp.png";
                    try (FileOutputStream out = new FileOutputStream(f_name)) {
                        photo.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                        captured_file = f_name;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    captured_file = "";
                    Toast.makeText(this, "Camera was cancelled", Toast.LENGTH_SHORT).show();
                }
                refreshLayout();
            });

    ActivityResultLauncher<Intent> videoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    Uri contentUri = data.getData();
                    captured_file = ImageFilePath.getPath(this, contentUri);
                } else {
                    captured_file = "";
                }
                refreshLayout();
            });

    void uploadTest(){
        sharedPref = new SaveSharedPrefrence();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/front.png";
        String token = sharedPref.getString(this, SaveSharedPrefrence.PREFS_AUTH_TOKEN);
        File ff = new File(path);
        API.uploadCaptureFile(token, ff, new APICallback<UploadedData>() {
            @Override
            public void onSuccess(UploadedData response) {
                Toast.makeText(MainActivity.this, "Success! Uploaded file : " + response.data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MainActivity.this, "Uploading Error : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSiteMapDlg(String pano_url){
        SiteMapDlg inputDlg = new SiteMapDlg(this, pano_url);

        View decorView = inputDlg.getWindow().getDecorView();
        decorView.setBackgroundResource(android.R.color.transparent);
        inputDlg.show();
    }

    void uploadFilesToServer(String local_path){
        sharedPref = new SaveSharedPrefrence();
        String token = sharedPref.getString(this, SaveSharedPrefrence.PREFS_AUTH_TOKEN);
        if (token.isEmpty()) {
            Toast.makeText(this, "Autherntication failed!", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(local_path);
        if (file.exists()){
            showProgress("Uploading...");
            API.uploadCaptureFile(token, file, new APICallback<UploadedData>() {
                @Override
                public void onSuccess(UploadedData response) {
                    dismissProgress();
                    Toast.makeText(MainActivity.this, "Success! Uploaded file : " + response.data, Toast.LENGTH_SHORT).show();
                    showSiteMapDlg(response.data);
                }

                @Override
                public void onFailure(String error) {
                    dismissProgress();
                    Toast.makeText(MainActivity.this, "Uploading Error : " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Can not find the uploading file!", Toast.LENGTH_SHORT).show();
        }
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
