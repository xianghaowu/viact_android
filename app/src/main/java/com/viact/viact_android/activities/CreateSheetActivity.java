package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_SHEET_PATH;
import static com.viact.viact_android.utils.Const.SHEET_TYPE_NORMAL;
import static com.viact.viact_android.utils.Const.SHEET_TYPE_NO_FILE;
import static com.viact.viact_android.utils.Const.SITE_MAP_URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.viact.viact_android.R;
import com.viact.viact_android.dialogs.ConfirmSheetDlg;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.utils.FileUtils;
import com.viact.viact_android.utils.ImageFilePath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateSheetActivity extends BaseActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.create_sheet_name_view)    View                        name_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.create_sheet_map_view)     View                        map_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.create_sheet_iv_logo)      ImageView                   iv_logo;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.create_sheet_et_name)      EditText                    edit_name;

    String sheet_name = "";
    Project cur_proc;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sheet);
        ButterKnife.bind(this);
        Bundle data = getIntent().getExtras();
        cur_proc = (Project) data.getParcelable("project");
        dbHelper = DatabaseHelper.getInstance(this);

        initLayout();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.create_sheet_camera) void onClickCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoResultLauncher.launch(cameraIntent);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.create_sheet_pick) void onClickPick(){
        Intent intent = new  Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        chooseLauncher.launch(intent);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.create_sheet_dropbox) void onClickDropbox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dropbox URL");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(SITE_MAP_URL);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String m_Text = input.getText().toString();
            if (m_Text.length() > 0){
                downloadFile(m_Text);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.create_sheet_google) void onClickGoogle(){
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("project", cur_proc);
        mapIntent.putExtra("sheet_name", sheet_name);
        mapsLauncher.launch(mapIntent);
//        startActivity(mapIntent);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.create_sheet_no) void onClickNo(){
        String dir_name = EXT_STORAGE_SHEET_PATH;
        File dir = new File(dir_name);
        if (!dir.exists()){
            dir.mkdir();
        }
        long tsLong = System.currentTimeMillis()/60000;
        String ts = Long.toString(tsLong);
        String f_name = dir_name + cur_proc.name + "_" + sheet_name + "_" + ts + ".png";
        moveConfirmMap(SHEET_TYPE_NO_FILE, f_name);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.create_sheet_ib_back) void onClickBack(){
        onBackPressed();
    }

    void downloadFile(String url){
        showProgress();
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        dismissProgress();
                        String dir_name = EXT_STORAGE_SHEET_PATH;
                        File dir = new File(dir_name);
                        if (!dir.exists()){
                            dir.mkdir();
                        }
                        long tsLong = System.currentTimeMillis()/60000;
                        String ts = Long.toString(tsLong);
                        String f_name = dir_name + cur_proc.name + "_" + sheet_name + "_" + ts + ".png";
                        try (FileOutputStream out = new FileOutputStream(f_name)) {
                            resource.compress(Bitmap.CompressFormat.PNG, 100, out);
                            moveConfirmMap(SHEET_TYPE_NORMAL, f_name);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(CreateSheetActivity.this, "File save error!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        dismissProgress();
                    }
                });
    }

    void initLayout(){
        name_view.setVisibility(View.VISIBLE);
        map_view.setVisibility(View.GONE);
        iv_logo.setVisibility(View.GONE);
        edit_name.setText("");
        edit_name.setOnKeyListener((v, keyCode, event) -> {
            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                onClickNext();
                return true;
            }
            return false;
        });
        showLogo();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.create_sheet_tv_next) void onClickNext(){
        sheet_name = edit_name.getText().toString().trim();
        if (sheet_name.length() > 0){
            hideKeyboard();
            Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out_slow);
            iv_logo.startAnimation(fadeout);
            iv_logo.setVisibility(View.GONE);
            fadeout.setAnimationListener(new Animation.AnimationListener(){
                @Override
                public void onAnimationStart(Animation arg0) {
                }
                @Override
                public void onAnimationRepeat(Animation arg0) {
                }
                @Override
                public void onAnimationEnd(Animation arg0) {
                    name_view.setVisibility(View.GONE);
                    map_view.setVisibility(View.VISIBLE);
                }
            });

        } else {
            Toast.makeText(this, "Please type the sheet name", Toast.LENGTH_SHORT).show();
        }
    }

    void showLogo(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        iv_logo.startAnimation(fadein);
        iv_logo.setVisibility(View.VISIBLE);
        fadein.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                edit_name.requestFocus();
                showKeyboard();
            }
        });
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

    void moveConfirmMap(int type, String file_name){
        ConfirmSheetDlg confirmDlg = new ConfirmSheetDlg(this, sheet_name, cur_proc.id , type, file_name, confirmListener);

        View decorView = confirmDlg.getWindow().getDecorView();
        decorView.setBackgroundResource(android.R.color.transparent);
        confirmDlg.show();
    }

    ConfirmSheetDlg.EventListener confirmListener = this::finish;

    ActivityResultLauncher<Intent> photoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    String dir_name = EXT_STORAGE_SHEET_PATH;
                    File dir = new File(dir_name);
                    if (!dir.exists()){
                        dir.mkdir();
                    }
                    Long tsLong = System.currentTimeMillis()/60000;
                    String ts = tsLong.toString();
                    String temp_sheet = sheet_name.replaceAll("[;\\/:*?\"<>|&']", "_");
                    String f_name = dir_name + cur_proc.name + "_" + temp_sheet + "_" + ts + ".png";
                    try (FileOutputStream out = new FileOutputStream(f_name)) {
                        photo.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                        moveConfirmMap(SHEET_TYPE_NORMAL, f_name);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "File save error!", Toast.LENGTH_SHORT).show();
                    }
                } else {
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
                        String dir_name = EXT_STORAGE_SHEET_PATH;
                        File dir = new File(dir_name);
                        if (!dir.exists()){
                            dir.mkdir();
                        }
                        Long tsLong = System.currentTimeMillis()/60000;
                        String ts = tsLong.toString();
                        String temp_sheet = sheet_name.replaceAll("[;\\/:*?\"<>|&']", "_");
                        String f_name = dir_name + cur_proc.name + "_" + temp_sheet + "_" + ts + ".png";
                        File srcFile = new File(path);
                        File destFile = new File(f_name);
                        try {
                            FileUtils.copyFile(srcFile, destFile);
                            moveConfirmMap(SHEET_TYPE_NORMAL, f_name);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> mapsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    finish();
                }
            });
}