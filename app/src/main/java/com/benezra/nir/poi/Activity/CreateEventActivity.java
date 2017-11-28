package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

import com.benezra.nir.poi.Adapter.EventImagesAdapter;
import com.benezra.nir.poi.Adapter.ViewHolders;
import com.benezra.nir.poi.Utils.DateUtil;
import com.benezra.nir.poi.Fragment.CustomPlaceAutoCompleteFragment;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragment;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Adapter.CustomSpinnerAdapter;
import com.benezra.nir.poi.Fragment.UploadToFireBaseFragment;
import com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.Objects.User;
import com.benezra.nir.poi.Utils.LocationUtil;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_ANCHORED;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_COLLAPSED;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_DRAGGING;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_EXPANDED;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_HIDDEN;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_SETTLING;
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


public class CreateEventActivity extends BaseActivity
        implements View.OnClickListener,
        RecyclerTouchListener.ClickListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        CompoundButton.OnCheckedChangeListener,
        ImageCameraDialogFragment.ImageCameraDialogCallbackNew,
        TextWatcher,
        AdapterView.OnItemSelectedListener,
        AlertDialogFragment.DialogListenerCallback,
        MapFragment.MapFragmentCallback,
        PlaceSelectionListener,
        View.OnFocusChangeListener,
        UploadToFireBaseFragment.UploadListener,
        GoogleMapsBottomSheetBehavior.BottomSheetCallback,
        ViewTreeObserver.OnGlobalLayoutListener,
        TabLayout.OnTabSelectedListener {

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    TextView tvDatePicker, tvTimePicker;
    private Switch mSwitch;
    private Event mCurrentEvent;
    private Event mCurrentEventChangeFlag;
    final static String TAG = CreateEventActivity.class.getSimpleName();
    private Calendar mEventTime;
    private FirebaseDatabase mFirebaseInstance;
    private Spinner mspinnerCustom;
    private ArrayList<String> mInterestsList;
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
    private ArrayList<String> mEventImagesList;
    private TextView mTextViewDistance;
    private LinearLayout mNavigationBarLayout;

    private TabLayout mTabLayout;


    private int mTabSelectedIndex;


    private FirebaseRecyclerAdapter<User, ViewHolders.ParticipatesViewHolder> mParticipateAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventTime = Calendar.getInstance();
        mEventImagesList = new ArrayList<>();
        mEventImagesAdapter = new EventImagesAdapter(this, mEventImagesList);

        mPicturesKeys = new HashSet<>();


        initView();

        initParticipatesRecycleView();

        initImageRecycleView();

        inflateMapFragment();

        inflateMapAutoCompleteFragment();

        addKeboardChangeListener();


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mCurrentEventChangeFlag = savedInstanceState.getParcelable("event_clone");
            mInterestsList = savedInstanceState.getStringArrayList("interests");
            mParticipates = savedInstanceState.getParcelableArrayList("participates");
            mMode = savedInstanceState.getBoolean("mode");
            mTabSelectedIndex = savedInstanceState.getInt("tab_selected_index");

            setEventFields();


        } else {
            mInterestsList = new ArrayList<>();
            mParticipates = new ArrayList<>();

            if (getIntent().getStringExtra(EVENT_ID) != null) {
                mTabSelectedIndex = -1;
                mMode = false; //edit  existing event
                getEventIntent(getIntent());
            } else {
                mTabSelectedIndex = 0;
                mMode = true; //new event
                mCurrentEvent = new Event(UUID.randomUUID().toString(), mFirebaseUser.getUid());

            }


        }
        initMode();
        initCustomSpinner();

        initListeners();


        behavior.setParallax(mPicturesRecyclerView);
        behavior.setAnchorHeight(900);
        behavior.setHideable(false);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        behavior.setPeekHeight(100);

    }

    public void SelectCurrentEventPoint() {
        if (mTabSelectedIndex == -1) return;

        else
            mTabLayout.getTabAt(mTabSelectedIndex).select();


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
        tvDatePicker = findViewById(R.id.tv_date);
        tvDatePicker.setText(DateUtil.CalendartoDate(Calendar.getInstance().getTime()));
        tvTimePicker = findViewById(R.id.tv_time);
        mspinnerCustom = findViewById(R.id.spinnerCustom);
        mProgressBar = findViewById(R.id.pb_loading);
        mSwitch = findViewById(R.id.tgl_allday);
        mNestedScrollView = findViewById(R.id.nestedscrollview);
        behavior = GoogleMapsBottomSheetBehavior.from(mNestedScrollView);
        mTabLayout = findViewById(R.id.tab_layout_tab);
        mTextViewDistance = findViewById(R.id.tv_distance);


    }

    private void initListeners() {
        tvDatePicker.setOnClickListener(this);
        tvTimePicker.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(this);
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
        mNestedScrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        behavior.setBottomSheetCallback(this);
        mTabLayout.addOnTabSelectedListener(this);
        mTitle.addTextChangedListener(this);
        addInterestsChangeListener();
        getAllEventImages();
        participatesChangeListener();
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        switch (newState) {
            case STATE_DRAGGING:
                switchViews(false);
                Log.d("state", "STATE_DRAGGING");
                break;
            case STATE_SETTLING:
                Log.d("state", "STATE_SETTLING");
                break;
            case STATE_EXPANDED:
                mHorizontalScrollView.setVisibility(View.GONE);
                Log.d("state", "STATE_EXPANDED");
                //switchViews(false);
                break;
            case STATE_COLLAPSED:
                //behavior.setState(STATE_HIDDEN);
                Log.d("state", "STATE_OLLAPSED");
                switchViews(true);
                break;
            case STATE_HIDDEN:
                Log.d("state", "STATE_HIDDEN");
                // mHorizontalScrollView.setVisibility(View.GONE);
                break;
            case STATE_ANCHORED:
                // mHorizontalScrollView.setVisibility(View.VISIBLE);
                Log.d("state", "STATE_ANCHORED");
                switchViews(false);
                break;


        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

    }

    @Override
    public void onGlobalLayout() {
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(mPicturesRecyclerView.getMeasuredWidth(), behavior.getAnchorOffset());
        mPicturesRecyclerView.setLayoutParams(layoutParams);
        mNestedScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }


    private void initParticipatesRecycleView() {
        mParticipateRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mParticipateRecyclerView.setNestedScrollingEnabled(false);

    }


    private void initImageRecycleView() {
        mPicturesRecyclerView = findViewById(R.id.recycler_view_pictures);
        mPicturesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
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
                    setVisibility(mPicturesRecyclerView, 1.0f, 300, true);

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
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mapFragment, MapFragment.class.getSimpleName()).commit();
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

    private void delete() {
        deleteEventDataBase();
        deleteEventStorage();
    }

    private void deleteEventDataBase() {
        showProgress(getString(R.string.delete_event), getString(R.string.please_wait));

        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgressMessage();
            }
        });

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


        saveEventToFirebase();

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
                LocationUtil.distance(mCurrentEvent.getLatlng(), mCurrentEventChangeFlag.getLatlng()) > 0.02 ||
                !mCurrentEvent.getInterest().equals(mCurrentEventChangeFlag.getInterest()) ||
                mCurrentEvent.getStart() != mCurrentEventChangeFlag.getStart());
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
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
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
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    private void addKeboardChangeListener() {
        CoordinatorLayout contentView = findViewById(R.id.coordinatorlayout);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                CoordinatorLayout contentView = findViewById(R.id.coordinatorlayout);
                contentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = contentView.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                Log.d("Nifras", "keypadHeight = " + keypadHeight);

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    Log.d(TAG, "open");
                } else {
                    Log.d(TAG, "close");
                    behavior.setState(STATE_ANCHORED);
                    contentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String thedate = DateUtil.FormatDate(year, month, dayOfMonth);
        tvDatePicker.setText(thedate);

        mEventTime.set(Calendar.YEAR, year);
        mEventTime.set(Calendar.MONTH, month);
        mEventTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mCurrentEvent.setStart(mEventTime.getTimeInMillis());

        isChangeMade();

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

        if (behavior.getState() == STATE_COLLAPSED)
            behavior.setState(STATE_ANCHORED);
        else {

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


    @Override
    protected int getNavigationDrawerID() {
        return 0;
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


    }

    @Override
    public void onError(Status status) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mCurrentEvent.getLatitude() != 0)
            mapFragment.addSingeMarkerToMap(mCurrentEvent.getLatlng(), mCurrentEvent.getAddress());

        SelectCurrentEventPoint();


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
        mInterestsList.clear();
        mFirebaseInstance.getReference("interests_data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot data : snapshot.getChildren()) {
                    EventsInterestData interestData = data.getValue(EventsInterestData.class);
                    mInterestsList.add(interestData.getInterest());
                }

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
        outState.putParcelable("event_clone", mCurrentEventChangeFlag);
        outState.putStringArrayList("interests", mInterestsList);
        outState.putParcelableArrayList("participates", mParticipates);
        outState.putBoolean("mode", mMode);


    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            mTitle.setText(mCurrentEvent.getTitle());
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


        DatabaseReference eventReference = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId());


        GeoHash geoHash = new GeoHash(new GeoLocation(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude()));
        Map<String, Object> updates = new HashMap<>();

        if (mMode) {
            Map<String, User> map = setOwnerAsParticipate();
            updates.put(PARTICIPATES, map);
            showProgress(getString(R.string.creating_event), getString(R.string.please_wait));


        } else
            showProgress(getString(R.string.updating_event), getString(R.string.please_wait));


        updates.put(ID, mCurrentEvent.getId());
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
                cloneEvent();
                initMode();

            }

        });

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


    private void buildImageAndTitleChooser() {

        ImageCameraDialogFragment ImageCameraFragment = (ImageCameraDialogFragment) getSupportFragmentManager().findFragmentByTag(ImageCameraDialogFragment.class.getName());

        if (ImageCameraFragment == null) {
            Log.d(TAG, "opening image camera dialog");
            ImageCameraFragment = ImageCameraDialogFragment.newInstance();
            ImageCameraFragment.show(getSupportFragmentManager(), ImageCameraDialogFragment.class.getName());

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
        isChangeMade();

    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mTabSelectedIndex = tab.getPosition();
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
        switch (mTabSelectedIndex) {
            case LOCATION_TAB:
                navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                break;
            case SEARCH_TAB:
                mPlaceAutocompleteFragment.performClick();
                break;

        }
    }

    private void switchViews(boolean show) {
        if (show) {
            mNavigationBarLayout.setVisibility(View.VISIBLE);
            mHorizontalScrollView.setVisibility(View.GONE);

        } else {
            mNavigationBarLayout.setVisibility(View.GONE);
            mHorizontalScrollView.setVisibility(View.VISIBLE);
        }

    }
}

