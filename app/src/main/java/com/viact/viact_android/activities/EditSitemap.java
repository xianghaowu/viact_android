package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.CAMERA_360;
import static com.viact.viact_android.utils.Const.CAMERA_BUILT_IN;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_PHOTO_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_VIDEO_PATH;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MAX_LEN;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MIN_LEN;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener;
import com.arashivision.sdkmedia.export.ExportImageParamsBuilder;
import com.arashivision.sdkmedia.export.ExportUtils;
import com.arashivision.sdkmedia.export.ExportVideoParamsBuilder;
import com.arashivision.sdkmedia.export.IExportCallback;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.utils.ImageFilePath;
import com.viact.viact_android.utils.NetworkManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditSitemap extends BaseObserveCameraActivity implements ICaptureStatusListener, IExportCallback {

    Project cur_proc;
    List<PinPoint> pin_list = new ArrayList<>();
    DatabaseHelper dbHelper;

    int kind_camera;
    PinPoint edit_pin;
    int tempX, tempY;
    PinPoint selected_pin = null;


    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edit_site_photoview)    PhotoView    photo_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edit_site_thumbnail)    ImageView    iv_thumbnail;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pinsContainer)          RelativeLayout   pinsContainer;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_choose_device)    TextView txt_sel_camera;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_capture_status)    TextView mTvCaptureStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sitemap);
        setTitle(R.string.edit_sitemap_title);
        ButterKnife.bind(this);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.edit_site_thumbnail) void onClickThumbnail(){

    }

    void refreshLayout(){
        for (int i = 0 ; i < pin_list.size(); i++ ) {
            PinPoint pp_pin = pin_list.get(i);
            if (pp_pin.iv_mark != null){
                float img_scale = photo_view.getScale();
                int pin_wh = (int)((PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale);
                RectF rc = photo_view.getDisplayRect();
                float site_width = rc.width();
                float site_height = rc.height();
                tempX = (int) (site_width * pp_pin.x + rc.left) - pin_wh / 4;
                tempY = (int) (site_height * pp_pin.y + rc.top) - pin_wh * 3 / 4;

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pin_wh, pin_wh);
                // Setting position of our ImageView
                layoutParams.leftMargin = tempX;
                layoutParams.topMargin = tempY;
                pp_pin.iv_mark.setLayoutParams(layoutParams);
                pp_pin.iv_mark.requestLayout();

                int xMax = photo_view.getWidth();
                int yMax = photo_view.getHeight();

                if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
                    pp_pin.iv_mark.setVisibility(View.INVISIBLE);
                } else{
                    pp_pin.iv_mark.setVisibility(View.VISIBLE);
                }
            }
        }

        if (kind_camera == CAMERA_BUILT_IN){
            txt_sel_camera.setText(R.string.main_desc_camera_built_in);
        } else {
            txt_sel_camera.setText(R.string.main_desc_camera_360);
        }
    }

    void drawPins(){
        pinsContainer.removeAllViews();
        pin_list = dbHelper.getPinsForProject(cur_proc.id + "");
        for (int i = 0 ; i < pin_list.size(); i++ ){
            PinPoint p_pin = pin_list.get(i);
            // calculate Pin position
            float img_scale = photo_view.getScale();
            int pin_wh = (int)((PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale);
            RectF rc = photo_view.getDisplayRect();
            float site_width = rc.width();
            float site_height = rc.height();
            tempX = (int) (site_width * p_pin.x + rc.left) - pin_wh / 4;
            tempY = (int) (site_height * p_pin.y + rc.top) - pin_wh * 3 / 4;

            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.pin);
            iv.setOnClickListener(view -> {
                selected_pin = getPinFromList(view);
                if (selected_pin != null && !selected_pin.path.isEmpty()){
                    Glide.with (this)
                            .load (selected_pin.path)
                            .into (iv_thumbnail);
                }
            });

            iv.setOnLongClickListener(view -> {
                confirmDeletePin(view);
                return false;
            });

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            lp.setMargins(tempX, tempY, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            pinsContainer.addView(iv, lp);
            iv.getLayoutParams().height = pin_wh;
            iv.getLayoutParams().width = pin_wh;
            iv.requestLayout();
            int xMax = photo_view.getWidth();
            int yMax = photo_view.getHeight();

            if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
                iv.setVisibility(View.INVISIBLE);
            } else{
                iv.setVisibility(View.VISIBLE);
            }
            pin_list.get(i).iv_mark = iv;
        }

        Glide.with (this)
                .load (edit_pin.path)
                .into (iv_thumbnail);
    }

    void initLayout(){
        dbHelper = DatabaseHelper.getInstance(this);
        Bundle data = getIntent().getExtras();
        cur_proc = (Project) data.getParcelable("project");
        kind_camera = getIntent().getIntExtra("kind_camera", CAMERA_BUILT_IN);

        Glide.with (this)
                .load (cur_proc.site_map)
                .into (photo_view);

        photo_view.setOnMatrixChangeListener(rect -> refreshLayout());

        photo_view.setOnPhotoTapListener(new PhotoTapListener());

        if (kind_camera == CAMERA_360){
            if (InstaCameraManager.getInstance().getCameraConnectedType() == InstaCameraManager.CONNECT_TYPE_NONE) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Connection Type");
                // Set up the buttons
                builder.setPositiveButton("Wifi", (dialog, which) -> {
                    InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);
                });
                builder.setNegativeButton("USB", (dialog, which) -> {
                    InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_USB);
                });

                builder.show();
            }
        }

        drawPins();
    }

    private PinPoint getPinFromList(View view){
        for (int i = 0 ; i < pin_list.size(); i++){
            PinPoint pin = pin_list.get(i);
            if (pin.iv_mark != null && pin.iv_mark == view){
                return pin;
            }
        }
        return null;
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    void confirmDeletePin(final View view){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure to remove this Pin?");
        alertDialogBuilder.setPositiveButton("Yes", (arg0, arg1) -> {
            PinPoint pin = getPinFromList(view);
            if (pin.id > -1){
                dbHelper.deletePin(pin.id + "");
            }
            pin_list.remove(pin);
            pinsContainer.removeView(view);
            Toast.makeText(this, "Selected Pin was removed", Toast.LENGTH_SHORT).show();
        });
        alertDialogBuilder.setNegativeButton("No", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void selectCaptureKind(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_capture_camera);
        builder.setPositiveButton(R.string.main_capture_photo, (dialogInterface, i) -> {
            if (kind_camera == CAMERA_BUILT_IN) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoResultLauncher.launch(cameraIntent);
            } else {
                if (checkSdCardEnabled()) {
                    InstaCameraManager.getInstance().startNormalCapture(false);
                }
            }
        });
        builder.setNegativeButton(R.string.main_capture_video, (dialogInterface, i) -> {
            if (kind_camera == CAMERA_BUILT_IN) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    videoResultLauncher.launch(takeVideoIntent);
                }
            } else {

            }

        });
        builder.show();
    }

    //download image and video
    WorkWrapper mWorkWrapper;
    MaterialDialog mExportDialog;
    String exp_filename, EXPORT_DIR_PATH;
    int mCurrentExportId = -1;

    @Override
    public void onCaptureFinish(String[] filePaths) {
        mTvCaptureStatus.setVisibility(View.GONE);
        if (filePaths != null && filePaths.length > 0) {
            //download captured file as panorama
            mWorkWrapper = new WorkWrapper(filePaths);
            exp_filename = "exp_" + System.currentTimeMillis();
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

    private boolean checkSdCardEnabled() {
        if (!InstaCameraManager.getInstance().isSdCardEnabled()) {
            Toast.makeText(this, R.string.capture_toast_sd_card_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
        refreshLayout();
    }

    @Override
    public void onCaptureStarting() {
        mTvCaptureStatus.setText(R.string.capture_capture_starting);
        mTvCaptureStatus.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCaptureWorking() {
        mTvCaptureStatus.setText(R.string.capture_capture_working);
    }

    @Override
    public void onCaptureStopping() {
        mTvCaptureStatus.setVisibility(View.GONE);
        Toast.makeText(this, R.string.capture_capture_stopping, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess() {
        mExportDialog.dismiss();
        mCurrentExportId = -1;
        // add Pin
        edit_pin.path = EXPORT_DIR_PATH + "/" + exp_filename;
        dbHelper.addPin(edit_pin);
        drawPins();
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

    private void stopExport() {
        if (mCurrentExportId != -1) {
            ExportUtils.stopExport(mCurrentExportId);
            mCurrentExportId = -1;
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
                    String f_name = dir_name + cur_proc.id + ts + ".png";
                    try (FileOutputStream out = new FileOutputStream(f_name)) {
                        photo.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                        edit_pin.path = f_name;
                        dbHelper.addPin(edit_pin);
                        drawPins();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Camera was cancelled", Toast.LENGTH_SHORT).show();
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
                    edit_pin.path = ImageFilePath.getPath(this, contentUri);
                    dbHelper.addPin(edit_pin);
                    drawPins();
                } else {
                    Toast.makeText(this, "Camera capture failed!", Toast.LENGTH_SHORT).show();
                }
            });

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            edit_pin = new PinPoint();
            edit_pin.p_id = cur_proc.id + "";
            edit_pin.x = x;
            edit_pin.y = y;
            selectCaptureKind();
        }
    }
}