package com.arashivision.sdk.demo.dialog;

import static com.arashivision.sdk.demo.util.Const.PIN_SIZE_MAX_LEN;
import static com.arashivision.sdk.demo.util.Const.PIN_SIZE_MIN_LEN;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.arashivision.sdk.demo.R;
import com.arashivision.sdk.demo.models.PinPoint;
import com.arashivision.sdk.demo.util.API;
import com.arashivision.sdk.demo.util.APICallback;
import com.arashivision.sdk.demo.util.SaveSharedPrefrence;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.OnViewDragListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.kaopiz.kprogresshud.KProgressHUD;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SiteMapDlg extends Dialog {

    final String SITE_MAP_URL = "https://customindz-shinobi.s3.ap-southeast-1.amazonaws.com/cbimage.png";

    Context context;
    String panorama_url="";
    SaveSharedPrefrence sharedPref;
    private KProgressHUD hud;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.dlg_iv_sitemap)    ImageView         iv_sitemap;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.photo_view)        PhotoView         photo_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.pinsContainer)     RelativeLayout    pinsContainer;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_dlg_upload)    Button            btn_upload;

    ImageView pinV = null;
    int tempX, tempY;
    PinPoint    pinPoint = new PinPoint();

    public SiteMapDlg(@NonNull Context context, String pano_url) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.context = context;
        this.panorama_url = pano_url;
        sharedPref = new SaveSharedPrefrence();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_site_map);
        ButterKnife.bind(this);

        setCancelable(false);

        Glide.with (context)
                .load (SITE_MAP_URL)
                .into (photo_view);

        initLayout();
    }

    void initLayout(){
        btn_upload.setEnabled(false);

        photo_view.setOnMatrixChangeListener(rect -> refreshPin());

        photo_view.setOnPhotoTapListener(new PhotoTapListener());
    }

    void refreshPin(){
        if (pinV != null){
            float img_scale = photo_view.getScale();
            int pin_wh = (int)((PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale);
            RectF rc = photo_view.getDisplayRect();
            float site_width = rc.width();
            float site_height = rc.height();
            tempX = (int) (site_width * pinPoint.x + rc.left) - pin_wh / 4;
            tempY = (int) (site_height * pinPoint.y + rc.top) - pin_wh * 3 / 4;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pin_wh, pin_wh);
            // Setting position of our ImageView
            layoutParams.leftMargin = tempX;
            layoutParams.topMargin = tempY;
            pinV.setLayoutParams(layoutParams);
            pinV.requestLayout();

            int xMax = photo_view.getWidth();
            int yMax = photo_view.getHeight();

            if (tempX < 0 || tempY < 0 || tempX > xMax || tempY > yMax) {
                pinV.setVisibility(View.INVISIBLE);
            } else{
                pinV.setVisibility(View.VISIBLE);
            }
        }
    }

    void showPin(){
        // calculate Pin position
        float img_scale = photo_view.getScale();
        int pin_wh = (int)((PIN_SIZE_MAX_LEN - PIN_SIZE_MIN_LEN) * img_scale);
        RectF rc = photo_view.getDisplayRect();
        float site_width = rc.width();
        float site_height = rc.height();
        tempX = (int) (site_width * pinPoint.x + rc.left) - pin_wh / 4;
        tempY = (int) (site_height * pinPoint.y + rc.top) - pin_wh * 3 / 4;
        if (tempX < 0) {
            tempX = 0;
        }
        if (tempY < 0) {
            tempY = 0;
        }

        ImageView iv = new ImageView(context);
        iv.setImageResource(R.drawable.pin);
        iv.setOnClickListener(view -> {
            confirmDeletePin();
        });

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.setMargins(tempX, tempY, 0, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        pinsContainer.addView(iv, lp);
        iv.getLayoutParams().height = pin_wh;
        iv.getLayoutParams().width = pin_wh;
        iv.requestLayout();
        pinV = iv;
        btn_upload.setEnabled(true);
    }

    void confirmDeletePin(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage("Are you sure to remove the Pin?");
        alertDialogBuilder.setPositiveButton("Yes", (arg0, arg1) -> {
            pinsContainer.removeAllViews();
            pinV = null;
            btn_upload.setEnabled(false);
            Toast.makeText(context, "Selected Pin was removed", Toast.LENGTH_SHORT).show();
        });
        alertDialogBuilder.setNegativeButton("No", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_dlg_close) void onClickClose(){
        this.dismiss();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_dlg_upload) void onClickUpload(){
//        panorama_url = SITE_MAP_URL; // test code
        String token = sharedPref.getString(context, SaveSharedPrefrence.PREFS_AUTH_TOKEN);
        String cc_code = sharedPref.getString(context, SaveSharedPrefrence.PREFS_COMPANY_CODE);
        showProgress("Upload data...");
        API.uploadSiteMap(token, cc_code, panorama_url, pinPoint.x, pinPoint.y, new APICallback<String>() {
            @Override
            public void onSuccess(String response) {
                dismissProgress();
                Toast.makeText(context, "Upload data successful : " + response, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                dismissProgress();
                Toast.makeText(context, "Data upload failed : " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showProgress(String message) {
        hud = KProgressHUD.create(context).setLabel(message);
        hud.show();
    }

    public void dismissProgress() {
        if (hud != null) {
            hud.dismiss();
        }
    }

    private class PhotoTapListener implements OnPhotoTapListener {

        @Override
        public void onPhotoTap(ImageView view, float x, float y) {
            if (pinV == null) {
                pinPoint.x = x;
                pinPoint.y = y;
                showPin();
            } else {
                Toast.makeText(context, "The Pin was added", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
