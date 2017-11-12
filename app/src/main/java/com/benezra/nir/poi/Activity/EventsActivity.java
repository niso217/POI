package com.benezra.nir.poi.Activity;

/**
 * Created by nirb on 05/11/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.benezra.nir.poi.Fragment.EventByInterestMapFragment;
import com.benezra.nir.poi.Fragment.EventByInterestListFragment;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.R;

public class EventsActivity extends BaseActivity implements FragmentDataCallBackInterface {
    private DrawerLayout drawerLayout;
    private EventByInterestListFragment mUserEventFragment;
    final static String TAG = EventsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        Intent intent = getIntent();
        String interest = intent.getStringExtra("interest");


        mUserEventFragment = (EventByInterestListFragment) getSupportFragmentManager().findFragmentByTag(EventByInterestListFragment.class.getSimpleName());
        if (mUserEventFragment == null) {
            Log.d(TAG, "event fragment null");
            Bundle bundle = new Bundle();
            bundle.putString("interest", interest);
            mUserEventFragment = new EventByInterestListFragment();
            mUserEventFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.framelayout, mUserEventFragment, EventByInterestListFragment.class.getSimpleName()).commit();
        }


        // setSupportActionBar(toolbar);

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
}
