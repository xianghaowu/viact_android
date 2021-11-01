package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_APP_PATH;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.viact.viact_android.R;
import com.viact.viact_android.adapters.ProjectsAdapter;
import com.viact.viact_android.dialogs.CreateProjectDlg;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseObserveCameraActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_recyclerView)    RecyclerView        project_recycler;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.main_view_empty)      LinearLayout        view_empty;


    List<Project> list_project = new ArrayList<>();
    ProjectsAdapter projectsAdapter;

    DatabaseHelper  dbHelper;

    ProjectsAdapter.EventListener listener = new ProjectsAdapter.EventListener() {
        @Override
        public void onClickEdit(int index) {
            moveProjectActivity(index);
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
    public void onDestroy(){
        if (InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE) {
            InstaCameraManager.getInstance().closeCamera();
        }
        super.onDestroy();
    }

    void moveProjectActivity(int ind){
        Project proc = list_project.get(ind);
        Intent pjocIntent = new Intent(MainActivity.this, ProjectActivity.class);
        pjocIntent.putExtra("project", proc);
        startActivity(pjocIntent);
    }

    void initLayout(){
        File folder = new File(EXT_STORAGE_APP_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }

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
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.main_btn_add_project) void onClickAddProject(){
        CreateProjectDlg createDlg = new CreateProjectDlg(this, createListener);

        View decorView = createDlg.getWindow().getDecorView();
        decorView.setBackgroundResource(android.R.color.transparent);
        createDlg.show();
    }

    CreateProjectDlg.EventListener createListener = () -> refreshLayout();

    void confirmDeleteProject(final int ind){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_delete_confirm);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            procProjectDelete(ind);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void procProjectDelete(int index){
        Project proc = list_project.get(index);
        List<Sheet> sh_list = dbHelper.getAllSheets(proc.id);
        for (int i = 0; i < sh_list.size() ; i++){
            Sheet sh = sh_list.get(i);
            List<PinPoint> pt_list = dbHelper.getPinsForSheet(sh.id);
            for (int j = 0; j < pt_list.size(); j++){
                PinPoint pt = pt_list.get(j);
                List<SpotPhoto> sp_list = dbHelper.getAllSpots(pt.id);
                for (int k = 0; k < sp_list.size(); k++)
                {
                    SpotPhoto sp = sp_list.get(k);
                    File sp_f = new File(sp.path);
                    sp_f.delete();
                    dbHelper.deleteSpot(sp.id);
                }
                dbHelper.deletePin(pt.id);
            }
            File sh_f = new File(sh.path);
            sh_f.delete();
            dbHelper.deleteSheet(sh.id);
        }
        dbHelper.deleteProject(proc.id);
        refreshLayout();
    }

}