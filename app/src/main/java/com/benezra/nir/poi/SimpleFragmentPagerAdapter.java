package com.benezra.nir.poi;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.benezra.nir.poi.Fragment.EventByInterestFragment;
import com.benezra.nir.poi.Fragment.UserEventFragment;

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
        if (position == 0) {
            return new EventByInterestFragment();
        } else if (position == 1){
            return new UserEventFragment();
            //return new PlacesFragment();
        } else if (position == 2){
            //return new FoodFragment();
        } else {
            //return new NatureFragment();
        }
        return new EventByInterestFragment();
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 4;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return mContext.getString(R.string.event_by_interests);
            case 1:
                return mContext.getString(R.string.events_by_user);
            case 2:
                return mContext.getString(R.string.category_food);
            case 3:
                return mContext.getString(R.string.category_nature);
            default:
                return null;
        }
    }

}
