package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.CUSTOM_IMG_SIZE;
import static com.viact.viact_android.utils.Const.EXT_STORAGE_IMG_PATH;
import static com.viact.viact_android.utils.Const.SCENE_EDIT_IMAGE;
import static com.viact.viact_android.utils.Const.SCENE_EDIT_MARKUP;
import static com.viact.viact_android.utils.Const.SCENE_EDIT_NONE;
import static com.viact.viact_android.utils.Const.SCENE_MEDIA_PHOTO_360;
import static com.viact.viact_android.utils.Const.SCENE_MEDIA_PHOTO_BUILT;
import static com.viact.viact_android.utils.Const.SCENE_MEDIA_VIDEO_360;
import static com.viact.viact_android.utils.Const.SCENE_MEDIA_VIDEO_BUILT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.Group;

import com.arashivision.sdkmedia.player.image.ImageParamsBuilder;
import com.arashivision.sdkmedia.player.image.InstaImagePlayerView;
import com.arashivision.sdkmedia.player.listener.PlayerGestureListener;
import com.arashivision.sdkmedia.player.listener.PlayerViewListener;
import com.arashivision.sdkmedia.player.listener.VideoStatusListener;
import com.arashivision.sdkmedia.player.video.InstaVideoPlayerView;
import com.arashivision.sdkmedia.player.video.VideoParamsBuilder;
import com.arashivision.sdkmedia.work.WorkWrapper;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.dialogs.MarkupDlg;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.InsImg;
import com.viact.viact_android.models.Markup;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.SpotPhoto;
import com.viact.viact_android.utils.FileUtils;
import com.viact.viact_android.utils.ImageFilePath;
import com.viact.viact_android.utils.TimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RoomViewActivity extends BaseObserveCameraActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_photoview)           PhotoView               photoView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_txt_date)       TextView                    txt_date;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_current)          TextView                    mTvCurrent;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_total)            TextView                    mTvTotal;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.group_progress)      Group                       mGroupProgress;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.seek_bar)            SeekBar                     mSeekBar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_iv_back)        ImageView                   iv_back;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_iv_forward)     ImageView                   iv_forward;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_play_view)      RelativeLayout              player_container;

    //toolbar
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_ib_edit)    ImageButton               btn_edit;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_tv_title)   TextView                  tv_title;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_et_title)    EditText                 et_title;
    //side menu
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_view_photo_menu)    View                    view_photo_menu;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_tv_status)          TextView                tv_status;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_view_save_menu)    View                     view_save_menu;
    //Markup Menu
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.markup_menu_bg)             View                 markup_menu_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.markup_menu_view)           View                 markup_menu_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.markup_menu_tv_name)        TextView             menu_markup_title;
    //Preview
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_preview_image)      RelativeLayout              view_preview;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.iv_preview)              ImageView                   iv_preview;


    InstaImagePlayerView    mImagePlayerView;
    InstaVideoPlayerView    mVideoPlayerView;

    PinPoint    cur_pin;
    DatabaseHelper dbHelper;
    List<SpotPhoto> photo_list = new ArrayList<>();
    SpotPhoto sel_photo;
    WorkWrapper mWorkWrapper;
    private int sel_index = 0;
    private int media_type = SCENE_MEDIA_PHOTO_360;
    private int edit_type = SCENE_EDIT_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_view);
        ButterKnife.bind(this);
        cur_pin = (PinPoint) getIntent().getParcelableExtra("pin");
        setTitle(cur_pin.name);

        dbHelper = DatabaseHelper.getInstance(this);
        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_delete) void onClickDelete(){
        confirmDeletePhoto();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_back) void onClickBack(){
        if (sel_index > 0){
            sel_index--;
        }
        playPhoto();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_forward) void onClickForward(){
        if (sel_index < photo_list.size() - 1){
            sel_index++;
        }
        playPhoto();
    }

    void initLayout(){
        et_title.setVisibility(View.GONE);
        view_save_menu.setVisibility(View.GONE);
        markup_menu_bg.setVisibility(View.GONE);
        view_preview.setVisibility(View.GONE);
        txt_date.setText("");
        tv_title.setText(cur_pin.name);

        photo_list = dbHelper.getAllSpots(cur_pin.id);
        if (photo_list.size() == 0) {
            finish();
        } else {
            playPhoto();
        }

        et_title.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                onClickEdit();
                return true;
            }
            return false;
        });
        photoView.setOnMatrixChangeListener(rect -> {
//            redrawMarkup();
            refreshMarkups();
        });
        photoView.setMaximumScale(10.0f);
    }

    void playPhoto(){
        edit_type = SCENE_EDIT_NONE;
        tv_status.setVisibility(View.GONE);
        if (sel_index == 0) {
            iv_back.setVisibility(View.GONE);
        } else {
            iv_back.setVisibility(View.VISIBLE);
        }

        if (sel_index == photo_list.size() - 1) {
            iv_forward.setVisibility(View.GONE);
        } else {
            iv_forward.setVisibility(View.VISIBLE);
        }
        mGroupProgress.setVisibility(View.GONE);
        photoView.setVisibility(View.GONE);
        releasePlayerViews();
        if (player_container.getChildCount() > 0) {
            player_container.removeAllViews();
        }
        player_container.setVisibility(View.GONE);

        sel_photo = photo_list.get(sel_index);
        String filename = sel_photo.path;
        long ctime = Long.parseLong(sel_photo.create_time);
        showDateTime(ctime);

        if (image_parent.getChildCount() > 0){
            image_parent.removeAllViews();
        }
        mrk_list = dbHelper.getAllMarkups(sel_photo.id);
        insImg_list = dbHelper.getAllImgs(sel_photo.id);

        String[] filepaths = new String[] {filename};
        mWorkWrapper = new WorkWrapper(filepaths);
        if (filename.contains("exp_360_")){
            if (mWorkWrapper.isVideo()){
                media_type = SCENE_MEDIA_VIDEO_360;
                view_photo_menu.setVisibility(View.GONE);
                playVideo(mWorkWrapper);
            } else {
                media_type = SCENE_MEDIA_PHOTO_360;
                view_photo_menu.setVisibility(View.VISIBLE);
                playImage(mWorkWrapper);
            }
        } else {
            if (filename.contains(".png")){
                media_type = SCENE_MEDIA_PHOTO_BUILT;
                photoView.setVisibility(View.VISIBLE);
                view_photo_menu.setVisibility(View.VISIBLE);
                Glide.with (this)
                        .load (filename)
                        .into (photoView);
            } else {
                media_type = SCENE_MEDIA_VIDEO_BUILT;
                view_photo_menu.setVisibility(View.GONE);
                playVideo(mWorkWrapper);
            }
        }
    }

    private void showDateTime(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String sdate = DateFormat.format("MMM dd, yyyy hh:mm", cal).toString();
        txt_date.setText(sdate);
    }

    private void playVideo(WorkWrapper mWrapper) {
        mGroupProgress.setVisibility(View.VISIBLE);

        player_container.setVisibility(View.VISIBLE);
        mVideoPlayerView = new InstaVideoPlayerView(this);
        mVideoPlayerView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        player_container.addView(mVideoPlayerView);

        mVideoPlayerView.setLifecycle(getLifecycle());
        mVideoPlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(RoomViewActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
                mVideoPlayerView.refreshDrawableState();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(RoomViewActivity.this, toast, Toast.LENGTH_LONG).show();
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
//        if (isPlaneMode) {
//            builder.setRenderModelType(VideoParamsBuilder.RENDER_MODE_PLANE_STITCH);
//            builder.setScreenRatio(2, 1);
//        }

        mVideoPlayerView.prepare(mWrapper, builder);
        mVideoPlayerView.play();
    }

    private void playImage(WorkWrapper mWrapper) {
        player_container.setVisibility(View.VISIBLE);
        mImagePlayerView = new InstaImagePlayerView(this);
        mImagePlayerView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        player_container.addView(mImagePlayerView);

        mImagePlayerView.setLifecycle(getLifecycle());
        mImagePlayerView.setPlayerViewListener(new PlayerViewListener() {
            @Override
            public void onLoadingStatusChanged(boolean isLoading) {
            }

            @Override
            public void onLoadingFinish() {
                Toast.makeText(RoomViewActivity.this, R.string.play_toast_load_finish, Toast.LENGTH_SHORT).show();
                mImagePlayerView.refreshDrawableState();
                refreshMarkups();
            }

            @Override
            public void onFail(int errorCode, String errorMsg) {
                // if GPU not support, errorCode is -10003 or -10005 or -13020
                String toast = getString(R.string.play_toast_fail_desc, errorCode, errorMsg);
                Toast.makeText(RoomViewActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
        ImageParamsBuilder builder = new ImageParamsBuilder();
        builder.setWithSwitchingAnimation(true);
        builder.setImageFusion(mWrapper.isPanoramaFile());
//        if (isPlaneMode) {
//            builder.setRenderModelType(ImageParamsBuilder.RENDER_MODE_PLANE_STITCH);
//            builder.setScreenRatio(2, 1);
//        }

        mImagePlayerView.setGestureListener(new PlayerGestureListener() {
            @Override
            public void onUp() {
                refreshMarkups();
            }

            @Override
            public void onZoom() {
                refreshMarkups();
            }

            @Override
            public void onScroll() {
                refreshMarkups();
            }
        });


        mImagePlayerView.prepare(mWrapper, builder);
        mImagePlayerView.play();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_ib_back) void onClickClose(){
        finish();
    }

    void confirmDeletePhoto(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_delete_spot);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (sel_index >= 0 && sel_index < photo_list.size()){
                SpotPhoto sp = photo_list.get(sel_index);
                dbHelper.deleteMarkupsByPhoto(sp.id);
                dbHelper.deleteSpot(sp.id);
                photo_list.clear();
                photo_list = dbHelper.getAllSpots(cur_pin.id);
                if (sel_index == photo_list.size()){
                    sel_index--;
                }
                if (photo_list.size() == 0){
                    finish();
                }
                playPhoto();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayerViews();
    }

    void releasePlayerViews(){
        if (mImagePlayerView != null) {
            mImagePlayerView.destroy();
        }
        if (mVideoPlayerView != null) {
            mVideoPlayerView.destroy();
        }
    }

    //side menu
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_menu_photo) void onClickMenuPhoto(){
        if (edit_type == SCENE_EDIT_NONE){
            if (media_type == SCENE_MEDIA_PHOTO_BUILT || media_type == SCENE_MEDIA_PHOTO_360) {
                edit_type = SCENE_EDIT_IMAGE;
                tv_status.setVisibility(View.VISIBLE);
                tv_status.setText(R.string.scene_view_photo_status);
                editMarkup();
            }
        }
    }

    // draw markup
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_menu_edit) void onClickMenuEdit(){
        if (edit_type == SCENE_EDIT_NONE) {
            if (media_type == SCENE_MEDIA_PHOTO_BUILT || media_type == SCENE_MEDIA_PHOTO_360){
                view_save_menu.setVisibility(View.VISIBLE);
                tv_status.setVisibility(View.VISIBLE);
                tv_status.setText(R.string.scene_view_markup_status);
                edit_type = SCENE_EDIT_MARKUP;
                editMarkup();
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_menu_category) void onClickMenuCategory(){

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_iv_menu_store) void onClickMenuStore(){
        edit_type = SCENE_EDIT_NONE;
        view_save_menu.setVisibility(View.GONE);
        tv_status.setVisibility(View.GONE);
        if (media_type == SCENE_MEDIA_PHOTO_BUILT){
            photoView.setFocusableInTouchMode(true);
            //show Markup dialog
            if (iv_markup != null){
                Markup mrk = new Markup();
                mrk.rc = new RectF(originR);
                mrk.photo_id = sel_photo.id + "";
                MarkupDlg markupDlg = new MarkupDlg(this, mrk, markup_listener);

                View decorView = markupDlg.getWindow().getDecorView();
                decorView.setBackgroundResource(android.R.color.transparent);
                markupDlg.show();
                image_parent.removeView(iv_markup);
                iv_markup = null;
            }
        } else if (media_type == SCENE_MEDIA_PHOTO_360){
            mImagePlayerView.setFocusableInTouchMode(true);
            //show Markup dialog
            if (iv_markup != null){
                Markup mrk = new Markup();
                mrk.rc = new RectF(originR);
                mrk.photo_id = sel_photo.id + "";
                MarkupDlg markupDlg = new MarkupDlg(this, mrk, markup_listener);

                View decorView = markupDlg.getWindow().getDecorView();
                decorView.setBackgroundResource(android.R.color.transparent);
                markupDlg.show();
                image_parent.removeView(iv_markup);
                iv_markup = null;
            }
        }
        iv_markup = null;
        originR = null;
    }

    //Toolbar
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_ib_more) void onClickMore(){

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.room_ib_edit) void onClickEdit(){
        if (et_title.getVisibility() == View.GONE){
            et_title.setText(cur_pin.name);
            tv_title.setVisibility(View.GONE);
            et_title.setVisibility(View.VISIBLE);
            et_title.requestFocus();
            showKeyboard();
            btn_edit.setImageResource(R.drawable.ic_done);
        } else {
            hideKeyboard();
            String new_name = et_title.getText().toString().trim();
            if (!new_name.equals(cur_pin.name)){
                cur_pin.name = new_name;
                cur_pin.update_time = (long)System.currentTimeMillis()/1000 + "";
                dbHelper.updatePin(cur_pin);
                tv_title.setText(cur_pin.name);
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

    //Edit Markup
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.room_container)  RelativeLayout    image_parent;

    ImageView   iv_markup, iv_img;
    Point   s_pos = new Point();
    Point   e_pos = new Point();
    float   iv_scal = 0.0f;
    RectF   originF = new RectF();
    RectF   originR;

    List<Markup> mrk_list = new ArrayList<>();
    Markup sel_mrk;
    List<InsImg>    insImg_list = new ArrayList<>();
    InsImg sel_img;

    void editMarkup(){
        if (media_type == SCENE_MEDIA_PHOTO_BUILT){
            photoView.setFocusableInTouchMode(false);
        } else if (media_type == SCENE_MEDIA_PHOTO_360){
            mImagePlayerView.setFocusableInTouchMode(false);
        }

        image_parent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent mevent) {
                if (mevent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (edit_type == SCENE_EDIT_MARKUP) {
                        s_pos.x = (int)mevent.getX();
                        s_pos.y = (int)mevent.getY();
                        return true;
                    } else if (edit_type == SCENE_EDIT_IMAGE){
                        s_pos.x = (int)mevent.getX();
                        s_pos.y = (int)mevent.getY();
                        iv_img = new ImageView(RoomViewActivity.this);
                        iv_img.setImageResource(R.drawable.ic_image);
                        iv_img.setScaleType(ImageView.ScaleType.FIT_XY);
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(CUSTOM_IMG_SIZE, CUSTOM_IMG_SIZE);

                        lp.setMargins(s_pos.x - CUSTOM_IMG_SIZE/2, s_pos.y - CUSTOM_IMG_SIZE/2, 0, 0);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        image_parent.addView(iv_img, lp);
                        iv_img.requestLayout();
                        chooseCustomImage();
                        return true;
                    }
                } else if (mevent.getAction() == MotionEvent.ACTION_MOVE){
                    if (edit_type == SCENE_EDIT_MARKUP){
                        e_pos.x = (int) mevent.getX();
                        e_pos.y = (int) mevent.getY();
                        if (media_type == SCENE_MEDIA_PHOTO_360){
                            float fov = mImagePlayerView.getFov();
                            if (fov < 1.0){
                                moveImageView(s_pos, e_pos);
                            }
                        } else {
                            moveImageView(s_pos, e_pos);
                        }
                        return true;
                    }
                } else if (mevent.getAction() == MotionEvent.ACTION_UP){
                    if (edit_type == SCENE_EDIT_MARKUP || edit_type == SCENE_EDIT_IMAGE){
                        if (iv_markup != null){
                            if (media_type == SCENE_MEDIA_PHOTO_BUILT){
                                iv_scal = photoView.getScale();
                                originF = photoView.getDisplayRect();
                                originR = new RectF();
                                originR.left = (Math.abs(originF.left) + iv_markup.getLeft())/iv_scal;
                                originR.top = (Math.abs(originF.top) + iv_markup.getTop())/iv_scal;
                                originR.right = originR.left + iv_markup.getWidth()/iv_scal;
                                originR.bottom = originR.top + iv_markup.getHeight()/iv_scal;
                                redrawMarkup();
                            } else if (media_type == SCENE_MEDIA_PHOTO_360){
                                Rect win_rc = new Rect();
                                mImagePlayerView.getDrawingRect(win_rc);

                                float fov = mImagePlayerView.getFov();
                                float yaw = (float) (mImagePlayerView.getYaw() % (2* Math.PI));
                                float pitch = (float) (mImagePlayerView.getPitch() % (Math.PI));
                                if (fov > 1.0) return false;

                                int x1 = iv_markup.getLeft();
                                int y1 = iv_markup.getTop();
                                int x2 = iv_markup.getRight();
                                int y2 = iv_markup.getBottom();

                                int o_w = mWorkWrapper.O8〇oO8〇88.getWidthOrigin();
                                int o_h = mWorkWrapper.O8〇oO8〇88.getHeightOrigin();
                                float radius = (float)(o_w / Math.PI); //(float) o_w / 4;
                                int o_x = (int)((float)radius * (-1) * yaw / (2 * Math.PI));
                                int o_y = (int)((float)radius * (-1) * pitch / (2 * Math.PI));

                                double delta_l = (double)(win_rc.width()/2 - x1) * Math.tan(fov / 2);
                                double delta_t = (double)(win_rc.height()/2 - y1) * Math.tan(fov / 2);
                                double delta_r = (double)(win_rc.width()/2 - x2) * Math.tan(fov / 2);
                                double delta_b = (double)(win_rc.height()/2 - y2) * Math.tan(fov / 2);

                                originR = new RectF();
                                originR.left = (float) (o_x - delta_l);
                                originR.top = (float) (o_y - delta_t);
                                originR.right = (float) (o_x - delta_r);
                                originR.bottom = (float) (o_y - delta_b);
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    void redrawMarkup(){
        if (iv_markup != null){
            float cur_scale = photoView.getScale();
            int width = (int) (originR.width() * cur_scale);
            int height = (int) (originR.height() * cur_scale);
            RectF rcF = photoView.getDisplayRect();
            float dx_view = originR.left * cur_scale + rcF.left;
            float dy_view = originR.top * cur_scale + rcF.top;
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.leftMargin = (int)dx_view;
            layoutParams.topMargin = (int)dy_view;
            iv_markup.setLayoutParams(layoutParams);
            iv_markup.requestLayout();
        }
    }

    void redrawMarkup360(){
        Rect win_rc = new Rect();
        mImagePlayerView.getDrawingRect(win_rc);
        float fov = mImagePlayerView.getFov();
        float yaw = (float) (mImagePlayerView.getYaw() % (2 * Math.PI));
        float pitch = (float) (mImagePlayerView.getPitch() % (Math.PI));

        int o_w = mWorkWrapper.O8〇oO8〇88.getWidthOrigin();
        int o_h = mWorkWrapper.O8〇oO8〇88.getHeightOrigin();
        //------05/11------
        float radius = (float)(o_w / Math.PI); //(float) o_w / 4;
        int o_x = (int)((float)radius * (-1) * yaw / (2 * Math.PI));
        int o_y = (int)((float)radius * (-1) * pitch /  (2 * Math.PI));

        float delta_l = (float) (o_x - originR.left);
        float delta_t = (float) (o_y - originR.top);
        float delta_r = (float) (o_x - originR.right);
        float delta_b = (float) (o_y - originR.bottom);
        int left = win_rc.width()/2 - (int)(delta_l / Math.tan(fov/2));
        int top = win_rc.height()/2 - (int)(delta_t / Math.tan(fov/2));
        int right = win_rc.width()/2 - (int)(delta_r / Math.tan(fov/2));
        int bottom = win_rc.height()/2 - (int)(delta_b / Math.tan(fov/2));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Math.abs(right-left), Math.abs(bottom - top));
        layoutParams.leftMargin = Math.min(left, right);
        layoutParams.topMargin = Math.min(top, bottom);
        iv_markup.setLayoutParams(layoutParams);
        iv_markup.requestLayout();
    }

    void moveImageView(Point sp, Point ep){
        int sx = sp.x;
        int ex = ep.x;
        if (sp.x > ep.x) {
            sx = ep.x;
            ex = sp.x;
        }
        int sy = sp.y;
        int ey = ep.y;
        if (sp.y > ep.y){
            sy = ep.y;
            ey = sp.y;
        }

        if (iv_markup == null){
            iv_markup = new ImageView(this);
            iv_markup.setImageResource(R.drawable.ic_rect);
            iv_markup.setScaleType(ImageView.ScaleType.FIT_XY);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ex-sx, ey-sy);

            lp.setMargins(sx, sy, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            image_parent.addView(iv_markup, lp);
            iv_markup.requestLayout();

        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ex-sx, ey-sy);
            layoutParams.leftMargin = sx;
            layoutParams.topMargin = sy;
            iv_markup.setLayoutParams(layoutParams);
            iv_markup.requestLayout();
        }
    }

    //Markup menu
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.markup_menu_open) void onClickMarkupOpen(){
        hideMarkupMenu();
        if (sel_type == SEL_MARKUP) {
            MarkupDlg markupDlg = new MarkupDlg(this, sel_mrk, markup_listener);

            View decorView = markupDlg.getWindow().getDecorView();
            decorView.setBackgroundResource(android.R.color.transparent);
            markupDlg.show();
        } else if (sel_type == SEL_IMAGE){
            if (sel_img != null){
                view_preview.setVisibility(View.VISIBLE);

                Glide.with (this)
                        .load (sel_img.path)
                        .into (iv_preview);
            }
        }
        sel_type = SEL_NONE;
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.markup_menu_delete) void onClickMarkupDelete(){
        hideMarkupMenu();
        confirmDeleteMarkup();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.markup_menu_bg) void onClickMarkupMenuBG(){
        hideMarkupMenu();
        sel_type = SEL_NONE;
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.preview_iv_close) void onClickPreviewClose(){
        view_preview.setVisibility(View.GONE);
    }


    void showMarkupMenu(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        markup_menu_bg.startAnimation(fadein);
        markup_menu_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        markup_menu_view.startAnimation(bottomUp);
        markup_menu_view.setVisibility(View.VISIBLE);
    }

    void hideMarkupMenu(){
        Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        markup_menu_view.startAnimation(bottomDown);
        markup_menu_view.setVisibility(View.GONE);
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        markup_menu_bg.startAnimation(fadeout);
        markup_menu_bg.setVisibility(View.GONE);
    }

    void confirmDeleteMarkup(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_delete_confirm);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            //DeleteMarkup
            if (sel_type == SEL_MARKUP){
                if (sel_mrk != null){
                    dbHelper.deleteMarkup(sel_mrk.id);
                    mrk_list.remove(sel_mrk);
                    image_parent.removeView(sel_mrk.iv_markup);
                    refreshMarkups();
                    sel_mrk = null;
                }
            } else if (sel_type == SEL_IMAGE) {
                if (sel_img != null){
                    dbHelper.deleteInsImg(sel_img.id);
                    insImg_list.remove(sel_img);
                    image_parent.removeView(sel_img.iv_img);
                    refreshMarkups();
                    sel_img = null;
                }
            }
            sel_type = SEL_NONE;
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            sel_type = SEL_NONE;
            dialog.cancel();
        });

        builder.show();
    }

    MarkupDlg.EventListener markup_listener = new MarkupDlg.EventListener() {
        @Override
        public void onClickCreate() {
            mrk_list = dbHelper.getAllMarkups(sel_photo.id);
            refreshMarkups();
        }
    };

    void refreshMarkups(){
        if (iv_markup != null){
            image_parent.removeView(iv_markup);
            iv_markup = null;
        }

        for (int i = 0; i < mrk_list.size(); i++){
            Markup mrk = mrk_list.get(i);
            if (media_type == SCENE_MEDIA_PHOTO_BUILT){
                float cur_scale = photoView.getScale();
                int width = (int) (mrk.rc.width() * cur_scale);
                int height = (int) (mrk.rc.height() * cur_scale);
                RectF rcF = photoView.getDisplayRect();
                float dx_view = mrk.rc.left * cur_scale + rcF.left;
                float dy_view = mrk.rc.top * cur_scale + rcF.top;
                if (mrk.iv_markup == null){
                    mrk.iv_markup = new ImageView(this);
                    mrk.iv_markup.setImageResource(R.drawable.ic_rect);
                    mrk.iv_markup.setScaleType(ImageView.ScaleType.FIT_XY);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);

                    lp.setMargins((int)dx_view, (int)dy_view, 0, 0);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    image_parent.addView(mrk.iv_markup, lp);
                    mrk.iv_markup.requestLayout();

                    mrk.iv_markup.setOnClickListener(view -> {
                        sel_mrk = getMarkupFromList(view);
                        if (sel_mrk != null){
                            sel_type = SEL_MARKUP;
                            showMarkupMenu();
                        }
                    });
                } else {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
                    layoutParams.leftMargin = (int)dx_view;
                    layoutParams.topMargin = (int)dy_view;
                    mrk.iv_markup.setLayoutParams(layoutParams);
                    mrk.iv_markup.requestLayout();
                }
            } else if (media_type == SCENE_MEDIA_PHOTO_360){
                Rect win_rc = new Rect();
                mImagePlayerView.getDrawingRect(win_rc);
                float fov = mImagePlayerView.getFov();
                float yaw = (float) (mImagePlayerView.getYaw() % (2 * Math.PI));
                float pitch = (float) (mImagePlayerView.getPitch() % (Math.PI));

//                if (fov > 1.0){
//                    if (mrk.iv_markup != null) {
//                        mrk.iv_markup.setVisibility(View.INVISIBLE);
//                    }
//                    return;
//                }

                int o_w = mWorkWrapper.O8〇oO8〇88.getWidthOrigin();
                int o_h = mWorkWrapper.O8〇oO8〇88.getHeightOrigin();
                //------05/11------
                float radius = (float)(o_w / Math.PI); //(float) o_w / 4;
                int o_x = (int)((float)radius * (-1) * yaw / (2 * Math.PI));
                int o_y = (int)((float)radius * (-1) * pitch /  (2 * Math.PI));

                float delta_l = (float) (o_x - mrk.rc.left);
                float delta_t = (float) (o_y - mrk.rc.top);
                float delta_r = (float) (o_x - mrk.rc.right);
                float delta_b = (float) (o_y - mrk.rc.bottom);
                int left = win_rc.width()/2 - (int)(delta_l / Math.tan(fov/2));
                int top = win_rc.height()/2 - (int)(delta_t / Math.tan(fov/2));
                int right = win_rc.width()/2 - (int)(delta_r / Math.tan(fov/2));
                int bottom = win_rc.height()/2 - (int)(delta_b / Math.tan(fov/2));

                if (mrk.iv_markup == null) {
                    mrk.iv_markup = new ImageView(this);
                    mrk.iv_markup.setImageResource(R.drawable.ic_rect);
                    mrk.iv_markup.setScaleType(ImageView.ScaleType.FIT_XY);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(Math.abs(right-left), Math.abs(bottom - top));
                    lp.setMargins(Math.min(left, right), Math.min(top, bottom), 0, 0);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    image_parent.addView(mrk.iv_markup, lp);
                    mrk.iv_markup.requestLayout();

                    mrk.iv_markup.setOnClickListener(view -> {
                        sel_mrk = getMarkupFromList(view);
                        if (sel_mrk != null){
                            sel_type = SEL_MARKUP;
                            showMarkupMenu();
                        }
                    });
                } else {
                    mrk.iv_markup.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Math.abs(right-left), Math.abs(bottom - top));
                    layoutParams.leftMargin = Math.min(left, right);
                    layoutParams.topMargin = Math.min(top, bottom);
                    mrk.iv_markup.setLayoutParams(layoutParams);
                    mrk.iv_markup.requestLayout();
                }
            }
        }

        if (iv_img != null){
            image_parent.removeView(iv_img);
            iv_img = null;
        }

        for (int i = 0; i < insImg_list.size(); i++){
            InsImg insImg = insImg_list.get(i);
            if (media_type == SCENE_MEDIA_PHOTO_BUILT){
                float cur_scale = photoView.getScale();
                int width = CUSTOM_IMG_SIZE;
                int height = CUSTOM_IMG_SIZE;
                RectF rcF = photoView.getDisplayRect();
                float dx_view = insImg.point.x * cur_scale + rcF.left;
                float dy_view = insImg.point.y * cur_scale + rcF.top;
                if (insImg.iv_img == null){
                    insImg.iv_img = new ImageView(this);
                    insImg.iv_img.setImageResource(R.drawable.ic_image);
                    insImg.iv_img.setScaleType(ImageView.ScaleType.FIT_XY);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);

                    lp.setMargins((int)dx_view, (int)dy_view, 0, 0);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    image_parent.addView(insImg.iv_img, lp);
                    insImg.iv_img.requestLayout();

                    insImg.iv_img.setOnClickListener(view -> {
                        sel_img = getImageFromList(view);
                        if (sel_img != null){
                            sel_type = SEL_IMAGE;
                            showMarkupMenu();
                        }
                    });
                } else {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
                    layoutParams.leftMargin = (int)dx_view;
                    layoutParams.topMargin = (int)dy_view;
                    insImg.iv_img.setLayoutParams(layoutParams);
                    insImg.iv_img.requestLayout();
                }
            } else if (media_type == SCENE_MEDIA_PHOTO_360){
                Rect win_rc = new Rect();
                mImagePlayerView.getDrawingRect(win_rc);
                float fov = mImagePlayerView.getFov();
                float yaw = (float) (mImagePlayerView.getYaw() % (2 * Math.PI));
                float pitch = (float) (mImagePlayerView.getPitch() % (Math.PI));

                int o_w = mWorkWrapper.O8〇oO8〇88.getWidthOrigin();
                int o_h = mWorkWrapper.O8〇oO8〇88.getHeightOrigin();
                //------05/11------
                float radius = (float)(o_w / Math.PI); //(float) o_w / 4;
                int o_x = (int)((float)radius * (-1) * yaw / (2 * Math.PI));
                int o_y = (int)((float)radius * (-1) * pitch /  (2 * Math.PI));

                float delta_l = (float) (o_x - insImg.point.x);
                float delta_t = (float) (o_y - insImg.point.y);
                int left = win_rc.width()/2 - (int)(delta_l / Math.tan(fov/2));
                int top = win_rc.height()/2 - (int)(delta_t / Math.tan(fov/2));

                if (insImg.iv_img == null) {
                    insImg.iv_img = new ImageView(this);
                    insImg.iv_img.setImageResource(R.drawable.ic_image);
                    insImg.iv_img.setScaleType(ImageView.ScaleType.FIT_XY);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(CUSTOM_IMG_SIZE, CUSTOM_IMG_SIZE);
                    lp.setMargins(left, top, 0, 0);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    image_parent.addView(insImg.iv_img, lp);
                    insImg.iv_img.requestLayout();

                    insImg.iv_img.setOnClickListener(view -> {
                        sel_img = getImageFromList(view);
                        if (sel_img != null){
                            sel_type = SEL_IMAGE;
                            showMarkupMenu();
                        }
                    });
                } else {
                    insImg.iv_img.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(CUSTOM_IMG_SIZE, CUSTOM_IMG_SIZE);
                    layoutParams.leftMargin = left;
                    layoutParams.topMargin = top;
                    insImg.iv_img.setLayoutParams(layoutParams);
                    insImg.iv_img.requestLayout();
                }
            }
        }
    }

    private Markup getMarkupFromList(View view){
        for (int i = 0; i < mrk_list.size(); i++){
            Markup pin = mrk_list.get(i);
            if (pin.iv_markup != null && pin.iv_markup == view){
                return mrk_list.get(i);
            }
        }
        return null;
    }

    private InsImg getImageFromList(View view){
        for (int i = 0; i < insImg_list.size(); i++){
            InsImg pin = insImg_list.get(i);
            if (pin.iv_img != null && pin.iv_img == view){
                return insImg_list.get(i);
            }
        }
        return null;
    }

    void chooseCustomImage(){
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {

            if (options[item].equals("Take Photo")) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                photoResultLauncher.launch(cameraIntent);

            } else if (options[item].equals("Choose from Gallery")) {
                Intent intent = new  Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                chooseLauncher.launch(intent);

            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
                releaseEditMode();
            }
        });
        builder.show();
    }

    void saveCustomImg(String fpath){
        PointF pos = new PointF();
        if (media_type == SCENE_MEDIA_PHOTO_BUILT){
            iv_scal = photoView.getScale();
            originF = photoView.getDisplayRect();
            pos.x = (Math.abs(originF.left) + iv_img.getLeft())/iv_scal;
            pos.y = (Math.abs(originF.top) + iv_img.getTop())/iv_scal;
        } else if (media_type == SCENE_MEDIA_PHOTO_360){
            Rect win_rc = new Rect();
            mImagePlayerView.getDrawingRect(win_rc);

            float fov = mImagePlayerView.getFov();
            float yaw = (float) (mImagePlayerView.getYaw() % (2* Math.PI));
            float pitch = (float) (mImagePlayerView.getPitch() % (Math.PI));

            int x1 = iv_img.getLeft();
            int y1 = iv_img.getTop();

            int o_w = mWorkWrapper.O8〇oO8〇88.getWidthOrigin();
            int o_h = mWorkWrapper.O8〇oO8〇88.getHeightOrigin();
            float radius = (float)(o_w / Math.PI); //(float) o_w / 4;
            int o_x = (int)((float)radius * (-1) * yaw / (2 * Math.PI));
            int o_y = (int)((float)radius * (-1) * pitch / (2 * Math.PI));

            double delta_l = (double)(win_rc.width()/2 - x1) * Math.tan(fov / 2);
            double delta_t = (double)(win_rc.height()/2 - y1) * Math.tan(fov / 2);

            pos.x = (float) (o_x - delta_l);
            pos.y = (float) (o_y - delta_t);
        }
        releaseEditMode();

        InsImg ins = new InsImg();
        ins.photo_id = sel_photo.id + "";
        ins.point = pos;
        ins.path = fpath;
        long timestamp = System.currentTimeMillis()/1000;
        ins.create_time = timestamp + "";
        ins.update_time = timestamp + "";
        dbHelper.addInsImg(ins);
        insImg_list = dbHelper.getAllImgs(sel_photo.id);
        refreshMarkups();

    }

    ActivityResultLauncher<Intent> photoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    String dir_name = EXT_STORAGE_IMG_PATH;
                    File dir = new File(dir_name);
                    if (!dir.exists()){
                        dir.mkdir();
                    }
                    Long tsLong = System.currentTimeMillis()/60000;
                    String ts = tsLong.toString();
                    String f_name = dir_name + sel_photo.id + "_" + ts + ".png";
                    try (FileOutputStream out = new FileOutputStream(f_name)) {
                        photo.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                        saveCustomImg(f_name);
                    } catch (IOException e) {
                        releaseEditMode();
                        e.printStackTrace();
                        Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    releaseEditMode();
                    Toast.makeText(this, "Camera was cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    ActivityResultLauncher<Intent> chooseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    Uri contentUri = data.getData();
                    String path = ImageFilePath.getPath(this, contentUri);
                    if (path != null){
                        String dir_name = EXT_STORAGE_IMG_PATH;
                        File dir = new File(dir_name);
                        if (!dir.exists()){
                            dir.mkdir();
                        }
                        Long tsLong = System.currentTimeMillis()/60000;
                        String ts = tsLong.toString();
                        String f_name = dir_name + sel_photo.id + "_" + ts + ".png";
                        File srcFile = new File(path);
                        File destFile = new File(f_name);
                        try {
                            FileUtils.copyFile(srcFile, destFile);
                            saveCustomImg(f_name);
                        } catch (IOException e) {
                            releaseEditMode();
                            e.printStackTrace();
                        }
                    } else {
                        releaseEditMode();
                    }
                } else {
                    releaseEditMode();
                }
            });

    void releaseEditMode(){
        if (iv_img != null){
            image_parent.removeView(iv_img);
            iv_img = null;
        }
        edit_type = SCENE_EDIT_NONE;
        tv_status.setVisibility(View.GONE);
        if (media_type == SCENE_MEDIA_PHOTO_BUILT){
            photoView.setFocusableInTouchMode(true);
        } else if (media_type == SCENE_MEDIA_PHOTO_360){
            mImagePlayerView.setFocusableInTouchMode(true);
        }
    }

    final int SEL_MARKUP  = 1;
    final int SEL_IMAGE   = 2;
    final int SEL_NONE    = 0;
    private int sel_type = SEL_NONE;

}