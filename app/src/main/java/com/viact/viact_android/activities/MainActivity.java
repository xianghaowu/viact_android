package com.viact.viact_android.activities;

import static com.viact.viact_android.utils.Const.EXT_STORAGE_APP_PATH;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.viact.viact_android.R;
import com.viact.viact_android.adapters.ProjectsAdapter;
import com.viact.viact_android.dialogs.CreateProjectDlg;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.Project;

import java.io.File;
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


    List<Project> list_project = new ArrayList<>();
    ProjectsAdapter projectsAdapter;

    DatabaseHelper  dbHelper;

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
    public void onDestroy(){
        if (InstaCameraManager.getInstance().getCameraConnectedType() != InstaCameraManager.CONNECT_TYPE_NONE) {
            InstaCameraManager.getInstance().closeCamera();
        }
        super.onDestroy();
    }

    void moveEditSitemap(int ind){
        Project proc = list_project.get(ind);
        Intent editIntent = new Intent(MainActivity.this, EditSitemap.class);
        editIntent.putExtra("project", proc);
        startActivity(editIntent);
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

}