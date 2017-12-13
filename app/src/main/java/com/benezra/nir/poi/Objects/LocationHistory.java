package com.benezra.nir.poi.Objects;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by nirb on 13/12/2017.
 */

public class LocationHistory {

    private List<Double> location;
    private Long date;


    public LocationHistory(List<Double> location, Long date) {
        this.location = location;
        this.date = date;
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
