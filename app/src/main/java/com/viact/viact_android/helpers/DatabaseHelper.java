package com.viact.viact_android.helpers;

import static com.viact.viact_android.utils.Const.APP_NAME_REF;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Project;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    //database name
    public static final String DATABASE_NAME = APP_NAME_REF;
    //database version
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_PROJECTS = "tbl_projects";
    public static final String TABLE_PINS = "tbl_pins";

    // projects Table Columns
    private static final String KEY_PROJECT_NAME = "name";
    private static final String KEY_PROJECT_ID = "ID";
    private static final String KEY_PROJECT_COMPANY = "company";
    private static final String KEY_PROJECT_ADDRESS = "address";
    private static final String KEY_PROJECT_DESC = "description";
    private static final String KEY_PROJECT_SITEMAP = "sitemap";
    private static final String KEY_PROJECT_SYNC = "sync";

    // pins Table Columns
    private static final String KEY_PIN_ID = "ID";
    private static final String KEY_PIN_PROJECT_ID = "project_id";
    private static final String KEY_PIN_X = "x_loc";
    private static final String KEY_PIN_Y = "y_loc";
    private static final String KEY_PIN_PATH = "path";

    private static DatabaseHelper sInstance;

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query;
        //creating table
        query = "CREATE TABLE " + TABLE_PROJECTS + " (" + KEY_PROJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_PROJECT_NAME + " TEXT, "
                + KEY_PROJECT_COMPANY + " TEXT, "
                + KEY_PROJECT_ADDRESS + " TEXT, "
                + KEY_PROJECT_SITEMAP + " TEXT, "
                + KEY_PROJECT_DESC + " TEXT, "
                + KEY_PROJECT_SYNC + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_PINS + " (" + KEY_PIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_PIN_PROJECT_ID + " TEXT, "
                + KEY_PIN_X + " TEXT, "
                + KEY_PIN_Y + " TEXT, "
                + KEY_PIN_PATH + " TEXT);";
        db.execSQL(query);

    }

    //upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PINS + ";");
            onCreate(db);
        }
    }

    //add the new pin
    public void addPin(PinPoint ct) {
        SQLiteDatabase sqLiteDatabase = this .getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PIN_PROJECT_ID, ct.p_id);
        values.put(KEY_PIN_X, ct.x + "");
        values.put(KEY_PIN_Y, ct.y + "");
        values.put(KEY_PIN_PATH, ct.path);
        //inserting new row
        sqLiteDatabase.insert(TABLE_PINS, null , values);
        //close database connection
        sqLiteDatabase.close();
    }

    //get the pins for project
    public ArrayList<PinPoint> getPinsForProject(String proc_id) {
        ArrayList<PinPoint> arrayList = new ArrayList<>();

        // select all query
        String select_query= "SELECT * FROM " + TABLE_PINS + " WHERE project_id = " + proc_id + ";";

        SQLiteDatabase db = this .getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PinPoint pin = new PinPoint();
                pin.id = cursor.getInt(0);
                pin.p_id = cursor.getString(1);
                pin.x = Float.parseFloat(cursor.getString(2));
                pin.y = Float.parseFloat(cursor.getString(3));
                pin.path = cursor.getString(4);
                arrayList.add(pin);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }

    //delete the pin
    public void deletePin(String ID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_PINS, "ID=" + ID, null);
        sqLiteDatabase.close();
    }

    public void deletePinsForProject(String proID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_PINS, "project_id=" + proID, null);
        sqLiteDatabase.close();
    }

    //add the new project
    public void addProject(Project ct) {
        SQLiteDatabase sqLiteDatabase = this .getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PROJECT_NAME, ct.name);
        values.put(KEY_PROJECT_COMPANY, ct.company);
        values.put(KEY_PROJECT_ADDRESS, ct.address);
        values.put(KEY_PROJECT_DESC, ct.desc);
        values.put(KEY_PROJECT_SITEMAP, ct.site_map);
        values.put(KEY_PROJECT_SYNC, ct.sync);
        //inserting new row
        sqLiteDatabase.insert(TABLE_PROJECTS, null , values);
        //close database connection
        sqLiteDatabase.close();
    }

    //get the all notes
    public ArrayList<Project> getProjects() {
        ArrayList<Project> arrayList = new ArrayList<>();

        // select all query
        String select_query= "SELECT * FROM " + TABLE_PROJECTS;

        SQLiteDatabase db = this .getWritableDatabase();
        Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Project proc = new Project();
                proc.id = cursor.getInt(0);
                proc.name = cursor.getString(1);
                proc.company = cursor.getString(2);
                proc.address = cursor.getString(3);
                proc.site_map = cursor.getString(4);
                proc.desc = cursor.getString(5);
                proc.sync = cursor.getString(6);
                arrayList.add(proc);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }

    //delete the note
    public void delete(String ID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_PROJECTS, "ID=" + ID, null);
        sqLiteDatabase.close();
    }

    //update the note
    public void updateProject(Project ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(KEY_PROJECT_NAME, ct.name);
        values.put(KEY_PROJECT_COMPANY, ct.company);
        values.put(KEY_PROJECT_ADDRESS, ct.address);
        values.put(KEY_PROJECT_DESC, ct.desc);
        values.put(KEY_PROJECT_SITEMAP, ct.site_map);
        values.put(KEY_PROJECT_SYNC, ct.sync);
        //updating row
        sqLiteDatabase.update(TABLE_PROJECTS, values, "ID=" + ct.id, null);
        sqLiteDatabase.close();
    }
}
