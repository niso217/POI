package com.benezra.nir.poi.Fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nir on 12/31/15.
 */
public class MainFragment extends Fragment{

    private static final String TAG = MainFragment.class.getSimpleName();
    private MainActivity mActivity;




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mActivity.setToolbarBackground(null);
        mActivity.setmCurrentFragment(TAG);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);


        ViewPager viewPager = view.findViewById(R.id.viewpager);
        TabLayout tabLayout =  view.findViewById(R.id.sliding_tabs);

        //Add tabs icon with setIcon() or simple text with .setText()
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_explore_white_48dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_favorite_white_48dp));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_thumb_up_white_48dp));

        //Add fragments
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
        adapter.addFragment(new MainEventFragment());
        adapter.addFragment(new UserEventFragment());
        adapter.addFragment(new ParticipateEventFragment());

        //Setting adapter
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));



        return view;
    }



    class PagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }

}
