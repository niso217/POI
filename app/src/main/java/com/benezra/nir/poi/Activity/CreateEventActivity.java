package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Arrays;
import java.util.Calendar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.benezra.nir.poi.Adapter.EventImagesAdapter;
import com.benezra.nir.poi.Adapter.EventParticipatesAdapter;
import com.benezra.nir.poi.Fragment.ChatFragment;
import com.benezra.nir.poi.Fragment.ProgressDialogFragment;
import com.benezra.nir.poi.Fragment.UploadEventToFireBaseFragment;
import com.benezra.nir.poi.Helper.SharePref;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.benezra.nir.poi.Utils.DateUtil;
import com.benezra.nir.poi.Fragment.CustomPlaceAutoCompleteFragment;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragment;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Adapter.CustomSpinnerAdapter;
import com.benezra.nir.poi.Fragment.UploadImageToFireBaseFragment;
import com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.Objects.User;
import com.benezra.nir.poi.Utils.LocationUtil;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Fragment.MapFragment.EVENT_LOC_TAB;
import static com.benezra.nir.poi.Fragment.MapFragment.LOCATION_TAB;
import static com.benezra.nir.poi.Fragment.MapFragment.SEARCH_TAB;
import static com.benezra.nir.poi.Interface.Constants.CREATE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_END;
import static com.benezra.nir.poi.Interface.Constants.ID_TOKEN;
import static com.benezra.nir.poi.Interface.Constants.PROD_ADD;
import static com.benezra.nir.poi.Interface.Constants.PROD_UPDATE;
import static com.benezra.nir.poi.Interface.Constants.REMOVE;
import static com.benezra.nir.poi.Interface.Constants.STATUS;
import static com.benezra.nir.poi.Interface.Constants.UPDATE;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_ANCHORED;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_COLLAPSED;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_DRAGGING;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_EXPANDED;
import static com.benezra.nir.poi.Interface.Constants.ACTION_FINISH;
import static com.benezra.nir.poi.Interface.Constants.ACTION_REMOVE;
import static com.benezra.nir.poi.Interface.Constants.ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.DETAILS;
import static com.benezra.nir.poi.Interface.Constants.END;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ID;
import static com.benezra.nir.poi.Interface.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Interface.Constants.EVENT_START;
import static com.benezra.nir.poi.Interface.Constants.EVENT_TITLE;
import static com.benezra.nir.poi.Interface.Constants.ID;
import static com.benezra.nir.poi.Interface.Constants.IMAGE;
import static com.benezra.nir.poi.Interface.Constants.INTEREST;
import static com.benezra.nir.poi.Interface.Constants.LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.OWNER;
import static com.benezra.nir.poi.Interface.Constants.PARTICIPATES;
import static com.benezra.nir.poi.Interface.Constants.START;
import static com.benezra.nir.poi.Interface.Constants.TITLE;


public class CreateEventActivity extends AppCompatActivity
        implements View.OnClickListener,
        RecyclerTouchListener.ClickListener,
        ImageCameraDialogFragment.ImageCameraDialogCallbackNew,
        TextWatcher,
        AdapterView.OnItemSelectedListener,
        AlertDialogFragment.DialogListenerCallback,
        MapFragment.MapFragmentCallback,
        PlaceSelectionListener,
        View.OnFocusChangeListener,
        UploadImageToFireBaseFragment.UploadListener,
        GoogleMapsBottomSheetBehavior.BottomSheetCallback,
        TabLayout.OnTabSelectedListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        Response.Listener,
        Response.ErrorListener,
        ViewTreeObserver.OnGlobalLayoutListener, UploadEventToFireBaseFragment.UploadEventListener {

    private static final String LIST_STATE_KEY = "state";
    private static final String SCROLL_STATE = "scroll_state";

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private TextView tvDatePickerStart, tvDatePickerEnd, tvTimePickerStart, tvTimePickerEnd;
    private LinearLayout mLayoutStart, mLayoutEnd;
    private Event mCurrentEvent;
    private Event mCurrentEventChangeFlag;
    final static String TAG = CreateEventActivity.class.getSimpleName();
    private Calendar mEventTimeStart;
    private Calendar mEventTimeEnd;
    private FirebaseDatabase mFirebaseInstance;
    private SearchableSpinner mspinnerCustom;
    private ArrayList<EventsInterestData> mInterestsList;
    private CustomSpinnerAdapter mCustomSpinnerAdapter;
    private EditText mEventDetails, mTitle;
    private ProgressBar mProgressBar;
    private boolean mMode; //true = new | false = edit
    private FusedLocationProviderClient mFusedLocationClient;
    private ArrayList<User> mParticipates;
    private ImageButton mAddImage, mChat, mShare, mLocation, mClear, mSave, mDelete;
    private MapFragment mapFragment;
    private RecyclerView mPicturesRecyclerView;
    private RecyclerView mParticipateRecyclerView;
    private LinearLayout mHorizontalScrollView;
    private CustomPlaceAutoCompleteFragment mPlaceAutocompleteFragment;
    private LinearLayout mPlaceAutoCompleteLayout;
    private final static int DETAILS_FOCUS = 0;
    private final static int TITLE_FOCUS = 1;
    private int mFocusedEditText;
    private Set<String> mPicturesKeys;
    private GoogleMapsBottomSheetBehavior behavior;
    private NestedScrollView mNestedScrollView;
    private EventImagesAdapter mEventImagesAdapter;
    private ArrayList<User> mEventParticipateList;
    private ArrayList<String> mEventImagesList;
    private TextView mTextViewDistance;
    private LinearLayout mNavigationBarLayout;
    public static final String ACTION_SHOW_ANYWAYS = TAG + ".ACTION_SHOW_ANYWAYS";
    private CoordinatorLayout mCoordinatorLayout;
    private EventParticipatesAdapter mParticipateAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Parcelable mListState;
    private int mScrollingState;


    private TabLayout mTabLayout;


    private int mTabSelectedIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventTimeStart = Calendar.getInstance();
        mEventTimeEnd = Calendar.getInstance();

        mScrollingState = STATE_ANCHORED;
        mEventImagesList = new ArrayList<>();
        mEventImagesAdapter = new EventImagesAdapter(this, mEventImagesList);
        mEventParticipateList = new ArrayList<>();
        mParticipateAdapter = new EventParticipatesAdapter(this, mEventParticipateList);
        mPicturesKeys = new HashSet<>();


        initView();

        initParticipateRecycleView();

        initImageRecycleView();

        inflateMapFragment();

        inflateMapAutoCompleteFragment();

        addKeboardChangeListener();


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mCurrentEventChangeFlag = savedInstanceState.getParcelable("event_clone");
            mInterestsList = savedInstanceState.getParcelableArrayList("interests");
            mParticipates = savedInstanceState.getParcelableArrayList("participates");
            mMode = savedInstanceState.getBoolean("mode");
            mTabSelectedIndex = savedInstanceState.getInt("tab_selected_index");
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            mScrollingState = savedInstanceState.getInt(SCROLL_STATE);
            setEventFields();


        } else {
            mTabSelectedIndex = LOCATION_TAB;
            mInterestsList = new ArrayList<>();
            mParticipates = new ArrayList<>();

            if (getIntent().getStringExtra(EVENT_ID) != null) {
                mMode = false; //edit  existing event
                getEventIntent(getIntent());
            } else {
                mMode = true; //new event
                mCurrentEvent = new Event(UUID.randomUUID().toString(), mFirebaseUser.getUid());
                setAllDayStartTime();
                setAllDayEndTime();


            }


        }
        initMode();
        initCustomSpinner();

        initListeners();


    }

    private int calculateDeviceHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    private void initView() {
        mNavigationBarLayout = findViewById(R.id.tab_layout);
        mParticipateRecyclerView = findViewById(R.id.participate_recycler_view);
        mHorizontalScrollView = findViewById(R.id.scrolling_icons);
        mShare = findViewById(R.id.btn_share);
        mAddImage = findViewById(R.id.btn_add_image);
        mChat = findViewById(R.id.btn_chat);
        mLocation = findViewById(R.id.btn_location);
        mClear = findViewById(R.id.btn_clear);
        mSave = findViewById(R.id.btn_save);
        mDelete = findViewById(R.id.btn_delete);
        mPlaceAutoCompleteLayout = findViewById(R.id.place_autocomplete_layout);
        mEventDetails = findViewById(R.id.tv_desciption);
        mTitle = findViewById(R.id.tv_title);
        mCoordinatorLayout = findViewById(R.id.coordinatorlayout);
        mPicturesRecyclerView = findViewById(R.id.recycler_view_pictures);

        mLayoutStart = findViewById(R.id.layout_start);
        mLayoutEnd = findViewById(R.id.layout_end);

        tvDatePickerStart = findViewById(R.id.tv_date_start);
        tvDatePickerEnd = findViewById(R.id.tv_date_end);
        tvTimePickerStart = findViewById(R.id.tv_time_start);
        tvTimePickerEnd = findViewById(R.id.tv_time_end);

        setTimeText();

        mspinnerCustom = findViewById(R.id.spinnerCustom);
        mspinnerCustom.setTitle("Select Item");
        mspinnerCustom.setPositiveButton("OK");
        mProgressBar = findViewById(R.id.pb_loading);
        mNestedScrollView = findViewById(R.id.nestedscrollview);
        mTabLayout = findViewById(R.id.tab_layout_tab);
        mTextViewDistance = findViewById(R.id.tv_distance);
        behavior = GoogleMapsBottomSheetBehavior.from(mNestedScrollView);
        behavior.setAnchorHeight(calculateDeviceHeight() / 2);
        behavior.setHideable(false);
        behavior.setPeekHeight(100);
        behavior.setParallax(mPicturesRecyclerView);

    }

    private void initListeners() {
        mLayoutStart.setOnClickListener(this);
        mLayoutEnd.setOnClickListener(this);
        mspinnerCustom.setOnItemSelectedListener(this);
        mShare.setOnClickListener(this);
        mAddImage.setOnClickListener(this);
        mChat.setOnClickListener(this);
        mSave.setOnClickListener(this);
        mClear.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mLocation.setOnClickListener(this);
        mEventDetails.setOnFocusChangeListener(this);
        mTitle.setOnFocusChangeListener(this);
        mEventDetails.addTextChangedListener(this);
        behavior.setBottomSheetCallback(this);
        mTabLayout.addOnTabSelectedListener(this);
        mTitle.addTextChangedListener(this);
        addInterestsChangeListener();
        getAllEventImages();
        participatesChangeListener();
        mPicturesRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mNestedScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (isVisible(ChatFragment.class.getSimpleName()))
                    setNestedScrollViewMargin(100);
                else
                    setNestedScrollViewMargin(0);


            }
        });

    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        mScrollingState = newState;
        initScrollViewState();
    }


    private void initScrollViewState() {

        switch (mScrollingState) {
            case STATE_DRAGGING:
                mNavigationBarLayout.setVisibility(View.GONE);
                mHorizontalScrollView.setVisibility(View.VISIBLE);
                setVisibility(mPicturesRecyclerView, 1.0f, 300, true);
                break;
            case STATE_EXPANDED:
                mHorizontalScrollView.setVisibility(View.GONE);
                break;
            case STATE_COLLAPSED:
                if (!isVisible(ChatFragment.class.getSimpleName()))
                    mNavigationBarLayout.setVisibility(View.VISIBLE);
                else
                    mNavigationBarLayout.setVisibility(View.GONE);

                mHorizontalScrollView.setVisibility(View.GONE);
                setVisibility(mPicturesRecyclerView, 0.0f, 0, false);
                break;
            case STATE_ANCHORED:
                mNavigationBarLayout.setVisibility(View.GONE);
                mHorizontalScrollView.setVisibility(View.VISIBLE);
                setVisibility(mPicturesRecyclerView, 1.0f, 300, true);
                break;


        }
    }

    private boolean isVisible(String fragment) {
        Fragment hm = getSupportFragmentManager().findFragmentByTag(fragment);
        if (hm != null)
            if (hm.isVisible())
                return true;


        return false;
    }


    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }


    private void initParticipateRecycleView() {
        mParticipateRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mParticipateRecyclerView.setNestedScrollingEnabled(false);
        mParticipateRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mParticipateRecyclerView, null));
        mParticipateRecyclerView.setAdapter(mParticipateAdapter);
    }


    private void initImageRecycleView() {
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, true);
        mPicturesRecyclerView.setLayoutManager(mLinearLayoutManager);
        mPicturesRecyclerView.setNestedScrollingEnabled(false);
        mPicturesRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mPicturesRecyclerView, this));
        mPicturesRecyclerView.setAdapter(mEventImagesAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mPicturesRecyclerView);
    }


    private void getAllEventImages() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("pictures");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventImagesList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        EventPhotos eventPhotos = data.getValue(EventPhotos.class);
                        mEventImagesList.add(eventPhotos.getUrl());

                    }
                    mEventImagesAdapter.notifyDataSetChanged();
                    initScrollViewState();

                    if (mListState != null) {
                        mLinearLayoutManager.onRestoreInstanceState(mListState);
                    }


                } else
                    setVisibility(mPicturesRecyclerView, 0.0f, 0, false);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void inflateMapAutoCompleteFragment() {
        if (mPlaceAutocompleteFragment == null) {
            mPlaceAutocompleteFragment = (CustomPlaceAutoCompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

            mPlaceAutocompleteFragment.setOnPlaceSelectedListener(this);
        }
    }

    private void inflateMapFragment() {
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.class.getSimpleName());
        if (mapFragment == null) {
            Log.d(TAG, "map fragment null");
            mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mapFragment, MapFragment.class.getSimpleName()).commit();
        }
    }


    private void initMode() {
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

            isChangeMade();
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.layout_start:
                initEventStart();
                break;

            case R.id.layout_end:
                initEventEnd();
                break;
            case R.id.btn_chat:
//                Intent i = new Intent(CreateEventActivity.this, ChatActivity.class);
//                i.putExtra(EVENT_ID, mCurrentEvent.getId());
//                startActivity(i);
                behavior.setState(mScrollingState = STATE_COLLAPSED);
                initScrollViewState();
                ChatFragment chat = ChatFragment.newInstance(mCurrentEvent.getId());
                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, chat, ChatFragment.class.getSimpleName()).addToBackStack(null).commit();
                setNestedScrollViewMargin(100);

                break;
            case R.id.btn_add_image:
                navigateToCaptureFragment(new String[]{Manifest.permission.CAMERA});
                break;
            case R.id.btn_clear:
                //setAppBarOffset();
                break;
            case R.id.btn_location:
                navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                break;
            case R.id.btn_share:

                break;
            case R.id.btn_save:
                checkEvent();
                break;
            case R.id.btn_delete:
                BuildDeleteFragment();
                break;

        }
    }

    private void setNestedScrollViewMargin(int top) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mNestedScrollView
                .getLayoutParams();
        layoutParams.setMargins(0, top, 0, 0);
        mNestedScrollView.setLayoutParams(layoutParams);


    }

    private void initEventEnd() {
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTimeInMillis(mEventTimeStart.getTimeInMillis());
        cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
        int year_end = mEventTimeEnd.get(Calendar.YEAR);
        int month_end = mEventTimeEnd.get(Calendar.MONTH);
        int day_end = mEventTimeEnd.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog_end = new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                endDateChange(year, month, dayOfMonth);
                int hour_start = mEventTimeEnd.get(Calendar.HOUR_OF_DAY);
                int minute_start = mEventTimeEnd.get(Calendar.MINUTE);
                new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTimeChanged(hourOfDay, minute);

                    }
                }, hour_start, minute_start, false).show();


            }
        }, year_end, month_end, day_end);
        datePickerDialog_end.getDatePicker().setMinDate(cal.getTimeInMillis());
        datePickerDialog_end.show();
    }

    private void initEventStart() {
        int year_start = mEventTimeStart.get(Calendar.YEAR);
        int month_start = mEventTimeStart.get(Calendar.MONTH);
        int day_start = mEventTimeStart.get(Calendar.DAY_OF_MONTH);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, 1);

        DatePickerDialog datePickerDialog_start = new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                startDateChange(year, month, dayOfMonth);
                int hour_start = mEventTimeStart.get(Calendar.HOUR_OF_DAY);
                int minute_start = mEventTimeStart.get(Calendar.MINUTE);

                if (mEventTimeStart.getTimeInMillis() > mEventTimeEnd.getTimeInMillis()) {
                    mEventTimeEnd.setTimeInMillis(mEventTimeStart.getTimeInMillis());
                    endDateChange(mEventTimeEnd.get(Calendar.YEAR), mEventTimeEnd.get(Calendar.MONTH), mEventTimeEnd.get(Calendar.DAY_OF_MONTH));
                    endTimeChanged(mEventTimeEnd.get(Calendar.HOUR), mEventTimeEnd.get(Calendar.MINUTE));
                }

                new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTimeChanged(hourOfDay, minute);

                    }
                }, hour_start, minute_start, false).show();

            }
        }, year_start, month_start, day_start);
        datePickerDialog_start.getDatePicker().setMinDate(now.getTimeInMillis());
        datePickerDialog_start.show();
    }

    private void delete() {
        BuildUploadEventProgressDialogFragment(REMOVE);
    }


    private void deleteEventStorage() {

        Iterator<String> itr = mPicturesKeys.iterator();
        while (itr.hasNext()) {
            String child = itr.next();
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("images").child(mCurrentEvent.getId()).child(child);
            fileRef.delete();
        }
        finish();


    }


    private void checkEvent() {
        if (mCurrentEvent != null) {

            if (DateUtil.getTimeDiff(mEventTimeEnd.getTimeInMillis(), mEventTimeStart.getTimeInMillis()) < 0) {
                showSnackBar(getString(R.string.time_def));
                return;
            }

            if (mCurrentEvent.getDetails() == null || mCurrentEvent.getDetails().equals("")) {
                showSnackBar(getString(R.string.missing_details));
                return;
            }
            if (mCurrentEvent.getInterest() == null || mCurrentEvent.getInterest().equals("")) {
                showSnackBar(getString(R.string.missing_interest));

                return;
            }
            if (mCurrentEvent.getLatitude() == 0 || mCurrentEvent.getLongitude() == 0) {
                showSnackBar(getString(R.string.missing_location));

                return;
            }
            if (mCurrentEvent.getTitle() == null || mCurrentEvent.getTitle().equals("")) {
                showSnackBar(getString(R.string.missing_title));
                return;
            }
        }


        saveEventToFirebase();

    }

    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();

    }

    private void isChangeMade() {

        if (!mMode) {

            if (isEventChanged())
                setSaveEnable(true);
            else
                setSaveEnable(false);

        }

    }

    private boolean isEventChanged() {
        return (!mCurrentEvent.getTitle().equals(mCurrentEventChangeFlag.getTitle()) ||
                (mCurrentEvent.getImage() != null && !mCurrentEvent.getImage().equals(mCurrentEventChangeFlag.getImage())) ||
                !mCurrentEvent.getDetails().equals(mCurrentEventChangeFlag.getDetails()) ||
                isLocationChanged() ||
                !mCurrentEvent.getInterest().equals(mCurrentEventChangeFlag.getInterest()) ||
                mCurrentEvent.getStart() != mCurrentEventChangeFlag.getStart() ||
                mCurrentEvent.getEnd() != mCurrentEventChangeFlag.getEnd());
    }

    private boolean isLocationChanged() {
        return LocationUtil.distance(mCurrentEvent.getLatlng(), mCurrentEventChangeFlag.getLatlng()) > 0.02;
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
                    delete();
                else {
                    finish();
                }
                break;
            case BUTTON_NEGATIVE:
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    private void addKeboardChangeListener() {
        mCoordinatorLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                mCoordinatorLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = mCoordinatorLayout.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                Log.d("Nifras", "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    Log.d(TAG, "open");
                } else {
                    Log.d(TAG, "close");
                    behavior.setState(mScrollingState);
                    initScrollViewState();
                    mCoordinatorLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }


    private void startDateChange(int year, int month, int dayOfMonth) {
        mEventTimeStart.set(Calendar.YEAR, year);
        mEventTimeStart.set(Calendar.MONTH, month);
        mEventTimeStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mCurrentEvent.setStart(mEventTimeStart.getTimeInMillis());
        setTimeText();
        isChangeMade();

    }


    private void endDateChange(int year, int month, int dayOfMonth) {
        mEventTimeEnd.set(Calendar.YEAR, year);
        mEventTimeEnd.set(Calendar.MONTH, month);
        mEventTimeEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mCurrentEvent.setEnd(mEventTimeEnd.getTimeInMillis());
        setTimeText();
        isChangeMade();


    }

    private void startTimeChanged(int hourOfDay, int minute) {
        mEventTimeStart.set(Calendar.MINUTE, minute);
        mEventTimeStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCurrentEvent.setStart(mEventTimeStart.getTimeInMillis());
        setTimeText();
        isChangeMade();

    }

    private void endTimeChanged(int hourOfDay, int minute) {
        mEventTimeEnd.set(Calendar.MINUTE, minute);
        mEventTimeEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCurrentEvent.setEnd(mEventTimeEnd.getTimeInMillis());
        setTimeText();
        isChangeMade();

    }


    private void setAllDayStartTime() {
        mEventTimeStart.set(Calendar.HOUR_OF_DAY, 0);
        mEventTimeStart.set(Calendar.MINUTE, 0);
        mEventTimeStart.set(Calendar.SECOND, 0);
        mCurrentEvent.setStart(mEventTimeStart.getTimeInMillis());
        setTimeText();

    }

    private void setAllDayEndTime() {
        mEventTimeEnd.set(Calendar.HOUR_OF_DAY, 23);
        mEventTimeEnd.set(Calendar.MINUTE, 59);
        mEventTimeEnd.set(Calendar.SECOND, 0);
        mCurrentEvent.setEnd(mEventTimeEnd.getTimeInMillis());
        setTimeText();

    }

    private void setTimeText() {
        tvTimePickerStart.setText(DateUtil.CalendartoTime(mEventTimeStart.getTime()));
        tvDatePickerEnd.setText(DateUtil.CalendartoDate(mEventTimeEnd.getTime()));
        tvDatePickerStart.setText(DateUtil.CalendartoDate(mEventTimeStart.getTime()));
        tvTimePickerEnd.setText(DateUtil.CalendartoTime(mEventTimeEnd.getTime()));

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
                mCurrentEvent.setDetails(s.toString());
                break;
            case TITLE_FOCUS:
                mCurrentEvent.setTitle(s.toString());
                break;
        }
        isChangeMade();
    }

    private void setSaveEnable(boolean enable) {
        mSave.setEnabled(enable);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String interest = parent.getItemAtPosition(position).toString();
        mCurrentEvent.setInterest(interest);

        isChangeMade();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onBackPressed() {

        if (behavior.getState() == STATE_COLLAPSED) {
            if (isVisible(ChatFragment.class.getSimpleName())) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mNestedScrollView
                        .getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                mNestedScrollView.setLayoutParams(layoutParams);
                getSupportFragmentManager().popBackStack();
            }
            behavior.setState(STATE_ANCHORED);

        } else {

            if (mMode)
                BuildReturnDialogFragment();
            else {
                if (isEventChanged())
                    BuildReturnDialogFragment();
                else
                    super.onBackPressed();
            }
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
        mCurrentEvent.setEnd(intent.getLongExtra(EVENT_END, 0));
        mCurrentEvent.setLatitude(intent.getDoubleExtra(EVENT_LATITUDE, 0));
        mCurrentEvent.setLongitude(intent.getDoubleExtra(EVENT_LONGITUDE, 0));
        mCurrentEvent.setImage(intent.getStringExtra(EVENT_IMAGE));
        mCurrentEvent.setAddress(intent.getStringExtra(EVENT_ADDRESS));

        cloneEvent();
        setEventFields();


    }

    private void cloneEvent() {
        try {
            mCurrentEventChangeFlag = (Event) mCurrentEvent.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        mCurrentEvent.setAddress(place.getAddress().toString());
        mCurrentEvent.setLatLang(place.getLatLng());
        mapFragment.setEventLocation(place.getLatLng(), place.getAddress().toString());
        mTextViewDistance.setText(place.getAddress());
        isChangeMade();


    }

    @Override
    public void onError(Status status) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mCurrentEvent.getLatitude() != 0)
            mapFragment.addSingeMarkerToMap(mCurrentEvent.getLatlng(), mCurrentEvent.getAddress());

//        else if (mMode)
//        SelectCurrentEventPoint();


    }

    @Override
    public void onEventLocationChanged(LatLng latLng, String address) {
        mCurrentEvent.setLatLang(latLng);
        mCurrentEvent.setAddress(address);
        mTextViewDistance.setText(address);
        isChangeMade();

    }

    @Override
    public void LocationPermission() {

    }

    @Override
    public void onDistanceChanged(String add) {
        mTextViewDistance.setText(add);

    }


    private void participatesChangeListener() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventParticipateList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        User user = data.getValue(User.class);
                        mEventParticipateList.add(user);

                    }
                    mParticipateAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void initCustomSpinner() {
        mCustomSpinnerAdapter = new CustomSpinnerAdapter(this, getInterests(mInterestsList));
        mspinnerCustom.setAdapter(mCustomSpinnerAdapter);
    }

    private ArrayList<String> getInterests(ArrayList<EventsInterestData> eventsInterestData) {
        ArrayList<String> interest = new ArrayList<>();
        for (int i = 0; i < eventsInterestData.size(); i++) {
            interest.add(eventsInterestData.get(i).getInterest());
        }
        return interest;
    }


    private void addInterestsChangeListener() {
        mInterestsList.clear();
        mFirebaseInstance.getReference("interests_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot data : snapshot.getChildren()) {
                    EventsInterestData interestData = data.getValue(EventsInterestData.class);
                    mInterestsList.add(interestData);
                    sortList();
                }

                if (mInterestsList != null) {
                    mCustomSpinnerAdapter.updateInterestList(getInterests(mInterestsList));
                    mspinnerCustom.setSelection(mCustomSpinnerAdapter.getPosition(mCurrentEvent.getInterest()));
                }

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    private void sortList() {
        Collections.sort(mInterestsList, new Comparator<EventsInterestData>() {
            @Override
            public int compare(EventsInterestData s1, EventsInterestData s2) {
                return s1.getInterest().compareToIgnoreCase(s2.getInterest());
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("event", mCurrentEvent);
        outState.putParcelable("event_clone", mCurrentEventChangeFlag);
        outState.putParcelableArrayList("interests", mInterestsList);
        outState.putParcelableArrayList("participates", mParticipates);
        outState.putBoolean("mode", mMode);
        outState.putInt(SCROLL_STATE, mScrollingState);
        outState.putParcelable(LIST_STATE_KEY, mLinearLayoutManager.onSaveInstanceState());
    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            mTitle.setText(mCurrentEvent.getTitle());
            mEventDetails.setText(mCurrentEvent.getDetails());
            mEventTimeStart.setTimeInMillis(mCurrentEvent.getStart());
            mEventTimeEnd.setTimeInMillis(mCurrentEvent.getEnd());
            setTimeText();

        }

    }

    private int timeDifferences(Calendar c1, Calendar c2) {
        long seconds = (c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000;
        int hours = (int) (seconds / 3600);
        return hours;
    }


    @Override
    public void navigateToCaptureFragment(String[] permissions) {

        if (isPermissionGranted(permissions)) {


            if (Arrays.asList(permissions).contains(ACCESS_FINE_LOCATION)) {
                mapFragment.initFusedLocation(EVENT_LOC_TAB);
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

    @Override
    public void UserIgnoredPermissionDialog() {

    }

    private boolean isPermissionGranted(String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }


    private void saveEventToFirebase() {


        if (mCurrentEvent.getImage() == null || mCurrentEvent.getImage().equals("")) {
            int index = mCustomSpinnerAdapter.getPosition((String) mspinnerCustom.getSelectedItem());
            if (index > -1)
                mCurrentEvent.setImage(mInterestsList.get(index).getImage());
        }

        if (mMode)
        BuildUploadEventProgressDialogFragment(CREATE);
        else
            BuildUploadEventProgressDialogFragment(UPDATE);


    }

    private void BuildUploadEventProgressDialogFragment(int operation) {
        UploadEventToFireBaseFragment progressDialog = (UploadEventToFireBaseFragment) getSupportFragmentManager().findFragmentByTag(UploadEventToFireBaseFragment.class.getName());
        if (progressDialog == null) {
            Log.d(TAG, "opening BuildUploadEventProgressDialogFragment");
            progressDialog = UploadEventToFireBaseFragment.newInstance(mCurrentEvent, operation);
            progressDialog.show(getSupportFragmentManager(), UploadEventToFireBaseFragment.class.getName());

        }
    }


    private void buildImageAndTitleChooser() {

        ImageCameraDialogFragment ImageCameraFragment = (ImageCameraDialogFragment) getSupportFragmentManager().findFragmentByTag(ImageCameraDialogFragment.class.getName());

        if (ImageCameraFragment == null) {
            Log.d(TAG, "opening buildImageAndTitleChooser");
            ImageCameraFragment = ImageCameraDialogFragment.newInstance();
            ImageCameraFragment.show(getSupportFragmentManager(), ImageCameraDialogFragment.class.getName());

        }

    }

    private void BuildProgressDialogFragment(Uri uri) {
        UploadImageToFireBaseFragment progressDialog = (UploadImageToFireBaseFragment) getSupportFragmentManager().findFragmentByTag(UploadImageToFireBaseFragment.class.getName());
        if (progressDialog == null) {
            Log.d(TAG, "opening origress dialog");
            progressDialog = UploadImageToFireBaseFragment.newInstance(uri, mCurrentEvent.getId());
            progressDialog.show(getSupportFragmentManager(), UploadImageToFireBaseFragment.class.getName());

        }
    }

    @Override
    public void DialogResults(Uri picUri) {
        BuildProgressDialogFragment(picUri);
    }

    @Override
    public void onClick(View view, int position) {
        Intent galleryIntent = new Intent(CreateEventActivity.this, GalleryActivity.class);
        galleryIntent.putExtra(ID, mCurrentEvent.getId());
        startActivity(galleryIntent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }


    private void setVisibility(final View view, final float alpha, long duration, boolean animate) {

        if (animate)
            view.animate()
                    .alpha(alpha)
                    .setDuration(duration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            if (alpha == 1.0f)
                                view.setVisibility(View.VISIBLE);
                            if (alpha == 0.0f)
                                view.setVisibility(View.GONE);
                            Log.d(TAG, alpha + "");

                        }
                    });
        else {
            if (alpha == 1.0f) {
                view.setAlpha(1.0f);
                view.setVisibility(View.VISIBLE);
            }

            if (alpha == 0.0f) {
                view.setAlpha(0.0f);
                view.setVisibility(View.GONE);
            }

        }

    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus)
            behavior.setState(STATE_EXPANDED);

        switch (v.getId()) {
            case R.id.tv_title:
                mFocusedEditText = TITLE_FOCUS;
                break;
            case R.id.tv_desciption:
                mFocusedEditText = DETAILS_FOCUS;
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        try {

            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                if (mEventDetails.isFocused()) {
                    mEventDetails.clearFocus();
                    //behavior.setState(STATE_ANCHORED);

                }
                if (mTitle.isFocused()) {
                    mTitle.clearFocus();
                    //behavior.setState(STATE_ANCHORED);

                }

            }
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            Log.e(TAG, "dispatchTouchEvent " + e.toString());
            return false;
        }
    }


    @Override
    public void onFinishDialog(String image) {
        mCurrentEvent.setImage(image);
        showSnackBar(getString(R.string.image_uploaded));
        isChangeMade();

    }

    @Override
    public void onErrorDialog(String error) {
        showSnackBar(getString(R.string.image_uploaded_error));

    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mTabSelectedIndex = tab.getPosition();
        onTabSelect();

    }

    private void onTabSelect() {
        switch (mTabSelectedIndex) {
            case LOCATION_TAB:
                navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                break;
            case SEARCH_TAB:
                mPlaceAutocompleteFragment.performClick();
                break;

        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelect();
    }


    public void NotifyAllUsersNewEvent() {

        JSONObject manJson = new JSONObject();

        try {
            manJson.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            manJson.put("title", getString(R.string.new_event_title) + " " + mCurrentEvent.getInterest());
            manJson.put("body", getString(R.string.new_event_body));
            manJson.put("interest", mCurrentEvent.getInterest());
            manJson.put("event_id", mCurrentEvent.getId());
            manJson.put("mode", mMode);
            manJson.put("lat", mCurrentEvent.getLatitude());
            manJson.put("lon", mCurrentEvent.getLongitude());
            manJson.put("id_token", SharePref.getInstance(this).getString(ID_TOKEN, ""));
            VolleyHelper.getInstance(this).put(PROD_ADD, manJson, this, this);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void NotifyAllUsersUpdateEvent() {

        if (!isLocationChanged()) return;

        JSONObject manJson = new JSONObject();

        try {
            manJson.put("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
            manJson.put("title", getString(R.string.location_changed_title));
            manJson.put("body", getString(R.string.location_changed_body));
            manJson.put("event_id", mCurrentEvent.getId());
            manJson.put("id_token", SharePref.getInstance(this).getString(ID_TOKEN, ""));

            VolleyHelper.getInstance(this).put(PROD_UPDATE, manJson, this, this);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.toString());
    }

    @Override
    public void onResponse(Object response) {
        Log.d(TAG, response.toString());
    }

    public void showProgress(String title, String message) {
        ProgressDialogFragment mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment == null) {
            Log.d(TAG, "opening origress dialog");
            mProgressDialogFragment = ProgressDialogFragment.newInstance(
                    title, message, ProgressDialog.STYLE_SPINNER);
            mProgressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getName());
        }
    }

    public void hideProgressMessage() {
        ProgressDialogFragment mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment != null)
            mProgressDialogFragment.dismiss();

    }

    @Override
    public void onGlobalLayout() {
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, calculateDeviceHeight() / 2);
        mPicturesRecyclerView.setLayoutParams(layoutParams);

    }


    @Override
    public void onEventFinishDialog(int operation) {
        switch (operation) {

            case CREATE:
                showSnackBar(getString(R.string.event_created));
                NotifyAllUsersNewEvent();
                mMode = false;
                cloneEvent();
                initMode();
                break;
            case UPDATE:
                showSnackBar(getString(R.string.event_update));
                NotifyAllUsersUpdateEvent();
                mMode = false;
                cloneEvent();
                initMode();
                break;
            case REMOVE:
                deleteEventStorage();
                break;


        }
    }

    @Override
    public void onEventErrorDialog(String error) {

    }
}

