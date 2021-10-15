package com.viact.viact_android;

import android.app.Application;

import com.arashivision.sdkcamera.InstaCameraSDK;
import com.arashivision.sdkmedia.InstaMediaSDK;
import com.viact.viact_android.activities.StitchActivity;
import com.viact.viact_android.utils.AssetsUtil;

import java.io.File;

public class MyApp extends Application {

    private static MyApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        // Init SDK
        InstaCameraSDK.init(this);
        InstaMediaSDK.init(this);

        // Copy sample pictures from assets to local
        copyHdrSourceFromAssets();
    }

    private void copyHdrSourceFromAssets() {
        File dir = new File(StitchActivity.COPY_DIR);
        if (!dir.exists()) {
            AssetsUtil.copyFilesFromAssets(this, "hdr_source", dir.getAbsolutePath());
        }
    }

    public static MyApp getInstance() {
        return sInstance;
    }

}
