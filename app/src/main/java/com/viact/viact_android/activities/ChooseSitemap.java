package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.SITE_MAP_URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.viact.viact_android.R;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.utils.ImageFilePath;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseSitemap extends BaseActivity {

    Project project;
    DatabaseHelper dbHelper;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.photo_view)    PhotoView photo_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_sitemap);
        setTitle(R.string.choose_sitemap_title);

        ButterKnife.bind(this);
        dbHelper = DatabaseHelper.getInstance(this);
        initLayout();
    }

    void initLayout(){
        Bundle data = getIntent().getExtras();
        project = (Project) data.getParcelable("project");
        Glide.with (this)
                .load (SITE_MAP_URL)
                .into (photo_view);
//        project.site_map = SITE_MAP_URL;
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.iv_choose) void onClickChoose(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_sitemap_title));
        builder.setPositiveButton(R.string.choose_sitemap_from_url, (dialogInterface, i) -> {
            inputSitemapUrl();
        });
        builder.setNegativeButton(R.string.choose_sitemap_from_gallery, (dialogInterface, i) -> {
            Intent intent = new  Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            chooseLauncher.launch(intent);
        });
        builder.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btn_confirm) void onClickNext(){
        Drawable bm = photo_view.getDrawable();
        if (bm != null){
            long timestamp = System.currentTimeMillis()/1000;
            project.create_time = timestamp + "";
            project.update_time = timestamp + "";
            dbHelper.addProject(project);
            finish();
        } else {
            showToast("Please choose the sitemap");
        }
    }

    @Override
    public void onBackPressed(){
    }

    void inputSitemapUrl(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sitemap URL");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(SITE_MAP_URL);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String m_Text = input.getText().toString();
            Glide.with (this)
                    .load (m_Text)
                    .into (photo_view);
//            project.site_map = m_Text;
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

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

                        Glide.with (this)
                                .load (path)
                                .into (photo_view);
//                        project.site_map = path;
                    }
                }
            });
}