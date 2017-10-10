package com.benezra.nir.poi.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nir on 09/10/2017.
 */

public class SharePref {
    private static SharePref sharePref = new SharePref();
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private static final String IMAGE = "image";

    private SharePref() {} //prevent creating multiple instances by making the constructor private

    //The context passed into the getInstance should be application level context.
    public static SharePref getInstance(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        return sharePref;
    }

    public void saveImage(String image) {
        editor.putString(IMAGE, image);
        editor.commit();
    }

    public String getImage() {
        return sharedPreferences.getString(IMAGE, "");
    }

    public void removeImage() {
        editor.remove(IMAGE);
        editor.commit();
    }

    public void clearAll() {
        editor.clear();
        editor.commit();
    }

}