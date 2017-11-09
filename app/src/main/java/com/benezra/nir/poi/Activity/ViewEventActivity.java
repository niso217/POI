package com.benezra.nir.poi.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.benezra.nir.poi.Adapter.ViewHolders;
import com.benezra.nir.poi.Bitmap.BitmapUtil;
import com.benezra.nir.poi.Bitmap.DateUtil;
import com.benezra.nir.poi.ChatActivity;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.Fragment.ImageCameraDialogFragmentNew;
import com.benezra.nir.poi.Fragment.MapFragment;
import com.benezra.nir.poi.Fragment.UploadToFireBaseFragment;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

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


public class ViewEventActivity extends AppCompatActivity
        implements View.OnClickListener,
        MapFragment.MapFragmentCallback,
        ImageCameraDialogFragmentNew.ImageCameraDialogCallbackNew,
        RecyclerTouchListener.ClickListener,
        AppBarLayout.OnOffsetChangedListener,
        CompoundButton.OnCheckedChangeListener,UploadToFireBaseFragment.UploadListener {

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private Event mCurrentEvent;
    private TextView mEventDetails;
    final static String TAG = ViewEventActivity.class.getSimpleName();
    private CollapsingToolbarLayout collapsingToolbar;
    private FirebaseDatabase mFirebaseInstance;
    private ProgressBar mProgressBar;
    private TextView tvDatePicker, tvTimePicker;
    private boolean mJoinEvent;
    private Menu mMenu;
    private LinearLayout mPrivateLinearLayout;

    private NestedScrollView mNestedScrollView;
    private MapFragment mapFragment;
    private CoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private boolean mCanDrag = true;
    private int mCurrentOffset;
    private LinearLayout mHorizontalScrollView;
    private int mScrollDirection;
    private boolean mTouchEventFired;
    private ImageButton mNavigate, mAddImage, mChat, mShare;
    private ToggleButton mJoin;
    private Toolbar mToolbar;
    private EditText mTitle;
    private RecyclerView mPicturesRecyclerView;
    private RecyclerView mParticipateRecyclerView;

    private FirebaseRecyclerAdapter<EventPhotos, ViewHolders.PicturesViewHolder> mPicturesAdapter;
    private FirebaseRecyclerAdapter<User, ViewHolders.ParticipatesViewHolder> mParticipateAdapter;


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
                buildImageAndTitleChooser();
                break;
        }
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();

        //Using the ToolBar as ActionBar
        //Find the toolbar view inside the activity layout
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        //Sets the Toolbar to act as the ActionBar for this Activity window.
        //Make sure the toolbar exists in the activity and is not null
//        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setDisplayShowTitleEnabled(false);


        //Setting the category name onto collapsing toolbar
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        mHorizontalScrollView = (LinearLayout) findViewById(R.id.scrolling_icons);
        mTitle = (EditText) findViewById(R.id.tv_title);
        mTitle.setEnabled(false);
        mJoin = (ToggleButton) findViewById(R.id.btn_join);
        mShare = (ImageButton) findViewById(R.id.btn_share);
        mNavigate = (ImageButton) findViewById(R.id.btn_navigate);
        mAddImage = (ImageButton) findViewById(R.id.btn_add_image);
        mChat = (ImageButton) findViewById(R.id.btn_chat);
        mJoin.setOnCheckedChangeListener(this);

        mShare.setOnClickListener(this);
        mNavigate.setOnClickListener(this);
        mAddImage.setOnClickListener(this);
        mChat.setOnClickListener(this);

        //Setting the styles to expanded and collapsed toolbar
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        //Setting the category mDialogImageView onto collapsing toolbar

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appbar);


        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);


        mPrivateLinearLayout = (LinearLayout) findViewById(R.id.private_layout);
        mNestedScrollView = (NestedScrollView) findViewById(R.id.nested_scrollview);


        tvDatePicker = (TextView) findViewById(R.id.tv_date);
        tvTimePicker = (TextView) findViewById(R.id.tv_time);

        mEventDetails = (TextView) findViewById(R.id.first_paragraph);


        //mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.class.getSimpleName());
        if (mapFragment==null)
        {
            Log.d(TAG,"map fragment null");
            mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mapFragment,MapFragment.class.getSimpleName()).commit();
        }



        mPicturesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_pictures);
        mPicturesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mPicturesRecyclerView.setNestedScrollingEnabled(false);
        mPicturesRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mPicturesRecyclerView, this));
        //mPicturesRecyclerView.setHasFixedSize(true);


        mParticipateRecyclerView = (RecyclerView) findViewById(R.id.participate_recycler_view);
        mParticipateRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        mParticipateRecyclerView.setNestedScrollingEnabled(false);
        //mParticipateRecyclerView.setHasFixedSize(true);


        collapsingToolbar.setOnClickListener(this);


        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mJoinEvent = savedInstanceState.getBoolean("join");
            mTouchEventFired = savedInstanceState.getBoolean("touch");
            setEventFields();
            setMenuItemChecked();


        } else {
            mJoinEvent = false;
            getEventIntent(getIntent());
            setAppBarOffset();
            isJoined();


        }

        mAppBarLayout.addOnOffsetChangedListener(this);


        setCoordinatorLayoutBehavior();
        addImagesChangeListener();
        participatesChangeListener();


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


    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void setNestedScrollViewOverlayTop(int n) {
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mNestedScrollView.getLayoutParams();
        AppBarLayout.ScrollingViewBehavior behavior =
                (AppBarLayout.ScrollingViewBehavior) params.getBehavior();
        behavior.setOverlayTop(dpToPx(n)); // Note: in pixels
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.view_event_menu, menu);
        return true;
    }


    private void addImagesChangeListener() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("pictures");

        mPicturesAdapter = new FirebaseRecyclerAdapter<EventPhotos, ViewHolders.PicturesViewHolder>(
                EventPhotos.class, R.layout.grid_item_event_pic, ViewHolders.PicturesViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewHolders.PicturesViewHolder picturesViewHolder, EventPhotos model, int position) {
                Picasso.with(ViewEventActivity.this)
                        .load(model.getUrl())
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
        mPicturesAdapter.cleanup();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.clear:
                setAppBarOffset();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setIcon(MenuItem item) {
        StateListDrawable stateListDrawable = (StateListDrawable) getResources().getDrawable(R.drawable.toggle_selector);
        int[] state = {item.isChecked() ? android.R.attr.state_checked : android.R.attr.state_empty};
        stateListDrawable.setState(state);
        item.setIcon(stateListDrawable.getCurrent());
    }

    private void JoinLeaveEvent() {
        if (mJoinEvent) JoinEvent();
        else LeaveEvent();

    }

    private void setMenuItemChecked() {

        if (mJoinEvent) {
            mPrivateLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mPrivateLinearLayout.setVisibility(View.GONE);
        }
        mJoin.setChecked(mJoinEvent);

    }

    private void JoinEvent() {
        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates").child(mFirebaseUser.getUid()).setValue(getUser());
    }


    private void LeaveEvent() {
        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates").child(mFirebaseUser.getUid()).removeValue();
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


    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            mTitle.setText(mCurrentEvent.getTitle());
            mEventDetails.setText(mCurrentEvent.getDetails());
           // setImageBack();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mCurrentEvent.getStart());
            tvDatePicker.setText(DateUtil.CalendartoDate(calendar.getTime()));
            tvTimePicker.setText(DateUtil.CalendartoTime(calendar.getTime()));


        }

    }


//    private void setImageBack() {
//        if (mCurrentEvent.getImage() != null) {
//            mProgressBar.setVisibility(View.VISIBLE);
//            Picasso.with(this)
//                    .load(mCurrentEvent.getImage())
//                    .into(mToolbarBackgroundImage, new com.squareup.picasso.Callback() {
//                        @Override
//                        public void onSuccess() {
//                            mProgressBar.setVisibility(View.GONE);
//                        }
//
//                        @Override
//                        public void onError() {
//                            mProgressBar.setVisibility(View.GONE);
//
//                        }
//                    });
//        } else {
//            Bitmap bitmap = BitmapUtil.UriToBitmap(this, mCurrentEvent.getUri());
//            if (bitmap != null)
//                mToolbarBackgroundImage.setImageBitmap(bitmap);
//        }
//
//    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapFragment.ShowNavigationLayout();
        // Add a marker in the respective location and move the camera and set the zoom level to 15
        LatLng location = new LatLng(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude());
        mapFragment.setEventLocation(location, mCurrentEvent.getAddress());
        mapFragment.addSingeMarkerToMap(location,mCurrentEvent.getAddress());
        mapFragment.SelectCurrentEventPoint();


    }

    @Override
    public void onEventLocationChanged(LatLng latLng,String address) {

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        mTouchEventFired = true;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        float bottom = mapFragment.getBottomHeight();

        try {

            if (ev.getAction() == MotionEvent.ACTION_DOWN && !mCanDrag) {
                if (y > mCoordinatorLayout.getMeasuredHeight() - bottom)
                    mCanDrag = true;

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
        Intent galleryIntent = new Intent(ViewEventActivity.this, SpaceGalleryActivity.class);
        galleryIntent.putExtra(ID,mCurrentEvent.getId());
        startActivity(galleryIntent);

    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int lastOfsset = mCurrentOffset;


        mCurrentOffset = Math.abs(verticalOffset);

        Log.d(TAG,mCurrentOffset+"");


        if (mCurrentOffset - lastOfsset > 0)
            mScrollDirection = 1;
        else
            mScrollDirection = -1;


        if (mCurrentOffset == 0) {
            mCanDrag = false;
            if (mMenu != null && !mMenu.findItem(R.id.clear).isVisible())
                mMenu.findItem(R.id.clear).setVisible(true);

            if (mTouchEventFired && mHorizontalScrollView.getVisibility() == View.VISIBLE)
                setVisibility(mHorizontalScrollView, 0.0f);

        } else

        {
            mCanDrag = true;
            if (mMenu != null && mMenu.findItem(R.id.clear).isVisible()) {
                mMenu.findItem(R.id.clear).setVisible(false);
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

    }


    @Override
    public void onFinishDialog(String image) {

    }
}




