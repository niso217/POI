package com.benezra.nir.poi.Helper;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nirb on 18/10/2017.
 */

public class AsyncGeocoder extends AsyncTask<AsyncGeocoder.AsyncGeocoderObject, Void, List<Address>> {


    private onAddressFoundListener listener;


    // getting a listener instance from the constructor
    public AsyncGeocoder(onAddressFoundListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<Address> doInBackground(AsyncGeocoderObject... asyncGeocoderObjects) {
        List<Address> addresses = null;
        AsyncGeocoderObject asyncGeocoderObject = asyncGeocoderObjects[0];
        try {
            addresses = asyncGeocoderObject.geocoder.getFromLocation(asyncGeocoderObject.location.getLatitude(),
                    asyncGeocoderObject.location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) {
        Log.v("onPostExecute", "location: " + addresses);
        String address;
        if (addresses != null && addresses.size()>0){
            List<String> addr = new ArrayList<>();
            if(addresses.get(0).getLocality() != null) addr.add(addresses.get(0).getLocality());
            if(addresses.get(0).getCountryName() != null) addr.add(addresses.get(0).getCountryName());
            if(addresses.get(0).getThoroughfare() != null) addr.add(addresses.get(0).getThoroughfare());

            address = android.text.TextUtils.join(",", addr);
        }
        else address = "Service unavailable.";

        listener.onAddressFound(address);

    }

     public static class AsyncGeocoderObject {

        public Location location; // location to get address from
        Geocoder geocoder; // the geocoder

        public AsyncGeocoderObject(Geocoder geocoder, Location location) {
            this.geocoder = geocoder;
            this.location = location;
        }
    }

    public interface onAddressFoundListener {
        void onAddressFound(String result);
    }
}