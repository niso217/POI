package com.benezra.nir.poi.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.benezra.nir.poi.Helper.DirectionsJSONParser;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RelativeLayoutTouchListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import static com.benezra.nir.poi.R.string.id;

public class MapFragment extends Fragment implements
        OnMapReadyCallback, PlaceSelectionListener,
        View.OnClickListener,
        Response.Listener,
        Response.ErrorListener {

    private GoogleMap mMap;
    private MapView mMapView;
    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    private Context mContext;
    private MapFragmentCallback mListener;
    private static final String TAG = MapFragment.class.getSimpleName();
    private LinearLayout linearLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mCurrentLocation;
    private LatLng mDestination;
    private Polyline mPolyline;
    private Marker mMarker;
    private CameraUpdate mCameraUpdate;


    public void setDestination(LatLng mDestination) {
        this.mDestination = mDestination;
    }

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
        linearLayout = (LinearLayout) view.findViewById(R.id.tab_layout);
        TabLayout tabs = (TabLayout) view.findViewById(R.id.tab_layout_tab);
        tabs.setOnTouchListener(new RelativeLayoutTouchListener(getContext()));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        initFusedLocation("driving");
                        break;
                    case 1:
                        initFusedLocation("walking");
                        break;
                    case 2:
                        initFusedLocation("cycling");
                        break;
                    case 3:
                        initFusedLocation("current_location");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        view.findViewById(R.id.current_location).setOnClickListener(this);


        return view;
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest, String how) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=" + how;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof MapFragment.MapFragmentCallback) {
            mListener = (MapFragment.MapFragmentCallback) context;
        }
    }

    public void setTabVisibility(boolean visible) {
        if (visible)
            linearLayout.setVisibility(View.VISIBLE);
        else {
            linearLayout.setVisibility(View.GONE);
            if (mPolyline != null) mPolyline.remove();
            //if (mMarker != null) mMarker.remove();


        }

        mListener.onTabVisible(visible);


    }

    public boolean isTabVisible() {
        switch (linearLayout.getVisibility()) {
            case View.VISIBLE:
                return true;
            case View.GONE:
                return false;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
        setRetainInstance(true);
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

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(Object response) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = objectToJSONObject(response);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }


        ArrayList points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList();
            lineOptions = new PolylineOptions();

            List<HashMap<String, String>> path = routes.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(3);
            lineOptions.color(Color.BLUE);
            lineOptions.geodesic(true);

        }

        if (lineOptions == null) return;

// Drawing polyline in the Google Map for the i-th route
        if (mPolyline != null) mPolyline.remove();
        mPolyline = this.mMap.addPolyline(lineOptions);
        //if (mMarker!=null) mMarker.remove();
        //mMarker = this.mMap.addMarker(new MarkerOptions().position(mCurrentLocation).title(""));

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (mCameraUpdate==null){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mCurrentLocation);
            builder.include(mDestination);
            LatLngBounds bounds = builder.build();

            mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mMap.animateCamera(mCameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    //CameraUpdate zout = CameraUpdateFactory.zoomBy(-1);
                    //mMap.animateCamera(zout);
                }

                @Override
                public void onCancel() {

                }
            });
        }



    }


    public JSONObject objectToJSONObject(Object object){
        Object json = null;
        JSONObject jsonObject = null;
        try {
            json = new JSONTokener(object.toString()).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json instanceof JSONObject) {
            jsonObject = (JSONObject) json;
        }
        return jsonObject;
    }


    public interface MapFragmentCallback {
        void onPlaceSelected(Place place);

        void onError(Status status);

        void onTabVisible(boolean visible);

        void onMapReady(GoogleMap googleMap);

        void onCurrentLocationClicked();

    }

    private void initFusedLocation(final String mode) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                            mCurrentLocation = new LatLng(location.getLatitude(),location.getLongitude());

                            if (mode.equals("current_location"))
                            {
                                //if (mMarker!=null) mMarker.remove();
                                if (mPolyline!=null) mPolyline.remove();
                                //mMarker = mMap.addMarker(new MarkerOptions().position(mCurrentLocation).title(""));

                                CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(
                                        mCurrentLocation, 15);
                                mMap.animateCamera(loc);
                            }
                            else{
                                String directions = getDirectionsUrl(mCurrentLocation,mDestination,mode);
                                VolleyHelper.getInstance(getContext()).get(directions, null, MapFragment.this, MapFragment.this);
                            }



                        }
                    }
                });
    }





}
