package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_PHOTO_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_SPOT_PATH;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_VIDEO_PATH;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MAX_LEN;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MIN_LEN;
import static com.viact.viact_android.utils.Const.SITE_MAX_SCALE;
import static com.viact.viact_android.utils.Const.SPEED_MODE_COUNTDOWN_DEFAULT;
import static com.viact.viact_android.utils.Const.SPEED_MODE_INIT_END;
import static com.viact.viact_android.utils.Const.SPEED_MODE_INIT_NONE;
import static com.viact.viact_android.utils.Const.SPEED_MODE_INIT_START;
import static com.viact.viact_android.utils.Const.SPEED_MODE_INIT_STEPS;
import static com.viact.viact_android.utils.Const.SPEED_MODE_INIT_WALK;
import static com.viact.viact_android.utils.Const.SPEED_MODE_RUN;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.arashivision.sdkcamera.camera.callback.ICaptureStatusListener;
import com.arashivision.sdkmedia.export.ExportImageParamsBuilder;
import com.arashivision.sdkmedia.export.ExportUtils;
import com.arashivision.sdkmedia.export.IExportCallback;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;
import com.viact.viact_android.utils.Compass;
import com.viact.viact_android.utils.NetworkManager;
import com.viact.viact_android.utils.Pedometer;
import com.viact.viact_android.views.PixelGridView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SpeedModeActivity extends BaseObserveCameraActivity implements ICaptureStatusListener, IExportCallback {

    Sheet cur_sheet;
    DatabaseHelper dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_photoview)        PhotoView           photo_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pinsContainer)               RelativeLayout      pinsContainer;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_tv_title)         TextView            tv_title;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_choose_device)           TextView            txt_device;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_capture_status)          TextView            txt_status;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_bg_init)          RelativeLayout      view_setup_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_setup)            LinearLayout        view_setup;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_setup_photoview)       PhotoView           setup_photoview;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.setup_container)             RelativeLayout      setup_container;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.grid_parent)                 RelativeLayout      view_grid_parent;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_init_next)             Button              btn_next;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_init_cancel)           Button              btn_cancel;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_init_progress)    ProgressBar         progressBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_guide)            TextView            txt_guide;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_tip)              TextView            txt_tip;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_iv_guide)         ImageView            iv_guide;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_bg)               RelativeLayout      menu_view_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_view)             View                 menu_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_cb_hdr)           CheckBox             cb_hdr;

    List<PinPoint> scene_list = new ArrayList<>();
    List<PinPoint> scene_list_init = new ArrayList<>();

    boolean bHdr = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_mode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);

        InstaCameraManager.getInstance().setCaptureStatusListener(this);

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

        menu_view_bg.setVisibility(View.GONE);
        menu_timer_view_bg.setVisibility(View.GONE);
        view_setup_bg.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(SPEED_MODE_INIT_STEPS);
        btn_cancel.setEnabled(false);
        btn_next.setEnabled(false);
        btn_next.setText(R.string.setting_dialog_start);
        txt_guide.setText(R.string.speed_mode_init_desc);
        txt_tip.setText(R.string.speed_mode_init_tip);
        iv_guide.setVisibility(View.GONE);
        txt_status.setVisibility(View.GONE);

        Glide.with (this)
                .load (cur_sheet.path)
                .into (photo_view);

        Glide.with (this)
                .load (cur_sheet.path)
                .into (setup_photoview);

        PixelGridView pixelGrid = new PixelGridView(this);
        pixelGrid.setNumColumns(4);
        pixelGrid.setNumRows(3);
        view_grid_parent.addView(pixelGrid);

        tv_title.setText(cur_sheet.name);

        photo_view.setOnMatrixChangeListener(rect -> {
            if (scene_list.size() > 0){
                if (scene_list.get(0).iv_mark == null)
                    drawPins();
                else
                    refreshLayout();
            }
        });
        setup_photoview.setOnMatrixChangeListener(rect -> {
            if (scene_list_init.size() > 0){
                if (scene_list_init.get(0).iv_mark == null)
                    drawPins_Init();
                else
                    refreshLayout_Init();
            }
            Matrix mat = new Matrix();
            setup_photoview.getSuppMatrix(mat);
            photo_view.setSuppMatrix(mat);
        });
        setup_photoview.setOnPhotoTapListener(new PhotoTapListener());
        configureRoomList();

        photo_view.setMaximumScale(SITE_MAX_SCALE);
        setup_photoview.setMaximumScale(SITE_MAX_SCALE);
        setupSensor();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_init_next) void onClickNext(){
        if (mode == SPEED_MODE_INIT_START){
            count_steps = 0;
            step_angles.clear();
            progressBar.setProgress(count_steps);
            progressBar.setVisibility(View.VISIBLE);
            iv_guide.setVisibility(View.GONE);
            txt_guide.setText(R.string.speed_mode_init_desc_2);
            txt_tip.setVisibility(View.GONE);
            btn_next.setEnabled(false);
            mode = SPEED_MODE_INIT_WALK;
            compass.start();
            pedometer.start();
        } else if (mode == SPEED_MODE_INIT_END){
            progressBar.setVisibility(View.GONE);
            view_setup_bg.setVisibility(View.GONE);
            mode = SPEED_MODE_RUN;
            // calculate the angle and distance unit
            calculateAngleDistance();
            addNavigateView();
            compass.start();
            pedometer.start();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_mode_ib_more) void onClickMore(){
        cb_hdr.setChecked(bHdr);
        showSpeedMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_mode_ib_back) void onClickBack(){
        compass.stop();
        pedometer.stop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        finish();
    }

    @Override
    public void onStop(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onStop();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_init_cancel) void onClickCancel(){
        mode = SPEED_MODE_INIT_NONE;
        if(iv_start != null) {
            setup_container.removeView(iv_start);
            iv_start = null;
        }
        if (iv_end != null){
            setup_container.removeView(iv_end);
            iv_end = null;
        }
        step_angles.clear();
        step_feet = 0;
        base_angle = 0;
        count_steps = 0;
        compass.stop();
        pedometer.stop();

        btn_cancel.setEnabled(false);
        btn_next.setEnabled(false);
        btn_next.setText(R.string.setting_dialog_start);
        progressBar.setVisibility(View.GONE);
        txt_guide.setText(R.string.speed_mode_init_desc);
        txt_tip.setVisibility(View.VISIBLE);

    }

    int tempX, tempY;
    PinPoint selected_scene = null;

    void refreshLayout_Init(){
        for (int i = 0; i < scene_list_init.size(); i++ ) {
            PinPoint pp_pin = scene_list_init.get(i);
            if (pp_pin.iv_mark != null){
                float img_scale = setup_photoview.getScale();
                float img_max_scale = setup_photoview.getMaximumScale();
                int pin_wh = (int)(PIN_SIZE_MAX_LEN - (float)(PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale / img_max_scale);
                RectF rc = setup_photoview.getDisplayRect();
                float site_width = rc.width();
                float site_height = rc.height();
                tempX = (int) (site_width * pp_pin.x + rc.left) - pin_wh / 2;
                tempY = (int) (site_height * pp_pin.y + rc.top) - pin_wh / 2;

//                pp_pin.iv_mark.setImageResource(R.drawable.ic_point);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pin_wh, pin_wh);
                layoutParams.leftMargin = tempX;
                layoutParams.topMargin = tempY;
                pp_pin.iv_mark.setLayoutParams(layoutParams);
                pp_pin.iv_mark.requestLayout();

                int xMax = setup_photoview.getWidth();
                int yMax = setup_photoview.getHeight();

                if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
                    pp_pin.iv_mark.setVisibility(View.INVISIBLE);
                } else{
                    pp_pin.iv_mark.setVisibility(View.VISIBLE);
                }

            }
        }
        if (iv_start != null){
            refreshMarkView(iv_start, pos_start);
        }
    }

    void drawPins_Init(){
        if (setup_container.getChildCount() > 0)
            setup_container.removeAllViews();
        if (scene_list_init.size() == 0) {
            configureRoomList();
        }

        for (int i = 0; i < scene_list_init.size(); i++ ){
            PinPoint p_pin = scene_list_init.get(i);
            // calculate Pin position
            float img_scale = setup_photoview.getScale();
            float img_max_scale = setup_photoview.getMaximumScale();
            int pin_wh = (int)(PIN_SIZE_MAX_LEN - (float)(PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale / img_max_scale);
            RectF rc = setup_photoview.getDisplayRect();
            float site_width = rc.width();
            float site_height = rc.height();
            tempX = (int) (site_width * p_pin.x + rc.left) - pin_wh / 2;
            tempY = (int) (site_height * p_pin.y + rc.top) - pin_wh / 2;

            ImageView iv = new ImageView(this);
            iv.setImageResource(R.drawable.ic_point);
            iv.setOnClickListener(view -> {
//                selected_scene = getRoomFromList_Init(view);
//
//                if (selected_scene != null){
//                    selected_scene.iv_mark.setImageResource(R.drawable.ic_point_sel);
//                }
            });

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            lp.setMargins(tempX, tempY, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            setup_container.addView(iv, lp);
            iv.getLayoutParams().height = pin_wh;
            iv.getLayoutParams().width = pin_wh;
            iv.requestLayout();

            int xMax = setup_photoview.getWidth();
            int yMax = setup_photoview.getHeight();

            if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
                iv.setVisibility(View.INVISIBLE);
            } else{
                iv.setVisibility(View.VISIBLE);
            }
            scene_list_init.get(i).iv_mark = iv;
        }
    }

    private PinPoint getRoomFromList_Init(View view){
        for (int i = 0; i < scene_list_init.size(); i++){
            PinPoint pin = scene_list_init.get(i);
            if (pin.iv_mark != null && pin.iv_mark == view){
                return scene_list_init.get(i);
            }
        }
        return null;
    }

    void refreshLayout(){
        for (int i = 0; i < scene_list.size(); i++ ) {
            PinPoint pp_pin = scene_list.get(i);
            if (pp_pin.iv_mark != null){
                moveMarkerView(pp_pin.iv_mark, pp_pin.x, pp_pin.y, false);
//                pp_pin.iv_mark.setImageResource(R.drawable.ic_point);
            }
        }
        if (iv_step != null){
            moveMarkerView(iv_step, pos_start.x, pos_start.y, false);
        }
    }

    void moveMarkerView(ImageView iView, float xx, float yy, boolean bMove){
        float img_scale = photo_view.getScale();
        float img_max_scale = photo_view.getMaximumScale();
        int pin_wh = (int)(PIN_SIZE_MAX_LEN - (float)(PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale / img_max_scale);
        RectF rc = photo_view.getDisplayRect();
        float site_width = rc.width();
        float site_height = rc.height();
        tempX = (int) (site_width * xx + rc.left) - pin_wh / 2;
        tempY = (int) (site_height * yy + rc.top) - pin_wh / 2;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pin_wh, pin_wh);
        layoutParams.leftMargin = tempX;
        layoutParams.topMargin = tempY;
        iView.setLayoutParams(layoutParams);
        iView.requestLayout();

        int xMax = photo_view.getWidth();
        int yMax = photo_view.getHeight();

        if (tempX < 0 || tempY < 0 || tempX + PIN_SIZE_MAX_LEN > xMax || tempY + PIN_SIZE_MAX_LEN > yMax) {
            if (bMove){//move sheet
                moveSheet(tempX, tempY);
            } else {
                iView.setVisibility(View.INVISIBLE);
            }
        } else{
            iView.setVisibility(View.VISIBLE);
        }
    }

    void drawPins(){
        if (pinsContainer.getChildCount() > 0)
            pinsContainer.removeAllViews();
        if (scene_list.size() == 0) {
            configureRoomList();
        }

        for (int i = 0; i < scene_list.size(); i++ ){
            PinPoint p_pin = scene_list.get(i);
            scene_list.get(i).iv_mark = addMarkerView(p_pin.x, p_pin.y);
            scene_list.get(i).iv_mark.setImageResource(R.drawable.ic_point);
            scene_list.get(i).iv_mark.setOnClickListener(view -> {
//                    selected_scene = getRoomFromList(view);
//
//                    if (selected_scene != null){
//                        selected_scene.iv_mark.setImageResource(R.drawable.ic_point_sel);
//                    }
            });
        }
    }

    void moveSheet(int xxx, int yyy){
        int xMax = photo_view.getWidth();
        int yMax = photo_view.getHeight();
        int nLeft = 0, nTop = 0;
        Matrix mat = new Matrix();
        photo_view.getSuppMatrix(mat);
        if (xxx < 0) {
            nLeft = PIN_SIZE_MIN_LEN - xxx;
        }
        if (xxx + PIN_SIZE_MAX_LEN > xMax) {
            nLeft = xMax - xxx - PIN_SIZE_MAX_LEN;
        }
        if (yyy < 0) {
            nTop = PIN_SIZE_MIN_LEN - yyy;
        }
        if (yyy + PIN_SIZE_MAX_LEN > yMax) {
            nTop = yMax - yyy - PIN_SIZE_MAX_LEN;
        }

        mat.postTranslate(nLeft, nTop);
        photo_view.setSuppMatrix(mat);
    }

    ImageView addMarkerView(float x, float y){
        // calculate Pin position
        float img_scale = photo_view.getScale();
        float img_max_scale = photo_view.getMaximumScale();
        int pin_wh = (int)(PIN_SIZE_MAX_LEN - (float)(PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale / img_max_scale);
        RectF rc = photo_view.getDisplayRect();
        float site_width = rc.width();
        float site_height = rc.height();
        tempX = (int) (site_width * x + rc.left) - pin_wh / 2;
        tempY = (int) (site_height * y + rc.top) - pin_wh / 2;

        ImageView iv = new ImageView(this);
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
        return iv;
    }

    private PinPoint getRoomFromList(View view){
        for (int i = 0; i < scene_list.size(); i++){
            PinPoint pin = scene_list.get(i);
            if (pin.iv_mark != null && pin.iv_mark == view){
                return scene_list.get(i);
            }
        }
        return null;
    }

    void configureRoomList(){
        if (scene_list.size() > 0){
            scene_list.clear();
        }
        if (scene_list_init.size() > 0){
            scene_list_init.clear();
        }
        scene_list = dbHelper.getPinsForSheet(cur_sheet.id);
        scene_list_init = dbHelper.getPinsForSheet(cur_sheet.id);
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            if (mode == SPEED_MODE_INIT_NONE){
                if (iv_start != null){
                    setup_container.removeView(iv_start);
                    iv_start = null;
                }
                startInitSpeedMode(x, y);
            } else if (mode == SPEED_MODE_INIT_WALK && count_steps == SPEED_MODE_INIT_STEPS){
                if(iv_end != null){
                    setup_container.removeView(iv_end);
                    iv_end = null;
                }
                endInitSpeedMode(x, y);
            }
        }
    }
    //Setup section
    int count_steps = 0;
    ImageView iv_start, iv_end, iv_step;
    PointF pos_start, pos_end;
    List<Integer> step_angles = new ArrayList<>();
    int mode = SPEED_MODE_INIT_NONE;

    private Compass compass;
    private Pedometer pedometer;
    int pre_azimuth = 0;

    float base_angle = 0;       //orientation of sheet
    float step_feet = 0.0f;      // distance of a feet
    float nearby_limit = 0;
    Handler handler;
    int downcounter = 3;

    void addNavigateView(){
        if (iv_step != null){
            pinsContainer.removeView(iv_step);
            iv_step = null;
        }
        iv_step = addMarkerView(pos_start.x, pos_start.y);
        iv_step.setImageResource(R.drawable.ic_navigation);
        //rotate
        iv_step.setRotation(base_angle);
        iv_step.requestLayout();
    }

    void calculateAngleDistance(){
        //Angle
        int angle_total = 0;
        for (int i = 0; i < step_angles.size(); i++){
            angle_total += step_angles.get(i);
        }
        float avg_angle = (float) angle_total/step_angles.size();
        float angle_pos = 0;
        if (pos_start.x == pos_end.x){
            if (pos_end.y > pos_start.y){
                angle_pos = 180;
            } else {
                angle_pos = 0;
            }
        } else {
            double angle1 = Math.atan(Math.abs(pos_end.y - pos_start.y) / Math.abs(pos_end.x - pos_start.x));
            if (pos_start.x > pos_end.x){
                if (pos_start.y > pos_end.y){
                    angle_pos = (float)(270 + angle1 * 180 / Math.PI);
                } else {
                    angle_pos = (float)(270 - angle1 * 180 / Math.PI);
                }
            } else {
                if (pos_start.y > pos_end.y){
                    angle_pos = (float)(90 - angle1 * 180 / Math.PI);
                } else {
                    angle_pos = (float)(90 + angle1 * 180 / Math.PI);
                }
            }
        }

        base_angle = avg_angle - angle_pos;

        //feet unit
        double dist = Math.sqrt((pos_end.x-pos_start.x)*(pos_end.x-pos_start.x) + (pos_end.y - pos_start.y) * (pos_end.y - pos_start.y));
        step_feet = (float) (dist / step_angles.size());
        if (step_feet == 0){
            Toast.makeText(this, "Please try to init the setup of SpeedMode.", Toast.LENGTH_SHORT).show();
            onClickCancel();
        }
        nearby_limit = step_feet * 2;
    }

    void startInitSpeedMode(float x, float y){
        mode = SPEED_MODE_INIT_START;
        pos_start = new PointF(x, y);

        iv_start = createMarkView(x,y);

        iv_guide.setVisibility(View.VISIBLE);
        txt_guide.setText(R.string.speed_mode_init_desc_1);
        btn_next.setEnabled(true);
        btn_cancel.setEnabled(true);
        btn_next.setText(R.string.btn_next);
    }

    void endInitSpeedMode(float x, float y){
        mode = SPEED_MODE_INIT_END;
        pos_end = new PointF(x, y);

        iv_end = createMarkView(x,y);

        btn_next.setEnabled(true);
        btn_next.setText(R.string.setting_dialog_sure);
        txt_guide.setText(R.string.speed_mode_init_desc_4);
    }

    ImageView createMarkView(float x, float y){
        int pin_wh = 48;
        RectF rc = setup_photoview.getDisplayRect();
        float site_width = rc.width();
        float site_height = rc.height();
        tempX = (int) (site_width * x + rc.left) - pin_wh / 2;
        tempY = (int) (site_height * y + rc.top) - pin_wh / 2;

        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.ic_gps_fixed);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.setMargins(tempX, tempY, 0, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        setup_container.addView(iv, lp);
        iv.getLayoutParams().height = pin_wh;
        iv.getLayoutParams().width = pin_wh;
        iv.requestLayout();
        return iv;
    }

    void refreshMarkView(ImageView imgV, PointF pos){
        int pin_wh = 48;
        RectF rc = setup_photoview.getDisplayRect();
        float site_width = rc.width();
        float site_height = rc.height();
        tempX = (int) (site_width * pos.x + rc.left) - pin_wh / 2;
        tempY = (int) (site_height * pos.y + rc.top) - pin_wh / 2;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pin_wh, pin_wh);
        layoutParams.leftMargin = tempX;
        layoutParams.topMargin = tempY;
        imgV.setLayoutParams(layoutParams);
        imgV.requestLayout();

        int xMax = setup_photoview.getWidth();
        int yMax = setup_photoview.getHeight();

        if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
            imgV.setVisibility(View.INVISIBLE);
        } else{
            imgV.setVisibility(View.VISIBLE);
        }
    }

    PinPoint nearby_pin = null;

    void updateNavigateView(){
        //calculate the updated position
        float azi_real = (pre_azimuth + base_angle) % 360;
        double angle = Math.PI * azi_real / 180;
        float xx = pos_start.x + (float) (step_feet * Math.sin(angle));
        float yy = pos_start.y - (float) (step_feet * Math.cos(angle));
        moveMarkerView(iv_step, xx, yy, true);
        pos_start.x = xx;
        pos_start.y = yy;
        //checking the near-by marks
        for(int i = 0 ; i < scene_list.size(); i++){
            PinPoint pin = scene_list.get(i);
            float delta_dis = (float) Math.sqrt((pos_start.x - pin.x)*(pos_start.x - pin.x) + (pos_start.y - pin.y)*(pos_start.y - pin.y));
            if (!bCount && delta_dis < nearby_limit && (nearby_pin == null || nearby_pin != pin)){
                nearby_pin = pin;
                doCountDown(pin);
                return;
            }
        }
    }

    boolean bCount = false;
    private int countdown = SPEED_MODE_COUNTDOWN_DEFAULT;
    void doCountDown(final PinPoint nearPin){
        downcounter = countdown;
        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                if (downcounter > 0){
                    if(downcounter == 5){
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_5);
                    } else if(downcounter == 4){
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_4);
                    } else if(downcounter == 3){
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_3);
                    } else if (downcounter == 2) {
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_2);
                    } else if (downcounter == 1) {
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_1);
                        capture360Photo();
                    }
                    downcounter--;
                    handler.postDelayed(this, 1000);
                } else {
                    //testing code
//                    goBackSpeedMode();
//                    nearby_pin.iv_mark.setImageResource(R.drawable.ic_point_check);
                    //capture 360 image
                }
            }
        };

        handler.postDelayed(r, 1000);
        bCount = true;
    }

    void setupSensor(){
        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);

        pedometer = new Pedometer(this);
        Pedometer.PedometerListener pl = getPedometerListener();
        pedometer.setListener(pl);
    }

    void adjustAngles(float azimuth){
        pre_azimuth = (int)azimuth;
        if (mode == SPEED_MODE_RUN){
            float azi_real = (pre_azimuth + base_angle) % 360;
            iv_step.setRotation(azi_real);
            iv_step.requestLayout();
        }
    }

    void adjustPedometer(){
        if (mode == SPEED_MODE_RUN){
            updateNavigateView();
        } else { // SPEED_MODE_INIT_WALK
            if (count_steps < SPEED_MODE_INIT_STEPS){
                count_steps++;
                progressBar.setProgress(count_steps);
                step_angles.add(pre_azimuth);
            }
            if (count_steps >= SPEED_MODE_INIT_STEPS){
                txt_guide.setText(R.string.speed_mode_init_desc_3);
                compass.stop();
                pedometer.stop();
            }
        }
    }

    private Compass.CompassListener getCompassListener() {
        return azimuth -> {
            runOnUiThread(() -> adjustAngles(azimuth));
        };
    }
    private Pedometer.PedometerListener getPedometerListener(){
        return step -> runOnUiThread(this::adjustPedometer);
    }

    //==================== Capture 360 photo ==========================//
    WorkWrapper mWorkWrapper;
    String exp_filename, EXPORT_DIR_PATH;
    int mCurrentExportId = -1;

    void capture360Photo(){
        if (bHdr){
            int funcMode = InstaCameraManager.FUNCTION_MODE_HDR_CAPTURE;
            InstaCameraManager.getInstance().setAEBCaptureNum(funcMode, 3);
            InstaCameraManager.getInstance().setExposureEV(funcMode, 2f);
            InstaCameraManager.getInstance().startHDRCapture(false);
        } else {
            InstaCameraManager.getInstance().startNormalCapture(false);
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
    }

    @Override
    public void onCaptureWorking() {
        Log.d("Viact", "capture working");
        txt_status.setText(R.string.capture_capture_working);
    }

    @Override
    public void onCaptureStopping() {
        txt_status.setVisibility(View.GONE);
        Toast.makeText(this, R.string.capture_capture_stopping, Toast.LENGTH_SHORT).show();
        goBackSpeedMode();
    }

    @Override
    public void onCaptureTimeChanged(long captureTime) {

    }

    @Override
    public void onCaptureCountChanged(int captureCount) {

    }

    @Override
    public void onCaptureFinish(String[] filePaths) {
        txt_status.setVisibility(View.VISIBLE);
        if (filePaths != null && filePaths.length > 0) {
            //download captured file as panorama
            mWorkWrapper = new WorkWrapper(filePaths);
            exp_filename = "exp_360_" + (long)(System.currentTimeMillis()/1000);
            File folder = new File(EXT_STORAGE_PHOTO_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            EXPORT_DIR_PATH = EXT_STORAGE_PHOTO_PATH;
            exp_filename = exp_filename + ".jpg";
            exportImageOriginal();
            txt_status.setText(R.string.export_dialog_msg_exporting);
        } else {
            Toast.makeText(this, "Capture failed!", Toast.LENGTH_SHORT).show();
            goBackSpeedMode();
        }
    }

    private void exportImageOriginal() {
        ExportImageParamsBuilder builder = new ExportImageParamsBuilder()
                .setExportMode(ExportUtils.ExportMode.PANORAMA)
                .setImageFusion(mWorkWrapper.isPanoramaFile())
                .setTargetPath(EXPORT_DIR_PATH + "/" +exp_filename);

        mCurrentExportId = ExportUtils.exportImage(mWorkWrapper, builder, this);
    }

    @Override
    public void onSuccess() {
        String filename = EXPORT_DIR_PATH + "/" + exp_filename;
        if (bCount && nearby_pin != null){
            SpotPhoto spot = new SpotPhoto();
            List<SpotPhoto> spp_list = dbHelper.getAllSpots(nearby_pin.id);
            spot.pin_id = nearby_pin.id + "";
            spot.path = filename;
            if (spp_list.size() > 0){
                spot.category = spp_list.get(0).category;
            }
            spot.create_time = (long)(System.currentTimeMillis()/1000) + "";
            dbHelper.addSpot(spot);
            nearby_pin.iv_mark.setImageResource(R.drawable.ic_point_check);
        }
        bCount = false;
        txt_status.setVisibility(View.GONE);
        mCurrentExportId = -1;
    }

    @Override
    public void onFail(int errorCode, String errorMsg) {
        // if GPU not support, errorCode is -10003 or -10005 or -13020
        Toast.makeText(this, R.string.export_dialog_msg_export_failed, Toast.LENGTH_SHORT).show();
        goBackSpeedMode();
    }

    @Override
    public void onCancel() {
        Toast.makeText(this, R.string.export_dialog_msg_export_stopped, Toast.LENGTH_SHORT).show();
        goBackSpeedMode();
    }

    @Override
    public void onProgress(float progress) {
        // callback only when exporting video
        txt_status.setText(getString(R.string.export_dialog_msg_export_progress, String.format(Locale.CHINA, "%.1f", progress * 100) + "%"));
    }

    void goBackSpeedMode(){
        nearby_pin.iv_mark.setImageResource(R.drawable.ic_point);
        txt_status.setVisibility(View.GONE);
        mCurrentExportId = -1;
        bCount = false;
    }

    //======== Menu ==============
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_bg) void onClickMenuBg(){
        hideSpeedMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_hdr) void onClickMenuHdr(){
        bHdr = !bHdr;
        cb_hdr.setChecked(bHdr);
        hideSpeedMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_times) void onClickMenuTimes(){
        cb_timer_1.setChecked(false);
        cb_timer_2.setChecked(false);
        cb_timer_3.setChecked(false);
        cb_timer_4.setChecked(false);
        cb_timer_5.setChecked(false);
        switch (countdown){
            case 1:
                cb_timer_1.setChecked(true);
                break;
            case 2:
                cb_timer_2.setChecked(true);
                break;
            case 4:
                cb_timer_4.setChecked(true);
                break;
            case 5:
                cb_timer_5.setChecked(true);
                break;
            default:
                cb_timer_3.setChecked(true);
                break;
        }
        hideSpeedMenu();
        showSpeedTimerMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_reset) void onClickMenuReset(){
        hideSpeedMenu();
        view_setup_bg.setVisibility(View.VISIBLE);
        onClickCancel();
    }

    void showSpeedMenu(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        menu_view_bg.startAnimation(fadein);
        menu_view_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        menu_view.startAnimation(bottomUp);
        menu_view.setVisibility(View.VISIBLE);
    }

    void hideSpeedMenu(){
        Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        menu_view.startAnimation(bottomDown);
        menu_view.setVisibility(View.GONE);
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        menu_view_bg.startAnimation(fadeout);
        menu_view_bg.setVisibility(View.GONE);
    }

    //======= Timer menu ===========
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_timer_bg)               RelativeLayout      menu_timer_view_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_timer_view)             View                menu_timer_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_cb_timer_1)             CheckBox             cb_timer_1;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_cb_timer_2)             CheckBox             cb_timer_2;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_cb_timer_3)             CheckBox             cb_timer_3;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_cb_timer_4)             CheckBox             cb_timer_4;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_menu_cb_timer_5)             CheckBox             cb_timer_5;

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_timer_bg) void onClickMenuTimerBg(){
        hideSpeedTimerMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_timer_1) void onClickMenuTimer1(){
        countdown = 1;
        hideSpeedTimerMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_timer_2) void onClickMenuTimer2(){
        countdown = 2;
        hideSpeedTimerMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_timer_3) void onClickMenuTimer3(){
        countdown = SPEED_MODE_COUNTDOWN_DEFAULT;
        hideSpeedTimerMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_timer_4) void onClickMenuTimer4(){
        countdown = 4;
        hideSpeedTimerMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.speed_menu_timer_5) void onClickMenuTimer5(){
        countdown = 5;
        hideSpeedTimerMenu();
    }

    void showSpeedTimerMenu(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        menu_timer_view_bg.startAnimation(fadein);
        menu_timer_view_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        menu_timer_view.startAnimation(bottomUp);
        menu_timer_view.setVisibility(View.VISIBLE);
    }

    void hideSpeedTimerMenu(){
        Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        menu_timer_view.startAnimation(bottomDown);
        menu_timer_view.setVisibility(View.GONE);
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        menu_timer_view_bg.startAnimation(fadeout);
        menu_timer_view_bg.setVisibility(View.GONE);
    }
}