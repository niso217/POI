package com.benezra.nir.poi;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Calendar;

import com.benezra.nir.poi.Adapter.ParticipateAdapter;
import com.benezra.nir.poi.Bitmap.BitmapUtil;
import com.benezra.nir.poi.Bitmap.DateUtil;
import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragment;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Fragment.ProgressDialogFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Adapter.CustomSpinnerAdapter;
import com.benezra.nir.poi.View.DividerItemDecoration;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Helper.Constants.DETAILS;
import static com.benezra.nir.poi.Helper.Constants.END;
import static com.benezra.nir.poi.Helper.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ID;
import static com.benezra.nir.poi.Helper.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Helper.Constants.EVENT_START;
import static com.benezra.nir.poi.Helper.Constants.EVENT_TITLE;
import static com.benezra.nir.poi.Helper.Constants.IMAGE;
import static com.benezra.nir.poi.Helper.Constants.INTEREST;
import static com.benezra.nir.poi.Helper.Constants.LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.START;
import static com.benezra.nir.poi.Helper.Constants.TITLE;


public class CreateEventActivity extends BaseActivity
        implements View.OnClickListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        CompoundButton.OnCheckedChangeListener,
        ImageCameraDialogFragment.ImageCameraDialogCallback,
        TextWatcher,
        AdapterView.OnItemSelectedListener,
        AlertDialogFragment.DialogListenerCallback,
        MapFragment.MapFragmentCallback {

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
    private Spinner mspinnerCustom;
    private ArrayList<String> mInterestsList;
    private CustomSpinnerAdapter mCustomSpinnerAdapter;
    private EditText mEventDetails;
   //private ParticipatesAdapter mParticipatesAdapterAdapter;
    private ProgressBar mProgressBar;
    private ProgressDialogFragment mProgressDialogFragment;
    private boolean mMode; //true = new | false = edit
    private FusedLocationProviderClient mFusedLocationClient;
    private RecyclerView mRecyclerView;
    private ParticipateAdapter mParticipateAdapter;
    private List<User> mParticipates;






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
                checkEvent();
                break;
            case R.id.collapsing_toolbar:
                navigateToCaptureFragment(new String[]{Manifest.permission.CAMERA});
                break;

        }
    }

    private void checkEvent() {
        if (mCurrentEvent != null) {
            if (mCurrentEvent.getDetails() == null || mCurrentEvent.getDetails().equals("")) {
                Toast.makeText(this, getString(R.string.missing_details), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentEvent.getInterest() == null || mCurrentEvent.getInterest().equals("")) {
                Toast.makeText(this, getString(R.string.missing_interest), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentEvent.getLatitude() == 0 || mCurrentEvent.getLongitude() == 0) {
                Toast.makeText(this, getString(R.string.missing_location), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCurrentEvent.getTitle() == null || mCurrentEvent.getTitle().equals(getString(R.string.collapsingtoolbar_title))) {
                Toast.makeText(this, getString(R.string.missing_title), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (mCurrentEvent.getImage() == null && mCurrentEvent.getUri()==null)
            BuildDialogFragment();
        else
            saveEvent();

    }

    private void BuildDialogFragment() {
        AlertDialogFragment alertDialog = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.class.getName());
        if (alertDialog == null) {
            Log.d(TAG, "opening alert dialog");
            HashMap<Integer, String> map = new HashMap<>();
            map.put(BUTTON_POSITIVE, getString(R.string.sure));
            map.put(BUTTON_NEUTRAL, getString(R.string.return_to_event));
            alertDialog = AlertDialogFragment.newInstance(
                    getString(R.string.no_image_title), getString(R.string.no_image_message), map);
            alertDialog.show(getSupportFragmentManager(), AlertDialogFragment.class.getName());

        }
    }



    @Override
    public void onFinishDialog(int state) {
        switch (state) {
            case BUTTON_POSITIVE:
                saveEvent();
                break;
        }
    }

    private void saveEvent() {
        if (mCurrentEvent.getUri() != null)
            uploadBytes(mCurrentEvent.getUri());
        else
            saveEventToFirebase();
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String interest = parent.getItemAtPosition(position).toString();
        mCurrentEvent.setInterest(interest);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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


        //Setting the category name onto collapsing toolbar
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


        //Setting the styles to expanded and collapsed toolbar
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbar.setTitle(getString(R.string.collapsingtoolbar_title));

        //Setting the category mDialogImageView onto collapsing toolbar
        mToolbarBackgroundImage = (ImageView) findViewById(R.id.backdrop);


        //Setting the paragraph text onto TextView
        mEventDetails = (EditText) findViewById(R.id.first_paragraph);
        mEventDetails.addTextChangedListener(this);

        tvDatePicker = (TextView) findViewById(R.id.tv_date);
        tvDatePicker.setText(DateUtil.CalendartoDate(Calendar.getInstance().getTime()));
        tvTimePicker = (TextView) findViewById(R.id.tv_time);

        mspinnerCustom = (Spinner) findViewById(R.id.spinnerCustom);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);

        mSwitch = (Switch) findViewById(R.id.tgl_allday);
        btnSave = (Button) findViewById(R.id.btn_save);


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());




        btnSave.setOnClickListener(this);
        tvDatePicker.setOnClickListener(this);
        tvTimePicker.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(this);
        collapsingToolbar.setOnClickListener(this);
        mspinnerCustom.setOnItemSelectedListener(this);


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mInterestsList = savedInstanceState.getStringArrayList("interests");
            mParticipates = savedInstanceState.getParcelableArrayList("participates");
            mMode = savedInstanceState.getBoolean("mode");

            initCustomSpinner();
            initParticipates();
            setEventFields();


        } else {
            mInterestsList = new ArrayList<>();
            initCustomSpinner();
            mParticipates = new ArrayList<>();
            initParticipates();

            if (getIntent().getStringExtra(EVENT_ID) != null) {
                mMode = false; //edit  existing event
                getEventIntent(getIntent());
            } else {
                mMode = true; //new event
                mCurrentEvent = new Event(UUID.randomUUID().toString(), mFirebaseUser.getUid());

            }

            addParticipateChangeListener();
            addInterestsChangeListener();
        }


    }


    private void addParticipateChangeListener() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded");

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged");

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mParticipates.clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        User user = data.getValue(User.class);
                        mParticipates.add(user);
                    }

                    //mParticipatesAdapterAdapter.setItems(new ArrayList<User>(mParticipates));
                    mParticipateAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void DialogResults(String title, Uri picuri) {
        collapsingToolbar.setTitle(title);
        mCurrentEvent.setTitle(title);
        mCurrentEvent.setUri(picuri);
        if (picuri != null)
            mCurrentEvent.setImage(null);
        setImageBack();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getEventIntent(Intent intent) {
        //showDialog();

        mCurrentEvent = new Event();
        mCurrentEvent.setId(intent.getStringExtra(EVENT_ID));
        mCurrentEvent.setDetails(intent.getStringExtra(EVENT_DETAILS));
        mCurrentEvent.setInterest(intent.getStringExtra(EVENT_INTEREST));
        mCurrentEvent.setOwner(intent.getStringExtra(EVENT_OWNER));
        mCurrentEvent.setTitle(intent.getStringExtra(EVENT_TITLE));
        mCurrentEvent.setStart(intent.getLongExtra(EVENT_START, 0));
        mCurrentEvent.setLatitude(intent.getDoubleExtra(EVENT_LATITUDE, 0));
        mCurrentEvent.setLongitude(intent.getDoubleExtra(EVENT_LONGITUDE, 0));
        mCurrentEvent.setImage(intent.getStringExtra(EVENT_IMAGE));
        setEventFields();


    }


    public void onPlaceSelected(Place place) {
        Log.d(TAG, place.getName().toString());
        mCurrentEvent.setLatitude(place.getLatLng().latitude);
        mCurrentEvent.setLongitude(place.getLatLng().longitude);
        initMap(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude), place.getAddress().toString());

    }

    @Override
    public void onError(Status status) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMap(new LatLng(mCurrentEvent.getLatitude(),mCurrentEvent.getLongitude()),"");
    }

    @Override
    public void onCurrentLocationClicked() {
        navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }


    private void buildImageAndTitleChooser() {

        ImageCameraDialogFragment ImageCameraFragment = (ImageCameraDialogFragment) getSupportFragmentManager().findFragmentByTag(ImageCameraDialogFragment.class.getName());

        if (ImageCameraFragment == null) {
            Log.d(TAG, "opening image camera dialog");
            ImageCameraFragment = ImageCameraDialogFragment.newInstance();
            if (mCurrentEvent.getImage() != null)
                ImageCameraFragment.setPicURL(mCurrentEvent.getImage());
            else if (mCurrentEvent.getUri() != null)
                ImageCameraFragment.setImageUri(mCurrentEvent.getUri());
            if (mCurrentEvent.getTitle() != null)
                ImageCameraFragment.setTitle(mCurrentEvent.getTitle());

            ImageCameraFragment.show(getSupportFragmentManager(), ImageCameraDialogFragment.class.getName());

        }

    }

    private void initCustomSpinner() {
        mCustomSpinnerAdapter = new CustomSpinnerAdapter(this, new ArrayList<String>(mInterestsList));
        mspinnerCustom.setAdapter(mCustomSpinnerAdapter);
    }

    private void initParticipates() {
        mParticipateAdapter = new ParticipateAdapter(this,mParticipates);
        mRecyclerView.setAdapter(mParticipateAdapter);
    }


    private void addInterestsChangeListener() {
        mFirebaseInstance.getReference("interests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                };
                mInterestsList = snapshot.getValue(t);
                if (mInterestsList != null) {
                    mCustomSpinnerAdapter.updateInterestList(new ArrayList<String>(mInterestsList));
                    mspinnerCustom.setSelection(mCustomSpinnerAdapter.getPosition(mCurrentEvent.getInterest()));
                }

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }


    public void initMap(LatLng latLang, String address) {

        mMap.addMarker(new MarkerOptions().position(latLang).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 15.0f));


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("event", mCurrentEvent);
        outState.putStringArrayList("interests", mInterestsList);
        //outState.putParcelableArrayList("participates", mParticipates);
        outState.putBoolean("mode", mMode);

    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            collapsingToolbar.setTitle(mCurrentEvent.getTitle());
            setImageBack();
            mEventDetails.setText(mCurrentEvent.getDetails());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mCurrentEvent.getStart());
            tvDatePicker.setText(DateUtil.CalendartoDate(calendar.getTime()));

        }

    }


    @Override
    public void navigateToCaptureFragment(String [] permissions) {

        if (isPermissionGranted(permissions)) {


            if (Arrays.asList(permissions).contains(ACCESS_FINE_LOCATION))
            {
                initFusedLocation();
            }
            if (Arrays.asList(permissions).contains(Manifest.permission.CAMERA))
            {
                buildImageAndTitleChooser();
            }
        } else {
            PermissionsDialogFragment dialogFragment = (PermissionsDialogFragment) getSupportFragmentManager().findFragmentByTag(PermissionsDialogFragment.class.getName());
            if (dialogFragment == null) {
                Log.d(TAG, "opening dialog");
                PermissionsDialogFragment permissionsDialogFragment = PermissionsDialogFragment.newInstance();
                permissionsDialogFragment.setPermissions(permissions);
                permissionsDialogFragment.show(getSupportFragmentManager(), PermissionsDialogFragment.class.getName());

            }
        }
    }

    private boolean isPermissionGranted(String [] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    private void initFusedLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLang = new LatLng(location.getLatitude(), location.getLongitude());
                            mCurrentEvent.setLatitude(latLang.latitude);
                            mCurrentEvent.setLongitude(latLang.longitude);
                            initMap(latLang,"");
                        }
                    }
                });
    }


    private void BuildProgressDialogFragment() {
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (progressDialog == null) {
            Log.d(TAG, "opening origress dialog");
            progressDialog = ProgressDialogFragment.newInstance(
                    getString(R.string.creating_event), getString(R.string.please_wait), ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show(getSupportFragmentManager(), ProgressDialogFragment.class.getName());

        }
    }

    private void uploadBytes(Uri picUri) {

        if (picUri != null) {
            String fileName = mCurrentEvent.getTitle();

            if (!validateInputFileName(fileName)) {
                return;
            }


            mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
            if (mProgressDialogFragment == null) {
                Log.d(TAG, "opening origress dialog");
                mProgressDialogFragment = ProgressDialogFragment.newInstance(
                        getString(R.string.creating_event), getString(R.string.please_wait), ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getName());
            }

            Bitmap bitmap = BitmapUtil.UriToBitmap(this, picUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("images").child(mCurrentEvent.getId());
            fileRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //mProgressDialogFragment.dismiss();

                            Log.i(TAG, "Uri: " + taskSnapshot.getDownloadUrl());
                            Log.i(TAG, "Name: " + taskSnapshot.getMetadata().getName());
                            mCurrentEvent.setImage(taskSnapshot.getDownloadUrl().toString());
                            saveEventToFirebase();

                            Toast.makeText(CreateEventActivity.this, "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            mProgressDialogFragment.dismiss();

                            Toast.makeText(CreateEventActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Log.d(TAG, "addOnProgressListener " + progress + "");
                            // percentage in progress dialog
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putInt("prg",(int)progress);
                            message.setData(bundle);
                            mProgressDialogFragment.setProgress(message);
                        }
                    })
                    .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("Upload is paused!");
                        }
                    });
        } else {
            Toast.makeText(CreateEventActivity.this, "No File!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveEventToFirebase() {
        DatabaseReference eventReference = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId());
        if (mMode)

        {

            mCurrentEvent.setParticipates(setOwnerAsParticipate());
            eventReference.setValue(mCurrentEvent);

        }
        else{
            eventReference.child(DETAILS).setValue(mCurrentEvent.getDetails());
            eventReference.child(START).setValue(mCurrentEvent.getStart());
            eventReference.child(END).setValue(mCurrentEvent.getEnd());
            eventReference.child(IMAGE).setValue(mCurrentEvent.getImage());
            eventReference.child(LATITUDE).setValue(mCurrentEvent.getLatitude());
            eventReference.child(LONGITUDE).setValue(mCurrentEvent.getLongitude());
            eventReference.child(TITLE).setValue(mCurrentEvent.getTitle());
            eventReference.child(INTEREST).setValue(mCurrentEvent.getInterest());

        }
        finish();
    }

    private Map<String,User> setOwnerAsParticipate(){
        User owner = new User();
        owner.setName(mFirebaseUser.getDisplayName());
        owner.setEmail(mFirebaseUser.getEmail());
        owner.setAvatar(mFirebaseUser.getPhotoUrl().toString());
        HashMap<String, User> map = new HashMap<>();
        map.put(mFirebaseUser.getUid(),owner);
        return map;
    }

    private boolean validateInputFileName(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }



    private void setImageBack() {
        if (mCurrentEvent.getImage() != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(mCurrentEvent.getImage())
                    .into(mToolbarBackgroundImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mProgressBar.setVisibility(View.GONE);

                        }
                    });
        } else {
            Bitmap bitmap = BitmapUtil.UriToBitmap(this, mCurrentEvent.getUri());
            if (bitmap != null)
                mToolbarBackgroundImage.setImageBitmap(bitmap);
        }

    }


}

