package com.benezra.nir.poi.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by nir on 09/10/2017.
 */

public class SharePref {
    private static SharePref sharePref = new SharePref();
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences defaultPreferences;
    private static SharedPreferences.Editor editor;



    private SharePref() {} //prevent creating multiple instances by making the constructor private

    //The context passed into the getInstance should be application level context.
    public static SharePref getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
            defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            editor = sharedPreferences.edit();
        }
        return sharePref;
    }

    public int getDefaultRadiusgetDefaultRadius(){
        return defaultPreferences.getInt("key_radius",10);
    }

    public void putFloat(String key, float value){
        editor.putFloat(key,value);
        editor.commit();
    }

    public void putInt(String key, int value){
        editor.putInt(key,value);
        editor.commit();
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }


    public boolean getBoolean(String key,boolean defValue) {
        return sharedPreferences.getBoolean(key, defValue);
    }

    public int getInt(String key,int defValue) {
        return sharedPreferences.getInt(key,defValue);
    }

    public String getString(String key,String defValue) {
        return sharedPreferences.getString(key,defValue);
    }

    public float getFloat(String key, float defValue) {
        return sharedPreferences.getFloat(key,defValue);
    }

}