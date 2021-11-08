package com.viact.viact_android.utils;

import android.os.Environment;

import java.io.File;

public class Const {
    public static final String APP_NAME_REF = "viact_android";

    public static final String EXT_STORAGE_DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator;
    public static final String EXT_STORAGE_APP_PATH = EXT_STORAGE_DOC_PATH + APP_NAME_REF + File.separator;
    public static final String EXT_STORAGE_VIDEO_PATH = EXT_STORAGE_APP_PATH + "video" + File.separator;
    public static final String EXT_STORAGE_PHOTO_PATH = EXT_STORAGE_APP_PATH + "photo" + File.separator;
    public static final String EXT_STORAGE_SPOT_PATH  = EXT_STORAGE_APP_PATH + "spots" + File.separator;
    public static final String EXT_STORAGE_SHEET_PATH = EXT_STORAGE_APP_PATH + "sheets" + File.separator;

    public static final String SITE_MAP_URL = "https://customindz-shinobi.s3.ap-southeast-1.amazonaws.com/cbimage.png";

    public final static int PIN_SIZE_MIN_LEN                      = 48;
    public final static int PIN_SIZE_MAX_LEN                      = 112;

    public final static int ACTIVE_MAIN_PAGE                      = 0;
    public final static int ACTIVE_OTHER_PAGE                     = 1;

    public final static int CONNECT_MODE_NONE                     = 0;
    public final static int CONNECT_MODE_WIFI                     = 1;
    public final static int CONNECT_MODE_USB                      = 2;

    public final static int SHEET_TYPE_NORMAL                     = 0;
    public final static int SHEET_TYPE_GMAP                       = 1;
    public final static int SHEET_TYPE_NO_FILE                    = 2;
    //Scene Media Type
    public final static int SCENE_MEDIA_PHOTO_360                = 0;
    public final static int SCENE_MEDIA_PHOTO_BUILT              = 1;
    public final static int SCENE_MEDIA_VIDEO_360                = 2;
    public final static int SCENE_MEDIA_VIDEO_BUILT              = 3;
    //Scene Edit Type
    public final static int SCENE_EDIT_NONE                      = 0;
    public final static int SCENE_EDIT_MARKUP                    = 1;
    public final static int SCENE_EDIT_IMAGE                     = 2;
    //Camera Type
    public final static int CAMERA_BUILT_IN                        = 1;
    public final static int CAMERA_360                             = 2;

}
