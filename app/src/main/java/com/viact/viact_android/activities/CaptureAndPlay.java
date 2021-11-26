package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.CAMERA_360;
import static com.viact.viact_android.utils.Const.CAMERA_BUILT_IN;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_PHOTO_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_SPOT_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_VIDEO_PATH;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener;
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
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.SpotPhoto;
import com.viact.viact_android.utils.ImageFilePath;
import com.viact.viact_android.utils.NetworkManager;
import com.viact.viact_android.utils.TimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CaptureAndPlay extends BaseObserveCameraActivity implements ICaptureStatusListener, IExportCallback {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.player_image)    InstaImagePlayerView    mImagePlayerView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.player_video)    InstaVideoPlayerView    mVideoPlayerView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.photoview)       PhotoView               photoView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_capture_status) TextView             txt_status;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_choose_device) TextView              txt_device;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_current)      TextView                mTvCurrent;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_total)        TextView                mTvTotal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.group_progress)  Group                   mGroupProgress;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.seek_bar)        SeekBar                 mSeekBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_rec_stop)     LinearLayout            ll_rec_stop;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_confirm_group) LinearLayout            ll_confirm_group;


    int camera_kind = CAMERA_360;
    String capture_mode = "photo";
    String filename = "";
    int pin_id = 0;
    boolean bVideo_capture = false;

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_and_play);
        ButterKnife.bind(this);

        camera_kind = getIntent().getIntExtra("camera_kind", CAMERA_360);
        capture_mode = getIntent().getStringExtra("capture_mode");
        pin_id = getIntent().getIntExtra("pin_id", 0);

        if (camera_kind == CAMERA_360){
            // Capture Status Callback
            InstaCameraManager.getInstance().setCaptureStatusListener(this);
        }

        initLayout();
        capturePhotoAndVideo();
    }

    void initLayout(){
        dbHelper = DatabaseHelper.getInstance(this);
        mImagePlayerView.setVisibility(View.GONE);
        mVideoPlayerView.setVisibility(View.GONE);
        photoView.setVisibility(View.GONE);
        ll_rec_stop.setVisibility(View.GONE);
        ll_confirm_group.setVisibility(View.GONE);
        File folder = new File(EXT_STORAGE_SPOT_PATH);
        folder.mkdirs();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_confirm) void onClickConfirm(){
        PinPoint pin = dbHelper.getPin(pin_id + "");
        List<SpotPhoto> spp_list = dbHelper.getAllSpots(pin_id);

        SpotPhoto spot = new SpotPhoto();
        spot.pin_id = pin_id + "";
        spot.path = filename;
        if (spp_list.size() > 0){
            spot.category = spp_list.get(0).category;
        }
        spot.create_time = (long)(System.currentTimeMillis()/1000) + "";
        dbHelper.addSpot(spot);

        pin.update_time = (long)(System.currentTimeMillis()/1000) + "";
        dbHelper.updatePin(pin);
        Toast.makeText(this, "Spot Photo was added successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_cancel) void onClickCancel(){
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_rec_stop) void onClickVideoCapture(){
        InstaCameraManager.getInstance().stopNormalRecord();
    }

    void capturePhotoAndVideo(){
        if (capture_mode.equals("photo")){
            if (camera_kind == CAMERA_BUILT_IN) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoResultLauncher.launch(cameraIntent);
            } else {
                InstaCameraManager.getInstance().startNormalCapture(false);
            }
        } else {
            if (camera_kind == CAMERA_BUILT_IN){
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    videoResultLauncher.launch(takeVideoIntent);
                }
            } else {
                InstaCameraManager.getInstance().startNormalRecord();
                bVideo_capture = true;
            }
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        if (camera_kind == CAMERA_BUILT_IN){
            txt_device.setText(R.string.main_desc_camera_built_in);
        } else {
            txt_device.setText(R.string.main_desc_camera_360);
        }
    }

    @Override
    public void onCameraConnectError() {
        super.onCameraConnectError();
        Toast.makeText(this, R.string.main_toast_camera_connect_error, Toast.LENGTH_SHORT).show();
        finish();
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

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        if (enabled) {
            Toast.makeText(this, R.string.main_toast_camera_connected, Toast.LENGTH_SHORT).show();
        } else {
            NetworkManager.getInstance().clearBindProcess();
            Toast.makeText(this, R.string.main_toast_camera_disconnected, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onCaptureStarting() {
        Log.d("Viact", "capture start");
        txt_status.setText(R.string.capture_capture_starting);
        txt_status.setVisibility(View.VISIBLE);
        if (bVideo_capture){
            ll_rec_stop.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCaptureWorking() {
        Log.d("Viact", "capture working");
        txt_status.setText(R.string.capture_capture_working);
        if (bVideo_capture){
            ll_rec_stop.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCaptureStopping() {
        bVideo_capture = false;
        txt_status.setVisibility(View.GONE);
        ll_rec_stop.setVisibility(View.GONE);
        Toast.makeText(this, R.string.capture_capture_stopping, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCaptureTimeChanged(long captureTime) {

    }

    @Override
    public void onCaptureCountChanged(int captureCount) {

    }

    @Override
    public void onCaptureFinish(String[] filePaths) {
        bVideo_capture = false;
        ll_rec_stop.setVisibility(View.GONE);
        txt_status.setVisibility(View.GONE);
        if (filePaths != null && filePaths.length > 0) {
            //download captured file as panorama
            mWorkWrapper = new WorkWrapper(filePaths);
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
                File folder = new File(EXT_STORAGE_PHOTO_PATH);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                EXPORT_DIR_PATH = EXT_STORAGE_PHOTO_PATH;
                exp_filename = exp_filename + ".jpg";
                exportImageOriginal();
            }
            showExportDialog();
        } else {
            Toast.makeText(this, "Capture failed!", Toast.LENGTH_SHORT).show();
            finish();
        }
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

    private void exportImageOriginal() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setImageFusion(mWorkWrapper.isPanoramaFile())
                .setTargetPath(EXPORT_DIR_PATH + "/" +exp_filename);

        mCurrentExportId = ExportUtils.exportImage(mWorkWrapper, builder, this);
    }

    private void exportVideoOriginal() {
        ExportVideoParamsBuilder builder = new ExportVideoParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setTargetPath(EXPORT_DIR_PATH + "/" + exp_filename)
                // 导出视频对手机性能要求较高，如导出5.7k时遇到oom或者app被系统强制杀掉的情况，请自行设置较小宽高
                // Exporting video requires high performance of mobile phones. For example, when exporting 5.7k,
                // you encounter oom or app being forcibly killed by the system, please set a smaller width and height by yourself
                .setWidth(2048)
                .setHeight(1024);
//                .setBitrate(20 * 1024 * 1024);
        mCurrentExportId = ExportUtils.exportVideo(mWorkWrapper, builder, this);
    }

    //download image and video
    WorkWrapper mWorkWrapper;
    MaterialDialog mExportDialog;
    String exp_filename, EXPORT_DIR_PATH;
    int mCurrentExportId = -1;

    @Override
    public void onSuccess() {
        mExportDialog.dismiss();
        mCurrentExportId = -1;

        filename = EXPORT_DIR_PATH + "/" + exp_filename;
        ll_confirm_group.setVisibility(View.VISIBLE);
        playCapturedFile();
    }

    @Override
    public void onFail(int errorCode, String errorMsg) {
        // if GPU not support, errorCode is -10003 or -10005 or -13020
        mExportDialog.setContent(R.string.export_dialog_msg_export_failed, errorCode);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
        Toast.makeText(this, "Export failed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onCancel() {
        mExportDialog.setContent(R.string.export_dialog_msg_export_stopped);
        mExportDialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.VISIBLE);
        mExportDialog.getActionButton(DialogAction.NEUTRAL).setVisibility(View.GONE);
        mCurrentExportId = -1;
        Toast.makeText(this, "Export failed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onProgress(float progress) {
        // callback only when exporting video
        mExportDialog.setContent(getString(R.string.export_dialog_msg_export_progress, String.format(Locale.CHINA, "%.1f", progress * 100) + "%"));
    }

    private void stopExport() {
        if (mCurrentExportId != -1) {
            ExportUtils.stopExport(mCurrentExportId);
            mCurrentExportId = -1;
            Toast.makeText(this, "Export stopped!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    void playCapturedFile(){
        String[] urls = new String[] {filename};
        mWorkWrapper = new WorkWrapper(urls);
        if (mWorkWrapper.isVideo()){
            mVideoPlayerView.setVisibility(View.VISIBLE);
            playVideo(false);
        } else {
            mImagePlayerView.setVisibility(View.VISIBLE);
            playImage(false);
        }
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
                Toast.makeText(CaptureAndPlay.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(CaptureAndPlay.this, toast, Toast.LENGTH_LONG).show();
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
                Toast.makeText(CaptureAndPlay.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(CaptureAndPlay.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        ImageParamsBuilder builder = new ImageParamsBuilder();
        builder.setWithSwitchingAnimation(true);
        builder.setImageFusion(mWorkWrapper.isPanoramaFile());
        if (isPlaneMode) {
            builder.setRenderModelType(ImageParamsBuilder.RENDER_MODE_PLANE_STITCH);
            builder.setScreenRatio(2, 1);
        }

        mImagePlayerView.prepare(mWorkWrapper, builder);
        mImagePlayerView.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImagePlayerView != null) {
            mImagePlayerView.destroy();
        }
        if (mVideoPlayerView != null) {
            mVideoPlayerView.destroy();
        }
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> photoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    String dir_name = EXT_STORAGE_PHOTO_PATH;
                    File dir = new File(dir_name);
                    if (!dir.exists()){
                        dir.mkdir();
                    }
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    String f_name = dir_name + pin_id +"_" + ts + ".png";
                    try (FileOutputStream out = new FileOutputStream(f_name)) {
                        photo.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                        filename = f_name;
                        ll_confirm_group.setVisibility(View.VISIBLE);
                        photoView.setVisibility(View.VISIBLE);
                        Glide.with (this)
                                .load (filename)
                                .into (photoView);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "Camera was cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    ActivityResultLauncher<Intent> videoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    Uri contentUri = data.getData();
                    filename = ImageFilePath.getPath(this, contentUri);
                    ll_confirm_group.setVisibility(View.VISIBLE);
                    playCapturedFile();
                } else {
                    Toast.makeText(this, "Camera capture failed!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
}