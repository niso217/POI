package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.StateListDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.benezra.nir.poi.Adapter.EventImagesAdapter;
import com.benezra.nir.poi.Adapter.ViewHolders;
import com.benezra.nir.poi.Bitmap.DateUtil;
import com.benezra.nir.poi.ChatActivity;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragmentNew;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Fragment.UploadToFireBaseFragment;
import com.benezra.nir.poi.GoogleMapsBottomSheetBehavior;
import com.benezra.nir.poi.Helper.AsyncGeocoder;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.text.Line;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.benezra.nir.poi.GoogleMapsBottomSheetBehavior.PEEK_HEIGHT_AUTO;
import static com.benezra.nir.poi.GoogleMapsBottomSheetBehavior.STATE_ANCHORED;
import static com.benezra.nir.poi.GoogleMapsBottomSheetBehavior.STATE_COLLAPSED;
import static com.benezra.nir.poi.GoogleMapsBottomSheetBehavior.STATE_DRAGGING;
import static com.benezra.nir.poi.GoogleMapsBottomSheetBehavior.STATE_EXPANDED;
import static com.benezra.nir.poi.GoogleMapsBottomSheetBehavior.STATE_HIDDEN;
import static com.benezra.nir.poi.GoogleMapsBottomSheetBehavior.STATE_SETTLING;
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


public class ViewEventActivity extends BaseActivity
        implements View.OnClickListener,
        MapFragment.MapFragmentCallback,
        ImageCameraDialogFragmentNew.ImageCameraDialogCallbackNew,
        RecyclerTouchListener.ClickListener,
        CompoundButton.OnCheckedChangeListener,
        UploadToFireBaseFragment.UploadListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        GoogleMapsBottomSheetBehavior.BottomSheetCallback,
        ViewTreeObserver.OnGlobalLayoutListener,
        TabLayout.OnTabSelectedListener

{

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private Event mCurrentEvent;
    final static String TAG = ViewEventActivity.class.getSimpleName();
    private CollapsingToolbarLayout collapsingToolbar;
    private FirebaseDatabase mFirebaseInstance;
    private ProgressBar mProgressBar;
    private TextView tvDatePicker, tvTimePicker;
    private boolean mJoinEvent;
    private Menu mMenu;
    private LinearLayout mPrivateLinearLayout;

    private MapFragment mapFragment;
    private CoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private boolean mCanDrag = true;
    private int mCurrentOffset;
    private int mScrollDirection;
    private boolean mTouchEventFired;
    private ImageButton mNavigate, mAddImage, mChat, mShare;
    private ToggleButton mJoin;
    private Toolbar mToolbar;
    private TextView mTitle, mDetails,mTextViewDistance;

    private RecyclerView mPicturesRecyclerView;
    private RecyclerView mParticipateRecyclerView;
    private ArrayList<String> mEventImagesList;
    private EventImagesAdapter mEventImagesAdapter;
    private GoogleMapsBottomSheetBehavior behavior;
    private NestedScrollView mNestedScrollView;

    private LinearLayout mHorizontalScrollView;
    private LinearLayout mNavigationBarLayout;

    private TabLayout mTabLayout;

    public static final int DRIVING_TAB = 0;
    public static final int WALKING_TAB = 1;
    public static final int CYCLING_TAB = 2;
    public static final int EVENT_LOC_TAB = 3;
    private int mTabSelectedIndex;



    private FirebaseRecyclerAdapter<User, ViewHolders.ParticipatesViewHolder> mParticipateAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventImagesList = new ArrayList<>();
        mEventImagesAdapter = new EventImagesAdapter(this, mEventImagesList);

        initView();

        initParticipatesRecycleView();

        initImageRecycleView();

        inflateMapFragment();

        addKeboardChangeListener();


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mJoinEvent = savedInstanceState.getBoolean("join");
            mTouchEventFired = savedInstanceState.getBoolean("touch");
            mTabSelectedIndex = savedInstanceState.getInt("tab_selected_index");
            setEventFields();
            setMenuItemChecked();


        } else {
            mTabSelectedIndex = 0;
            mJoinEvent = false;
            getEventIntent(getIntent());
            isJoined();


        }


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

    private void initView() {
        mHorizontalScrollView = findViewById(R.id.scrolling_icons);
        mNavigationBarLayout = findViewById(R.id.tab_layout);
        mTitle = findViewById(R.id.tv_title);
        mTitle.setEnabled(false);
        mJoin = findViewById(R.id.btn_join);
        mShare = findViewById(R.id.btn_share);
        mNavigate = findViewById(R.id.btn_navigate);
        mAddImage = findViewById(R.id.btn_add_image);
        mChat = findViewById(R.id.btn_chat);
        mProgressBar = findViewById(R.id.pb_loading);
        mPrivateLinearLayout = findViewById(R.id.private_layout);
        tvDatePicker = findViewById(R.id.tv_date);
        tvTimePicker = findViewById(R.id.tv_time);
        mParticipateRecyclerView = findViewById(R.id.participate_recycler_view);
        mNestedScrollView = findViewById(R.id.nestedscrollview);
        mDetails = findViewById(R.id.tv_desciption);
        behavior = GoogleMapsBottomSheetBehavior.from(mNestedScrollView);
        mTabLayout = findViewById(R.id.tab_layout_tab);
        mTextViewDistance =  findViewById(R.id.tv_distance);



    }

    private void initParticipatesRecycleView() {
        mParticipateRecyclerView = findViewById(R.id.participate_recycler_view);
        mParticipateRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mParticipateRecyclerView.setNestedScrollingEnabled(false);

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
        mJoin.setOnCheckedChangeListener(this);
        mShare.setOnClickListener(this);
        mNavigate.setOnClickListener(this);
        mAddImage.setOnClickListener(this);
        mChat.setOnClickListener(this);
        mNestedScrollView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        behavior.setBottomSheetCallback(this);
        mTabLayout.addOnTabSelectedListener(this);
        getAllEventImages();
        participatesChangeListener();


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
                String shareBodyText = "Check it out. Your message goes here";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
                break;
            case R.id.btn_chat:
                Intent i = new Intent(ViewEventActivity.this, ChatActivity.class);
                i.putExtra(EVENT_ID, mCurrentEvent.getId());
                startActivity(i);
                break;
            case R.id.btn_add_image:
                navigateToCaptureFragment(new String[]{Manifest.permission.CAMERA});
                break;
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.btn_join:
                mJoinEvent = isChecked;
                setMenuItemChecked();
                JoinLeaveEvent();
                Log.d(TAG, "isJoined onCheckedChanged " + mJoinEvent);
                break;
        }

    }


    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }


    private void setVisibility(final View view, final float alpha, long duration, boolean animate) {

        if (animate) {
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
        } else {

            view.setAlpha(alpha);

            if (alpha == 1.0f) {
                view.setVisibility(View.VISIBLE);
            }

            if (alpha == 0.0f) {
                view.setVisibility(View.GONE);
            }

        }

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

    private void participatesChangeListener() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates");

        mParticipateAdapter = new FirebaseRecyclerAdapter<User, ViewHolders.ParticipatesViewHolder>(
                User.class, R.layout.participate_list_row, ViewHolders.ParticipatesViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewHolders.ParticipatesViewHolder participatesViewHolder, User model, int position) {
                participatesViewHolder.name.setText(model.getName());
                Picasso.with(ViewEventActivity.this)
                        .load(model.getAvatar())
                        .error(R.drawable.common_google_signin_btn_icon_dark)
                        .into(participatesViewHolder.image);
            }

        };

        mParticipateRecyclerView.setAdapter(mParticipateAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mParticipateAdapter.cleanup();

    }



    @Override
    public void onBackPressed() {
        if (behavior.getState() == STATE_COLLAPSED)
            behavior.setState(STATE_ANCHORED);
        else
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
        mJoin.setChecked(mJoinEvent);

    }

    private void setButtonState(boolean state)
    {
        mNavigate.setEnabled(state);
        mShare.setEnabled(state);
        mChat.setEnabled(state);
        mAddImage.setEnabled(state);
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



    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            mTitle.setText(mCurrentEvent.getTitle());
            mDetails.setText(mCurrentEvent.getDetails());
            // setImageBack();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mCurrentEvent.getStart());
            tvDatePicker.setText(DateUtil.CalendartoDate(calendar.getTime()));
            tvTimePicker.setText(DateUtil.CalendartoTime(calendar.getTime()));


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
        Intent galleryIntent = new Intent(ViewEventActivity.this, SpaceGalleryActivity.class);
        galleryIntent.putExtra(ID, mCurrentEvent.getId());
        startActivity(galleryIntent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }


    @Override
    public void onFinishDialog(String image) {

    }

    @Override
    public void onGlobalLayout() {
        CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(mPicturesRecyclerView.getMeasuredWidth(), behavior.getAnchorOffset());
        mPicturesRecyclerView.setLayoutParams(layoutParams);
        mNestedScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
                mapFragment.addSingeMarkerToMap(latLng,result);
                Log.d(TAG, "the address is: " + result);

            }
        }).execute(new AsyncGeocoder.AsyncGeocoderObject(
                new Geocoder(this), LatLongToLocation(latLng)));
    }


    private Location LatLongToLocation(LatLng latLng){
        Location loc = new Location("");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }
}




