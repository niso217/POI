package com.benezra.nir.poi;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivityOld extends AppCompatActivity{

    private static final String TAG = MainActivityOld.class.getSimpleName();
    private TextView txtDetails;
    private EditText inputName, inputEmail;
    private Button btnSave;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private String userId;
    private List<Event> participateEventList;
    private List<Event> interestEventList;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private EventModel mEventModel;
    private FirebaseDatabase mFirebaseInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);

        participateEventList = new ArrayList<>();
        interestEventList = new ArrayList<>();
        txtDetails = (TextView) findViewById(R.id.txt_user);
        inputName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        btnSave = (Button) findViewById(R.id.btn_save);
        mEventModel = new EventModel();
        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        //mFirebaseDatabase = mFirebaseInstance.getReference("interests");


        mAuth = FirebaseAuth.getInstance();

        writeNewUser();


    }


    private void updateInterests(){

    }

    private void updateParticipateEvents(){

    }

    private void writeNewUser() {
        if (mAuth.getCurrentUser() != null)

        {
            FirebaseUser user = mAuth.getCurrentUser();
            mFirebaseInstance.getReference("users").child(user.getUid()).child("name").setValue(user.getDisplayName());
            mFirebaseInstance.getReference("users").child(user.getUid()).child("email").setValue(user.getEmail());
            mFirebaseInstance.getReference("users").child(user.getUid()).child("avatar").setValue(user.getPhotoUrl().toString());

        }

    }


    private void initFusedLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            addEventChangeListener();
                            addInterestsChangeListener();
                            getUserEventsChangeListener();
                        }
                    }
                });
    }

    /**
     * User data change listener
     */

    private void getUserParticipateEventsChangeListener(List<String> events) {

        for (int i = 0; i < events.size(); i++) {
            Query query = mFirebaseInstance.getReference("events").child(events.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Event event = dataSnapshot.getValue(Event.class);
                        event.setId(dataSnapshot.getKey());
                        participateEventList.add(event);

                    }
                    Log.d("MainActivityOld", participateEventList.toString());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    private void getUserEventsChangeListener() {
        // User data change listener
        Query query = mFirebaseInstance.getReference("users").child(mAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                getUserParticipateEventsChangeListener(user.getEvents());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    private void addInterestsChangeListener() {
        mFirebaseInstance.getReference("interests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                };
                ArrayList<String> yourStringArray = snapshot.getValue(t);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });
    }

    private void addEventChangeListener() {
        String[] temp = new String[]{"Dance", "Swim"};

        for (int i = 0; i < temp.length; i++) {
            Query query = mFirebaseInstance.getReference("events").orderByChild("interest").equalTo(temp[i]);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && !dataSnapshot.getKey().equals(mAuth.getCurrentUser().getUid())) {
                        // dataSnapshot is the "issue" node with all children with id 0
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Event event = data.getValue(Event.class);
                            mEventModel.addEvent(data.getKey(),event);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

}