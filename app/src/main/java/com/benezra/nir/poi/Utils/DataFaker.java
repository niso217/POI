package com.benezra.nir.poi.Utils;

import android.location.Location;
import android.widget.Toast;

import com.benezra.nir.poi.Activity.CreateEventActivity;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.User;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.benezra.nir.poi.Helper.Constants.ADDRESS;
import static com.benezra.nir.poi.Helper.Constants.DETAILS;
import static com.benezra.nir.poi.Helper.Constants.END;
import static com.benezra.nir.poi.Helper.Constants.ID;
import static com.benezra.nir.poi.Helper.Constants.IMAGE;
import static com.benezra.nir.poi.Helper.Constants.INTEREST;
import static com.benezra.nir.poi.Helper.Constants.LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.OWNER;
import static com.benezra.nir.poi.Helper.Constants.PARTICIPATES;
import static com.benezra.nir.poi.Helper.Constants.START;
import static com.benezra.nir.poi.Helper.Constants.TITLE;

/**
 * Created by nirb on 06/11/2017.
 */

public class DataFaker {

    private void saveEventToFirebase() {


        Event mCurrentEvent  = new Event();
        mCurrentEvent.setLatLang(LocationUtil.getLocationInLatLngRad(50000,new LatLng(32.0852999,34.78176759999997)));
        mCurrentEvent.setId(UUID.randomUUID().toString());
        mCurrentEvent.setDetails("");

        DatabaseReference eventReference = FirebaseDatabase.getInstance().getReference("events").child(UUID.randomUUID().toString());


        GeoHash geoHash = new GeoHash(new GeoLocation(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude()));
        Map<String, Object> updates = new HashMap<>();

        updates.put(ID, mCurrentEvent.getId());
        updates.put(DETAILS, mCurrentEvent.getDetails());
        updates.put(START, mCurrentEvent.getStart());
        updates.put(END, mCurrentEvent.getEnd());
        updates.put(IMAGE, mCurrentEvent.getImage());
        updates.put(LATITUDE, mCurrentEvent.getLatitude());
        updates.put(LONGITUDE, mCurrentEvent.getLongitude());
        updates.put(TITLE, mCurrentEvent.getTitle());
        updates.put(INTEREST, mCurrentEvent.getInterest());
        updates.put(ADDRESS, mCurrentEvent.getAddress());
        updates.put(OWNER, mCurrentEvent.getOwner());

        updates.put("/g", geoHash.getGeoHashString());
        updates.put("/l", Arrays.asList(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude()));
        eventReference.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //finish();
            }
        });

    }

    String [] Deatils = new String [] {
            "",
            "",
            
    };


}


