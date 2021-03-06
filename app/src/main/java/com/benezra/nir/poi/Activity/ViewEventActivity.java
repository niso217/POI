package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CalendarContract;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.benezra.nir.poi.Adapter.EventImagesAdapter;
import com.benezra.nir.poi.Adapter.EventParticipatesAdapter;
import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.ChatFragment;
import com.benezra.nir.poi.Utils.DateUtil;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragment;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Fragment.UploadImageToFireBaseFragment;
import com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior;
import com.benezra.nir.poi.Helper.AsyncGeocoder;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.Objects.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Fragment.MapFragment.LOCATION_TAB;
import static com.benezra.nir.poi.Interface.Constants.ACTION_REMOVE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_END;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_ANCHORED;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_COLLAPSED;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_DRAGGING;
import static com.benezra.nir.poi.View.GoogleMapsBottomSheetBehavior.STATE_EXPANDED;
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


public class ViewEventActivity extends AppCompatActivity
        implements View.OnClickListener,
        MapFragment.MapFragmentCallback,
        ImageCameraDialogFragment.ImageCameraDialogCallbackNew,
        RecyclerTouchListener.ClickListener,
        UploadImageToFireBaseFragment.UploadListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        GoogleMapsBottomSheetBehavior.BottomSheetCallback,
        TabLayout.OnTabSelectedListener,
        AlertDialogFragment.DialogListenerCallback,
        ViewTreeObserver.OnGlobalLayoutListener

{

    private static final String LIST_STATE_KEY = "state";
    private static final String SCROLL_STATE = "scroll_state";
    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private Event mCurrentEvent;
    final static String TAG = ViewEventActivity.class.getSimpleName();
    private FirebaseDatabase mFirebaseInstance;
    private ProgressBar mProgressBar;
    private TextView tvDatePickerStart, tvTimePickerStart, tvDatePickerEnd, tvTimePickerEnd;
    private boolean mJoinEvent;
    private LinearLayout mPrivateLinearLayout;
    private MapFragment mapFragment;
    private boolean mTouchEventFired;
    private ImageButton mNavigate, mAddImage, mChat, mShare, mCalender, mJoin;
    private TextView mTitle, mDetails, mTextViewDistance;
    private RecyclerView mPicturesRecyclerView;
    private RecyclerView mParticipateRecyclerView;
    private ArrayList<String> mEventImagesList;
    private ArrayList<User> mEventParticipateList;
    private EventImagesAdapter mEventImagesAdapter;
    private GoogleMapsBottomSheetBehavior behavior;
    private NestedScrollView mNestedScrollView;
    private LinearLayout mHorizontalScrollView;
    private LinearLayout mNavigationBarLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private TabLayout mTabLayout;
    private LinearLayoutManager mLinearLayoutManager;
    private Parcelable mListState;
    private int mScrollingState;

    public static final int DRIVING_TAB = 0;
    public static final int WALKING_TAB = 1;
    public static final int CYCLING_TAB = 2;
    public static final int EVENT_LOC_TAB = 3;
    private int mTabSelectedIndex;

    private EventParticipatesAdapter mParticipateAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mScrollingState = STATE_ANCHORED;
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventImagesList = new ArrayList<>();
        mEventImagesAdapter = new EventImagesAdapter(this, mEventImagesList);
        mEventParticipateList = new ArrayList<>();
        mParticipateAdapter = new EventParticipatesAdapter(this, mEventParticipateList);


        initView();

        initImageRecycleView();

        initParticipateRecycleView();

        inflateMapFragment();

        addKeboardChangeListener();


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mJoinEvent = savedInstanceState.getBoolean("join");
            mTouchEventFired = savedInstanceState.getBoolean("touch");
            mTabSelectedIndex = savedInstanceState.getInt("tab_selected_index");
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
            mScrollingState = savedInstanceState.getInt(SCROLL_STATE);
            setEventFields();
            setMenuItemChecked();


        } else {
            mTabSelectedIndex = LOCATION_TAB;
            mJoinEvent = false;
            getEventIntent(getIntent());
            isJoined();


        }

        initListeners();

    }


    private int calculateDeviceHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }


    private void initView() {
        mHorizontalScrollView = findViewById(R.id.scrolling_icons);
        // focusRight();
        mNavigationBarLayout = findViewById(R.id.tab_layout);
        mTitle = findViewById(R.id.tv_title);
        mTitle.setEnabled(false);
        mCoordinatorLayout = findViewById(R.id.coordinatorlayout);
        mJoin = findViewById(R.id.btn_join);
        mShare = findViewById(R.id.btn_share);
        mNavigate = findViewById(R.id.btn_navigate);
        mAddImage = findViewById(R.id.btn_add_image);
        mChat = findViewById(R.id.btn_chat);
        mCalender = findViewById(R.id.btn_cal);
        mProgressBar = findViewById(R.id.pb_loading);
        mPrivateLinearLayout = findViewById(R.id.private_layout);
        tvDatePickerStart = findViewById(R.id.tv_date_start);
        tvTimePickerStart = findViewById(R.id.tv_time_start);
        tvDatePickerEnd = findViewById(R.id.tv_date_end);
        tvTimePickerEnd = findViewById(R.id.tv_time_end);
        mNestedScrollView = findViewById(R.id.nestedscrollview);
        mDetails = findViewById(R.id.tv_desciption);
        mTabLayout = findViewById(R.id.tab_layout_tab);
        mTextViewDistance = findViewById(R.id.tv_distance);
        mPicturesRecyclerView = findViewById(R.id.recycler_view_pictures);
        mParticipateRecyclerView = findViewById(R.id.participate_recycler_view);
        behavior = GoogleMapsBottomSheetBehavior.from(mNestedScrollView);
        behavior.setAnchorHeight(calculateDeviceHeight() / 2);
        behavior.setHideable(false);
        behavior.setPeekHeight(100);
        behavior.setParallax(mPicturesRecyclerView);


    }


    private void inflateMapFragment() {
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.class.getSimpleName());
        if (mapFragment == null) {
            Log.d(TAG, "map fragment null");
            mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mapFragment, MapFragment.class.getSimpleName()).commit();
        }
    }

    private void initListeners() {
        mJoin.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mNavigate.setOnClickListener(this);
        mAddImage.setOnClickListener(this);
        mChat.setOnClickListener(this);
        mCalender.setOnClickListener(this);
        mTabLayout.addOnTabSelectedListener(this);
        behavior.setBottomSheetCallback(this);
        getAllEventImages();
        participatesChangeListener();
        setEventRemovedListener();
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
                    behavior.setState(mScrollingState);
                    initScrollViewState();
                    contentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_navigate:
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + mCurrentEvent.getLatitude() + "," + mCurrentEvent.getLongitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            case R.id.btn_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBodyText = "Check it out. Your chat_message_item goes here";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
                break;
            case R.id.btn_chat:
                behavior.setState(mScrollingState = STATE_COLLAPSED);
                initScrollViewState();
                ChatFragment chat = ChatFragment.newInstance(mCurrentEvent.getId());
                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, chat, ChatFragment.class.getSimpleName()).addToBackStack(null).commit();
                setNestedScrollViewMargin(100);
                break;
            case R.id.btn_add_image:
                navigateToCaptureFragment(new String[]{Manifest.permission.CAMERA});
                break;
            case R.id.btn_cal:
                addToCalender();
                break;
            case R.id.btn_join:
                mJoinEvent = !mJoin.isActivated();
                setMenuItemChecked();
                JoinLeaveEvent();
                Log.d(TAG, "isJoined onCheckedChanged " + mJoinEvent);
                break;
        }
    }

    private void setNestedScrollViewMargin(int top) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mNestedScrollView
                .getLayoutParams();
        layoutParams.setMargins(0, top, 0, 0);
        mNestedScrollView.setLayoutParams(layoutParams);


    }

    private void BuildDeleteFragment() {
        AlertDialogFragment alertDialog = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.class.getName());
        if (alertDialog == null) {
            Log.d(TAG, "opening alert dialog");
            HashMap<Integer, String> map = new HashMap<>();
            map.put(BUTTON_POSITIVE, getString(R.string.leave));
            alertDialog = AlertDialogFragment.newInstance(
                    getString(R.string.user_removed_event), getString(R.string.user_removed_event_message), map, ACTION_REMOVE);
            alertDialog.show(getSupportFragmentManager(), AlertDialogFragment.class.getName());

        }
    }


    @Override
    public void navigateToCaptureFragment(String[] permissions) {

        if (isPermissionGranted(permissions)) {


            if (Arrays.asList(permissions).contains(ACCESS_FINE_LOCATION)) {
                mapFragment.initFusedLocation(mTabSelectedIndex);
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

    @Override
    public void UserIgnoredPermissionDialog() {

    }


    private void isJoined() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(mFirebaseUser.getUid())) {
                    mJoinEvent = true;
                } else
                    mJoinEvent = false;

                Log.d(TAG, "isJoined mFirebaseInstance " + mJoinEvent);
                setMenuItemChecked();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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


    private void initParticipateRecycleView() {
        mParticipateRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mParticipateRecyclerView.setNestedScrollingEnabled(false);
        mParticipateRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mParticipateRecyclerView, null));
        mParticipateRecyclerView.setAdapter(mParticipateAdapter);
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
        } else
            super.onBackPressed();
    }

    private void JoinLeaveEvent() {
        if (mJoinEvent) JoinEvent();
        else LeaveEvent();

    }

    private void setMenuItemChecked() {

        if (mJoinEvent) {
            mPrivateLinearLayout.setVisibility(View.VISIBLE);
            setButtonState(true);

        } else {
            mPrivateLinearLayout.setVisibility(View.INVISIBLE);
            setButtonState(false);
        }
        mJoin.setActivated(mJoinEvent);

    }

    private void setButtonState(boolean state) {
        mNavigate.setEnabled(state);
        mShare.setEnabled(state);
        mChat.setEnabled(state);
        mAddImage.setEnabled(state);
        mCalender.setEnabled(state);
    }

    private void setEventRemovedListener() {

        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).addChildEventListener(new ChildEventListener() {
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
                if (dataSnapshot.getValue().equals(mCurrentEvent.getId()))
                    BuildDeleteFragment();
                Log.d(TAG, "onChildRemoved");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildMoved");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled");

            }
        });
    }

    private void JoinEvent() {
        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates").child(mFirebaseUser.getUid()).setValue(getUser());
        mFirebaseInstance.getReference("users").child(mFirebaseUser.getUid()).child("events").child(mCurrentEvent.getId()).setValue(true);

    }


    private void LeaveEvent() {
        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates").child(mFirebaseUser.getUid()).removeValue();
        mFirebaseInstance.getReference("users").child(mFirebaseUser.getUid()).child("events").child(mCurrentEvent.getId()).removeValue();

    }

    private User getUser() {
        User currentUser = new User();
        currentUser.setName(mFirebaseUser.getDisplayName());
        currentUser.setEmail(mFirebaseUser.getEmail());
        if (mFirebaseUser.getPhotoUrl() != null)
            currentUser.setAvatar(mFirebaseUser.getPhotoUrl().toString());
        return currentUser;
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
        setEventFields();


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("event", mCurrentEvent);
        outState.putBoolean("join", mJoinEvent);
        outState.putBoolean("touch", mTouchEventFired);
        outState.putInt("tab_selected_index", mTabSelectedIndex);
        outState.putInt(SCROLL_STATE, mScrollingState);
        outState.putParcelable(LIST_STATE_KEY, mLinearLayoutManager.onSaveInstanceState());


    }


    public void addToCalender() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, mCurrentEvent.getTitle())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, mCurrentEvent.getAddress())
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, mCurrentEvent.getStart())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, mCurrentEvent.getEnd());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            mTitle.setText(mCurrentEvent.getTitle());
            mDetails.setText(mCurrentEvent.getDetails());


            tvDatePickerStart.setText(DateUtil.CalendartoDate(mCurrentEvent.getStartTime()));
            tvTimePickerStart.setText(DateUtil.CalendartoTime(mCurrentEvent.getStartTime()));
            tvDatePickerEnd.setText(DateUtil.CalendartoDate(mCurrentEvent.getEndTime()));
            tvTimePickerEnd.setText(DateUtil.CalendartoTime(mCurrentEvent.getEndTime()));

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mapFragment.ShowNavigationLayout();
        // Add a marker in the respective location and move the camera and set the zoom level to 15
        LatLng location = new LatLng(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude());
        mapFragment.setEventLocation(location, mCurrentEvent.getAddress());
        mapFragment.addSingeMarkerToMap(location, mCurrentEvent.getAddress());
        SelectCurrentEventPoint();


    }

    public void SelectCurrentEventPoint() {
        if (mTabSelectedIndex == 0)
            mTabLayout.getTabAt(EVENT_LOC_TAB).select();
        else
            mTabLayout.getTabAt(mTabSelectedIndex).select();


    }

    @Override
    public void onEventLocationChanged(LatLng latLng, String address) {

    }

    @Override
    public void LocationPermission() {
        navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @Override
    public void onDistanceChanged(String add) {
        mTextViewDistance.setText(add);
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
        Intent galleryIntent = new Intent(ViewEventActivity.this, GalleryActivity.class);
        galleryIntent.putExtra(ID, mCurrentEvent.getId());
        startActivity(galleryIntent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }


    @Override
    public void onFinishDialog(String image) {
        showSnackBar(getString(R.string.image_uploaded));
    }

    @Override
    public void onErrorDialog(String error) {
        showSnackBar(getString(R.string.image_uploaded_error));

    }

    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();

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
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

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

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mTabSelectedIndex = tab.getPosition();
        switch (mTabSelectedIndex) {
            case DRIVING_TAB:
                LocationPermission();
                break;
            case WALKING_TAB:
                LocationPermission();
                break;
            case CYCLING_TAB:
                LocationPermission();
                break;
            case EVENT_LOC_TAB:
                getAddress(mCurrentEvent.getLatlng());
                break;

        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    public void getAddress(final LatLng latLng) {

        new AsyncGeocoder(new AsyncGeocoder.onAddressFoundListener() {
            @Override
            public void onAddressFound(String result) {
                mapFragment.addSingeMarkerToMap(latLng, result);
                Log.d(TAG, "the address is: " + result);

            }
        }).execute(new AsyncGeocoder.AsyncGeocoderObject(
                new Geocoder(this), LatLongToLocation(latLng)));
    }


    private Location LatLongToLocation(LatLng latLng) {
        Location loc = new Location("");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
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


    @Override
    public void onGlobalLayout() {
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, calculateDeviceHeight() / 2);
        mPicturesRecyclerView.setLayoutParams(layoutParams);

    }

    @Override
    public void onFinishDialog(int state, int action) {
        finish();
    }
}




