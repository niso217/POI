package com.benezra.nir.poi.Fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Adapter.EventsAdapter;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Helper.MapStateManager;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.benezra.nir.poi.Interface.Constants.APP_BAR_SIZE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_END;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ID;
import static com.benezra.nir.poi.Interface.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LIST;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Interface.Constants.EVENT_START;
import static com.benezra.nir.poi.Interface.Constants.EVENT_TITLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventByInterestMapFragment extends Fragment implements

        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        RecyclerTouchListener.ClickListener {

    private FirebaseDatabase mFirebaseInstance;
    final static String TAG = EventByInterestMapFragment.class.getSimpleName();
    private GoogleMap mMap;
    private MapView mMapView;
    private Event mCurrentSelectedEvent;
    private RecyclerView mEventsRecyclerView;
    private ArrayList<Event> mEventList;
    private EventsAdapter mEventsAdapter;
    private LatLngBounds.Builder mBoundsBuilder;
    private Marker lastClicked = null;
    private int mLastSelectedIndex;
    private MainActivity mActivity;
    private CameraPosition mCameraPosition;
    private MapStateManager mMapStateManager;


    public static EventByInterestMapFragment newInstance(ArrayList<Event> event_list) {
        EventByInterestMapFragment eventByInterestMapFragment = new EventByInterestMapFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EVENT_LIST, event_list);
        eventByInterestMapFragment.setArguments(args);
        return eventByInterestMapFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseInstance.getReference().keepSynced(true);
        mEventList = new ArrayList<>();
        mBoundsBuilder = new LatLngBounds.Builder();
        mMapStateManager = new MapStateManager(getContext());

        if (savedInstanceState != null) {
            mLastSelectedIndex = savedInstanceState.getInt("index", 0);
            mCameraPosition = mMapStateManager.getSavedCameraPosition();
        }

        mEventList = getArguments().getParcelableArrayList(EVENT_LIST);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity.setAppBarHeight(APP_BAR_SIZE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("index", mLastSelectedIndex);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapStateManager.saveMapState(mMap);
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
        mMap.setOnMarkerClickListener(this);
        mMap.setMapType(mMapStateManager.getSavedMapType());

        //mMap.setPadding(0, 300, 300, 300);

        if (!mEventList.isEmpty()) {
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

        if (mCameraPosition != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(mCameraPosition);
            mMap.moveCamera(update);
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

        mEventsRecyclerView = rootView.findViewById(R.id.recycler_view_events);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true);
        mEventsRecyclerView.setLayoutManager(layoutManager);
        mEventsRecyclerView.setNestedScrollingEnabled(false);
        mEventsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mEventsRecyclerView, this));
        mEventsRecyclerView.setBackgroundResource(R.drawable.image_border);
        mEventsRecyclerView.setAdapter(mEventsAdapter);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mEventsRecyclerView);

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.setAppBarHeight(0);
    }


    @Override
    public void onResume() {
        super.onResume();
        mActivity.setmCurrentFragment(TAG);

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

    private void PaintSelectedEvent(Marker marker) {
        if (lastClicked != null)
            lastClicked.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if (marker != null)
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
