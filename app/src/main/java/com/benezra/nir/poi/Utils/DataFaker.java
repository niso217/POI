package com.benezra.nir.poi.Utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Helper.RetrieveInterestsImagesTask;
import com.benezra.nir.poi.Helper.RetrieveInterestsTask;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Helper.AsyncGeocoder;
import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.Objects.User;
import com.benezra.nir.poi.R;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.benezra.nir.poi.Interface.Constants.ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.DETAILS;
import static com.benezra.nir.poi.Interface.Constants.END;
import static com.benezra.nir.poi.Interface.Constants.ID;
import static com.benezra.nir.poi.Interface.Constants.IMAGE;
import static com.benezra.nir.poi.Interface.Constants.INTEREST;
import static com.benezra.nir.poi.Interface.Constants.LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.OWNER;
import static com.benezra.nir.poi.Interface.Constants.PARTICIPATES;
import static com.benezra.nir.poi.Interface.Constants.START;
import static com.benezra.nir.poi.Interface.Constants.STATUS;
import static com.benezra.nir.poi.Interface.Constants.TITLE;

/**
 * Created by nirb on 06/11/2017.
 */

public class DataFaker extends AppCompatActivity implements
        MapFragment.MapFragmentCallback, RetrieveInterestsTask.AsyncResponse,
        PlaceSelectionListener {


    ArrayList<EventsInterestData> mInterestData;
    Random mRandom;
    MapFragment mapFragment;
    final static String TAG = DataFaker.class.getSimpleName();
    LatLng mCurrentPin;
    PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    GoogleMap mMap;
    int mRadius = 1;
    int mNumber = 1;
    private DatabaseReference mFirebaseEventPicReference;
    private Element nextsib;
    List<String> images;
    EventsInterestData temp;
    private Button start, stop;
    private ProgressBar mProgressBar;
    private ImageView imageView;
    private TextView textview;
    private RetrieveInterestsTask mRetrieveFeedTask;
    private RetrieveInterestsImagesTask mRetrieveInterestsImagesTask;
    private static int MIN_YEAR = 2018;
    private static int MIN_MONTH = 4;
    private static int MIN_DAY = 1;


    Handler handler;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_faker);
        mInterestData = new ArrayList<>();
        handler = new Handler();
        mRandom = new Random();
        images = new ArrayList<>();

        start = findViewById(R.id.btn_start);
        stop = findViewById(R.id.btn_stop);
        mProgressBar = findViewById(R.id.pb_interest);
        textview = findViewById(R.id.tv_interest_name);


        imageView = findViewById(R.id.interest_image);

        mCurrentPin = new LatLng(32.0852999, 34.78176759999997);
        Button btn = (Button) findViewById(R.id.btn_go);
        addEventChangeListener();


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRetrieveFeedTask = new RetrieveInterestsTask(DataFaker.this, imageView, mProgressBar, textview,DataFaker.this);
                mRetrieveFeedTask.execute();

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRetrieveFeedTask != null)
                    mRetrieveFeedTask.cancel(true);
                if (mRetrieveInterestsImagesTask != null)
                    mRetrieveInterestsImagesTask.cancel(true);

            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < mNumber; i++) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LatLng latLng = LocationUtil.getLocationInLatLngRad(mRadius * 1000, mCurrentPin);
                            getAddress(latLng);


                        }
                    }, 1000);
                }
                Toast.makeText(DataFaker.this, "finished creating events", Toast.LENGTH_LONG).show();


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

    private Runnable periodicUpdate = new Runnable() {
        public void run() {
            // scheduled another events to be in 10 seconds later
            handler.postDelayed(periodicUpdate, 1 * 1000); //milliseconds);
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
                Calendar cal = randomStartDate();
                event.setStart(cal.getTimeInMillis());
                event.setEnd(randomEndDate(cal.getTimeInMillis()));
                int rand = mRandom.nextInt(mInterestData.size());
                event.setInterest(mInterestData.get(rand).getInterest());
                event.setImage(mInterestData.get(rand).getImage());
                event.setTitle(mInterestData.get(rand).getTitle());
                event.setDetails(mInterestData.get(rand).getDetails());
                event.setOwner(UUID.randomUUID().toString());
                event.setStatus(true);
                save(event);

            }
        }).execute(new AsyncGeocoder.AsyncGeocoderObject(
                new Geocoder(this), LatLongToLocation(latLng)));
    }

    private Location LatLongToLocation(LatLng latLng) {
        Location loc = new Location("");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private void save(Event event) {
        DatabaseReference eventReference = FirebaseDatabase.getInstance().getReference("events").child(event.getId());


        GeoHash geoHash = new GeoHash(new GeoLocation(event.getLatitude(), event.getLongitude()));
        Map<String, Object> updates = new HashMap<>();

        Map<String, User> map = setOwnerAsParticipate();
        updates.put(PARTICIPATES, map);

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
        updates.put(STATUS, event.isStatus());
        updates.put(OWNER, "9bZ1hOMJlYcmXgMfHQXmsuJONoz1");

        updates.put("/g", geoHash.getGeoHashString());
        updates.put("/l", Arrays.asList(event.getLatitude(), event.getLongitude()));
        eventReference.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            }
        });

    }

    private Map<String, User> setOwnerAsParticipate() {
        User owner = new User();
        owner.setName("Shara");
        owner.setEmail("sara@gmail.com");
        owner.setAvatar("https://i.pinimg.com/736x/d0/b1/c1/d0b1c100c871ee188bfb7e6357c61a38--profile-photography-white-photography.jpg");
        HashMap<String, User> map = new HashMap<>();
        map.put("9bZ1hOMJlYcmXgMfHQXmsuJONoz1", owner);
        return map;
    }




    private Calendar randomStartDate() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR, new Random().nextInt(20 - 1) + 1);
        Log.d(TAG,"Start : " + DateUtil.CalendartoDate(now.getTime()) + " " + DateUtil.CalendartoTime(now.getTime()));
        return now;
    }

    private long randomEndDate(long cal) {
        Calendar newCal = Calendar.getInstance();
        newCal.setTimeInMillis(cal);
        newCal.add(Calendar.DAY_OF_YEAR, new Random().nextInt(6 - 1) + 1);
        Log.d(TAG,"End : " + DateUtil.CalendartoDate(newCal.getTime()) + " " + DateUtil.CalendartoTime(newCal.getTime()));
        return newCal.getTimeInMillis();
    }


    private void addEventChangeListener() {

        Query query = FirebaseDatabase.getInstance().getReference("interests_data");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        EventsInterestData event_data = data.getValue(EventsInterestData.class);
                        if (event_data != null)
                            mInterestData.add(event_data);
                    }
                    Toast.makeText(DataFaker.this, "Good to go", Toast.LENGTH_LONG).show();

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
    public void onDistanceChanged(String add) {

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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }




    @Override
    public void processFinish(boolean output, Map<String, Object> list) {
        if (output) {
            // Do something awesome here
            Toast.makeText(this, "Task completed uploading to firebase", Toast.LENGTH_SHORT).show();
            mProgressBar.setProgress(0);
            mRetrieveInterestsImagesTask = new RetrieveInterestsImagesTask(this, imageView, mProgressBar, textview);
            mRetrieveInterestsImagesTask.execute(list);
        } else {
            Toast.makeText(this, "Task failed, network issue", Toast.LENGTH_SHORT).show();
        }
    }
}


