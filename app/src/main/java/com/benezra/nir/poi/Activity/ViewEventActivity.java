package com.benezra.nir.poi.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.benezra.nir.poi.Adapter.ParticipateAdapter;
import com.benezra.nir.poi.BaseActivity;
import com.benezra.nir.poi.Bitmap.BitmapUtil;
import com.benezra.nir.poi.Bitmap.DateUtil;
import com.benezra.nir.poi.ChatActivity;
import com.benezra.nir.poi.Event;
import com.benezra.nir.poi.Fragment.ProgressDialogFragment;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.User;
import com.benezra.nir.poi.View.DividerItemDecoration;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import static com.benezra.nir.poi.Helper.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ID;
import static com.benezra.nir.poi.Helper.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Helper.Constants.EVENT_START;
import static com.benezra.nir.poi.Helper.Constants.EVENT_TITLE;


public class ViewEventActivity extends BaseActivity
        implements View.OnClickListener,
        OnMapReadyCallback,
        RecyclerTouchListener.ClickListener {

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private Event mCurrentEvent;
    private TextView mEventDetails;
    final static String TAG = ViewEventActivity.class.getSimpleName();
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView mToolbarBackgroundImage;
    private FirebaseDatabase mFirebaseInstance;
    private ProgressBar mProgressBar;
    private ProgressDialogFragment mProgressDialogFragment;
    private RecyclerView mRecyclerView;
    private ParticipateAdapter mParticipateAdapter;
    private ArrayList<User> mParticipates;
    private FusedLocationProviderClient mFusedLocationClient;
    private TextView tvDatePicker, tvTimePicker;
    private boolean mJoinEvent;
    private Menu mMenu;
    private LinearLayout mPrivateLinearLayout;
    private NestedScrollView mNestedScrollView;


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.navigate_now:
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + mCurrentEvent.getLatitude() + "," + mCurrentEvent.getLongitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            case R.id.recycler_view:
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


        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mPrivateLinearLayout = (LinearLayout) findViewById(R.id.private_layout);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.nested_scrollview);

        tvDatePicker = (TextView) findViewById(R.id.tv_date);
        tvTimePicker = (TextView) findViewById(R.id.tv_time);

        // Find the Navigate Now button
        TextView navigateNow = (TextView) findViewById(R.id.navigate_now);

        // Set a click listener on the button
        navigateNow.setOnClickListener(this);

        mEventDetails = (TextView) findViewById(R.id.first_paragraph);

        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, this));
        collapsingToolbar.setOnClickListener(this);


        if (savedInstanceState != null) {
            mCurrentEvent = savedInstanceState.getParcelable("event");
            mParticipates = savedInstanceState.getParcelableArrayList("participates");
            mJoinEvent = savedInstanceState.getBoolean("join");

            initParticipates();
            setEventFields();


        } else {
            mJoinEvent = false;
            mParticipates = new ArrayList<>();
            initParticipates();
            getEventIntent(getIntent());
            addParticipateChangeListener();

        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

                Log.d(TAG, "child exist  " + mJoinEvent);
                setMenuItemChecked();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_event_menu, menu);
        mMenu = menu;
        isJoined();
        return true;
    }

    private void addParticipateChangeListener() {
        Query query = mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mParticipates.clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        User user = data.getValue(User.class);
                        mParticipates.add(user);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBodyText = "Check it out. Your message goes here";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);
                startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
                return true;
            case R.id.join:
                mJoinEvent = !item.isChecked();
                item.setChecked(mJoinEvent);
                setMenuItemChecked();
                JoinLeaveEvent();
                setIcon(item);
                return true;
            case R.id.chat:
                Intent i = new Intent(ViewEventActivity.this, ChatActivity.class);
                i.putExtra(EVENT_ID, mCurrentEvent.getId());
                startActivity(i);
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

        MenuItem chatMenuItem = mMenu.findItem(R.id.chat);
        MenuItem join = mMenu.findItem(R.id.join);
        join.setChecked(mJoinEvent);
        setIcon(join);
        Log.d(TAG,"join.setChecked " + mJoinEvent);

        if (mJoinEvent) {
            mPrivateLinearLayout.setVisibility(View.VISIBLE);
            //mNestedScrollView.setNestedScrollingEnabled(true);
            chatMenuItem.setVisible(true);
        } else {
            mPrivateLinearLayout.setVisibility(View.GONE);
            //mNestedScrollView.setNestedScrollingEnabled(false);
            chatMenuItem.setVisible(false);
        }
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
        setEventFields();


    }


    private void initParticipates() {
        mParticipateAdapter = new ParticipateAdapter(this, mParticipates);
        mRecyclerView.setAdapter(mParticipateAdapter);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("event", mCurrentEvent);
        outState.putParcelableArrayList("participates", mParticipates);
        outState.putBoolean("join", mJoinEvent);


    }


    private void setEventFields() {


        if (mCurrentEvent != null) {
            collapsingToolbar.setTitle(mCurrentEvent.getTitle());
            mEventDetails.setText(mCurrentEvent.getDetails());
            setImageBack();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mCurrentEvent.getStart());
            tvDatePicker.setText(DateUtil.CalendartoDate(calendar.getTime()));
            tvTimePicker.setText(DateUtil.CalendartoTime(calendar.getTime()));


        }

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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in the respective location and move the camera and set the zoom level to 15
        LatLng location = new LatLng(mCurrentEvent.getLatitude(), mCurrentEvent.getLongitude());
        mMap.addMarker(new MarkerOptions().position(location).title(mCurrentEvent.getTitle()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
    }

    @Override
    public void onClick(View view, int position) {

    }

    @Override
    public void onLongClick(View view, int position) {

    }
}

