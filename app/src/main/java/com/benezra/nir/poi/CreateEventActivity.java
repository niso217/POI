package com.benezra.nir.poi;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import com.benezra.nir.poi.Bitmap.BitmapUtil;
import com.benezra.nir.poi.Bitmap.DateUtil;
import com.benezra.nir.poi.Helper.PermissionsDialogFragment;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class CreateEventActivity extends AppCompatActivity
        implements View.OnClickListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        CompoundButton.OnCheckedChangeListener ,
        ImageCameraDialogFragment.ImageCameraDialogCallback {

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    TextView tvDatePicker, tvTimePicker;
    private Switch mSwitch;
    private Button btnSave;
    private Event mCurrentEvent;
    private LatLng mLatLng;
    final static String TAG = CreateEventActivity.class.getSimpleName();
    CollapsingToolbarLayout collapsingToolbar;
    private Calendar mEventTime;

    //event fields
    private Bitmap mBackgroundImage;


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_date:
                // Get Current Date
                int year = Calendar.getInstance().get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                // Launch Date Picker Dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, year, month, day);
                datePickerDialog.show();
                break;
            case R.id.tv_time:
                // Get Current Time
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, this, hour, minute, false);
                timePickerDialog.show();
                break;
            case R.id.btn_save:
                Event event = new Event();
                //event.setId();
                event.setLatitude(mLatLng.latitude);
                event.setLongitude(mLatLng.longitude);
                event.setStart(tvTimePicker.getText().toString());
                event.setEnd("");
                event.setOwner(mFirebaseUser.getUid());
                event.setInterest("Dance");
                //event.setTitle(mEventTitle);
                event.setDetails("dsfsdfdsFdsfdsfdsfdsfdsfdsfdsfdsfdsfdsfds");

                FirebaseDatabase.getInstance().getReference("events").child(event.getId()).setValue(event);
                finish();
                break;
            case R.id.collapsing_toolbar:
                navigateToCaptureFragment();
                break;

        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String thedate = DateUtil.FormatDate(year, month, dayOfMonth);
        tvDatePicker.setText(thedate);

        mEventTime.set(Calendar.YEAR, year);
        mEventTime.set(Calendar.MONTH, month);
        mEventTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mCurrentEvent.setStart(mEventTime.getTimeInMillis() + "");

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int hour = hourOfDay % 12;
        tvTimePicker.setText(String.format("%2d:%02d %s", hour == 0 ? 12 : hour,
                minute, hourOfDay < 12 ? "AM" : "PM"));

        mEventTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mEventTime.set(Calendar.MINUTE, minute);
        mCurrentEvent.setStart(mEventTime.getTimeInMillis() + "");

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            tvTimePicker.setVisibility(View.INVISIBLE);
        else
            tvTimePicker.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentEvent = new Event(UUID.randomUUID().toString());
        mEventTime = Calendar.getInstance();


        //Using the ToolBar as ActionBar
        //Find the toolbar view inside the activity layout
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Sets the Toolbar to act as the ActionBar for this Activity window.
        //Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Setting the category title onto collapsing toolbar
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setOnClickListener(this);


        //Setting the styles to expanded and collapsed toolbar
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);


        //Setting the category mDialogImageView onto collapsing toolbar
        ImageView imageView = (ImageView) findViewById(R.id.backdrop);


        //Setting the paragraph text onto TextView
        TextView textView = (TextView) findViewById(R.id.first_paragraph);

        tvDatePicker = (TextView) findViewById(R.id.tv_date);
        tvDatePicker.setText(DateUtil.CalendartoDate(Calendar.getInstance().getTime()));
        tvTimePicker = (TextView) findViewById(R.id.tv_time);

        mSwitch = (Switch) findViewById(R.id.tgl_allday);
        btnSave = (Button) findViewById(R.id.btn_save);


        btnSave.setOnClickListener(this);
        tvDatePicker.setOnClickListener(this);
        tvTimePicker.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(this);


    }

    public void setmMap(GoogleMap map) {
        mMap = map;
        onMapReady();
    }

    public void onPlaceSelected(Place place) {
        Log.d("", place.getName().toString());
        mLatLng = place.getLatLng();
        mMap.addMarker(new MarkerOptions().position(mLatLng).title(place.getAddress().toString()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15.0f));
    }

    private void buildImageAndTitleChooser() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        ImageCameraDialogFragment imageCameraDialogFragment = new ImageCameraDialogFragment();
        imageCameraDialogFragment.show(fragmentManager, "dialog");

    }

    public void onMapReady() {
        //Getting the intent with the attraction latitude and longitude
        Intent categoryDetail = getIntent();
        double longitude = categoryDetail.getDoubleExtra("longitude", 0);
        double latitude = categoryDetail.getDoubleExtra("latitude", 0);
        String locationTitle = categoryDetail.getStringExtra("locationTitle");


        // Add a marker in the respective location and move the camera and set the zoom level to 15
        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(locationTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title",collapsingToolbar.getTitle().toString());
        outState.putParcelable("back_image",mBackgroundImage);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String title = savedInstanceState.getString("title");
        Bitmap bitmap = savedInstanceState.getParcelable("back_image");

        if (title!=null)
        collapsingToolbar.setTitle(title);
        if (bitmap!=null)
        collapsingToolbar.setBackground(new BitmapDrawable(getResources(), bitmap));

    }


    @Override
    public void navigateToCaptureFragment() {
        if (isPermissionGranted()) {
            buildImageAndTitleChooser();
        } else {
            PermissionsDialogFragment dialogFragment = (PermissionsDialogFragment) getSupportFragmentManager().findFragmentByTag(PermissionsDialogFragment.class.getName());
            if (dialogFragment == null) {
                Log.d(TAG, "opening dialog");
                PermissionsDialogFragment permissionsDialogFragment = PermissionsDialogFragment.newInstance();
                permissionsDialogFragment.setPermissions(new String[]{android.Manifest.permission.CAMERA});
                permissionsDialogFragment.show(getSupportFragmentManager(), PermissionsDialogFragment.class.getName());

            }
        }
    }


    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void DialogResults(String title, Bitmap bitmap) {
        mBackgroundImage = bitmap;
        collapsingToolbar.setTitle(title);
        collapsingToolbar.setBackground(new BitmapDrawable(getResources(), bitmap));

        mCurrentEvent.setTitle(title);
        mCurrentEvent.setImage(BitmapUtil.encodeBitmapAndSaveToFirebase(bitmap));
    }
}

