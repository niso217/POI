package com.benezra.nir.poi.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Adapter.EventsAdapter;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.benezra.nir.poi.View.DividerItemDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import static com.benezra.nir.poi.Interface.Constants.LOCATION;
import static com.benezra.nir.poi.Interface.Constants.LOCATION_CHANGED;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParticipateEventFragment extends Fragment implements RecyclerTouchListener.ClickListener {

    private FirebaseDatabase mFirebaseInstance;
    private FirebaseUser mFirebaseUser;
    private Context mContext;
    private RecyclerView mEventsRecyclerView;
    private List<Event> mEventList;
    private EventsAdapter mEventsAdapter;
    private MainActivity mActivity;
    private ProgressBar mProgressBar;
    private RelativeLayout mRootLayout;
    private Event mCurrentEvent;
    private static final String TAG = ParticipateEventFragment.class.getSimpleName();



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver,
                new IntentFilter(LOCATION_CHANGED));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LOCATION);
            if (location != null)
                updateEventDistance(location);
        }
    };

    private void updateEventDistance(Location location) {
        for (int i = 0; i < mEventList.size(); i++) {
            mEventList.get(i).setDistance(location);
        }
        mEventsAdapter.notifyDataSetChanged();
        Collections.sort(mEventList);
    }

    @Override
    public void onClick(View view, int position) {
        mCurrentEvent = mEventList.get(position);

        if (mCurrentEvent.isStatus()) {
            Intent userEvent = new Intent(getActivity(), ViewEventActivity.class);
            userEvent.putExtra(EVENT_ID, mCurrentEvent.getId());
            userEvent.putExtra(EVENT_TITLE, mCurrentEvent.getTitle());
            userEvent.putExtra(EVENT_OWNER, mCurrentEvent.getOwner());
            userEvent.putExtra(EVENT_IMAGE, mCurrentEvent.getImage());
            userEvent.putExtra(EVENT_DETAILS, mCurrentEvent.getDetails());
            userEvent.putExtra(EVENT_LATITUDE, mCurrentEvent.getLatitude());
            userEvent.putExtra(EVENT_LONGITUDE, mCurrentEvent.getLongitude());
            userEvent.putExtra(EVENT_INTEREST, mCurrentEvent.getInterest());
            userEvent.putExtra(EVENT_START, mCurrentEvent.getStart());
            userEvent.putExtra(EVENT_END, mCurrentEvent.getEnd());
            userEvent.putExtra(EVENT_ADDRESS, mCurrentEvent.getAddress());
            startActivity(userEvent);
        } else {
            showSnackBarWithAction(getString(R.string.event_finished));
        }
    }

    @Override
    public void onLongClick(View view, int position) {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance.getReference().keepSynced(true);
        mEventList = new ArrayList<>();
        mEventsAdapter = new EventsAdapter(getContext(), mEventList);
    }


    public ParticipateEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);


        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb);
        mRootLayout = rootView.findViewById(R.id.rootlayout);
        mEventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_events_list);
        mEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEventsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mEventsRecyclerView, this));
        mEventsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        mEventsRecyclerView.setAdapter(mEventsAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        getUserEventsChangeListener();



        return rootView;
    }


    private void getUserParticipateEventsChangeListener(List<String> events) {
        mEventList.clear();
        for (int i = 0; i < events.size(); i++) {
            Query query = mFirebaseInstance.getReference("events").child(events.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        Event event = dataSnapshot.getValue(Event.class);
                        if (mActivity.getUserLocation() != null)
                            event.setDistance(mActivity.getUserLocation());
                        mEventList.add(event);

                    }
                    mEventsAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        if (events.isEmpty()) {
            mProgressBar.setVisibility(View.GONE);
            mEventsAdapter.notifyDataSetChanged();

        }


    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.setFABCallBack(null);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

    private void getUserEventsChangeListener() {
        // User data change listener
        final List<String> events = new ArrayList<>();
        Query query = mFirebaseInstance.getReference("users").child(mFirebaseUser.getUid()).child("events");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                events.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        events.add(data.getKey());
                    }

                }
                getUserParticipateEventsChangeListener(events);


            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    public void showSnackBarWithAction(String message) {
        Snackbar snackbar = Snackbar
                .make(mRootLayout, message, Snackbar.LENGTH_LONG)
                .setAction("LEAVE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LeaveEvent();
                    }
                });

        snackbar.show();


    }

    private void LeaveEvent() {
        mFirebaseInstance.getReference("events").child(mCurrentEvent.getId()).child("participates").child(mFirebaseUser.getUid()).removeValue();
        mFirebaseInstance.getReference("users").child(mFirebaseUser.getUid()).child("events").child(mCurrentEvent.getId()).removeValue();

    }

}

