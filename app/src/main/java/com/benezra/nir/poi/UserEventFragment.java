package com.benezra.nir.poi;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.benezra.nir.poi.Helper.PermissionsDialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import static com.benezra.nir.poi.Helper.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Helper.Constants.EVENT_ID;
import static com.benezra.nir.poi.Helper.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Helper.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Helper.Constants.EVENT_START;
import static com.benezra.nir.poi.Helper.Constants.EVENT_TITLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserEventFragment extends Fragment implements ValueEventListener {

    private EventModel mEventModel;
    private ArrayList<Event> mEventList;
    private FirebaseDatabase mFirebaseInstance;
    private CategoryAdapter mCategoryAdapter;
    private ListView mListView;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private FirebaseUser mFirebaseUser;
    private UserEventFragmentCallback mListener;
    private Context mContext;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof UserEventFragmentCallback) {
            mListener = (UserEventFragmentCallback) context;
        }
    }

    public interface UserEventFragmentCallback {
        void showDialog();
        void hideDialog();

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventModel = new EventModel();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mEventList = new ArrayList<>();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        addEventChangeListener();
    }


    private void addEventChangeListener() {
            Query query = mFirebaseInstance.getReference("events").orderByChild("owner").equalTo(mFirebaseUser.getUid());
            query.addValueEventListener(this);
             mListener.showDialog();
    }


    public UserEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_list, container, false);


        // Create an {@link CategoryAdapter}, whose data source is a list of
        // {@link Categories}. The adapter knows how to create list item views for each item
        // in the list.
        mCategoryAdapter = new CategoryAdapter(getActivity(), mEventList,mFirebaseUser.getPhotoUrl().toString());

        // Get a reference to the ListView, and attach the adapter to the listView.
        mListView = (ListView) rootView.findViewById(R.id.list);
        mListView.setAdapter(mCategoryAdapter);


        //Set a click listener on that View
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {

                // Get the Category object at the given position the user clicked on
                Event event = mEventList.get(position);


                Intent userEvent = new Intent(getActivity(), CreateEventActivity.class);
                userEvent.putExtra(EVENT_ID, event.getId());
                userEvent.putExtra(EVENT_TITLE, event.getTitle());
                userEvent.putExtra(EVENT_OWNER, event.getOwner());
                //userEvent.putExtra(EVENT_IMAGE, event.getImage());
                userEvent.putExtra(EVENT_DETAILS, event.getDetails());
                userEvent.putExtra(EVENT_LATITUDE, event.getLatitude());
                userEvent.putExtra(EVENT_LONGITUDE, event.getLongitude());
                userEvent.putExtra(EVENT_INTEREST, event.getInterest());
                userEvent.putExtra(EVENT_START, event.getStart());


                try{
                    startActivity(userEvent);
                }
                catch (Exception e){
                    Log.d("",e.getMessage());
                }
            }
        });

        return rootView;
    }




    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                Event event = data.getValue(Event.class);
                    event.setId(data.getKey());
                    mEventModel.addEvent(data.getKey(),event);
            }
            mEventList =new ArrayList<>(mEventModel.getEvents().values());
            mCategoryAdapter.setItems(mEventList);

        }
        mListener.hideDialog();

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
