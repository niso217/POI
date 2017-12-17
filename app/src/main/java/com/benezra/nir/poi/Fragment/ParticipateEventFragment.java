package com.benezra.nir.poi.Fragment;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Adapter.EventsAdapter;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
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
import java.util.List;

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
import static com.benezra.nir.poi.Interface.Constants.USER_LOCATION;

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
    private FragmentDataCallBackInterface mListener;
    private ProgressBar mProgressBar;
    private Location mLastKnownLocation;
    private RelativeLayout mRootLayout;
    private Event mCurrentEvent;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof FragmentDataCallBackInterface) {
            mListener = (FragmentDataCallBackInterface) context;

        }
    }

    @Override
    public void onClick(View view, int position) {
        mCurrentEvent = mEventList.get(position);

        if (mCurrentEvent.isStatus()){
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
        userEvent.putExtra(EVENT_ADDRESS, mCurrentEvent.getAddress());
        startActivity(userEvent);
        }
        else{
            showSnackBarWithAction(getString(R.string.event_finished));
        }
    }

    @Override
    public void onLongClick(View view, int position) {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance.getReference().keepSynced(true);
        mEventList = new ArrayList<>();
        mEventsAdapter = new EventsAdapter(getContext(), mEventList);
        mLastKnownLocation = (Location) getArguments().get(USER_LOCATION);

        if (savedInstanceState != null)
            mCurrentEvent = savedInstanceState.getParcelable("event");
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
        mEventsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        mEventsRecyclerView.setAdapter(mEventsAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        getUserEventsChangeListener();

//        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mEventsAdapter);
//        mItemTouchHelper = new ItemTouchHelper(callback);
//        mItemTouchHelper.attachToRecyclerView(mEventsRecyclerView);


        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("event", mCurrentEvent);

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
                        if (mLastKnownLocation != null)
                            event.setDistance(mLastKnownLocation);
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
        if (events.isEmpty()){
            mProgressBar.setVisibility(View.GONE);
            mEventsAdapter.notifyDataSetChanged();

        }


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

