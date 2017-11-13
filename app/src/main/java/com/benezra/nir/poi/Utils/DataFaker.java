package com.benezra.nir.poi.Utils;

import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.benezra.nir.poi.Activity.CreateEventActivity;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Helper.AsyncGeocoder;
import com.benezra.nir.poi.Objects.InterestData;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.User;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
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

public class DataFaker extends AppCompatActivity implements
        MapFragment.MapFragmentCallback,
        PlaceSelectionListener {



    ArrayList<InterestData> mInterestData;
    Random mRandom;
    MapFragment mapFragment;
    final static String TAG = DataFaker.class.getSimpleName();
    LatLng mCurrentPin;
    PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    GoogleMap mMap;
    int mRadius = 1;
    int mNumber = 1;

    Handler handler;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_faker);
        mInterestData = new ArrayList<>();
        handler = new Handler();
        mRandom = new Random();
        mCurrentPin  = new LatLng(32.0852999,34.78176759999997);
        Button btn = (Button) findViewById(R.id.btn_go);
        addEventChangeListener();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < mNumber; i++) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LatLng latLng = LocationUtil.getLocationInLatLngRad(mRadius * 1000,mCurrentPin);
                            getAddress(latLng);


                        }
                    },1000);
                }
                Toast.makeText(DataFaker.this,"finished creating events",Toast.LENGTH_LONG).show();


            }
        });

        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.class.getSimpleName());
        if (mapFragment == null) {
            Log.d(TAG, "map fragment null");
            mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mapFragment, MapFragment.class.getSimpleName()).commit();
        }

        if (mPlaceAutocompleteFragment == null) {
            mPlaceAutocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

            mPlaceAutocompleteFragment.setOnPlaceSelectedListener(this);
        }

        NumberPicker radius = (NumberPicker) findViewById(R.id.np_radius);
        radius.setMinValue(1);
        radius.setMaxValue(100);
        radius.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mRadius = newVal;
            }
        });

        NumberPicker number = (NumberPicker) findViewById(R.id.np_number);
        number.setMinValue(1);
        number.setMaxValue(100);
        number.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mNumber = newVal;
            }
        });

    }

    private Runnable periodicUpdate = new Runnable () {
        public void run() {
            // scheduled another events to be in 10 seconds later
            handler.postDelayed(periodicUpdate, 1*1000); //milliseconds);
                    // below is whatever you want to do

        }
    };


    public void getAddress(LatLng latLng) {

        final Event event = new Event();
        event.setLatLang(latLng);

        new AsyncGeocoder(new AsyncGeocoder.onAddressFoundListener() {
            @Override
            public void onAddressFound(String result) {
                event.setAddress(result);
                event.setId(UUID.randomUUID().toString());
                event.setStart(randomDate());
                int rand = mRandom.nextInt(mInterestData.size());
                event.setInterest(mInterestData.get(rand).getInterest());
                event.setImage(mInterestData.get(rand).getImage());
                event.setTitle(mInterestData.get(rand).getTitle());
                event.setDetails(mInterestData.get(rand).getDetails());
                event.setOwner(UUID.randomUUID().toString());
                save(event);

            }
        }).execute(new AsyncGeocoder.AsyncGeocoderObject(
                new Geocoder(this), LatLongToLocation(latLng)));
    }

    private Location LatLongToLocation(LatLng latLng){
        Location loc = new Location("");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private void save(Event event){
        DatabaseReference eventReference = FirebaseDatabase.getInstance().getReference("events").child(event.getId());


        GeoHash geoHash = new GeoHash(new GeoLocation(event.getLatitude(), event.getLongitude()));
        Map<String, Object> updates = new HashMap<>();

        updates.put(ID, event.getId());
        updates.put(DETAILS, event.getDetails());
        updates.put(START, event.getStart());
        updates.put(END, event.getEnd());
        updates.put(IMAGE, event.getImage());
        updates.put(LATITUDE, event.getLatitude());
        updates.put(LONGITUDE, event.getLongitude());
        updates.put(TITLE, event.getTitle());
        updates.put(INTEREST, event.getInterest());
        updates.put(ADDRESS, event.getAddress());
        updates.put(OWNER, event.getOwner());

        updates.put("/g", geoHash.getGeoHashString());
        updates.put("/l", Arrays.asList(event.getLatitude(), event.getLongitude()));
        eventReference.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            }
        });

    }


    private long randomDate(){
        Calendar now = Calendar.getInstance();
        Calendar min = Calendar.getInstance();
        Calendar randomDate = (Calendar) now.clone();
        int minYear = 2017;
        int minMonth = 12;
        int minDay = 1;
        min.set(minYear, minMonth-1, minDay);
        int numberOfDaysToAdd = (int) (Math.random() * (daysBetween(min, now) + 1));
        randomDate.add(Calendar.DAY_OF_YEAR, -numberOfDaysToAdd);

        return randomDate.getTimeInMillis();
    }

    public static int daysBetween(Calendar from, Calendar to) {
        Calendar date = (Calendar) from.clone();
        int daysBetween = 0;
        while (date.before(to)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        System.out.println(daysBetween);
        return daysBetween;
    }


    private void addEventChangeListener() {

            Query query = FirebaseDatabase.getInstance().getReference("interests_data");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // dataSnapshot is the "issue" node with all children with id 0
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            InterestData event_data = data.getValue(InterestData.class);
                            if (event_data!=null)
                                mInterestData.add(event_data);
                        }
                        Toast.makeText(DataFaker.this,"Good to go",Toast.LENGTH_LONG).show();

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onEventLocationChanged(LatLng latLng, String address) {
        mCurrentPin = latLng;
    }

    @Override
    public void LocationPermission() {

    }

    @Override
    public void onPlaceSelected(Place place) {
        mCurrentPin = place.getLatLng();

        MarkerOptions markerOptions = new MarkerOptions()
                .position(mCurrentPin).title(place.getAddress().toString()).draggable(true);

        Marker marker = mMap.addMarker(markerOptions);

        CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(mCurrentPin, 15);
        mMap.moveCamera(loc);
    }

    @Override
    public void onError(Status status) {

    }
}


