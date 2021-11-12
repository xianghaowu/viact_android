package com.viact.viact_android.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.viact.viact_android.R;
import com.viact.viact_android.adapters.ListSheetsAdapter;
import com.viact.viact_android.adapters.QuickSheetsAdapter;
import com.viact.viact_android.helpers.DatabaseHelper;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProjectActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.project_quick_recycler)    RecyclerView          quick_recycler;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.project_sheet_recycler)    RecyclerView          sheet_recycler;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.project_tv_title)          TextView              tv_title;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.project_menu_bg)           View                  menu_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.project_menu_view)         View                  menu_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.project_menu_tv_name)      TextView              menu_project_title;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.sheet_menu_bg)             View                  sheet_menu_bg;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.sheet_menu_view)           View                  sheet_menu_view;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.sheet_menu_tv_name)        TextView              menu_sheet_title;

    DatabaseHelper dbHelper;

    Project     cur_proc;
    Sheet       sel_sheet;

    QuickSheetsAdapter  quickAdapter;
    ListSheetsAdapter   listAdapter;
    List<Sheet>         sheet_list;

    QuickSheetsAdapter.EventListener quickListener = new QuickSheetsAdapter.EventListener() {
        @Override
        public void onClickItem(int index) {
            moveEditSitemap(index);
        }
    };

    ListSheetsAdapter.EventListener listListener = new ListSheetsAdapter.EventListener() {
        @Override
        public void onClickItem(int index) {
            moveEditSitemap(index);
        }
        @Override
        public void onClickMenu(int index) {
            sel_sheet = sheet_list.get(index);
            menu_sheet_title.setText(sel_sheet.name);
            showSheetMenu();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        ButterKnife.bind(this);
        dbHelper = DatabaseHelper.getInstance(this);

        Bundle data = getIntent().getExtras();
        cur_proc = (Project) data.getParcelable("project");

        initLayout();
    }

    @Override
    public void onResume(){
        super.onResume();
        sheet_list = dbHelper.getAllSheets(cur_proc.id);
        if (sheet_list.size() == 0) {
            onAddSheet();
        } else {
            quickAdapter.setDataList(sheet_list);
            listAdapter.setDataList(sheet_list);
        }
    }

    void moveEditSitemap(int ind){
        Sheet sheet = sheet_list.get(ind);
        Intent editIntent = new Intent(this, EditSheetActivity.class);
        editIntent.putExtra("sheet_id", sheet.id);
        startActivity(editIntent);
    }

    void initLayout(){
        tv_title.setText(cur_proc.name);
        menu_project_title.setText(cur_proc.name);
        sheet_list = dbHelper.getAllSheets(cur_proc.id);

        quickAdapter = new QuickSheetsAdapter(this, sheet_list, quickListener);
        quick_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        quick_recycler.setAdapter(quickAdapter);

        listAdapter = new ListSheetsAdapter(this, sheet_list, listListener);
        sheet_recycler.setLayoutManager(new LinearLayoutManager(this));
        sheet_recycler.setAdapter(listAdapter);
    }

    void refreshLayout(){
        tv_title.setText(cur_proc.name);
        menu_project_title.setText(cur_proc.name);
        sheet_list = dbHelper.getAllSheets(cur_proc.id);
        quickAdapter.setDataList(sheet_list);
        listAdapter.setDataList(sheet_list);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_ib_back) void onClickHome(){
        super.onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_ib_menu) void onClickMenu(){
        showProjectMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_btn_add) void onAddSheet(){
        Intent pjocIntent = new Intent(this, CreateSheetActivity.class);
        pjocIntent.putExtra("project", cur_proc);
        startActivity(pjocIntent);
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_ib_upload) void onClickUpload(){

    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_menu_bg) void onClickProjectMenuBg(){
        hideProjectMenu();
    }

    //Process Project Menu
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_menu_rename) void onClickProjectRename(){
        hideProjectMenu();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.project_type_project_name_desc);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(cur_proc.name);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String m_Text = input.getText().toString().trim();
            if (m_Text.length() > 0 && !m_Text.equals(cur_proc.name)){
                cur_proc.name = m_Text;
                dbHelper.updateProject(cur_proc);
                refreshLayout();
            } else {
                Toast.makeText(this, "Please type the new project name.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_menu_remove) void onClickProjectRemove(){
        hideProjectMenu();
        confirmDeleteProject();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.project_menu_report) void onClickProjectReport(){

    }

    void showProjectMenu(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        menu_bg.startAnimation(fadein);
        menu_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        menu_view.startAnimation(bottomUp);
        menu_view.setVisibility(View.VISIBLE);
    }

    void hideProjectMenu(){
        Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        menu_view.startAnimation(bottomDown);
        menu_view.setVisibility(View.GONE);
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        menu_bg.startAnimation(fadeout);
        menu_bg.setVisibility(View.GONE);
    }

    //Process Sheet Menu
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.sheet_menu_bg) void onClickSheetMenuBg(){
        hideSheetMenu();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.sheet_menu_rename) void onClickSheetRename(){
        hideSheetMenu();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.project_type_sheet_name_desc);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(sel_sheet.name);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String m_Text = input.getText().toString().trim();
            if (m_Text.length() > 0 && !m_Text.equals(sel_sheet.name)){
                sel_sheet.name = m_Text;
                dbHelper.updateSheet(sel_sheet);
                refreshLayout();
            } else {
                Toast.makeText(this, "Please type the new project name.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.sheet_menu_remove) void onClickSheetRemove(){
        hideSheetMenu();
        confirmDeleteSheet();
    }

    void showSheetMenu(){
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        sheet_menu_bg.startAnimation(fadein);
        sheet_menu_bg.setVisibility(View.VISIBLE);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        sheet_menu_view.startAnimation(bottomUp);
        sheet_menu_view.setVisibility(View.VISIBLE);
    }

    void hideSheetMenu(){
        Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
        sheet_menu_view.startAnimation(bottomDown);
        sheet_menu_view.setVisibility(View.GONE);
        Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        sheet_menu_bg.startAnimation(fadeout);
        sheet_menu_bg.setVisibility(View.GONE);
    }

    //Remove the project
    void confirmDeleteProject(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.main_desc_delete_confirm);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            procProjectDelete();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void procProjectDelete(){
        List<Sheet> sh_list = dbHelper.getAllSheets(cur_proc.id);
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
                    dbHelper.deleteMarkupsByPhoto(sp.id);
                    dbHelper.deleteSpot(sp.id);
                }
                dbHelper.deletePin(pt.id);
            }
            File sh_f = new File(sh.path);
            sh_f.delete();
            dbHelper.deleteSheet(sh.id);
        }
        dbHelper.deleteProject(cur_proc.id);
        finish();
    }

    //Remove the Sheet
    void confirmDeleteSheet(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.project_desc_delete_confirm_sheet);
        // Set up the buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            procSheetDelete();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    void procSheetDelete(){
        List<PinPoint> pt_list = dbHelper.getPinsForSheet(sel_sheet.id);
        for (int j = 0; j < pt_list.size(); j++){
            PinPoint pt = pt_list.get(j);
            List<SpotPhoto> sp_list = dbHelper.getAllSpots(pt.id);
            for (int k = 0; k < sp_list.size(); k++)
            {
                SpotPhoto sp = sp_list.get(k);
                File sp_f = new File(sp.path);
                sp_f.delete();
                dbHelper.deleteMarkupsByPhoto(sp.id);
                dbHelper.deleteSpot(sp.id);
            }
            dbHelper.deletePin(pt.id);
        }
        File sh_f = new File(sel_sheet.path);
        sh_f.delete();
        dbHelper.deleteSheet(sel_sheet.id);
        refreshLayout();
    }
}