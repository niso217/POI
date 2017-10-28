package com.benezra.nir.poi.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Helper.DirectionsJSONParser;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.benezra.nir.poi.R;
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
    private ProgressBar mProgressBar;
    private TextView mTextViewDistance;
    private TabLayout mTabLayout;
    private LinearLayout mUpperMenu;


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

        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_loading);

        mUpperMenu = (LinearLayout) view.findViewById(R.id.map_upper_menu);

        mTextViewDistance = (TextView) view.findViewById(R.id.tv_distance);

        linearLayout = (LinearLayout) view.findViewById(R.id.tab_layout);
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout_tab);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
                        setPinOnCurrentEvent();
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

    public float getBottomHeight(){
        return mTextViewDistance.getY() + mTabLayout.getY();
    }

    private void SelectCurrentEventPoint(){
        TabLayout.Tab tab = mTabLayout.getTabAt(3);
        tab.select();
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
        SelectCurrentEventPoint();

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

    public void hideUpperMenu(){
        mUpperMenu.setVisibility(View.GONE);
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

            HashMap<String, String> duration_distance = path.get(0);
            String distance = duration_distance.get("distance");
            String duration = duration_distance.get("duration");

            mTextViewDistance.setText(distance +" " + duration);

            for (int j = 1; j < path.size(); j++) {
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

        mProgressBar.setVisibility(View.GONE);


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
        setMyLocationEnabled(true);

        if (mCameraUpdate == null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mCurrentLocation);
            builder.include(mDestination);
            LatLngBounds bounds = builder.build();

            mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mMap.setPadding(10,300,10,300);
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


    public JSONObject objectToJSONObject(Object object) {
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

        void onSwipe();

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
                            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            String directions = getDirectionsUrl(mCurrentLocation, mDestination, mode);
                            mProgressBar.setVisibility(View.VISIBLE);
                            VolleyHelper.getInstance(getContext()).get(directions, null, MapFragment.this, MapFragment.this);

                        }
                    }
                });
    }

    private void setPinOnCurrentEvent() {

        setMyLocationEnabled(false);
        //if (mMarker!=null) mMarker.remove();
        if (mPolyline != null) mPolyline.remove();
        //mMarker = mMap.addMarker(new MarkerOptions().position(mCurrentLocation).title(""));

        CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(
                mDestination, 15);
        mMap.moveCamera(loc);

        mCameraUpdate = null;
    }

    private void setMyLocationEnabled(boolean Enabled) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(Enabled);

    }



}
