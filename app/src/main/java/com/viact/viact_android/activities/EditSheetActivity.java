package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.ACTIVE_MAIN_PAGE;
import static com.viact.viact_android.utils.Const.ACTIVE_OTHER_PAGE;
import static com.viact.viact_android.utils.Const.CAMERA_360;
import static com.viact.viact_android.utils.Const.CAMERA_BUILT_IN;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MAX_LEN;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MIN_LEN;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.adapters.ScenesAdapter;
import com.viact.viact_android.dialogs.CreatePinDlg;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Scene;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;
import com.viact.viact_android.utils.NetworkManager;
import com.viact.viact_android.utils.TimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditSheetActivity extends BaseObserveCameraActivity {

    Sheet cur_sheet;

    DatabaseHelper dbHelper;

    PinPoint edit_pin;
    int tempX, tempY;
    Scene selected_scene = null;

    int kind_camera = CAMERA_360;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edit_site_photoview)    PhotoView    photo_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pinsContainer)          RelativeLayout   pinsContainer;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_choose_device)    TextView txt_sel_camera;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_side_menu)          LinearLayout ll_side_menu;
    //connection
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_bg_connect)        View        view_connect_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_connect_option)    View        view_connect_option;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_other)             View        view_other;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_home)                   View        view_home;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rl_menu_bg)                  View        view_menu_bg;
    //bottom bar
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_iv_main)    ImageView iv_bt_main;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_iv_other)    ImageView        iv_bt_other;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_iv_center)    ImageView       iv_bt_center;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_txt_main)    TextView         txt_bt_main;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.bottom_txt_other)   TextView         txt_bt_other;

    //toolbar
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edit_sheet_ib_edit)    ImageButton   btn_edit;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edit_sheet_tv_title)   TextView      tv_title;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.edit_sheet_et_title)    EditText     et_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sheet);
        ButterKnife.bind(this);

        Bundle data = getIntent().getExtras();
        int sh_id = data.getInt("sheet_id", -1);
        dbHelper = DatabaseHelper.getInstance(this);

        if (sh_id == -1){
            finish();
        }
        cur_sheet = dbHelper.getSheet(sh_id);
        if (cur_sheet == null) finish();

        Glide.with (this)
                .load (cur_sheet.path)
                .into (photo_view);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.rl_menu_bg) void onClickMenuHide(){
        hideSideMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_side_menu_add) void onClickMenuAdd(){
        hideSideMenu();
        selectCaptureKind();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_side_menu_view) void onClickMenuView(){
        hideSideMenu();
        if (selected_scene.ppt != null && selected_scene.ppt.id >= 0){
            Intent viewIntent = new Intent(this, RoomViewActivity.class);
            viewIntent.putExtra("pin", selected_scene.ppt);
            startActivity(viewIntent);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_side_menu_update) void onClickMenuUpdate(){
        hideSideMenu();
        if (selected_scene.ppt != null && selected_scene.ppt.id >= 0){
            CreatePinDlg createDlg = new CreatePinDlg(this, selected_scene.ppt, null);

            View decorView = createDlg.getWindow().getDecorView();
            decorView.setBackgroundResource(android.R.color.transparent);
            createDlg.show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_side_menu_delete) void onClickMenuDelete(){
        hideSideMenu();
        if (selected_scene.ppt != null && selected_scene.ppt.id >= 0){
            confirmDeletePin();
        }
    }

    void showSideMenu(){
        view_menu_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.side_up);
        ll_side_menu.startAnimation(bottomUp);
        ll_side_menu.setVisibility(View.VISIBLE);
    }

    void hideSideMenu(){
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.side_down);
        ll_side_menu.startAnimation(fadeout);
        ll_side_menu.setVisibility(View.GONE);
        view_menu_bg.setVisibility(View.GONE);
    }

    void refreshLayout(){
        for (int i = 0; i < scene_list.size(); i++ ) {
            PinPoint pp_pin = scene_list.get(i).ppt;
            if (pp_pin.iv_mark != null){
                float img_scale = photo_view.getScale();
                int pin_wh = (int)((PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale);
                RectF rc = photo_view.getDisplayRect();
                float site_width = rc.width();
                float site_height = rc.height();
                tempX = (int) (site_width * pp_pin.x + rc.left) - pin_wh / 4;
                tempY = (int) (site_height * pp_pin.y + rc.top) - pin_wh * 3 / 4;

                if (scene_list.get(i).photos.size() == 0){
                    pp_pin.iv_mark.setImageResource(R.drawable.ic_mark);
                } else {
                    pp_pin.iv_mark.setImageResource(R.drawable.ic_point);
                }

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pin_wh, pin_wh);
                layoutParams.leftMargin = tempX;
                layoutParams.topMargin = tempY;
                pp_pin.iv_mark.setLayoutParams(layoutParams);
                pp_pin.iv_mark.requestLayout();


                RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                tvParams.leftMargin = tempX;
                tvParams.topMargin = tempY + pin_wh;
                pp_pin.tv_time.setLayoutParams(tvParams);
                pp_pin.tv_time.requestLayout();

                pp_pin.tv_time.setText(TimeFormat.updateTimeFormat(Long.parseLong(pp_pin.update_time)));

                int xMax = photo_view.getWidth();
                int yMax = photo_view.getHeight();

                if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
                    pp_pin.iv_mark.setVisibility(View.INVISIBLE);
                } else{
                    pp_pin.iv_mark.setVisibility(View.VISIBLE);
                }

                if (tempX < 0 || tempY < 0 || tempX > xMax || tempY+pin_wh > yMax) {
                    pp_pin.tv_time.setVisibility(View.INVISIBLE);
                } else{
                    pp_pin.tv_time.setVisibility(View.VISIBLE);
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
        if (pinsContainer.getChildCount() > 0)
            pinsContainer.removeAllViews();
        configureRoomList();
        for (int i = 0; i < scene_list.size(); i++ ){
            PinPoint p_pin = scene_list.get(i).ppt;
            // calculate Pin position
            float img_scale = photo_view.getScale();
            int pin_wh = (int)((PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale);
            RectF rc = photo_view.getDisplayRect();
            float site_width = rc.width();
            float site_height = rc.height();
            tempX = (int) (site_width * p_pin.x + rc.left) - pin_wh / 4;
            tempY = (int) (site_height * p_pin.y + rc.top) - pin_wh * 3 / 4;

            ImageView iv = new ImageView(this);
//            iv.setImageResource(R.drawable.pin);
            if (scene_list.get(i).photos.size() == 0){
                iv.setImageResource(R.drawable.ic_mark);
            } else {
                iv.setImageResource(R.drawable.ic_point);
            }
            iv.setOnClickListener(view -> {
                selected_scene = getRoomFromList(view);
                if (selected_scene != null && selected_scene.ppt != null){
                    //show side menu
                    showSideMenu();
                }
            });

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            lp.setMargins(tempX, tempY, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            pinsContainer.addView(iv, lp);
            iv.getLayoutParams().height = pin_wh;
            iv.getLayoutParams().width = pin_wh;
            iv.requestLayout();

            TextView tv = new TextView(this);
            tv.setText(TimeFormat.updateTimeFormat(Long.parseLong(p_pin.update_time)));
            tv.setTextColor(Color.BLUE);
            RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            tvlp.setMargins(tempX, tempY+pin_wh, 0, 0);
            tvlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            tvlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            pinsContainer.addView(tv, lp);
            tv.requestLayout();


            int xMax = photo_view.getWidth();
            int yMax = photo_view.getHeight();

            if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
                iv.setVisibility(View.INVISIBLE);
            } else{
                iv.setVisibility(View.VISIBLE);
            }

            if (tempX < 0 || tempY < 0 || tempX > xMax || tempY+pin_wh > yMax) {
                tv.setVisibility(View.INVISIBLE);
            } else{
                tv.setVisibility(View.VISIBLE);
            }
            scene_list.get(i).ppt.iv_mark = iv;
            scene_list.get(i).ppt.tv_time = tv;
        }
    }

    void initLayout(){
        et_title.setVisibility(View.GONE);
        view_menu_bg.setVisibility(View.GONE);
        ll_side_menu.setVisibility(View.GONE);

        tv_title.setText(cur_sheet.name);

        photo_view.setOnMatrixChangeListener(rect -> {
            if (scene_list.size() > 0){
                if (scene_list.get(0).ppt.iv_mark == null)
                    drawPins();
                else
                    refreshLayout();
            }
        });

        photo_view.setOnPhotoTapListener(new PhotoTapListener());

        configureRoomList();
        scenesAdapter = new ScenesAdapter(this, scene_list, roomsListener);
        room_recycler.setLayoutManager(new LinearLayoutManager(this));
        room_recycler.setAdapter(scenesAdapter);

        et_title.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                onClickEdit();
                return true;
            }
            return false;
        });
    }

    private Scene getRoomFromList(View view){
        for (int i = 0; i < scene_list.size(); i++){
            PinPoint pin = scene_list.get(i).ppt;
            if (pin.iv_mark != null && pin.iv_mark == view){
                return scene_list.get(i);
            }
        }
        return null;
    }

    @Override
    public void onResume(){
        super.onResume();
        drawBottomBar();
        configureRoomList();
        refreshLayout();
    }

    void confirmDeletePin(){
        if (selected_scene.ppt != null && selected_scene.ppt.id > -1){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Are you sure to remove this Pin?");
            alertDialogBuilder.setPositiveButton("Yes", (arg0, arg1) -> {

                List<SpotPhoto> sp_list = dbHelper.getAllSpots(selected_scene.ppt.id);
                for (int k = 0; k < sp_list.size(); k++)
                {
                    SpotPhoto sp = sp_list.get(k);
                    File sp_f = new File(sp.path);
                    sp_f.delete();
                    dbHelper.deleteSpot(sp.id);
                }
                dbHelper.deletePin(selected_scene.ppt.id);
                scene_list.remove(selected_scene);
                pinsContainer.removeView(selected_scene.ppt.iv_mark);
                selected_scene = null;
                Toast.makeText(this, "Selected Pin was removed", Toast.LENGTH_SHORT).show();
                if (scene_list.size() == 0) {
                    finish();
                }
            });
            alertDialogBuilder.setNegativeButton("No", null);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    void selectCaptureKind(){
        if (selected_scene.ppt != null && selected_scene.ppt.id > -1){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.main_capture_camera);
            builder.setPositiveButton(R.string.main_capture_photo, (dialogInterface, i) -> {
                if (kind_camera == CAMERA_BUILT_IN) {
                    Intent intent = new Intent(this, CaptureAndPlay.class);
                    intent.putExtra("capture_mode", "photo");
                    intent.putExtra("pin_id", selected_scene.ppt.id);
                    intent.putExtra("camera_kind", kind_camera);
                    startActivity(intent);
                } else {
                    if (InstaCameraManager.getInstance().getCameraConnectedType() == InstaCameraManager.CONNECT_TYPE_NONE){
                        connectCamera();
                        Toast.makeText(this, "Please connect the 360 camera and try to add the spot photo again.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (checkSdCardEnabled()) {
                            Intent intent = new Intent(this, CaptureAndPlay.class);
                            intent.putExtra("capture_mode", "photo");
                            intent.putExtra("pin_id", selected_scene.ppt.id);
                            intent.putExtra("camera_kind", kind_camera);
                            startActivity(intent);
                        }
                    }
                }
            });
            builder.setNegativeButton(R.string.main_capture_video, (dialogInterface, i) -> {
                if (kind_camera == CAMERA_BUILT_IN) {
                    Intent intent = new Intent(this, CaptureAndPlay.class);
                    intent.putExtra("capture_mode", "video");
                    intent.putExtra("pin_id", selected_scene.ppt.id);
                    intent.putExtra("camera_kind", kind_camera);
                    startActivity(intent);
                } else {
                    if (InstaCameraManager.getInstance().getCameraConnectedType() == InstaCameraManager.CONNECT_TYPE_NONE){
                        connectCamera();
                        Toast.makeText(this, "Please connect the 360 camera and try to add the spot photo again.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (checkSdCardEnabled()) {
                            Intent intent = new Intent(this, CaptureAndPlay.class);
                            intent.putExtra("capture_mode", "video");
                            intent.putExtra("pin_id", selected_scene.ppt.id);
                            intent.putExtra("camera_kind", kind_camera);
                            startActivity(intent);
                        }
                    }
                }
            });
            builder.show();
        }
    }

    int active_page = ACTIVE_MAIN_PAGE;
    //bottom bar
    void drawBottomBar(){
        view_connect_option.setVisibility(View.GONE);
        view_connect_bg.setVisibility(View.GONE);

        if (active_page == ACTIVE_OTHER_PAGE) {
            setTitle(R.string.title_photo_library);
            configureRoomList();
            scenesAdapter.setDataList(scene_list);

            view_home.setVisibility(View.GONE);
            view_other.setVisibility(View.VISIBLE);
            iv_bt_main.setColorFilter(ContextCompat.getColor(this, R.color.menu_disable), PorterDuff.Mode.SRC_IN);
            txt_bt_main.setTextColor(ContextCompat.getColor(this, R.color.menu_disable));
            iv_bt_other.setColorFilter(ContextCompat.getColor(this, R.color.menu_active), PorterDuff.Mode.SRC_IN);
            txt_bt_other.setTextColor(ContextCompat.getColor(this, R.color.menu_active));
        } else {
            setTitle(R.string.edit_sitemap_title);
            view_home.setVisibility(View.VISIBLE);
            view_other.setVisibility(View.GONE);
            iv_bt_main.setColorFilter(ContextCompat.getColor(this, R.color.menu_active), PorterDuff.Mode.SRC_IN);
            txt_bt_main.setTextColor(ContextCompat.getColor(this, R.color.menu_active));
            iv_bt_other.setColorFilter(ContextCompat.getColor(this, R.color.menu_disable), PorterDuff.Mode.SRC_IN);
            txt_bt_other.setTextColor(ContextCompat.getColor(this, R.color.menu_disable));
        }

        if (InstaCameraManager.getInstance().getCameraConnectedType() == InstaCameraManager.CONNECT_TYPE_NONE){
            iv_bt_center.setImageResource(R.drawable.ic_disconnect);
        } else {
            iv_bt_center.setImageResource(R.drawable.ic_connect);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.bottom_ll_center) void onClickConnect(){
        if (InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE){
            InstaCameraManager.getInstance().closeCamera();
        } else if (view_connect_bg.getVisibility() != View.VISIBLE){
            connectCamera();
        } else if (view_connect_bg.getVisibility() == View.VISIBLE){
            onClickConnectBack();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_bottom_home) void onClickHome(){
        active_page = ACTIVE_MAIN_PAGE;
        drawBottomBar();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.ll_bottom_other) void onClickOther(){
        active_page = ACTIVE_OTHER_PAGE;
        drawBottomBar();
    }

    //connection camera
    void connectCamera(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view_connect_bg.startAnimation(fadein);
        view_connect_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        view_connect_option.startAnimation(bottomUp);
        view_connect_option.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.main_btn_wifi) void onClickConnectWifi(){
        InstaCameraManager.getInstance().openCamera(InstaCameraManager.CONNECT_TYPE_WIFI);
        onClickConnectBack();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.main_btn_usb) void onClickConnectUsb(){
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

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        if (enabled) {
            Toast.makeText(this, R.string.main_toast_camera_connected, Toast.LENGTH_SHORT).show();
            kind_camera = CAMERA_360;
            drawBottomBar();
        } else {
            NetworkManager.getInstance().clearBindProcess();
            Toast.makeText(this, R.string.main_toast_camera_disconnected, Toast.LENGTH_SHORT).show();
            drawBottomBar();
        }
        refreshLayout();
    }

    private boolean checkSdCardEnabled() {
        if (!InstaCameraManager.getInstance().isSdCardEnabled()) {
            Toast.makeText(this, R.string.capture_toast_sd_card_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_setting_choose_device) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.main_desc_choose_camera);
            builder.setPositiveButton("Built-in", (dialog, which) -> {
                kind_camera = CAMERA_BUILT_IN;
                refreshLayout();
            });
            builder.setNegativeButton("360 Camera", (dialog, which) -> {
                connectCamera();
            });

            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    //Spot Photo View
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_recycler)    RecyclerView room_recycler;
    List<Scene> scene_list = new ArrayList<>();

    ScenesAdapter scenesAdapter;

    void configureRoomList(){
        if (scene_list.size() > 0){
            scene_list.clear();
        }
        List<PinPoint> pin_list = dbHelper.getPinsForSheet(cur_sheet.id);
        for (int i = 0; i < pin_list.size(); i++){
            Scene scene = new Scene();
            PinPoint pin = pin_list.get(i);
            scene.ppt = pin;
            scene.photos = dbHelper.getAllSpots(pin.id);
            scene_list.add(scene);
        }
    }

    ScenesAdapter.EventListener roomsListener = index -> {
        Scene one = scene_list.get(index);
        Intent viewIntent = new Intent(this, RoomViewActivity.class);
        viewIntent.putExtra("pin", one.ppt);
        startActivity(viewIntent);
    };

    //Create Pin
    void createPin(PinPoint pin){
        CreatePinDlg createDlg = new CreatePinDlg(this, pin, pinlistener);

        View decorView = createDlg.getWindow().getDecorView();
        decorView.setBackgroundResource(android.R.color.transparent);
        createDlg.show();
    }

    CreatePinDlg.EventListener pinlistener = this::drawPins;

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            if (ll_side_menu.getVisibility() == View.VISIBLE){
                hideSideMenu();
            }
            edit_pin = new PinPoint();
            edit_pin.id = -1;
            edit_pin.sh_id = cur_sheet.id + "";
            edit_pin.x = x;
            edit_pin.y = y;
            createPin(edit_pin);
        }
    }
    //Toolbar
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.edit_sheet_ib_back) void onClickBack(){
        onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.edit_sheet_ib_more) void onClickMore(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_choose_camera);
        builder.setPositiveButton("Built-in", (dialog, which) -> {
            kind_camera = CAMERA_BUILT_IN;
            refreshLayout();
        });
        builder.setNegativeButton("360 Camera", (dialog, which) -> {
            connectCamera();
        });

        builder.show();
    }
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.edit_sheet_ib_edit) void onClickEdit(){
        if (et_title.getVisibility() == View.GONE){
            et_title.setText(cur_sheet.name);
            tv_title.setVisibility(View.GONE);
            et_title.setVisibility(View.VISIBLE);
            et_title.requestFocus();
            showKeyboard();
            btn_edit.setImageResource(R.drawable.ic_done);
        } else {
            hideKeyboard();
            String new_name = et_title.getText().toString().trim();
            if (!new_name.equals(cur_sheet.name)){
                cur_sheet.name = new_name;
                cur_sheet.update_time = (long)System.currentTimeMillis()/1000 + "";
                dbHelper.updateSheet(cur_sheet);
                tv_title.setText(cur_sheet.name);
            }
            et_title.setVisibility(View.GONE);
            tv_title.setVisibility(View.VISIBLE);
            btn_edit.setImageResource(R.drawable.ic_edit);
        }
    }
    void showKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}