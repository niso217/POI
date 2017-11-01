package com.benezra.nir.poi.Fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Adapter.CategoryAdapter;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.EventModel;
import com.benezra.nir.poi.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ID;
import static com.benezra.nir.poi.Helper.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Helper.Constants.EVENT_START;
import static com.benezra.nir.poi.Helper.Constants.EVENT_TITLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventByInterestFragment extends Fragment
        implements ValueEventListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private EventModel mEventModel;
    private ArrayList<Event> mEventList;
    private FirebaseDatabase mFirebaseInstance;
    private CategoryAdapter mCategoryAdapter;
    private ListView mListView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private FirebaseUser mFirebaseUser;
    final static String TAG = EventByInterestFragment.class.getSimpleName();
    private HashMap<String, Event> mEventHashMap;
    private GoogleMap mMap;
    private MapView mMapView;
    List<MarkerOptions> markers = new ArrayList<MarkerOptions>();
    private Event mCurrentSelectedEvent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventModel = new EventModel();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventList = new ArrayList<>();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance.getReference().keepSynced(true);
        mEventHashMap = new HashMap<>();


    }

    private void initFusedLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            //addEventChangeListener();
                            initGeoFire();
                        }
                    }
                });
    }


    private void addEventChangeListener() {
        String[] temp = new String[]{"Dance", "Swim", "Geocaching"};

        for (int i = 0; i < temp.length; i++) {
            Query query = mFirebaseInstance.getReference("events").orderByChild("interest").equalTo(temp[i]);
            query.addValueEventListener(this);

        }

    }


    public EventByInterestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

    }

    private void addEventsToMap() {

        mMap.clear();
        Iterator it = mEventHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Event event = (Event) pair.getValue();
        }
    }


    private void addSingeMarkerToMap(String id, GeoLocation location) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(location.latitude, location.longitude));
        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag(id);
        mEventHashMap.put(id, new Event(id, location, marker));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);


        // Create an {@link CategoryAdapter}, whose data source is a list of
        // {@link Categories}. The adapter knows how to create list item views for each item
        // in the list.
        mCategoryAdapter = new CategoryAdapter(getActivity(), mEventList, mFirebaseUser.getPhotoUrl().toString());

        // Get a reference to the ListView, and attach the adapter to the listView.
        mListView = (ListView) rootView.findViewById(R.id.list);
        mListView.setAdapter(mCategoryAdapter);


        //Set a click listener on that View
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {

                // Get the Category object at the given position the user clicked on
                Event event = mEventList.get(position);


                Intent userEvent = new Intent(getActivity(), ViewEventActivity.class);
                userEvent.putExtra(EVENT_ID, event.getId());
                userEvent.putExtra(EVENT_TITLE, event.getTitle());
                userEvent.putExtra(EVENT_OWNER, event.getOwner());
                userEvent.putExtra(EVENT_IMAGE, event.getImage());
                userEvent.putExtra(EVENT_DETAILS, event.getDetails());
                userEvent.putExtra(EVENT_LATITUDE, event.getLatitude());
                userEvent.putExtra(EVENT_LONGITUDE, event.getLongitude());
                userEvent.putExtra(EVENT_INTEREST, event.getInterest());
                userEvent.putExtra(EVENT_START, event.getStart());
                userEvent.putExtra(EVENT_ADDRESS, event.getAddress());


                startActivity(userEvent);
            }
        });

        //initFusedLocation();
        if (savedInstanceState == null)
            navigateToCaptureFragment(new String[]{ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

        return rootView;
    }

    private void initGeoFire() {
        HashSet setEvents = new HashSet();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("events");
        GeoFire geoFire = new GeoFire(ref);
        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 100);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
//                String[] tokens = key.split("\\_");
//                String interest = tokens[0];
//                String id = tokens[1];
//                String title = tokens[2];
//
                Event event = mEventHashMap.get(key);
                if (event == null) {
                    addSingeMarkerToMap(key, location);
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, String.format("Key %s is no longer in the search area", key));

                Event event = mEventHashMap.get(key);
                if (event!=null){
                    Marker marker = event.getMarker();
                    marker.remove();
                }

                mEventHashMap.remove(key);
                //addEventsToMap();

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(TAG, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                Event event = mEventHashMap.get(key);
                Marker marker = event.getMarker();
                marker.remove();
                addSingeMarkerToMap(key, location);
            }

            @Override
            public void onGeoQueryReady() {
                //addEventsToMap();
                Log.d(TAG, "All initial data has been loaded and events have been fired!");
                //addEventsToMap();

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            Event event = dataSnapshot.getValue(Event.class);
            if (!mFirebaseUser.getUid().equals(event.getOwner())) {  //skip events from owner
                event.setDistance(mLastLocation);
                event.setId(dataSnapshot.getKey());
                mEventModel.addEvent(dataSnapshot.getKey(), event);
            }
            mEventList = new ArrayList<>(mEventModel.getEvents().values());
            mCategoryAdapter.setItems(mEventList);

        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }


    @Override
    public void navigateToCaptureFragment(String[] permissions) {
        if (isPermissionGranted(permissions)) {
            initFusedLocation();
        } else {
            PermissionsDialogFragment permissionsDialogFragment = (PermissionsDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag(PermissionsDialogFragment.class.getName());
            if (permissionsDialogFragment == null) {
                Log.d(TAG, "opening dialog");
                permissionsDialogFragment = PermissionsDialogFragment.newInstance();
                permissionsDialogFragment.setPermissions(permissions);
                permissionsDialogFragment.show(getActivity().getSupportFragmentManager(), PermissionsDialogFragment.class.getName());

            }
        }
    }

    private boolean isPermissionGranted(String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(getContext(), permissions[i]) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {


        Intent userEvent = new Intent(getActivity(), ViewEventActivity.class);
        userEvent.putExtra(EVENT_ID, marker.getTag().toString());
        userEvent.putExtra(EVENT_TITLE, mCurrentSelectedEvent.getTitle());
        userEvent.putExtra(EVENT_OWNER, mCurrentSelectedEvent.getOwner());
        userEvent.putExtra(EVENT_IMAGE, mCurrentSelectedEvent.getImage());
        userEvent.putExtra(EVENT_DETAILS, mCurrentSelectedEvent.getDetails());
        userEvent.putExtra(EVENT_LATITUDE, mCurrentSelectedEvent.getLatitude());
        userEvent.putExtra(EVENT_LONGITUDE, mCurrentSelectedEvent.getLongitude());
        userEvent.putExtra(EVENT_INTEREST, mCurrentSelectedEvent.getInterest());
        userEvent.putExtra(EVENT_START, mCurrentSelectedEvent.getStart());
        userEvent.putExtra(EVENT_ADDRESS, mCurrentSelectedEvent.getAddress());


        startActivity(userEvent);


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Query query = mFirebaseInstance.getReference("events").child(marker.getTag().toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentSelectedEvent = dataSnapshot.getValue(Event.class);
                    marker.setTitle(mCurrentSelectedEvent.getTitle());
                    marker.showInfoWindow();
                    //startActivity(userEvent);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return false;
    }
}
