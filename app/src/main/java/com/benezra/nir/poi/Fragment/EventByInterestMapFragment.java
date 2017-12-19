package com.benezra.nir.poi.Fragment;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Adapter.EventsAdapter;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Helper.MapStateManager;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.View.DividerItemDecoration;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.benezra.nir.poi.Interface.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_END;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ID;
import static com.benezra.nir.poi.Interface.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Interface.Constants.EVENT_START;
import static com.benezra.nir.poi.Interface.Constants.EVENT_TITLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventByInterestMapFragment extends Fragment implements

        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener,
        RecyclerTouchListener.ClickListener {

    private FirebaseDatabase mFirebaseInstance;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private FirebaseUser mFirebaseUser;
    final static String TAG = EventByInterestMapFragment.class.getSimpleName();
    private GoogleMap mMap;
    private MapView mMapView;
    private Event mCurrentSelectedEvent;
    private RecyclerView mEventsRecyclerView;
    private ArrayList<Event> mEventList;
    private EventsAdapter mEventsAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private LatLngBounds mLatLngBounds;
    private LatLngBounds.Builder mBoundsBuilder;
    private FirebaseAuth mAuth;
    private Marker lastClicked = null;
    private int mLastSelectedIndex;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance.getReference().keepSynced(true);
        mEventList = new ArrayList<>();
        mBoundsBuilder = new LatLngBounds.Builder();
        mAuth = FirebaseAuth.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null && savedInstanceState==null) {
            mEventList = bundle.getParcelableArrayList("event_list");

        }
        else{
            mLastSelectedIndex = savedInstanceState.getInt("index",0);
            mEventList = savedInstanceState.getParcelableArrayList("event_list");

        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("event_list",mEventList);
        outState.putInt("index",mLastSelectedIndex);
    }

    @Override
    public void onPause() {
        super.onPause();
        MapStateManager mgr = new MapStateManager(getContext());
        mgr.saveMapState(mMap);
    }


    public EventByInterestMapFragment() {
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        //mMap.setPadding(0, 300, 300, 300);

        if (!mEventList.isEmpty())
        {
            addAllMarkersToMap();
            PaintSelectedEvent(mEventList.get(mLastSelectedIndex).getMarker());

        }

    }



    private void addAllMarkersToMap() {
        for (int i = 0; i < mEventList.size(); i++) {
            LatLng loc = mEventList.get(i).getLatlng();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(loc);
            Marker marker = mMap.addMarker(markerOptions);
            marker.setTag(mEventList.get(i).getId());
            mEventList.get(i).setMarker(marker);
            mBoundsBuilder.include(loc);

        }

        MapStateManager mgr = new MapStateManager(getContext());
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);

            mMap.setMapType(mgr.getSavedMapType());
        } else {
            if (mEventList.size() > 2) {
                LatLngBounds bounds = mBoundsBuilder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
            } else
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mEventList.get(0).getLatlng(), 10));
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.events_map_fragment, container, false);

        mEventsAdapter = new EventsAdapter(getContext(), mEventList);

        mEventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_events);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true);
        mEventsRecyclerView.setLayoutManager(layoutManager);
        mEventsRecyclerView.setNestedScrollingEnabled(false);
        mEventsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mEventsRecyclerView, this));
        //mEventsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.HORIZONTAL_LIST));
        mEventsRecyclerView.setBackgroundResource(R.drawable.image_border);
        mEventsRecyclerView.setAdapter(mEventsAdapter);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mEventsRecyclerView);

//        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mEventsAdapter);
//        mItemTouchHelper = new ItemTouchHelper(callback);
//        mItemTouchHelper.attachToRecyclerView(mEventsRecyclerView);

        mEventsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mLastSelectedIndex = layoutManager.findFirstVisibleItemPosition();
                        System.out.println("The RecyclerView is not scrolling " + mLastSelectedIndex);
                        if (mMap != null) {
                            CameraUpdate loc = CameraUpdateFactory.newLatLng(mEventList.get(mLastSelectedIndex).getLatlng());
                            PaintSelectedEvent(mEventList.get(mLastSelectedIndex).getMarker());
                            mMap.animateCamera(loc);
                        }

                        break;
                }
            }
        });


        return rootView;
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
        userEvent.putExtra(EVENT_END, mCurrentSelectedEvent.getEnd());

        userEvent.putExtra(EVENT_ADDRESS, mCurrentSelectedEvent.getAddress());


        startActivity(userEvent);


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        int index = 0;

        for (int i = 0; i < mEventList.size(); i++) {
            if (mEventList.get(i).getId().equals(marker.getTag())) {
                index = i;
                break;
            }

        }

        PaintSelectedEvent(marker);

        mEventsRecyclerView.smoothScrollToPosition(index);
        return false;
    }

    private void PaintSelectedEvent(Marker marker){
        if (lastClicked != null)
            lastClicked.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        lastClicked = marker;
    }

    @Override
    public void onClick(View view, int position) {
        Intent userEvent = new Intent(getActivity(), ViewEventActivity.class);
        Event event = mEventList.get(position);
        userEvent.putExtra(EVENT_ID, event.getId());
        userEvent.putExtra(EVENT_TITLE, event.getTitle());
        userEvent.putExtra(EVENT_OWNER, event.getOwner());
        userEvent.putExtra(EVENT_IMAGE, event.getImage());
        userEvent.putExtra(EVENT_DETAILS, event.getDetails());
        userEvent.putExtra(EVENT_LATITUDE, event.getLatitude());
        userEvent.putExtra(EVENT_LONGITUDE, event.getLongitude());
        userEvent.putExtra(EVENT_INTEREST, event.getInterest());
        userEvent.putExtra(EVENT_START, event.getStart());
        userEvent.putExtra(EVENT_END, event.getEnd());
        userEvent.putExtra(EVENT_ADDRESS, event.getAddress());


        startActivity(userEvent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
