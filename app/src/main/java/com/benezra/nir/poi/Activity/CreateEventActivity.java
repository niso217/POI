package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Calendar;

import com.benezra.nir.poi.Adapter.ViewHolders;
import com.benezra.nir.poi.BaseActivity;
import com.benezra.nir.poi.Bitmap.BitmapUtil;
import com.benezra.nir.poi.Bitmap.DateUtil;
import com.benezra.nir.poi.ChatActivity;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragmentNew;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Fragment.ProgressDialogFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Adapter.CustomSpinnerAdapter;
import com.benezra.nir.poi.Fragment.UploadToFireBaseFragment;
import com.benezra.nir.poi.Helper.AsyncGeocoder;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.User;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.Map;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Helper.Constants.ACTION_FINISH;
import static com.benezra.nir.poi.Helper.Constants.ACTION_REMOVE;
import static com.benezra.nir.poi.Helper.Constants.ADDRESS;
import static com.benezra.nir.poi.Helper.Constants.DETAILS;
import static com.benezra.nir.poi.Helper.Constants.END;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ID;
import static com.benezra.nir.poi.Helper.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Helper.Constants.EVENT_START;
import static com.benezra.nir.poi.Helper.Constants.EVENT_TITLE;
import static com.benezra.nir.poi.Helper.Constants.ID;
import static com.benezra.nir.poi.Helper.Constants.IMAGE;
import static com.benezra.nir.poi.Helper.Constants.INTEREST;
import static com.benezra.nir.poi.Helper.Constants.LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.OWNER;
import static com.benezra.nir.poi.Helper.Constants.PARTICIPATES;
import static com.benezra.nir.poi.Helper.Constants.START;
import static com.benezra.nir.poi.Helper.Constants.TITLE;


public class CreateEventActivity extends BaseActivity
        implements View.OnClickListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        RecyclerTouchListener.ClickListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        CompoundButton.OnCheckedChangeListener,
        ImageCameraDialogFragmentNew.ImageCameraDialogCallbackNew,
        TextWatcher,
        AppBarLayout.OnOffsetChangedListener,
        AdapterView.OnItemSelectedListener,
        AlertDialogFragment.DialogListenerCallback,
        MapFragment.MapFragmentCallback,
        PlaceSelectionListener,
        View.OnFocusChangeListener {

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    TextView tvDatePicker, tvTimePicker;
    private Switch mSwitch;
    private Event mCurrentEvent;
    final static String TAG = CreateEventActivity.class.getSimpleName();
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView mToolbarBackgroundImage;
    private Calendar mEventTime;
    private FirebaseDatabase mFirebaseInstance;
    private Spinner mspinnerCustom;
    private ArrayList<String> mInterestsList;
    private CustomSpinnerAdapter mCustomSpinnerAdapter;
    private EditText mEventDetails, mTitle;
    //private ParticipatesAdapter mParticipatesAdapterAdapter;
    private ProgressBar mProgressBar;
    private ProgressDialogFragment mProgressDialogFragment;
    private boolean mMode; //true = new | false = edit
    private FusedLocationProviderClient mFusedLocationClient;
    private RecyclerView mRecyclerView;
    private ArrayList<User> mParticipates;
    private Menu mMenu;
    private ImageButton mAddImage, mChat, mShare, mLocation, mClear, mSave, mDelete;
    private MapFragment mapFragment;
    private boolean mCanDrag = true;
    private AppBarLayout mAppBarLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private RecyclerView mPicturesRecyclerView;
    private RecyclerView mParticipateRecyclerView;
    private boolean mTouchEventFired;
    private int mScrollDirection;
    private int mCurrentOffset;
    private LinearLayout mHorizontalScrollView;
    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    private LinearLayout mPlaceAutoCompleteLayout;
    private Toolbar mToolbar;
    private final static int DETAILS_FOCUS = 0;
    private final static int TITLE_FOCUS = 1;
    private int mFocusedEditText;
    private boolean[] mEventChangeFlag;


    private FirebaseRecyclerAdapter<String, ViewHolders.PicturesViewHolder> mPicturesAdapter;
    private FirebaseRecyclerAdapter<User, ViewHolders.ParticipatesViewHolder> mParticipateAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventTime = Calendar.getInstance();

        mHorizontalScrollView = (LinearLayout) findViewById(R.id.scrolling_icons);

        mEventChangeFlag = new boolean[7];
        //[0]  - title
        //[1] - details
        //[2] - lat
        //[3] - long
        //[4] - time
        //[5] - date
        //[6] - interest

        //Using the ToolBar as ActionBar
        //Find the toolbar view inside the activity layout
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //Setting the category name onto collapsing toolbar
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        mShare = (ImageButton) findViewById(R.id.btn_share);
        mAddImage = (ImageButton) findViewById(R.id.btn_add_image);
        mChat = (ImageButton) findViewById(R.id.btn_chat);
        mLocation = (ImageButton) findViewById(R.id.btn_location);
        mClear = (ImageButton) findViewById(R.id.btn_clear);
        mSave = (ImageButton) findViewById(R.id.btn_save);
        mDelete = (ImageButton) findViewById(R.id.btn_delete);

        mShare.setOnClickListener(this);
        mAddImage.setOnClickListener(this);
        mChat.setOnClickListener(this);
        mSave.setOnClickListener(this);
        mClear.setOnClickListener(this);
        mDelete.setOnClickListener(this);


        mLocation.setOnClickListener(this);

        mPlaceAutoCompleteLayout = (LinearLayout) findViewById(R.id.place_autocomplete_layout);


        if (mPlaceAutocompleteFragment == null) {
            mPlaceAutocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

            mPlaceAutocompleteFragment.setOnPlaceSelectedListener(this);
        }

        //Setting the styles to expanded and collapsed toolbar
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        //Setting the category mDialogImageView onto collapsing toolbar
        mToolbarBackgroundImage = (ImageView) findViewById(R.id.backdrop);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        //Setting the paragraph text onto TextView
        mEventDetails = (EditText) findViewById(R.id.tv_desciption);
        mTitle = (EditText) findViewById(R.id.tv_title);

        mEventDetails.setOnFocusChangeListener(this);
        mTitle.setOnFocusChangeListener(this);


        tvDatePicker = (TextView) findViewById(R.id.tv_date);
        tvDatePicker.setText(DateUtil.CalendartoDate(Calendar.getInstance().getTime()));
        tvTimePicker = (TextView) findViewById(R.id.tv_time);

        mspinnerCustom = (Spinner) findViewById(R.id.spinnerCustom);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);

        mSwitch = (Switch) findViewById(R.id.tgl_allday);


        //mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        mPicturesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_pictures);
        mPicturesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mPicturesRecyclerView.setNestedScrollingEnabled(false);
        mPicturesRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mPicturesRecyclerView, this));
        //mPicturesRecyclerView.setHasFixedSize(true);


        mParticipateRecyclerView = (RecyclerView) findViewById(R.id.participate_recycler_view);
        mParticipateRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mParticipateRecyclerView.setNestedScrollingEnabled(false);
        //mParticipateRecyclerView.setHasFixedSize(true);


        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.class.getSimpleName());
        if (mapFragment == null) {
            Log.d(TAG, "map fragment null");
            mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mapFragment, MapFragment.class.getSimpleName()).commit();
        }


        tvDatePicker.setOnClickListener(this);
        tvTimePicker.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(this);
        collapsingToolbar.setOnClickListener(this);
        mspinnerCustom.setOnItemSelectedListener(this);
        mAppBarLayout.addOnOffsetChangedListener(this);


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mInterestsList = savedInstanceState.getStringArrayList("interests");
            mParticipates = savedInstanceState.getParcelableArrayList("participates");
            mMode = savedInstanceState.getBoolean("mode");
            mTouchEventFired = savedInstanceState.getBoolean("touch");

            setEventFields();


        } else {
            mInterestsList = new ArrayList<>();
            mParticipates = new ArrayList<>();

            if (getIntent().getStringExtra(EVENT_ID) != null) {
                mMode = false; //edit  existing event
                getEventIntent(getIntent());
            } else {
                mMode = true; //new event
                mCurrentEvent = new Event(UUID.randomUUID().toString(), mFirebaseUser.getUid());

            }
            setAppBarOffset();


        }
        initView();
        initCustomSpinner();
        addInterestsChangeListener();
        setCoordinatorLayoutBehavior();
        addImagesChangeListener();
        participatesChangeListener();

        mEventDetails.addTextChangedListener(this);
        mTitle.addTextChangedListener(this);


    }

    private void initView() {
        if (mMode) {
            mShare.setEnabled(false);
            mAddImage.setEnabled(false);
            mChat.setEnabled(false);
            mDelete.setVisibility(View.GONE);

        } else {
            mSave.setEnabled(false);
            mShare.setEnabled(true);
            mAddImage.setEnabled(true);
            mChat.setEnabled(true);
            mDelete.setVisibility(View.VISIBLE);
        }
    }

    public void HideShowPlaceAutoComplete(boolean visible) {
        if (visible)
            mPlaceAutoCompleteLayout.setVisibility(View.VISIBLE);
        else
            mPlaceAutoCompleteLayout.setVisibility(View.INVISIBLE);


    }

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
            case R.id.btn_chat:
                Intent i = new Intent(CreateEventActivity.this, ChatActivity.class);
                i.putExtra(EVENT_ID, mCurrentEvent.getId());
                startActivity(i);
                break;
            case R.id.btn_add_image:
                buildImageAndTitleChooser();
                break;
            case R.id.btn_clear:
                setAppBarOffset();
                break;
            case R.id.btn_location:
                mapFragment.initFusedLocation();
                break;
            case R.id.btn_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBodyText = "Check it out. Your message goes here";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
                break;
            case R.id.btn_save:
                checkEvent();
                break;
            case R.id.btn_delete:
                BuildDeleteFragment();
                break;

        }
    }

    private void deleteEvent() {
        showProgress(getString(R.string.delete_event), getString(R.string.please_wait));

        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgressMessage();
                finish();
            }
        });

    }

    private void setAppBarOffset() {

        mAppBarLayout.post(new Runnable() {
            @Override
            public void run() {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
                AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
                behavior.onNestedFling(mCoordinatorLayout, mAppBarLayout, null, 0, mAppBarLayout.getTotalScrollRange() * 2, false);

            }
        });

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

//        if (mCurrentEvent.getImage() == null && mCurrentEvent.getUri() == null)
//            BuildReturnDialogFragment();
//        else
        saveEventToFirebase();

    }

    private void BuildReturnDialogFragment() {
        AlertDialogFragment alertDialog = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.class.getName());
        if (alertDialog == null) {
            Log.d(TAG, "opening alert dialog");
            HashMap<Integer, String> map = new HashMap<>();
            map.put(BUTTON_POSITIVE, getString(R.string.sure));
            map.put(BUTTON_NEUTRAL, getString(R.string.return_to_event));
            alertDialog = AlertDialogFragment.newInstance(
                    getString(R.string.unsave_saved), getString(R.string.discard_event), map, ACTION_FINISH);
            alertDialog.show(getSupportFragmentManager(), AlertDialogFragment.class.getName());

        }
    }

    private void BuildDeleteFragment() {
        AlertDialogFragment alertDialog = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.class.getName());
        if (alertDialog == null) {
            Log.d(TAG, "opening alert dialog");
            HashMap<Integer, String> map = new HashMap<>();
            map.put(BUTTON_POSITIVE, getString(R.string.delete));
            map.put(BUTTON_NEUTRAL, getString(R.string.cancel));
            alertDialog = AlertDialogFragment.newInstance(
                    getString(R.string.delete_event_title), getString(R.string.delete_event_message), map, ACTION_REMOVE);
            alertDialog.show(getSupportFragmentManager(), AlertDialogFragment.class.getName());

        }
    }


    @Override
    public void onFinishDialog(int state, int action) {

        switch (state) {
            case BUTTON_POSITIVE:
                if (action == ACTION_REMOVE)
                    deleteEvent();
                else
                    finish();
                break;
            case BUTTON_NEGATIVE:
                break;
        }
    }

    private void saveEvent() {
//        if (mCurrentEvent.getUri() != null)
//            uploadBytes(mCurrentEvent.getUri());
//        else
        saveEventToFirebase();
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String thedate = DateUtil.FormatDate(year, month, dayOfMonth);
        tvDatePicker.setText(thedate);

        mEventTime.set(Calendar.YEAR, year);
        mEventTime.set(Calendar.MONTH, month);
        mEventTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (!mMode && mCurrentEvent.getStart() != mEventTime.getTimeInMillis())
            setSaveEnable();

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
        switch (mFocusedEditText) {
            case DETAILS_FOCUS:
                if (!mMode && !s.toString().equals(mCurrentEvent.getDetails()))
                    setSaveEnable();

                mCurrentEvent.setDetails(s.toString());
                break;
            case TITLE_FOCUS:
                if (!mMode && !s.toString().equals(mCurrentEvent.getTitle()))
                    setSaveEnable();


                mCurrentEvent.setTitle(s.toString());
                break;
        }
    }

    private void setSaveEnable() {
        mSave.setEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String interest = parent.getItemAtPosition(position).toString();
        if (!mMode && !mCurrentEvent.getInterest().equals(interest))
            setSaveEnable();

        mCurrentEvent.setInterest(interest);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private void setCoordinatorLayoutBehavior() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return mCanDrag;
            }
        });
        params.setBehavior(behavior);
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

    @Override
    public void onBackPressed() {
        if (!mCanDrag)
            setAppBarOffset();
        else {
            if (mMode)
                BuildReturnDialogFragment();
            else
                super.onBackPressed();
        }


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
        mCurrentEvent.setAddress(intent.getStringExtra(EVENT_ADDRESS));

        setEventFields();


    }

    @Override
    public void onPlaceSelected(Place place) {
        mapFragment.setEventLocation(place.getLatLng(), place.getAddress().toString());
        mapFragment.addSingeMarkerToMap(place.getLatLng());


    }

    @Override
    public void onError(Status status) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mCurrentEvent.getLatitude() != 0)
            mapFragment.addSingeMarkerToMap(mCurrentEvent.getLatlng());

    }

    @Override
    public void onEventLocationChanged(LatLng latLng) {
        if (distance(mCurrentEvent.getLatlng(), latLng) > 0.02)  // if distance < 30 meters we take locations as equal
            setSaveEnable();
        mCurrentEvent.setLatLang(latLng);


    }


    //navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});


    private void addImagesChangeListener() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("pictures");

        mPicturesAdapter = new FirebaseRecyclerAdapter<String, ViewHolders.PicturesViewHolder>(
                String.class, R.layout.grid_item_event_pic, ViewHolders.PicturesViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewHolders.PicturesViewHolder picturesViewHolder, String model, int position) {
                Picasso.with(CreateEventActivity.this)
                        .load(model)
                        .error(R.drawable.common_google_signin_btn_icon_dark)
                        .into(picturesViewHolder.imgThumbnail);

            }

        };

        mPicturesRecyclerView.setAdapter(mPicturesAdapter);

    }

    private void participatesChangeListener() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates");

        mParticipateAdapter = new FirebaseRecyclerAdapter<User, ViewHolders.ParticipatesViewHolder>(
                User.class, R.layout.participate_list_row, ViewHolders.ParticipatesViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewHolders.ParticipatesViewHolder participatesViewHolder, User model, int position) {
                participatesViewHolder.name.setText(model.getName());
                Picasso.with(CreateEventActivity.this)
                        .load(model.getAvatar())
                        .error(R.drawable.common_google_signin_btn_icon_dark)
                        .into(participatesViewHolder.image);
            }

        };

        mParticipateRecyclerView.setAdapter(mParticipateAdapter);

    }


    private void initCustomSpinner() {
        mCustomSpinnerAdapter = new CustomSpinnerAdapter(this, new ArrayList<String>(mInterestsList));
        mspinnerCustom.setAdapter(mCustomSpinnerAdapter);
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


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("event", mCurrentEvent);
        outState.putStringArrayList("interests", mInterestsList);
        outState.putParcelableArrayList("participates", mParticipates);
        outState.putBoolean("mode", mMode);
        outState.putBoolean("touch", mTouchEventFired);


    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            mTitle.setText(mCurrentEvent.getTitle());
            //setImageBack();
            mEventDetails.setText(mCurrentEvent.getDetails());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mCurrentEvent.getStart());
            tvDatePicker.setText(DateUtil.CalendartoDate(calendar.getTime()));
        }

    }


    @Override
    public void navigateToCaptureFragment(String[] permissions) {

        if (isPermissionGranted(permissions)) {


            if (Arrays.asList(permissions).contains(ACCESS_FINE_LOCATION)) {
                initFusedLocation();
            }
            if (Arrays.asList(permissions).contains(Manifest.permission.CAMERA)) {
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

    private boolean isPermissionGranted(String[] permissions) {
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
                            getAddress();
                        }
                    }
                });
    }

    private void getAddress() {
        new AsyncGeocoder(new AsyncGeocoder.onAddressFoundListener() {
            @Override
            public void onAddressFound(String result) {
                mCurrentEvent.setAddress(result);
                Log.d(TAG, "the address is: " + result);

            }
        }).execute(new AsyncGeocoder.AsyncGeocoderObject(
                new Geocoder(this),
                mCurrentEvent.getLocation()));
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


            showProgress(getString(R.string.creating_event), getString(R.string.please_wait));

            Bitmap bitmap = BitmapUtil.UriToBitmap(this, picUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();

            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("images").child(mCurrentEvent.getId());
            fileRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

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

                            hideProgressMessage();
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
                            setProgressValue(progress);

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


        GeoHash geoHash = new GeoHash(new GeoLocation(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude()));
        Map<String, Object> updates = new HashMap<>();

        if (mMode) {
            Map<String, User> map = setOwnerAsParticipate();
            updates.put(PARTICIPATES, map);
            showProgress(getString(R.string.creating_event), getString(R.string.please_wait));


        } else
            showProgress(getString(R.string.updating_event), getString(R.string.please_wait));


        updates.put(DETAILS, mCurrentEvent.getDetails());
        updates.put(START, mCurrentEvent.getStart());
        updates.put(END, mCurrentEvent.getEnd());
        updates.put(IMAGE, mCurrentEvent.getImage());
        updates.put(LATITUDE, mCurrentEvent.getLatitude());
        updates.put(LONGITUDE, mCurrentEvent.getLongitude());
        updates.put(TITLE, mCurrentEvent.getTitle());
        updates.put(INTEREST, mCurrentEvent.getInterest());
        updates.put(ADDRESS, mCurrentEvent.getAddress());
        updates.put(OWNER, mCurrentEvent.getOwner());

        updates.put("/g", geoHash.getGeoHashString());
        updates.put("/l", Arrays.asList(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude()));
        eventReference.updateChildren(updates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgressMessage();
                Toast.makeText(CreateEventActivity.this, getString(R.string.event_created), Toast.LENGTH_SHORT).show();
                //finish();
                mMode = false;
                initView();

            }
        });

    }

    private void showProgress(String title, String message) {
        mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment == null) {
            Log.d(TAG, "opening origress dialog");
            mProgressDialogFragment = ProgressDialogFragment.newInstance(
                    title, message, ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getName());
        }
    }

    private void hideProgressMessage() {
        mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment != null)
            mProgressDialogFragment.dismiss();

    }

    private void setProgressValue(double progress) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("prg", (int) progress);
        message.setData(bundle);
        if (mProgressDialogFragment != null)
            mProgressDialogFragment.setProgress(message);
    }

    private Map<String, User> setOwnerAsParticipate() {
        User owner = new User();
        owner.setName(mFirebaseUser.getDisplayName());
        owner.setEmail(mFirebaseUser.getEmail());
        owner.setAvatar(mFirebaseUser.getPhotoUrl().toString());
        HashMap<String, User> map = new HashMap<>();
        map.put(mFirebaseUser.getUid(), owner);
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

    private void setMenuItemChecked(MenuItem chatMenuItem) {

        if (!mMode) {
            chatMenuItem.setVisible(true);
        } else {
            chatMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        mTouchEventFired = true;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        float bottom = mapFragment.getBottomHeight();

        try {

            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                if (!mCanDrag)
                    if (y > mCoordinatorLayout.getMeasuredHeight() - bottom)
                        mCanDrag = true;
                if (mEventDetails.isFocused()) mEventDetails.clearFocus();
                if (mTitle.isFocused()) mTitle.clearFocus();


            }

            if (ev.getAction() == MotionEvent.ACTION_UP && mCanDrag) {
                float per = Math.abs(mAppBarLayout.getY()) / mAppBarLayout.getTotalScrollRange();
                boolean setExpanded = (per <= 0.2F);
                if (setExpanded) {
                    if (mScrollDirection < 0)
                        mAppBarLayout.setExpanded(setExpanded, true);

                }

            }

            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            Log.e(TAG, "dispatchTouchEvent " + e.toString());
            return false;
        }
    }

    private void buildImageAndTitleChooser() {

        ImageCameraDialogFragmentNew ImageCameraFragment = (ImageCameraDialogFragmentNew) getSupportFragmentManager().findFragmentByTag(ImageCameraDialogFragmentNew.class.getName());

        if (ImageCameraFragment == null) {
            Log.d(TAG, "opening image camera dialog");
            ImageCameraFragment = ImageCameraDialogFragmentNew.newInstance();
            ImageCameraFragment.show(getSupportFragmentManager(), ImageCameraDialogFragmentNew.class.getName());

        }

    }

    private void BuildProgressDialogFragment(Uri uri) {
        UploadToFireBaseFragment progressDialog = (UploadToFireBaseFragment) getSupportFragmentManager().findFragmentByTag(UploadToFireBaseFragment.class.getName());
        if (progressDialog == null) {
            Log.d(TAG, "opening origress dialog");
            progressDialog = UploadToFireBaseFragment.newInstance(uri, mCurrentEvent.getId());
            progressDialog.show(getSupportFragmentManager(), UploadToFireBaseFragment.class.getName());

        }
    }

    @Override
    public void DialogResults(Uri picUri) {
        BuildProgressDialogFragment(picUri);
    }

    @Override
    public void onClick(View view, int position) {
        Intent galleryIntent = new Intent(CreateEventActivity.this, SpaceGalleryActivity.class);
        galleryIntent.putExtra(ID, mCurrentEvent.getId());
        startActivity(galleryIntent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int lastOfsset = mCurrentOffset;


        mCurrentOffset = Math.abs(verticalOffset);

        Log.d(TAG, mCurrentOffset + "");


        if (mCurrentOffset - lastOfsset > 0)
            mScrollDirection = 1;
        else
            mScrollDirection = -1;


        if (mCurrentOffset == 0) {
            mCanDrag = false;
            if (mPlaceAutoCompleteLayout.getVisibility() == View.GONE)
                mPlaceAutoCompleteLayout.setVisibility(View.VISIBLE);

            if (mTouchEventFired && mHorizontalScrollView.getVisibility() == View.VISIBLE)
                setVisibility(mHorizontalScrollView, 0.0f);

        } else

        {
            mCanDrag = true;
            if (mPlaceAutoCompleteLayout.getVisibility() == View.VISIBLE)
                mPlaceAutoCompleteLayout.setVisibility(View.GONE);
        }
        if (mTouchEventFired) {

            if (mCurrentOffset < mHorizontalScrollView.getHeight()) {
                if (mHorizontalScrollView.getVisibility() == View.VISIBLE)
                    setVisibility(mHorizontalScrollView, 0.0f);
            } else {


                if (mCurrentOffset < mAppBarLayout.getTotalScrollRange() - mHorizontalScrollView.getHeight()) {
                    if (mHorizontalScrollView.getVisibility() == View.GONE)
                        setVisibility(mHorizontalScrollView, 1.0f);
                } else {
                    if (mHorizontalScrollView.getVisibility() == View.VISIBLE)
                        setVisibility(mHorizontalScrollView, 0.0f);
                }

            }
        }


    }


    private void setVisibility(final View view, final float alpha) {
        view.animate()
                .alpha(alpha)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (alpha == 1.0f)
                            view.setVisibility(View.VISIBLE);
                        else
                            view.setVisibility(View.GONE);

                    }
                });
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.tv_title:
                mFocusedEditText = TITLE_FOCUS;
                break;
            case R.id.tv_desciption:
                mFocusedEditText = DETAILS_FOCUS;
                break;
        }
    }

    /**
     * calculates the distance between two locations in MILES
     */
    private double distance(LatLng latLng1, LatLng latLng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(latLng2.latitude - latLng1.latitude);
        double dLng = Math.toRadians(latLng2.longitude - latLng1.longitude);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(latLng1.latitude)) * Math.cos(Math.toRadians(latLng2.latitude));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }
}

