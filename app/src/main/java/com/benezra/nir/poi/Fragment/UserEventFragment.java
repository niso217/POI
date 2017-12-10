package com.benezra.nir.poi.Fragment;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.benezra.nir.poi.Activity.CreateEventActivity;
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
public class UserEventFragment extends Fragment implements ValueEventListener, RecyclerTouchListener.ClickListener {

    private FirebaseDatabase mFirebaseInstance;
    private FirebaseUser mFirebaseUser;
    private RecyclerView mEventsRecyclerView;
    private List<Event> mEventList;
    private EventsAdapter mEventsAdapter;
    private Context mContext;
    private FragmentDataCallBackInterface mListener;
    private ProgressBar mProgressBar;
    private Location mLastKnownLocation;


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
        userEvent.putExtra(EVENT_ADDRESS, event.getAddress());


        startActivity(userEvent);
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
        ;
        mEventsAdapter = new EventsAdapter(getContext(), mEventList);
        mLastKnownLocation = (Location) getArguments().get(USER_LOCATION);


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

//        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mEventsAdapter);
//        mItemTouchHelper = new ItemTouchHelper(callback);
//        mItemTouchHelper.attachToRecyclerView(mEventsRecyclerView);
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
                if (mLastKnownLocation != null)
                    event.setDistance(mLastKnownLocation);

                mEventList.add(event);

            }
        }

        mEventsAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);


    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
