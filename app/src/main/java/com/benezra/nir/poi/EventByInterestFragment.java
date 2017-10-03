package com.benezra.nir.poi;


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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventByInterestFragment extends Fragment implements ValueEventListener {

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
    }

    private void initFusedLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            addEventChangeListener();
                        }
                    }
                });
    }


    private void addEventChangeListener() {
        String[] temp = new String[]{"Dance", "Swim"};

        for (int i = 0; i < temp.length; i++) {
            Query query = mFirebaseInstance.getReference("events").orderByChild("interest").equalTo(temp[i]);
            query.addValueEventListener(this);

        }

    }


    public EventByInterestFragment() {
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
//                Bitmap imageResourceUrl = null;
//                try {
//                    imageResourceUrl = decodeFromFirebaseBase64(event.getImage());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
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
               // categoryDetail.putExtra("imageResourceId", imageResourceUrl);
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

        initFusedLocation();

        return rootView;
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            for (DataSnapshot data : dataSnapshot.getChildren()) {
                Event event = data.getValue(Event.class);
                if (!mFirebaseUser.getUid().equals(event.getOwner())){  //skip events from owner
                    event.setDistance(mLastLocation);
                    event.setId(data.getKey());
                    mEventModel.addEvent(data.getKey(),event);
                }
            }
            mEventList =new ArrayList<>(mEventModel.getEvents().values());
            mCategoryAdapter.setItems(mEventList);

        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
