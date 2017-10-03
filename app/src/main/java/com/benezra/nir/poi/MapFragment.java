package com.benezra.nir.poi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback,PlaceSelectionListener {

    private GoogleMap mMap;
    private MapView mapView;
    private PlaceAutocompleteFragment autocompleteFragment;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.map_fragment, container, false);

        if (autocompleteFragment == null) {
            autocompleteFragment = (PlaceAutocompleteFragment)
                    getActivity().getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            autocompleteFragment.setOnPlaceSelectedListener(this);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
    }

    public GoogleMap getmMap() {
        if (mMap != null)
            return mMap;

        return null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try{
            ((CreateEventActivity) getActivity()).setmMap(googleMap);
        }catch (ClassCastException cce){

        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        try{
            ((CreateEventActivity) getActivity()).onPlaceSelected(place);
        }catch (ClassCastException cce){

        }
    }

    @Override
    public void onError(Status status) {
            Log.e("MapFragment",status.getStatusMessage());
    }
}
