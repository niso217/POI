package com.benezra.nir.poi.Activity;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by brijesh on 15/4/17.
 */

public class Constants {


    public static final String GEOFENCE_ID_STAN_UNI = "STAN_UNI";
    public static final float GEOFENCE_RADIUS_IN_METERS = 100;

    /**
     * Map for storing information about stanford university in the Stanford.
     */
    public static final HashMap<String, LatLng> AREA_LANDMARKS = new HashMap<String, LatLng>();

    static {
        // stanford university.
        AREA_LANDMARKS.put(GEOFENCE_ID_STAN_UNI, new LatLng(32.4762935, 34.9477096));
    }
}
