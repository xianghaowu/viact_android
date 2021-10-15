package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.CAMERA_360;
import static com.viact.viact_android.utils.Const.CAMERA_BUILT_IN;
import static com.viact.viact_android.utils.Const.SITE_MAP_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.bumptech.glide.Glide;
import com.viact.viact_android.R;
import com.viact.viact_android.adapters.ProjectsAdapter;
import com.viact.viact_android.dialogs.CreateProjectDlg;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.utils.NetworkManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseObserveCameraActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_recyclerView)    RecyclerView        project_recycler;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_empty)      LinearLayout        view_empty;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_txt_choose_device)    TextView      txt_sel_camera;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_bg_connect)   View          view_connect_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_connect_option)   View      view_connect_option;

    List<Project> list_project = new ArrayList<>();
    ProjectsAdapter projectsAdapter;

    DatabaseHelper  dbHelper;

    int kind_camera = CAMERA_BUILT_IN;

    ProjectsAdapter.EventListener listener = new ProjectsAdapter.EventListener() {
        @Override
        public void onClickEdit(int index) {
            moveEditSitemap(index);
        }

        @Override
        public void onClickDelete(int index) {
            confirmDeleteProject(index);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        dbHelper = DatabaseHelper.getInstance(this);
        initLayout();
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout();
    }

    @Override
    public void onStop(){
        if (InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE) {
            InstaCameraManager.getInstance().closeCamera();
        }
        super.onStop();
    }

    void moveEditSitemap(int ind){
        Project proc = list_project.get(ind);
        Intent editIntent = new Intent(MainActivity.this, EditSitemap.class);
        editIntent.putExtra("project", proc);
        editIntent.putExtra("kind_camera", kind_camera);
        startActivity(editIntent);
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
            // Set up the buttons
            builder.setPositiveButton("Built-in", (dialog, which) -> {
//                if (InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE) {
//                    InstaCameraManager.getInstance().closeCamera();
//                }
                kind_camera = CAMERA_BUILT_IN;
                refreshLayout();
            });
            builder.setNegativeButton("360 Camera", (dialog, which) -> {
                checkConnectionCamera();
            });

            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    void initLayout(){
        list_project = dbHelper.getProjects();
        projectsAdapter = new ProjectsAdapter(this, list_project, listener);
        project_recycler.setLayoutManager(new LinearLayoutManager(this));
        project_recycler.setAdapter(projectsAdapter);

        refreshLayout();
    }

    void refreshLayout(){
        list_project = dbHelper.getProjects();
        projectsAdapter.setDataList(list_project);
        if (list_project.size() > 0) {
            view_empty.setVisibility(View.GONE);
        } else {
            view_empty.setVisibility(View.VISIBLE);
        }

        if (kind_camera == CAMERA_BUILT_IN){
            txt_sel_camera.setText(R.string.main_desc_camera_built_in);
        } else {
            txt_sel_camera.setText(R.string.main_desc_camera_360);
        }
    }

    void chooseSiteMap(Project proc){
        Intent chooseIntent = new Intent(this, ChooseSitemap.class);
        chooseIntent.putExtra("project", proc);
        startActivity(chooseIntent);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.main_btn_add_project) void onClickAddProject(){
        CreateProjectDlg createDlg = new CreateProjectDlg(this, createListener);

        View decorView = createDlg.getWindow().getDecorView();
        decorView.setBackgroundResource(android.R.color.transparent);
        createDlg.show();
    }

    CreateProjectDlg.EventListener createListener = this::chooseSiteMap;

    void confirmDeleteProject(final int ind){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_delete_confirm);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            Project proc = list_project.get(ind);
            dbHelper.deletePinsForProject(proc.id + "");
            dbHelper.delete(proc.id + "");
            refreshLayout();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void checkConnectionCamera(){
        if (InstaCameraManager.getInstance().getCameraConnectedType() == InstaCameraManager.CONNECT_TYPE_NONE) {
            connectCamera();
        } else {
            refreshLayout();
        }
    }

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
        } else {
            NetworkManager.getInstance().clearBindProcess();
            Toast.makeText(this, R.string.main_toast_camera_disconnected, Toast.LENGTH_SHORT).show();
        }
        refreshLayout();
    }
}