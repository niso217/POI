package com.benezra.nir.poi;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

import java.util.ArrayList;

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

                //Getting the category name
                String title = event.getTitle();
                // Getting the image resource id for the category
                String imageResourceUrl = event.getImage();
                // Getting the first paragraph text
                String firstParagraph = event.getDetails();
                // Getting the longitude
                Double longitude = event.getLongitude();
                // Getting the latitude
                Double latitude = event.getLatitude();
                // Getting the map location title
                String locationTitle = event.getTitle();

                Intent categoryDetail = new Intent(getActivity(), CategoryDeatailActivity.class);
                //Passing the category title to the CategoryDetailActivity
                categoryDetail.putExtra("eventId", event.getId());
                //Passing the category title to the CategoryDetailActivity
                categoryDetail.putExtra("categoryTitle", title);
                //Passing the image id to the CategoryDetailActivity
                categoryDetail.putExtra("imageResourceId", imageResourceUrl);
                //Passing the first paragraph text to the CategoryDetailActivity
                categoryDetail.putExtra("firstParagraphText", firstParagraph);
                //Passing the longitude google coordinate to the CategoryDetailActivity
                categoryDetail.putExtra("longitude", longitude);
                //Passing the latitude google coordinate to the CategoryDetailActivity
                categoryDetail.putExtra("latitude", latitude);
                //Passing the map location title to the CategoryDetailActivity
                categoryDetail.putExtra("locationTitle", locationTitle);

                startActivity(categoryDetail);
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
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
