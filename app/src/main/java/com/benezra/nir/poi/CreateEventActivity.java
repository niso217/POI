package com.benezra.nir.poi;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.benezra.nir.poi.Helper.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ID;
import static com.benezra.nir.poi.Helper.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Helper.Constants.EVENT_START;
import static com.benezra.nir.poi.Helper.Constants.EVENT_TITLE;


public class CreateEventActivity extends BaseActivity
        implements View.OnClickListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        CompoundButton.OnCheckedChangeListener,
        ImageCameraDialogFragment.ImageCameraDialogCallback,
        TextWatcher {

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    TextView tvDatePicker, tvTimePicker;
    private Switch mSwitch;
    private Button btnSave;
    private Event mCurrentEvent;
    final static String TAG = CreateEventActivity.class.getSimpleName();
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView mToolbarBackgroundImage;
    private Calendar mEventTime;
    private FirebaseDatabase mFirebaseInstance;


    //event fields
    private Bitmap mBackgroundImage;
    private EditText mEventDetails;


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
                FirebaseDatabase.getInstance().getReference("events").child(mCurrentEvent.getId()).setValue(mCurrentEvent);
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
        mCurrentEvent.setStart(mEventTime.getTimeInMillis());

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        int hour = hourOfDay % 12;
        tvTimePicker.setText(String.format("%2d:%02d %s", hour == 0 ? 12 : hour,
                minute, hourOfDay < 12 ? "AM" : "PM"));

        mEventTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mEventTime.set(Calendar.MINUTE, minute);
        mCurrentEvent.setStart(mEventTime.getTimeInMillis());

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            tvTimePicker.setVisibility(View.INVISIBLE);
        else
            tvTimePicker.setVisibility(View.VISIBLE);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        mCurrentEvent.setDetails(s.toString());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();
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


        //Setting the styles to expanded and collapsed toolbar
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);


        //Setting the category mDialogImageView onto collapsing toolbar
        mToolbarBackgroundImage = (ImageView) findViewById(R.id.backdrop);


        //Setting the paragraph text onto TextView
        mEventDetails = (EditText) findViewById(R.id.first_paragraph);
        mEventDetails.addTextChangedListener(this);

        tvDatePicker = (TextView) findViewById(R.id.tv_date);
        tvDatePicker.setText(DateUtil.CalendartoDate(Calendar.getInstance().getTime()));
        tvTimePicker = (TextView) findViewById(R.id.tv_time);

        mSwitch = (Switch) findViewById(R.id.tgl_allday);
        btnSave = (Button) findViewById(R.id.btn_save);


        btnSave.setOnClickListener(this);
        tvDatePicker.setOnClickListener(this);
        tvTimePicker.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(this);
        collapsingToolbar.setOnClickListener(this);



        if (getIntent().getStringExtra(EVENT_ID) != null && savedInstanceState==null)
            getEventIntent(getIntent());
        else
            mCurrentEvent = new Event(UUID.randomUUID().toString(), mFirebaseUser.getUid());


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getEventIntent(Intent intent) {
        showDialog();
        mCurrentEvent = new Event();
        mCurrentEvent.setId(intent.getStringExtra(EVENT_ID));
        mCurrentEvent.setDetails(intent.getStringExtra(EVENT_DETAILS));
        mCurrentEvent.setInterest(intent.getStringExtra(EVENT_INTEREST));
        mCurrentEvent.setOwner(intent.getStringExtra(EVENT_OWNER));
        mCurrentEvent.setTitle(intent.getStringExtra(EVENT_TITLE));
        mCurrentEvent.setStart(intent.getLongExtra(EVENT_START, 0));
        mCurrentEvent.setLatitude(intent.getDoubleExtra(EVENT_LATITUDE, 0));
        mCurrentEvent.setLongitude(intent.getDoubleExtra(EVENT_LONGITUDE, 0));
        getImageFromFireBase(intent.getStringExtra(EVENT_ID));


    }

    private void getImageFromFireBase(String eventId) {
        Query query = mFirebaseInstance.getReference("events").child(eventId).child("image");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String image = snapshot.getValue(String.class);
                if (image != null) {
                    mCurrentEvent.setImage(image);
                    setEventFields();
                    hideDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    public void setmMap(GoogleMap map) {
        mMap = map;
        if (mCurrentEvent.getLatitude() > 0)
            initMap(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude(), "");

    }

    public void onPlaceSelected(Place place) {
        Log.d(TAG, place.getName().toString());

        mCurrentEvent.setLatitude(place.getLatLng().latitude);
        mCurrentEvent.setLongitude(place.getLatLng().longitude);
        initMap(place.getLatLng().latitude, place.getLatLng().longitude, place.getAddress().toString());

    }

    private void buildImageAndTitleChooser() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        ImageCameraDialogFragment imageCameraDialogFragment = new ImageCameraDialogFragment();
        imageCameraDialogFragment.show(fragmentManager, "dialog");

    }

    public void initMap(double latitude, double longitude, String address) {

        // Add a marker in the respective location and move the camera and set the zoom level to 15
        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("event", mCurrentEvent);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentEvent = savedInstanceState.getParcelable("event");

        setEventFields();

    }

    private void setEventFields() {
        if (mCurrentEvent != null) {
            collapsingToolbar.setTitle(mCurrentEvent.getTitle());
            if (mCurrentEvent.getImage() != null) {
                try {
                    mToolbarBackgroundImage.setImageBitmap(BitmapUtil.decodeFromFirebaseBase64(mCurrentEvent.getImage()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mEventDetails.setText(mCurrentEvent.getDetails());
            //initMap(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude(), "");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mCurrentEvent.getStart());
            tvDatePicker.setText(DateUtil.CalendartoDate(calendar.getTime()));
        }
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
        mToolbarBackgroundImage.setImageBitmap(bitmap);

        mCurrentEvent.setTitle(title);
        mCurrentEvent.setImage(BitmapUtil.encodeBitmapAndSaveToFirebase(bitmap));
    }


}

