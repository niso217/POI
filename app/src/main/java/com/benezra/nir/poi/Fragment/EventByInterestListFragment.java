package com.benezra.nir.poi.Fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Adapter.EventsAdapter;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.View.DividerItemDecoration;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.benezra.nir.poi.Interface.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_END;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ID;
import static com.benezra.nir.poi.Interface.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Interface.Constants.EVENT_START;
import static com.benezra.nir.poi.Interface.Constants.EVENT_TITLE;
import static com.benezra.nir.poi.Interface.Constants.SEARCH_RADIUS;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventByInterestListFragment extends Fragment implements
        MainActivity.FABClickedListener,
        RecyclerTouchListener.ClickListener {

    private FirebaseDatabase mFirebaseInstance;
    private MainActivity mActivity;
    private RecyclerView mEventsRecyclerView;
    private ArrayList<Event> mEventList;
    private EventsAdapter mEventsAdapter;
    final static String TAG = EventByInterestListFragment.class.getSimpleName();
    private List<String> mUserEvents;
    private Set<String> mEventHashSet;
    private FirebaseAuth mAuth;
    private String mSelectedInterest;
    private BubbleSeekBar mBbubbleSeekBar;
    private String mImageUrl;
    private AVLoadingIndicatorView mProgressBar;
    private AVLoadingIndicatorView mAVLoadingIndicatorView;
    private ToggleButton mToggleButton;

    private int mRadius = 30;


    public static EventByInterestListFragment newInstance(String interest, String image) {
        EventByInterestListFragment eventByInterestListFragment = new EventByInterestListFragment();
        Bundle args = new Bundle();
        args.putString(EVENT_INTEREST, interest);
        args.putString(EVENT_IMAGE, image);

        eventByInterestListFragment.setArguments(args);
        return eventByInterestListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventList = new ArrayList<>();
        mEventsAdapter = new EventsAdapter(getContext(), mEventList);
        mUserEvents = new ArrayList<>();
        mEventHashSet = new HashSet<>();
        mAuth = FirebaseAuth.getInstance();
        mSelectedInterest = getArguments().getString(EVENT_INTEREST);
        mImageUrl = getArguments().getString(EVENT_IMAGE);


        if (savedInstanceState!=null)
            mRadius = savedInstanceState.getInt(SEARCH_RADIUS);



    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;

        }
    }

    @Override
    public void onClick(View view, int position) {
        Event event = mEventList.get(position);

        if (!event.isStatus()) return;

        Intent userEvent = new Intent(getActivity(), ViewEventActivity.class);
        userEvent.putExtra(EVENT_ID, event.getId());
        userEvent.putExtra(EVENT_TITLE, event.getTitle());
        userEvent.putExtra(EVENT_OWNER, event.getOwner());
        userEvent.putExtra(EVENT_IMAGE, event.getImage());
        userEvent.putExtra(EVENT_DETAILS, event.getDetails());
        userEvent.putExtra(EVENT_LATITUDE, event.getLatitude());
        userEvent.putExtra(EVENT_LONGITUDE, event.getLongitude());
        userEvent.putExtra(EVENT_INTEREST, event.getInterest());
        userEvent.putExtra(EVENT_START, event.getStart());
        userEvent.putExtra(EVENT_END, event.getEnd());
        userEvent.putExtra(EVENT_ADDRESS, event.getAddress());


        startActivity(userEvent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    private void initToggle(){
        mToggleButton.setVisibility(View.VISIBLE);

        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFirebaseInstance.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("notification").child(mSelectedInterest).setValue(isChecked);

            }
        });
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToggleButton.isChecked())
                    mActivity.showSnackBar(getString(R.string.notification_on));
                else
                    mActivity.showSnackBar(getString(R.string.notification_off));
            }
        });
    }

    public void setNotifications() {
        Query query = mFirebaseInstance.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("notification").child(mSelectedInterest);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if ((boolean) dataSnapshot.getValue() == true)
                        mToggleButton.setChecked(true);
                    else
                        mToggleButton.setChecked(false);


                } else
                    mToggleButton.setChecked(false);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public EventByInterestListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SEARCH_RADIUS,mRadius);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity.setAppBarExpended();
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.setFABCallBack(this);
        mActivity.setmCurrentFragment(TAG);
        setFabVisibility();


    }


    @Override
    public void onPause() {
        super.onPause();
        mActivity.setFABCallBack(null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_browse_interest, container, false);


        mEventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.events_recycler_view);
        mEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEventsRecyclerView.setNestedScrollingEnabled(false);
        mEventsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mEventsRecyclerView, this));
        mEventsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        mEventsRecyclerView.setAdapter(mEventsAdapter);

        mAVLoadingIndicatorView = rootView.findViewById(R.id.avi);

        mActivity.setToolbarBackground(mImageUrl);
        mToggleButton = rootView.findViewById(R.id.switch_notify);
        initToggle();
        setNotifications();


        mBbubbleSeekBar = rootView.findViewById(R.id.sb_km);

        mBbubbleSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                mRadius = progress;
                initGeoFire(progress);


            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
        });

        getAllUserEvents();

        return rootView;
    }


    private void initGeoFire(int radius) {
        startAnim();
        mEventHashSet.clear();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("events");
        GeoFire geoFire = new GeoFire(ref);

        if (mActivity.getUserLocation() == null) return;
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mActivity.getUserLocation().getLatitude(), mActivity.getUserLocation().getLongitude()), radius);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                if (mUserEvents.contains(key)) return;

                mEventHashSet.add(key);


            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, String.format("Key %s is no longer in the search area", key));

                if (mUserEvents.contains(key)) return;

                if (mEventHashSet.contains(key))
                    mEventHashSet.remove(key);

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                if (mUserEvents.contains(key)) return;

                Log.d(TAG, String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                mEventHashSet.add(key);
            }

            @Override
            public void onGeoQueryReady() {
                getAllEventsByInterests();
                Log.d(TAG, "All initial data has been loaded and events have been fired!");

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });
    }


    public void getAllUserEvents() {
        Query query = mFirebaseInstance.getReference("events").orderByChild("owner").equalTo(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        mUserEvents.add(data.getKey());
                    }
                }
                initGeoFire(mRadius);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getAllEventsByInterests() {
        Query query = mFirebaseInstance.getReference("events").orderByChild("interest").equalTo(mSelectedInterest);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (!mUserEvents.contains(data.getKey()) && mEventHashSet.contains(data.getKey())) {
                            Event event = data.getValue(Event.class);
                            event.setDistance(mActivity.getUserLocation());
                            if (event.isStatus())
                            mEventList.add(event);
                        }
                    }
                    mEventsAdapter.notifyDataSetChanged();
                    Collections.sort(mEventList);
                }

                stopAnim();
                setFabVisibility();

                //mActivity.finishLoadingData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFabVisibility() {
        if (mEventList.size() > 0)
            mActivity.setFloatingActionVisibility(true);
        else
            mActivity.setFloatingActionVisibility(false);
    }

    private void startAnim() {
        mAVLoadingIndicatorView.smoothToShow();
    }

    private void stopAnim() {
        mAVLoadingIndicatorView.smoothToHide();
    }


    @Override
    public void onFABClicked() {
        mActivity.inflateFragment(EventByInterestMapFragment.newInstance(mEventList),true);
    }

    @Override
    public void onAppBarChanged() {
        if (mBbubbleSeekBar!=null)
        mBbubbleSeekBar.correctOffsetWhenContainerOnScrolling();
    }
}
