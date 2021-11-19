package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.PIN_SIZE_MAX_LEN;
import static com.viact.viact_android.utils.Const.PIN_SIZE_MIN_LEN;
import static com.viact.viact_android.utils.Const.SITE_MAX_SCALE;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.utils.Compass;
import com.viact.viact_android.utils.Pedometer;
import com.viact.viact_android.views.PixelGridView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SpeedModeActivity extends BaseObserveCameraActivity {

    Sheet cur_sheet;
    DatabaseHelper dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_photoview)        PhotoView           photo_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pinsContainer)               RelativeLayout      pinsContainer;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.speed_mode_tv_title)         TextView            tv_title;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.txt_choose_device)           TextView            txt_status;
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

    List<PinPoint> scene_list = new ArrayList<>();
    List<PinPoint> scene_list_init = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_mode);
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
        view_setup_bg.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(SPEED_MODE_INIT_STEPS);
        btn_cancel.setEnabled(false);
        btn_next.setEnabled(false);
        btn_next.setText(R.string.setting_dialog_start);
        txt_guide.setText(R.string.speed_mode_init_desc);
        txt_tip.setText(R.string.speed_mode_init_tip);
        iv_guide.setVisibility(View.GONE);

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
            setup_photoview.getDisplayMatrix(mat);
            photo_view.setDisplayMatrix(mat);
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
    @OnClick(R.id.speed_mode_ib_back) void onClickBack(){
        compass.stop();
        pedometer.stop();
        finish();
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

                pp_pin.iv_mark.setImageResource(R.drawable.ic_point);

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
                selected_scene = getRoomFromList_Init(view);

                if (selected_scene != null){
                    selected_scene.iv_mark.setImageResource(R.drawable.ic_point_sel);
                }
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
                moveMarkerView(pp_pin.iv_mark, pp_pin.x, pp_pin.y);
//                pp_pin.iv_mark.setImageResource(R.drawable.ic_point);
            }
        }
        if (iv_step != null){
            moveMarkerView(iv_step, pos_start.x, pos_start.y);
        }
    }

    void moveMarkerView(ImageView iView, float xx, float yy){
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

        if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
            iView.setVisibility(View.INVISIBLE);
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
        moveMarkerView(iv_step, xx, yy);
        pos_start.x = xx;
        pos_start.y = yy;
        //checking the near-by marks
        for(int i = 0 ; i < scene_list.size(); i++){
            PinPoint pin = scene_list.get(i);
            float delta_dis = (float) Math.sqrt((pos_start.x - pin.x)*(pos_start.x - pin.x) + (pos_start.y - pin.y)*(pos_start.y - pin.y));
            if (delta_dis < nearby_limit && (nearby_pin == null || nearby_pin != pin)){
                nearby_pin = pin;
                doCountDown(pin);
                return;
            }
        }
    }

    void doCountDown(final PinPoint nearPin){
        downcounter = 3;
        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                if (downcounter == 0){
                    nearPin.iv_mark.setImageResource(R.drawable.ic_point_check);
                } else {
                    if(downcounter == 3){
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_3);
                    } else if (downcounter == 2) {
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_2);
                    } else if (downcounter == 1) {
                        nearPin.iv_mark.setImageResource(R.drawable.ic_point_check_1);
                    }
                    downcounter--;
                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.postDelayed(r, 1000);
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
}