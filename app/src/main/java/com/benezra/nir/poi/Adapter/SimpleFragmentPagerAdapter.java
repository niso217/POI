package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.benezra.nir.poi.Fragment.ParticipateEventFragment;
import com.benezra.nir.poi.Fragment.MainEventFragment;
import com.benezra.nir.poi.Fragment.UserEventFragment;

import static com.benezra.nir.poi.Interface.Constants.USER_LOCATION;

/**
 * Created by justynagolawska on 12/03/2017.
 */

/**
 * Provides the appropriate {@link Fragment} for a view pager.
 */
public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private Location mLocation;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm, Location location) {
        super(fm);
        mContext = context;
        mLocation = location;
    }



    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {

        Bundle args = new Bundle();
        args.putParcelable(USER_LOCATION, mLocation);

        switch (position)
        {
            case 0:
                MainEventFragment mainEventFragment = new MainEventFragment();
                mainEventFragment.setArguments(args);
                return mainEventFragment;
            case 1:
                UserEventFragment myEventFragment = new UserEventFragment();
                myEventFragment.setArguments(args);
                return myEventFragment;
            case 2:
                ParticipateEventFragment likedEventFragment = new ParticipateEventFragment();
                likedEventFragment.setArguments(args);
                return likedEventFragment;
        }

         return new MainEventFragment();

    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 3;
    }


    // This determines the name for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate name based on item position
        switch (position) {
            case 0:
                //return mContext.getString(R.string.event_by_interests);
            case 1:
               // return mContext.getString(R.string.events_by_user);
            case 2:
                //return mContext.getString(R.string.liked_events);
            default:
                return null;
        }
    }


}
