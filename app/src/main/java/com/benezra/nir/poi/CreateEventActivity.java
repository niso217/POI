package com.benezra.nir.poi;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import com.android.volley.toolbox.ImageLoader;
import com.benezra.nir.poi.Adapter.ParticipatesAdapter;
import com.benezra.nir.poi.Bitmap.BitmapUtil;
import com.benezra.nir.poi.Bitmap.DateUtil;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragment;
import com.benezra.nir.poi.Helper.PermissionsDialogFragment;
import com.benezra.nir.poi.Helper.SharePref;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.benezra.nir.poi.View.CustomSpinnerAdapter;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static android.R.attr.bitmap;
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
        TextWatcher,
        AdapterView.OnItemSelectedListener {

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
    private ParticipatesAdapter mParticipatesAdapterAdapter;
    private ListView mListView;
    private ArrayList<User> mParticipates;
    private ProgressDialog progressDialog;
    private boolean mMode; //true = new | false = edit


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
                if (mCurrentEvent.getUri() != null)
                    uploadBytes(mCurrentEvent.getUri());
                else
                    saveEventToFirebase();

                break;
            case R.id.collapsing_toolbar:
                navigateToCaptureFragment();
                break;

        }
    }

    private Bitmap getBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) mToolbarBackgroundImage.getDrawable();
        if (drawable != null)
            return drawable.getBitmap();

        return null;
    }

    private Uri getBitmapUri() {
        BitmapDrawable drawable = (BitmapDrawable) mToolbarBackgroundImage.getDrawable();
        if (drawable != null)
            BitmapUtil.getImageUri(this, drawable.getBitmap());

        return null;
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


        progressDialog = new ProgressDialog(this);

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

        mspinnerCustom = (Spinner) findViewById(R.id.spinnerCustom);


        mListView = (ListView) findViewById(R.id.list_view_par);


        mSwitch = (Switch) findViewById(R.id.tgl_allday);
        btnSave = (Button) findViewById(R.id.btn_save);


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

            mspinnerCustom.setSelection(mCustomSpinnerAdapter.getPosition(mCurrentEvent.getInterest()));

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
                    User owner = new User();
                    owner.setName(mFirebaseUser.getDisplayName());
                    owner.setAvatar(mFirebaseUser.getPhotoUrl().toString());
                    mParticipates.add(owner);
                    for (int i = 0; i < mParticipates.size(); i++) {
                        Log.d(TAG, mParticipates.get(i).getName());
                    }

                    mParticipatesAdapterAdapter.setItems(new ArrayList<User>(mParticipates));
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

            ImageCameraFragment.show(getSupportFragmentManager(), PermissionsDialogFragment.class.getName());

        }

    }

    private void initCustomSpinner() {
        mCustomSpinnerAdapter = new CustomSpinnerAdapter(this, mInterestsList);
        mCustomSpinnerAdapter.updateInterestList(mInterestsList);
    }

    private void initParticipates() {
        Log.d(TAG, mParticipates.size() + "");
        mParticipatesAdapterAdapter = new ParticipatesAdapter(this, new ArrayList<User>(mParticipates));
        mListView.setAdapter(mParticipatesAdapterAdapter);
    }


    private void addInterestsChangeListener() {
        mFirebaseInstance.getReference("interests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                };
                mInterestsList = snapshot.getValue(t);
                if (mInterestsList != null)
                    initCustomSpinner();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
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
        outState.putStringArrayList("interests", mInterestsList);
        outState.putParcelableArrayList("participates", mParticipates);
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
            mspinnerCustom.setSelection(mCustomSpinnerAdapter.getPosition(mCurrentEvent.getInterest()));

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

    private void uploadBytes(Uri picUri) {

        if (picUri != null) {
            String fileName = mCurrentEvent.getTitle();

            if (!validateInputFileName(fileName)) {
                return;
            }


            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            Bitmap bitmap = BitmapUtil.UriToBitmap(this, picUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("images").child(mCurrentEvent.getId() );
            fileRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            Log.e(TAG, "Uri: " + taskSnapshot.getDownloadUrl());
                            Log.e(TAG, "Name: " + taskSnapshot.getMetadata().getName());
                            mCurrentEvent.setImage(taskSnapshot.getDownloadUrl().toString());
                            saveEventToFirebase();


//                            tvFileName.setText(taskSnapshot.getMetadata().getPath() + " - "
//                                    + taskSnapshot.getMetadata().getSizeBytes() / 1024 + " KBs");
                            Toast.makeText(CreateEventActivity.this, "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();

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
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
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
        FirebaseDatabase.getInstance().getReference("events").child(mCurrentEvent.getId()).setValue(mCurrentEvent);
        finish();
    }

    private boolean validateInputFileName(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void setImageBack() {
        if (mCurrentEvent.getImage() != null) {
            VolleyHelper.getInstance(this).getImageLoader().get(mCurrentEvent.getImage(), ImageLoader.getImageListener(mToolbarBackgroundImage,
                    R.drawable.image_border, android.R.drawable.ic_dialog_alert));
        } else {
            Bitmap bitmap = BitmapUtil.UriToBitmap(this, mCurrentEvent.getUri());
            if (bitmap != null)
                mToolbarBackgroundImage.setImageBitmap(bitmap);
        }

    }

}

