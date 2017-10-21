package com.benezra.nir.poi.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.benezra.nir.poi.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapFragment extends Fragment implements
        OnMapReadyCallback, PlaceSelectionListener,
        View.OnClickListener {

    private GoogleMap mMap;
    private MapView mMapView;
    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    private Context mContext;
    private MapFragmentCallback mListener;
    private static final String TAG = MapFragment.class.getSimpleName();
    private LinearLayout linearLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.map_fragment, container, false);

        if (mPlaceAutocompleteFragment == null) {
            mPlaceAutocompleteFragment = (PlaceAutocompleteFragment)
                    getActivity().getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            mPlaceAutocompleteFragment.setOnPlaceSelectedListener(this);
        }
        linearLayout = (LinearLayout)view.findViewById(R.id.tab_layout);
        view.findViewById(R.id.current_location).setOnClickListener(this);


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof MapFragment.MapFragmentCallback) {
            mListener = (MapFragment.MapFragmentCallback) context;
        }
    }

    public void setTabVisibility(boolean visible){
        if (visible)
            linearLayout.setVisibility(View.VISIBLE);
        else
            linearLayout.setVisibility(View.GONE);


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
        mListener.onMapReady(googleMap);
    }

    @Override
    public void onPlaceSelected(Place place) {
        mListener.onPlaceSelected(place);
    }

    @Override
    public void onError(Status status) {
        mListener.onError(status);
    }

    @Override
    public void onClick(View v) {
        mListener.onCurrentLocationClicked();
    }

    public interface MapFragmentCallback {
        void onPlaceSelected(Place place);

        void onError(Status status);

        void onMapReady(GoogleMap googleMap);

        void onCurrentLocationClicked();

    }



}
