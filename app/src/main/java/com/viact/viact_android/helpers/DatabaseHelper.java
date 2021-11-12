package com.viact.viact_android.helpers;

import static com.viact.viact_android.utils.Const.APP_NAME_REF;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;
import android.graphics.RectF;


import com.viact.viact_android.models.InsImg;
import com.viact.viact_android.models.Markup;
import com.viact.viact_android.models.PinPoint;
import com.viact.viact_android.models.Project;
import com.viact.viact_android.models.Sheet;
import com.viact.viact_android.models.SpotPhoto;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    //database name
    public static final String DATABASE_NAME = APP_NAME_REF;
    //database version
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_PROJECTS = "tbl_projects";
    public static final String TABLE_SHEETS = "tbl_sheets";
    public static final String TABLE_PINS = "tbl_pins";
    public static final String TABLE_SPOTS = "tbl_spots";
    public static final String TABLE_MARKUPS = "tbl_markups";
    public static final String TABLE_INS_IMGS = "tbl_ins_imgs";

    // projects Table Columns
    private static final String KEY_PROJECT_ID = "ID";
    private static final String KEY_PROJECT_NAME = "name";
    private static final String KEY_PROJECT_ADDRESS = "address";
    private static final String KEY_PROJECT_NOTE = "note";
    private static final String KEY_PROJECT_SYNC = "sync";
    private static final String KEY_PROJECT_CREATE = "create_time";
    private static final String KEY_PROJECT_UPDATE = "update_time";

    // sheets Table Columns
    private static final String KEY_SHEET_ID = "ID";
    private static final String KEY_SHEET_PROJECT_ID = "project_id";
    private static final String KEY_SHEET_NAME = "name";
    private static final String KEY_SHEET_PATH = "path";
    private static final String KEY_SHEET_TYPE = "type";
    private static final String KEY_SHEET_LT   = "lt_loc";
    private static final String KEY_SHEET_RB   = "rb_loc";
    private static final String KEY_SHEET_CREATE = "create_time";
    private static final String KEY_SHEET_UPDATE = "update_time";

    // pins Table Columns
    private static final String KEY_PIN_ID = "ID";
    private static final String KEY_PIN_SHEET_ID = "sh_id";
    private static final String KEY_PIN_X = "x_loc";
    private static final String KEY_PIN_Y = "y_loc";
    private static final String KEY_PIN_NAME = "name";
    private static final String KEY_PIN_NOTE = "note";
    private static final String KEY_PIN_CREATE = "create_time";
    private static final String KEY_PIN_UPDATE = "update_time";

    // spots Table Columns
    private static final String KEY_SPOT_ID = "ID";
    private static final String KEY_SPOT_PIN_ID = "pin_id";
    private static final String KEY_SPOT_PATH = "path";
    private static final String KEY_SPOT_CREATE = "create_time";

    // markups Table Columns
    private static final String KEY_MARKUP_ID = "ID";
    private static final String KEY_MARKUP_PHOTO_ID = "photo_id";
    private static final String KEY_MARKUP_NAME = "name";
    private static final String KEY_MARKUP_NOTE = "note";
    private static final String KEY_MARKUP_LEFT = "lt";
    private static final String KEY_MARKUP_TOP = "tp";
    private static final String KEY_MARKUP_RIGHT = "rt";
    private static final String KEY_MARKUP_BOTTOM = "bt";
    private static final String KEY_MARKUP_CREATE = "create_time";
    private static final String KEY_MARKUP_UPDATE = "update_time";

    // images Table Columns
    private static final String KEY_INS_IMG_ID = "ID";
    private static final String KEY_INS_IMG_PHOTO_ID = "photo_id";
    private static final String KEY_INS_IMG_PATH = "name";
    private static final String KEY_INS_IMG_X = "x_loc";
    private static final String KEY_INS_IMG_Y = "y_loc";
    private static final String KEY_INS_IMG_CREATE = "create_time";
    private static final String KEY_INS_IMG_UPDATE = "update_time";


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
                + KEY_PROJECT_ADDRESS + " TEXT, "
                + KEY_PROJECT_NOTE + " TEXT, "
                + KEY_PROJECT_SYNC + " TEXT, "
                + KEY_PROJECT_CREATE + " TEXT, "
                + KEY_PROJECT_UPDATE + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_SHEETS + " (" + KEY_SHEET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_SHEET_PROJECT_ID + " TEXT, "
                + KEY_SHEET_NAME + " TEXT, "
                + KEY_SHEET_PATH + " TEXT, "
                + KEY_SHEET_TYPE + " INTEGER, "
                + KEY_SHEET_LT + " TEXT, "
                + KEY_SHEET_RB + " TEXT, "
                + KEY_SHEET_CREATE + " TEXT, "
                + KEY_SHEET_UPDATE + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_PINS + " (" + KEY_PIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_PIN_SHEET_ID + " TEXT, "
                + KEY_PIN_X + " TEXT, "
                + KEY_PIN_Y + " TEXT, "
                + KEY_PIN_NAME + " TEXT, "
                + KEY_PIN_NOTE + " TEXT, "
                + KEY_PIN_CREATE + " TEXT, "
                + KEY_PIN_UPDATE + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_SPOTS + " (" + KEY_SPOT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_SPOT_PIN_ID + " TEXT, "
                + KEY_SPOT_PATH + " TEXT, "
                + KEY_SPOT_CREATE + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_MARKUPS + " (" + KEY_MARKUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_MARKUP_PHOTO_ID + " TEXT, "
                + KEY_MARKUP_NAME + " TEXT, "
                + KEY_MARKUP_NOTE + " TEXT, "
                + KEY_MARKUP_LEFT + " TEXT, "
                + KEY_MARKUP_TOP + " TEXT, "
                + KEY_MARKUP_RIGHT + " TEXT, "
                + KEY_MARKUP_BOTTOM + " TEXT, "
                + KEY_MARKUP_CREATE + " TEXT, "
                + KEY_MARKUP_UPDATE + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_INS_IMGS + " (" + KEY_INS_IMG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_INS_IMG_PHOTO_ID + " TEXT, "
                + KEY_INS_IMG_PATH + " TEXT, "
                + KEY_INS_IMG_X + " TEXT, "
                + KEY_INS_IMG_Y + " TEXT, "
                + KEY_INS_IMG_CREATE + " TEXT, "
                + KEY_INS_IMG_UPDATE + " TEXT);";
        db.execSQL(query);

    }

    //upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHEETS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PINS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOTS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKUPS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INS_IMGS + ";");

            onCreate(db);
        }
    }

    //insert image functions
    public void addInsImg(InsImg ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_INS_IMG_PHOTO_ID, ct.photo_id);
        values.put(KEY_INS_IMG_PATH, ct.path);
        values.put(KEY_INS_IMG_X, ct.point.x);
        values.put(KEY_INS_IMG_Y, ct.point.y);
        values.put(KEY_INS_IMG_CREATE, ct.create_time);
        values.put(KEY_INS_IMG_UPDATE, ct.update_time);
        //inserting new row
        sqLiteDatabase.insert(TABLE_INS_IMGS, null , values);
        //close database connection
        sqLiteDatabase.close();
    }

    public ArrayList<InsImg> getAllImgs(int photo_id) {
        ArrayList<InsImg> arrayList = new ArrayList<>();
        // select all query
        String select_query= "SELECT * FROM " + TABLE_INS_IMGS + " WHERE photo_id = " + photo_id + " ORDER BY create_time ASC;";

        SQLiteDatabase db = this .getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                InsImg sh = new InsImg();
                sh.id = cursor.getInt(0);
                sh.photo_id = cursor.getString(1);
                sh.path = cursor.getString(2);
                sh.point = new PointF();
                sh.point.x = Float.parseFloat(cursor.getString(3));
                sh.point.y = Float.parseFloat(cursor.getString(4));
                sh.create_time = cursor.getString(5);
                sh.update_time = cursor.getString(6);
                arrayList.add(sh);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }
    //update the image
    public void updateInsImg(InsImg ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(KEY_INS_IMG_PHOTO_ID, ct.photo_id);
        values.put(KEY_INS_IMG_PATH, ct.path);
        values.put(KEY_INS_IMG_X, ct.point.x);
        values.put(KEY_INS_IMG_Y, ct.point.y);
        values.put(KEY_INS_IMG_CREATE, ct.create_time);
        values.put(KEY_INS_IMG_UPDATE, ct.update_time);
        //updating row
        sqLiteDatabase.update(TABLE_INS_IMGS, values, "ID=" + ct.id, null);
        sqLiteDatabase.close();
    }
    //delete the image by id
    public void deleteInsImg(int mark_id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_INS_IMGS, "ID=" + mark_id, null);
        sqLiteDatabase.close();
    }
    //delete the image by photo_id
    public void deleteInsImgsByPhoto(int photo_id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_INS_IMGS, "photo_id=" + photo_id, null);
        sqLiteDatabase.close();
    }

    //markup functions
    public void addMarkup(Markup ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MARKUP_PHOTO_ID, ct.photo_id);
        values.put(KEY_MARKUP_NAME, ct.name);
        values.put(KEY_MARKUP_NOTE, ct.note);
        values.put(KEY_MARKUP_LEFT, ct.rc.left);
        values.put(KEY_MARKUP_TOP, ct.rc.top);
        values.put(KEY_MARKUP_RIGHT, ct.rc.right);
        values.put(KEY_MARKUP_BOTTOM, ct.rc.bottom);
        values.put(KEY_MARKUP_CREATE, ct.create_time);
        values.put(KEY_MARKUP_UPDATE, ct.update_time);
        //inserting new row
        sqLiteDatabase.insert(TABLE_MARKUPS, null , values);
        //close database connection
        sqLiteDatabase.close();
    }

    public ArrayList<Markup> getAllMarkups(int photo_id) {
        ArrayList<Markup> arrayList = new ArrayList<>();
        // select all query
        String select_query= "SELECT * FROM " + TABLE_MARKUPS + " WHERE photo_id = " + photo_id + " ORDER BY create_time ASC;";

        SQLiteDatabase db = this .getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Markup sh = new Markup();
                sh.id = cursor.getInt(0);
                sh.photo_id = cursor.getString(1);
                sh.name = cursor.getString(2);
                sh.note = cursor.getString(3);
                sh.rc = new RectF();
                sh.rc.left = Float.parseFloat(cursor.getString(4));
                sh.rc.top = Float.parseFloat(cursor.getString(5));
                sh.rc.right = Float.parseFloat(cursor.getString(6));
                sh.rc.bottom = Float.parseFloat(cursor.getString(7));
                sh.create_time = cursor.getString(8);
                sh.update_time = cursor.getString(9);
                arrayList.add(sh);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }
    //update the markup
    public void updateMarkup(Markup ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(KEY_MARKUP_PHOTO_ID, ct.photo_id);
        values.put(KEY_MARKUP_NAME, ct.name);
        values.put(KEY_MARKUP_NOTE, ct.note);
        values.put(KEY_MARKUP_LEFT, ct.rc.left);
        values.put(KEY_MARKUP_TOP, ct.rc.top);
        values.put(KEY_MARKUP_RIGHT, ct.rc.right);
        values.put(KEY_MARKUP_BOTTOM, ct.rc.bottom);
        values.put(KEY_MARKUP_CREATE, ct.create_time);
        values.put(KEY_MARKUP_UPDATE, ct.update_time);
        //updating row
        sqLiteDatabase.update(TABLE_MARKUPS, values, "ID=" + ct.id, null);
        sqLiteDatabase.close();
    }
    //delete the markup by id
    public void deleteMarkup(int mark_id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_MARKUPS, "ID=" + mark_id, null);
        sqLiteDatabase.close();
    }
    //delete the markups by photo_id
    public void deleteMarkupsByPhoto(int photo_id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_MARKUPS, "photo_id=" + photo_id, null);
        sqLiteDatabase.close();
    }

    //sheet functions
    public void addSheet(Sheet ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SHEET_PROJECT_ID, ct.pro_id);
        values.put(KEY_SHEET_NAME, ct.name);
        values.put(KEY_SHEET_PATH, ct.path);
        values.put(KEY_SHEET_TYPE, ct.type);
        values.put(KEY_SHEET_LT, ct.lt_loc);
        values.put(KEY_SHEET_RB, ct.rb_loc);
        values.put(KEY_SHEET_CREATE, ct.create_time);
        values.put(KEY_SHEET_UPDATE, ct.update_time);
        //inserting new row
        sqLiteDatabase.insert(TABLE_SHEETS, null , values);
        //close database connection
        sqLiteDatabase.close();
    }

    public boolean checkSheetName(int proc_id, String sh_name) {
        ArrayList<Sheet> arrayList = new ArrayList<>();
        // select all query
        String select_query= "SELECT * FROM " + TABLE_SHEETS + " WHERE project_id = " + proc_id + " AND name LIKE " + sh_name + ";";

        SQLiteDatabase db = this .getWritableDatabase();
        Cursor cursor = db.rawQuery(select_query, null);

        if (cursor == null) return false;
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Sheet sh = new Sheet();
                sh.id = cursor.getInt(0);
                sh.pro_id = cursor.getString(1);
                sh.name = cursor.getString(2);
                sh.path = cursor.getString(3);
                sh.type = cursor.getInt(4);
                sh.lt_loc = cursor.getString(5);
                sh.rb_loc = cursor.getString(6);
                sh.create_time = cursor.getString(7);
                sh.update_time = cursor.getString(8);
                arrayList.add(sh);
            }while (cursor.moveToNext());
        }
        if (arrayList.size() > 0){
            return true;
        } else {
            return false;
        }
    }

    public Sheet getSheet(int sh_id) {
        // select all query
        String select_query= "SELECT * FROM " + TABLE_SHEETS + " WHERE ID = " + sh_id + ";";

        SQLiteDatabase db = this .getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            Sheet sh = new Sheet();
            sh.id = cursor.getInt(0);
            sh.pro_id = cursor.getString(1);
            sh.name = cursor.getString(2);
            sh.path = cursor.getString(3);
            sh.type = cursor.getInt(4);
            sh.lt_loc = cursor.getString(5);
            sh.rb_loc = cursor.getString(6);
            sh.create_time = cursor.getString(7);
            sh.update_time = cursor.getString(8);
            return sh;
        }
        return null;
    }

    public ArrayList<Sheet> getAllSheets(int proc_id) {
        ArrayList<Sheet> arrayList = new ArrayList<>();
        // select all query
        String select_query= "SELECT * FROM " + TABLE_SHEETS + " WHERE project_id = " + proc_id + " ORDER BY create_time ASC;";

        SQLiteDatabase db = this .getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Sheet sh = new Sheet();
                sh.id = cursor.getInt(0);
                sh.pro_id = cursor.getString(1);
                sh.name = cursor.getString(2);
                sh.path = cursor.getString(3);
                sh.type = cursor.getInt(4);
                sh.lt_loc = cursor.getString(5);
                sh.rb_loc = cursor.getString(6);
                sh.create_time = cursor.getString(7);
                sh.update_time = cursor.getString(8);
                arrayList.add(sh);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }
    //update the sheet
    public void updateSheet(Sheet ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(KEY_SHEET_NAME, ct.name);
        values.put(KEY_SHEET_PATH, ct.path);
        values.put(KEY_SHEET_TYPE, ct.type);
        values.put(KEY_SHEET_LT, ct.lt_loc);
        values.put(KEY_SHEET_RB, ct.rb_loc);
        values.put(KEY_SHEET_CREATE, ct.create_time);
        values.put(KEY_SHEET_UPDATE, ct.update_time);
        //updating row
        sqLiteDatabase.update(TABLE_SHEETS, values, "ID=" + ct.id, null);
        sqLiteDatabase.close();
    }
    //delete the spot by id
    public void deleteSheet(int sheet_id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_SHEETS, "ID=" + sheet_id, null);
        sqLiteDatabase.close();
    }

    //add the new spot
    public void addSpot(SpotPhoto ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SPOT_PIN_ID, ct.pin_id);
        values.put(KEY_SPOT_PATH, ct.path);
        values.put(KEY_SPOT_CREATE, ct.create_time);
        //inserting new row
        sqLiteDatabase.insert(TABLE_SPOTS, null , values);
        //close database connection
        sqLiteDatabase.close();
    }

    //get the spots for pin
    public ArrayList<SpotPhoto> getAllSpots(int pin_id) {
        ArrayList<SpotPhoto> arrayList = new ArrayList<>();

        // select all query
        String select_query= "SELECT * FROM " + TABLE_SPOTS + " WHERE pin_id = " + pin_id + " ORDER BY create_time ASC;";

        SQLiteDatabase db = this .getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SpotPhoto spot = new SpotPhoto();
                spot.id = cursor.getInt(0);
                spot.pin_id = cursor.getString(1);
                spot.path = cursor.getString(2);
                spot.create_time = cursor.getString(3);
                arrayList.add(spot);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }
    //delete the spot by id
    public void deleteSpot(int spot_id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_SPOTS, "ID=" + spot_id, null);
        sqLiteDatabase.close();
    }

    //add the new pin
    public void addPin(PinPoint ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PIN_SHEET_ID, ct.sh_id);
        values.put(KEY_PIN_X, ct.x + "");
        values.put(KEY_PIN_Y, ct.y + "");
        values.put(KEY_PIN_NAME, ct.name);
        values.put(KEY_PIN_NOTE, ct.note);
        values.put(KEY_PIN_CREATE, ct.create_time);
        values.put(KEY_PIN_UPDATE, ct.update_time);
        //inserting new row
        sqLiteDatabase.insert(TABLE_PINS, null , values);
        //close database connection
        sqLiteDatabase.close();
    }

    //get the pin by id
    public PinPoint getPin(String pin_id) {
        PinPoint pin = new PinPoint();
        // select all query
        String select_query= "SELECT * FROM " + TABLE_PINS + " WHERE ID = " + pin_id + ";";

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            pin.id = cursor.getInt(0);
            pin.sh_id = cursor.getString(1);
            pin.x = Float.parseFloat(cursor.getString(2));
            pin.y = Float.parseFloat(cursor.getString(3));
            pin.name = cursor.getString(4);
            pin.note = cursor.getString(5);
            pin.create_time = cursor.getString(6);
            pin.update_time = cursor.getString(7);
            return pin;
        }
        return null;
    }

    //get the pins for project
    public ArrayList<PinPoint> getPinsForSheet(int sh_id) {
        ArrayList<PinPoint> arrayList = new ArrayList<>();
        // select all query
        String select_query= "SELECT * FROM " + TABLE_PINS + " WHERE sh_id = " + sh_id + ";";

        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PinPoint pin = new PinPoint();
                pin.id = cursor.getInt(0);
                pin.sh_id = cursor.getString(1);
                pin.x = Float.parseFloat(cursor.getString(2));
                pin.y = Float.parseFloat(cursor.getString(3));
                pin.name = cursor.getString(4);
                pin.note = cursor.getString(5);
                pin.create_time = cursor.getString(6);
                pin.update_time = cursor.getString(7);
                arrayList.add(pin);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }
    //update the note
    public void updatePin(PinPoint ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(KEY_PIN_SHEET_ID, ct.sh_id);
        values.put(KEY_PIN_X, ct.x + "");
        values.put(KEY_PIN_Y, ct.y + "");
        values.put(KEY_PIN_NAME, ct.name);
        values.put(KEY_PIN_NOTE, ct.note);
        values.put(KEY_PIN_CREATE, ct.create_time);
        values.put(KEY_PIN_UPDATE, ct.update_time);
        //updating row
        sqLiteDatabase.update(TABLE_PINS, values, "ID=" + ct.id, null);
        sqLiteDatabase.close();
    }
    //delete the pin
    public void deletePin(int ID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_PINS, "ID=" + ID, null);
        sqLiteDatabase.close();
    }

    //add the new project
    public void addProject(Project ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PROJECT_NAME, ct.name);
        values.put(KEY_PROJECT_ADDRESS, ct.address);
        values.put(KEY_PROJECT_NOTE, ct.note);
        values.put(KEY_PROJECT_SYNC, ct.sync);
        values.put(KEY_PROJECT_CREATE, ct.create_time);
        values.put(KEY_PROJECT_UPDATE, ct.update_time);
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

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(select_query, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Project proc = new Project();
                proc.id = cursor.getInt(0);
                proc.name = cursor.getString(1);
                proc.address = cursor.getString(2);
                proc.note = cursor.getString(3);
                proc.sync = cursor.getString(4);
                proc.create_time = cursor.getString(5);
                proc.update_time = cursor.getString(6);
                arrayList.add(proc);
            }while (cursor.moveToNext());
        }
        return arrayList;
    }

    //delete the note
    public void deleteProject(int ID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        //deleting row
        sqLiteDatabase.delete(TABLE_PROJECTS, "ID=" + ID, null);
        sqLiteDatabase.close();
    }

    //update the project
    public void updateProject(Project ct) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values =  new ContentValues();
        values.put(KEY_PROJECT_NAME, ct.name);
        values.put(KEY_PROJECT_ADDRESS, ct.address);
        values.put(KEY_PROJECT_NOTE, ct.note);
        values.put(KEY_PROJECT_SYNC, ct.sync);
        values.put(KEY_PROJECT_CREATE, ct.create_time);
        values.put(KEY_PROJECT_UPDATE, ct.update_time);
        //updating row
        sqLiteDatabase.update(TABLE_PROJECTS, values, "ID=" + ct.id, null);
        sqLiteDatabase.close();
    }
}
