package com.arashivision.sdk.demo.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveSharedPrefrence {
    SharedPreferences sharedPreferences;

    public static final String PREFS_NAME = "Viact_Demo";
    public static final String PREFS_AUTH_TOKEN = "auth_token";
    public static final String PREFS_COMPANY_CODE = "company_code";

    public String getString(Context context, String key) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(key, "");

        return status;
    }

    public void putString(Context context, String key, String value)
    {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void clear(Context context)
    {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void DeletePrefrence(Context context) {

        sharedPreferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }
}