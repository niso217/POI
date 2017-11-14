package com.benezra.nir.poi.Activity;

/**
 * Created by nirb on 05/11/2017.
 */

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.EventByInterestMapFragment;
import com.benezra.nir.poi.Fragment.EventByInterestListFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.R;

import java.util.HashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Helper.Constants.ACTION_REMOVE;

public class EventsActivity extends BaseActivity implements
        FragmentDataCallBackInterface,
        PermissionsDialogFragment.PermissionsGrantedCallback
        {
    private DrawerLayout drawerLayout;
    private EventByInterestListFragment mUserEventFragment;
    private AlertDialogFragment alertDialog;
    final static String TAG = EventsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Intent intent = getIntent();
        String interest = intent.getStringExtra("interest");
        String image = intent.getStringExtra("image");




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
