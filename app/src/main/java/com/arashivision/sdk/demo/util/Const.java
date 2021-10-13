package com.arashivision.sdk.demo.util;

import android.os.Environment;

import java.io.File;

public class Const {
    public static final String EXT_STORAGE_DOC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator;

    public final static int PIN_SIZE_MIN_LEN                      = 48;
    public final static int PIN_SIZE_MAX_LEN                      = 96;

    public final static int ACTIVE_MAIN_PAGE                      = 0;
    public final static int ACTIVE_OTHER_PAGE                     = 1;

    public final static int CONNECT_MODE_NONE                     = 0;
    public final static int CONNECT_MODE_WIFI                     = 1;
    public final static int CONNECT_MODE_USB                      = 2;

}
