package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Adapter.SimpleFragmentPagerAdapter;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends BaseActivity implements
        FragmentDataCallBackInterface,OnSuccessListener<Location>,OnFailureListener {

    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    TabLayout mTabLayout;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Location mLastKnownLocation;
    private ViewPager viewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // new RetrieveFeedTask().execute();

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        mToolbar.setTitle("");

        mAuth = FirebaseAuth.getInstance();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Find the view pager that will allow the user to swipe between fragments
         viewPager =  findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);


        // Give the TabLayout the ViewPager
        mTabLayout = findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(viewPager);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }


        mToolbar.setBackgroundResource(R.drawable.alcohol_party_dark);
        ViewGroup.LayoutParams layoutParams = mToolbar.getLayoutParams();
        layoutParams.height = dpToPx(250);
        mToolbar.setLayoutParams(layoutParams);
//        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

    }



    @Override
    protected void onResume() {
        navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

        super.onResume();
    }

    private void setupTabIcons() {
        mTabLayout.getTabAt(0).setIcon(R.drawable.ic_explore_white_48dp);
        mTabLayout.getTabAt(1).setIcon(R.drawable.ic_favorite_white_48dp);
        mTabLayout.getTabAt(2).setIcon(R.drawable.ic_thumb_up_white_48dp);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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

    private void initFusedLocation() {

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this,this)
                .addOnFailureListener(this, this);
    }

    @Override
    public void navigateToCaptureFragment(String[] permissions) {

        if (isPermissionGranted(permissions)) {


            if (Arrays.asList(permissions).contains(ACCESS_FINE_LOCATION)) {
                initFusedLocation();
            }
            if (Arrays.asList(permissions).contains(Manifest.permission.CAMERA)) {

            }
        } else {
            PermissionsDialogFragment dialogFragment = (PermissionsDialogFragment) getSupportFragmentManager().findFragmentByTag(PermissionsDialogFragment.class.getName());
            if (dialogFragment == null) {
                Log.d(TAG, "opening dialog");
                PermissionsDialogFragment permissionsDialogFragment = PermissionsDialogFragment.newInstance();
                permissionsDialogFragment.setPermissions(permissions);
                permissionsDialogFragment.show(getSupportFragmentManager(), PermissionsDialogFragment.class.getName());

            }
        }
    }

    @Override
    public void UserIgnoredPermissionDialog() {
        initPager();
    }

    private boolean isPermissionGranted(String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    @Override
    public void onSuccess(Location location) {
        // Got last known location. In some rare situations this can be null.
        if (location != null) {
            mLastKnownLocation = location;
            GeoHash geoHash = new GeoHash(new GeoLocation(location.getLatitude(), location.getLongitude()));
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                    .child("/g").setValue(geoHash.getGeoHashString());
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                    .child("/l").setValue(Arrays.asList(location.getLatitude(), location.getLongitude()));
        }
        initPager();

    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.d(TAG, e.getMessage().toString());
        initPager();

    }

    private void initPager(){
        // Create an adapter that knows which fragment should be shown on each page
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(this, getSupportFragmentManager(),mLastKnownLocation);
        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);
        setupTabIcons();
    }


    class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

        private Exception exception;
        List<EventsInterestData> list = new ArrayList<>();


        protected Void doInBackground(String... urls) {
            try {
                Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_hobbies").timeout(10000).get();


                Element intro = doc.body().select("p").first();
                while (intro.tagName().equals("p")) {
                    //here you will get an Elements object which you can
                    //iterate through to get the links in the intro
                    intro = intro.nextElementSibling();
                }

                for (Element h2 : doc.body().select("h2,h3")) {
                    if (h2.select("span").size() == 4 || h2.select("span").size() == 3) {
                        String main = h2.select("span").get(0).text();
                        if (main.equals("Outdoor hobbies") ||
                                main.equals("Indoor hobbies") ||
                                main.equals("Collection hobbies") ||
                                main.equals("Competitive hobbies") ||
                                main.equals("Observation hobbies")) {
                            Element nextsib = h2.nextElementSibling();
                            while (nextsib != null) {
                                if (nextsib.tagName().equals("div") || nextsib.tagName().equals("ul")) {
                                    //here you will get an Elements object which you
                                    //can iterate through to get the links in the
                                    //geography section
                                    Elements elements = nextsib.select("a[href][title]");
                                    if (elements.size() > 3) {
                                        for (Element el : elements) {
                                            String title = el.text();
                                            if (title.equals("Volleyball"))
                                            {
                                                Log.d("nir","");
                                            }
                                            if (title.equals("Water Polo"))
                                            {
                                                Log.d("nir","");
                                            }
                                            if (!title.equals("") && !isListContainsInterest(list,title))  {

                                                Document tempdoc = null;
                                                try {
                                                    tempdoc = Jsoup.connect(el.attr("abs:href")).timeout(20000).get();

                                                } catch (NullPointerException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                } catch (HttpStatusException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }

                                                if (tempdoc != null) {
                                                    Elements categories = tempdoc.select("div#mw-normal-catlinks");
                                                    Elements cat_list = new Elements();
                                                    cat_list = categories.select("a[href][title]");

                                                    String categories_list = "";
                                                    for (int i = 1; i < cat_list.size(); i++) {
                                                        String cat = cat_list.get(i).text();
                                                        if (!cat.equals("")) {
                                                            if (i != cat_list.size() - 1)
                                                                categories_list = categories_list + cat.toLowerCase() + ",";
                                                            else
                                                                categories_list = categories_list + cat.toLowerCase();

                                                        }
                                                    }


                                                    //Element masthead = tempdoc.select("div#mw-content-text").first();
                                                    Elements paragraphs = tempdoc.select("p:not(:has(#coordinates))");
                                                    Elements metaOgImage = tempdoc.select("meta[property=og:image]");
                                                    EventsInterestData temp = new EventsInterestData();
                                                    temp.setTitle(title);
                                                    temp.setInterest(title);
                                                    temp.setCategories(categories_list);
                                                    temp.setImage(metaOgImage == null ? "" : metaOgImage.attr("content"));
                                                    temp.setDetails(paragraphs == null ? "" : paragraphs.text());
                                                    list.add(temp);
                                                    Log.d("Added", "==========" + title + "===============");
                                                }


                                            }
                                            //list.add(title);
                                        }
                                    }


                                    nextsib = nextsib.nextElementSibling();
                                } else if (nextsib.tagName().equals("h2")) {
                                    nextsib = null;
                                } else {
                                    nextsib = nextsib.nextElementSibling();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                this.exception = e;

                //return null;
            }
            FirebaseDatabase.getInstance().getReference("interests_data").setValue(list);
            Log.d("Finsish", "==========finish===============");
            return null;
        }
    }

    private boolean isListContainsInterest(List<EventsInterestData> list,String interest){
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getInterest().equals(interest)) return true;
        }
        return false;
    }

    protected void onPostExecute(ArrayList<String> feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }


}

