package com.benezra.nir.poi.Activity;

/**
 * Created by nirb on 05/11/2017.
 */

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.EventByInterestListFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class BrowseInterestActivity extends BaseActivity implements
        FragmentDataCallBackInterface,
        PermissionsDialogFragment.PermissionsGrantedCallback {
    private DrawerLayout drawerLayout;
    private EventByInterestListFragment mUserEventFragment;
    private AlertDialogFragment alertDialog;
    final static String TAG = BrowseInterestActivity.class.getSimpleName();
    private ToggleButton mToggleButton;
    private FirebaseDatabase mFirebaseInstance;
    private String mSelectedInterest;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_interest);

        mFirebaseInstance = FirebaseDatabase.getInstance();


        Intent intent = getIntent();
        mSelectedInterest = intent.getStringExtra("interest");
        String image = intent.getStringExtra("image");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mSelectedInterest);

        mToggleButton = findViewById(R.id.switch_notify);
        mToggleButton.setVisibility(View.VISIBLE);

        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFirebaseInstance.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("notification").child(mSelectedInterest).setValue(isChecked);
            }
        });

        mUserEventFragment = (EventByInterestListFragment) getSupportFragmentManager().findFragmentByTag(EventByInterestListFragment.class.getSimpleName());
        if (mUserEventFragment == null) {
            Log.d(TAG, "event fragment null");
            Bundle bundle = new Bundle();
            bundle.putString("interest", mSelectedInterest);
            bundle.putString("image", image);
            mUserEventFragment = new EventByInterestListFragment();
            mUserEventFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mUserEventFragment, EventByInterestListFragment.class.getSimpleName()).commit();
        }

        setNotifications();
        // setSupportActionBar(toolbar);

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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigateToCaptureFragment(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION});

    }

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }


    @Override
    public void startLoadingData() {
        showProgress(getString(R.string.loading), getString(R.string.please_wait));

    }

    @Override
    public void finishLoadingData() {
        hideProgressMessage();
    }


    @Override
    public void UserIgnoredPermissionDialog() {
        finish();
    }


    @Override
    public void navigateToCaptureFragment(String[] permissions) {
        if (alertDialog != null && alertDialog.isVisible()) return;

        if (isPermissionGranted(permissions)) {
            if (mUserEventFragment != null)
                mUserEventFragment.getAllUserEvents();
        } else {
            PermissionsDialogFragment permissionsDialogFragment = (PermissionsDialogFragment) getSupportFragmentManager().findFragmentByTag(PermissionsDialogFragment.class.getName());
            if (permissionsDialogFragment == null) {
                Log.d(TAG, "opening dialog");
                permissionsDialogFragment = PermissionsDialogFragment.newInstance();
                permissionsDialogFragment.setPermissions(permissions);
                permissionsDialogFragment.show(getSupportFragmentManager(), PermissionsDialogFragment.class.getName());

            }
        }
    }

    private boolean isPermissionGranted(String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }
}
