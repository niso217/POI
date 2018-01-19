package com.benezra.nir.poi.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Adapter.EventsInterestsAdapter;
import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.benezra.nir.poi.Interface.Constants.LOCATION;
import static com.benezra.nir.poi.Interface.Constants.LOCATION_CALLBACK;
import static com.benezra.nir.poi.Interface.Constants.LOCATION_CHANGED;
import static com.benezra.nir.poi.Interface.Constants.POSITION;
import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainEventFragment extends Fragment implements
        RecyclerTouchListener.ClickListener, SearchView.OnQueryTextListener {

    private FirebaseDatabase mFirebaseInstance;
    private RecyclerView mInteresRecyclerView;
    private List<EventsInterestData> mEventsInterestDataList;
    final static String TAG = MainEventFragment.class.getSimpleName();
    private EventsInterestsAdapter mEventsInterestsAdapter;
    private SearchView mSearchView;
    private ProgressBar mProgressBar;
    private int mSelectedPosition;
    private boolean mAwaitCallback;

    private MainActivity mActivity;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                inflateEventByInterest(mSelectedPosition);
                LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventsInterestDataList = new ArrayList<>();
        mEventsInterestsAdapter = new EventsInterestsAdapter(getContext(), mEventsInterestDataList);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_interests, container, false);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        mSearchView = rootView.findViewById(R.id.search_view);
        mProgressBar = rootView.findViewById(R.id.pb);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager = new GridLayoutManager(getContext(), 7);
        }
        mInteresRecyclerView = rootView.findViewById(R.id.main_event_list);
        mInteresRecyclerView.setLayoutManager(layoutManager);
        mInteresRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mInteresRecyclerView, this));
        mInteresRecyclerView.setAdapter(mEventsInterestsAdapter);

        setupSearchView();

        if (mEventsInterestDataList.size() == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
            getAllInterests();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }


        return rootView;
    }


    private void getAllInterests() { //flag=false - first run flag=true - update database
        Query query = mFirebaseInstance.getReference("interests_data");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<EventsInterestData> eventsInterestDataList = new ArrayList<>();
                mEventsInterestDataList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        EventsInterestData eventsInterestData = data.getValue(EventsInterestData.class);
                        mEventsInterestDataList.add(eventsInterestData);
                    }

                }
                mProgressBar.setVisibility(View.GONE);
                mEventsInterestsAdapter.sortList();
                mEventsInterestsAdapter.filter("");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mEventsInterestsAdapter.sortList();
        mEventsInterestsAdapter.filter("");

    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Search Here");
    }

    public boolean onQueryTextChange(String newText) {
        mEventsInterestsAdapter.filter(newText);
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public void onClick(View view, int position) {

        mSelectedPosition = position;
        if (mActivity.getUserLocation() != null) {
            inflateEventByInterest(position);
        } else {
           // mAwaitCallback = true;
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver,
                    new IntentFilter(LOCATION_CHANGED));
            mActivity.setLocationResolver(false);
            mActivity.askForLocation();

        }
    }

    @Override
    public void onLongClick(View view, int position) {

    }


    private void inflateEventByInterest(int position) {
        mActivity.inflateFragment(EventByInterestListFragment.newInstance(
                mEventsInterestsAdapter.getFilterList().get(position).getInterest(),
                mEventsInterestsAdapter.getFilterList().get(position).getImage()),true);
    }
}