package com.benezra.nir.poi.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by nirb on 06/11/2017.
 */

public class MapStateManager {

    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";
    private SharePref sharePref;


    public MapStateManager(Context context) {
         sharePref = SharePref.getInstance(context);
    }

    public void saveMapState(GoogleMap mapMie) {

        CameraPosition position = mapMie.getCameraPosition();

        sharePref.putFloat(LATITUDE, (float) position.target.latitude);
        sharePref.putFloat(LONGITUDE, (float) position.target.longitude);
        sharePref.putFloat(ZOOM, position.zoom);
        sharePref.putFloat(TILT, position.tilt);
        sharePref.putFloat(BEARING, position.bearing);
        sharePref.putInt(MAPTYPE, mapMie.getMapType());
    }

    public CameraPosition getSavedCameraPosition() {
        double latitude = sharePref.getFloat(LATITUDE, 0);
        if (latitude == 0) {
            return null;
        }
        double longitude = sharePref.getFloat(LONGITUDE, 0);
        LatLng target = new LatLng(latitude, longitude);

        float zoom = sharePref.getFloat(ZOOM, 0);
        float bearing = sharePref.getFloat(BEARING, 0);
        float tilt = sharePref.getFloat(TILT, 0);

        CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
        return position;
    }

    public int getSavedMapType() {
        return sharePref.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
    }
}
