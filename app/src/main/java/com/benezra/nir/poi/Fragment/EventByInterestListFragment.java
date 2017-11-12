package com.benezra.nir.poi.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Adapter.EventsAdapter;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.EventModel;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
public class EventByInterestListFragment extends Fragment implements

        RecyclerTouchListener.ClickListener,
        PermissionsDialogFragment.PermissionsGrantedCallback {

    private EventModel mEventModel;
    private FirebaseDatabase mFirebaseInstance;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private FirebaseUser mFirebaseUser;
    private FragmentDataCallBackInterface mListener;
    private Context mContext;
    private RecyclerView mEventsRecyclerView;
    private ArrayList<Event> mEventList;
    private EventsAdapter mEventsAdapter;
    private ItemTouchHelper mItemTouchHelper;
    final static String TAG = EventByInterestListFragment.class.getSimpleName();
    private List<String> mUserEvents;
    private List<String> mEventInterest;
    private Set<String> mEventHashSet;
    private FirebaseAuth mAuth;
    private String mSelectedInterest;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventModel = new EventModel();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance.getReference().keepSynced(true);
        mEventList = new ArrayList<>();
        mEventsAdapter = new EventsAdapter(getContext(), mEventList);
        mUserEvents = new ArrayList<>();
        mEventHashSet = new HashSet<>();
        mAuth = FirebaseAuth.getInstance();
        mSelectedInterest = getArguments().getString("interest");

        getAllUserEvents();

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof FragmentDataCallBackInterface) {
            mListener = (FragmentDataCallBackInterface) context;
        }
    }

    @Override
    public void onClick(View view, int position) {
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

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void navigateToCaptureFragment(String[] permissions) {
        if (isPermissionGranted(permissions)) {
            mListener.startLoadingData();
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



    public EventByInterestListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);


        mEventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.events_recycler_view);
        mEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEventsRecyclerView.setNestedScrollingEnabled(false);
        mEventsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mEventsRecyclerView, this));
        mEventsRecyclerView.setAdapter(mEventsAdapter);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("event_list", mEventList);
                EventByInterestMapFragment eventByInterestMapFragment = new EventByInterestMapFragment();
                eventByInterestMapFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.framelayout, eventByInterestMapFragment, EventByInterestMapFragment.class.getSimpleName()).addToBackStack(null).commit();

            }
        });

//        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mEventsAdapter);
//        mItemTouchHelper = new ItemTouchHelper(callback);
//        mItemTouchHelper.attachToRecyclerView(mEventsRecyclerView);


        return rootView;
    }


    private void initFusedLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            initGeoFire();
                        }
                    }
                });
    }


    private void initGeoFire() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("events");
        GeoFire geoFire = new GeoFire(ref);
        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 100);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                if (mUserEvents.contains(key)) return;

                mEventHashSet.add(key);


            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, String.format("Key %s is no longer in the search area", key));

                if (mUserEvents.contains(key)) return;

                if (mEventHashSet.contains(key))
                    mEventHashSet.remove(key);

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                if (mUserEvents.contains(key)) return;

                Log.d(TAG, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                mEventHashSet.add(key);
            }

            @Override
            public void onGeoQueryReady() {
                getAllEventsByInterests();
                Log.d(TAG, "All initial data has been loaded and events have been fired!");

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }


    private void getAllUserEvents() {
        Query query = mFirebaseInstance.getReference("events").orderByChild("owner").equalTo(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        mUserEvents.add(data.getKey());
                    }
                }
                navigateToCaptureFragment(new String[]{ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAllEventsByInterests() {
        Query query = mFirebaseInstance.getReference("events").orderByChild("interest").equalTo(mSelectedInterest);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (!mUserEvents.contains(data.getKey()) && mEventHashSet.contains(data.getKey())) {
                            Event event = data.getValue(Event.class);
                            event.setDistance(mLastLocation);
                            mEventList.add(event);
                        }
                    }
                    mEventsAdapter.notifyDataSetChanged();
                }
                mListener.finishLoadingData();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}
