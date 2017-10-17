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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
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
        implements ValueEventListener, PermissionsDialogFragment.PermissionsGrantedCallback {

    private EventModel mEventModel;
    private ArrayList<Event> mEventList;
    private FirebaseDatabase mFirebaseInstance;
    private CategoryAdapter mCategoryAdapter;
    private ListView mListView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private FirebaseUser mFirebaseUser;
    final static String TAG = EventByInterestFragment.class.getSimpleName();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventModel = new EventModel();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventList = new ArrayList<>();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance.getReference().keepSynced(true);

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
                            addEventChangeListener();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);


        // Create an {@link CategoryAdapter}, whose data source is a list of
        // {@link Categories}. The adapter knows how to create list item views for each item
        // in the list.
        mCategoryAdapter = new CategoryAdapter(getActivity(), mEventList,mFirebaseUser.getPhotoUrl().toString());

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


                startActivity(userEvent);
            }
        });

        initFusedLocation();
        navigateToCaptureFragment(new String[]{ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

        return rootView;
    }



    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                Event event = data.getValue(Event.class);
                if (!mFirebaseUser.getUid().equals(event.getOwner())) {  //skip events from owner
                    event.setDistance(mLastLocation);
                    event.setId(data.getKey());
                    mEventModel.addEvent(data.getKey(), event);
                }
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
}
