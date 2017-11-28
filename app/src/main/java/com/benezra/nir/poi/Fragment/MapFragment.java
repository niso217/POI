package com.benezra.nir.poi.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
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
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.benezra.nir.poi.Helper.AsyncGeocoder;
import com.benezra.nir.poi.Helper.DirectionsJSONParser;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.benezra.nir.poi.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import static com.benezra.nir.poi.Interface.Constants.CYCLING;
import static com.benezra.nir.poi.Interface.Constants.DRIVING;
import static com.benezra.nir.poi.Interface.Constants.WALKING;

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        View.OnClickListener,
        Response.Listener,
        Response.ErrorListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private MapView mMapView;
    private Context mContext;
    private MapFragmentCallback mListener;
    private static final String TAG = MapFragment.class.getSimpleName();
    private LinearLayout linearLayout;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mCurrentLocation;
    private Polyline mPolyline;
    private Marker mMarker;
    private CameraUpdate mCameraUpdate;
    private ProgressBar mProgressBar;
    private LinearLayout mUpperMenu;
    private Marker mEventMarker;
    private String mEventAddress;
    private LatLng mEventLocation;
    public static final int DRIVING_TAB = 0;
    public static final int WALKING_TAB = 1;
    public static final int CYCLING_TAB = 2;
    public static final int EVENT_LOC_TAB = 3;
    public static final int LOCATION_TAB = 0;
    public static final int SEARCH_TAB = 1;


    public void setEventLocation(LatLng location,String address) {
        this.mEventLocation = location;
        addSingeMarkerToMap(location,address);

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);


        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_loading);





        if (savedInstanceState != null) {
            mEventLocation = savedInstanceState.getParcelable("event_location");
            mCurrentLocation = savedInstanceState.getParcelable("current_location");
            mEventAddress = savedInstanceState.getString("event_address");

        }


        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("current_location", mCurrentLocation);
        outState.putParcelable("event_location", mEventLocation);
        outState.putString("event_address",mEventAddress);

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
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setOnMarkerDragListener(this);
    }


    @Override
    public void onClick(View v) {
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

            HashMap<String, String> duration_distance = path.get(0);
            String distance = duration_distance.get("distance");
            String duration = duration_distance.get("duration");

            mListener.onDistanceChanged(distance + " " + duration);

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


        if (mPolyline != null) mPolyline.remove();
        mPolyline = this.mMap.addPolyline(lineOptions);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        setMyLocationEnabled(true);

        if (mCameraUpdate == null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mCurrentLocation);
            builder.include(mEventLocation);
            LatLngBounds bounds = builder.build();

            mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mMap.setPadding(10, 300, 10, 300);
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




    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mEventMarker = marker;
        getAddress(marker.getPosition());
    }


    public interface MapFragmentCallback {

        void onMapReady(GoogleMap googleMap);

        void onEventLocationChanged(LatLng latLng,String address);

        void LocationPermission();

        void onDistanceChanged(String add);

    }

    public void initFusedLocation(final int tab) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            String directions = "";
                            switch (tab) {
                                case DRIVING_TAB:
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    directions = getDirectionsUrl(mCurrentLocation, mEventLocation, DRIVING);
                                    break;
                                case CYCLING_TAB:
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    directions = getDirectionsUrl(mCurrentLocation, mEventLocation, CYCLING);
                                    break;
                                case WALKING_TAB:
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    directions = getDirectionsUrl(mCurrentLocation, mEventLocation, WALKING);
                                    break;
                                default:
                                    getAddress(mEventLocation = mCurrentLocation);
                                    break;

                            }
                            VolleyHelper.getInstance(mContext).get(directions, null, MapFragment.this, MapFragment.this);

                        }

                    }
                });

    }

    public void getAddress(final LatLng latLng) {

        new AsyncGeocoder(new AsyncGeocoder.onAddressFoundListener() {
            @Override
            public void onAddressFound(String result) {
                addSingeMarkerToMap(latLng,result);
                mListener.onEventLocationChanged(latLng,result);
                Log.d(TAG, "the address is: " + result);

            }
        }).execute(new AsyncGeocoder.AsyncGeocoderObject(
                new Geocoder(mContext), LatLongToLocation(latLng)));
    }

    private Location LatLongToLocation(LatLng latLng){
        Location loc = new Location("");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    public void addSingeMarkerToMap(LatLng location,String address) {

        setMyLocationEnabled(false);
        if (mPolyline != null) mPolyline.remove();

        MarkerOptions markerOptions = new MarkerOptions()
                .position(location).title(address).draggable(true);

        if (mEventMarker != null)
            mEventMarker.remove();

        mEventMarker = mMap.addMarker(markerOptions);
        mEventMarker.showInfoWindow();
        CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(location, 15);
        mMap.moveCamera(loc);

        mCameraUpdate = null;

    }


    public void setMyLocationEnabled(boolean Enabled) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

    }


}
