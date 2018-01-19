package com.benezra.nir.poi.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Activity.CreateEventActivity;
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
public class UserEventFragment extends Fragment implements ValueEventListener,
        RecyclerTouchListener.ClickListener {

    private FirebaseDatabase mFirebaseInstance;
    private FirebaseUser mFirebaseUser;
    private RecyclerView mEventsRecyclerView;
    private List<Event> mEventList;
    private EventsAdapter mEventsAdapter;
    private Context mContext;
    private MainActivity mActivity;
    private ProgressBar mProgressBar;
    private static final String TAG = UserEventFragment.class.getSimpleName();


    @Override
    public void onPause() {
        super.onPause();
        mActivity.setFABCallBack(null);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
    }

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

    @Override
    public void onClick(View view, int position) {
        Event event = mEventList.get(position);

        Intent userEvent = new Intent(getActivity(), CreateEventActivity.class);
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


    private void addEventChangeListener() {
        Query query = mFirebaseInstance.getReference("events").orderByChild("owner").equalTo(mFirebaseUser.getUid());
        query.addValueEventListener(this);
    }


    public UserEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb);
        mEventsRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_events_list);
        mEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEventsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mEventsRecyclerView, this));
        mEventsRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL_LIST));
        mEventsRecyclerView.setAdapter(mEventsAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        addEventChangeListener();

        return rootView;
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        mEventList.clear();
        if (dataSnapshot.exists()) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                Event event = data.getValue(Event.class);
                if (mActivity.getUserLocation() != null)
                    event.setDistance(mActivity.getUserLocation());

                mEventList.add(event);

            }
        }

        mEventsAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);


    }

    private void updateEventDistance(Location location) {
        for (int i = 0; i < mEventList.size(); i++) {
            mEventList.get(i).setDistance(location);
        }
        mEventsAdapter.notifyDataSetChanged();
        Collections.sort(mEventList);
    }


    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
