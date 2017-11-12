package com.benezra.nir.poi;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.benezra.nir.poi.Fragment.LikedEventFragment;
import com.benezra.nir.poi.Fragment.MainEventFragment;
import com.benezra.nir.poi.Fragment.MyEventFragment;

/**
 * Created by justynagolawska on 12/03/2017.
 */

/**
 * Provides the appropriate {@link Fragment} for a view pager.
 */
public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }



    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return new MainEventFragment();
            case 1:
                return new MyEventFragment();
            case 2:
                return new LikedEventFragment();
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
