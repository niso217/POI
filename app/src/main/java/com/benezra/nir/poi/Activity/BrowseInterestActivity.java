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

import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.EventByInterestListFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class BrowseInterestActivity extends BaseActivity implements
        FragmentDataCallBackInterface,
        PermissionsDialogFragment.PermissionsGrantedCallback
        {
    private DrawerLayout drawerLayout;
    private EventByInterestListFragment mUserEventFragment;
    private AlertDialogFragment alertDialog;
    final static String TAG = BrowseInterestActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_interest);

        Intent intent = getIntent();
        String interest = intent.getStringExtra("interest");
        String image = intent.getStringExtra("image");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(interest);



        mUserEventFragment = (EventByInterestListFragment) getSupportFragmentManager().findFragmentByTag(EventByInterestListFragment.class.getSimpleName());
        if (mUserEventFragment == null) {
            Log.d(TAG, "event fragment null");
            Bundle bundle = new Bundle();
            bundle.putString("interest", interest);
            bundle.putString("image", image);
            mUserEventFragment = new EventByInterestListFragment();
            mUserEventFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mUserEventFragment, EventByInterestListFragment.class.getSimpleName()).commit();
        }


        // setSupportActionBar(toolbar);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        navigateToCaptureFragment(new String[]{ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

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
        if(alertDialog!=null && alertDialog.isVisible()) return;

        if (isPermissionGranted(permissions)) {
            if (mUserEventFragment!=null)
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
