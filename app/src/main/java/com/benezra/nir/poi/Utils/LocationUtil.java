package com.benezra.nir.poi.Utils;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * Created by nirb on 06/11/2017.
 */

public class LocationUtil {

    static Random  random;

    protected static LatLng getLocationInLatLngRad(double radiusInMeters, LatLng currentLocation) {
        double x0 = currentLocation.longitude;
        double y0 = currentLocation.latitude;

        if (random==null)
        random = new Random();

        // Convert radius from meters to degrees.
        double radiusInDegrees = radiusInMeters / 111320f;

        // Get a random distance and a random angle.
        double u = random.nextDouble();
        double v = random.nextDouble();

        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        // Get the x and y delta values.
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Compensate the x value.
        double new_x = x / Math.cos(Math.toRadians(y0));

        double foundLatitude;
        double foundLongitude;

        foundLatitude = y0 + y;
        foundLongitude = x0 + new_x;


        return new LatLng(foundLatitude,foundLongitude);
    }
}
