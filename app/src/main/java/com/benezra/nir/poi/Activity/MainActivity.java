package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.Objects.LocationHistory;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Adapter.FragmentPagerAdapter;
import com.benezra.nir.poi.Utils.DateUtil;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends BaseActivity implements
        FragmentDataCallBackInterface, OnSuccessListener<Location>, OnFailureListener {

    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    TabLayout mTabLayout;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Location mLastKnownLocation;
    private ViewPager viewPager;
    private DatabaseReference mFirebaseEventPicReference;
    private Element nextsib;
    EventsInterestData temp;
    List<String> images;
    private FragmentPagerAdapter mFragmentPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // getImagefromGoogle();

        //new RetrieveFeedTask().execute();

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        images = new ArrayList<>();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        mToolbar.setTitle("");

        mAuth = FirebaseAuth.getInstance();


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Find the view pager that will allow the user to swipe between fragments
        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        mToolbar.setBackgroundResource(R.drawable.alcohol_party_dark);
        ViewGroup.LayoutParams layoutParams = mToolbar.getLayoutParams();
        layoutParams.height = dpToPx(250);
        mToolbar.setLayoutParams(layoutParams);
//        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (savedInstanceState == null)
            navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
        else
            initPages();


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
                .addOnSuccessListener(this, this)
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
        initPages();
        showSnackBar(getString(R.string.no_location_determined));
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
            List<Double> loc = Arrays.asList(location.getLatitude(), location.getLongitude());
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                    .child("/l").setValue(loc);


            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                    .child("location_history").push().setValue(
                    new LocationHistory(loc,DateUtil.getCurrentDateTimeInMilliseconds()));

        }

        initPages();

    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.d(TAG, e.getMessage().toString());

        initPages();

    }

    private void initPages() {
        mFragmentPagerAdapter = new FragmentPagerAdapter(this, getSupportFragmentManager(), mLastKnownLocation);
        viewPager.setAdapter(mFragmentPagerAdapter);
        // Give the TabLayout the ViewPager
        mTabLayout = findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(viewPager);
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
                            nextsib = h2.nextElementSibling();
                            while (nextsib != null) {
                                if (nextsib.tagName().equals("div") || nextsib.tagName().equals("ul")) {
                                    //here you will get an Elements object which you
                                    //can iterate through to get the links in the
                                    //geography section
                                    Elements elements = nextsib.select("a[href][title]");
                                    if (elements.size() > 3) {
                                        for (Element el : elements) {
                                            String title = el.text();

                                            if (!title.equals("") && !isListContainsInterest(list, title)) {


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
                                                    Random r = new Random();
                                                    int Low = 0;
                                                    int High = 5;
                                                    int Result = r.nextInt(High-Low) + Low;

                                                    String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
                                                    String url = "https://www.google.com/search?site=imghp&tbm=isch&source=hp&q=" + title + "&gws_rd=cr&tbs=isz:m,sur:f";

                                                    List<String> resultUrls = new ArrayList<String>();

                                                    try {

                                                        Document google_doc = Jsoup.connect(url).userAgent(userAgent).referrer("https://www.google.com/").get();

                                                        Elements google_elements = google_doc.select("div.rg_meta");

                                                        JSONObject jsonObject;
                                                        for (Element element : google_elements) {
                                                            if (element.childNodeSize() > 0) {
                                                                jsonObject = (JSONObject) new JSONParser().parse(element.childNode(0).toString());
                                                                resultUrls.add((String) jsonObject.get("ou"));
                                                            }
                                                        }

                                                        System.out.println("number of results: " + resultUrls.size());

//                                                        for (String imageUrl : resultUrls) {
//                                                            System.out.println(imageUrl);
//                                                        }

                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    } catch (org.json.simple.parser.ParseException e) {
                                                        e.printStackTrace();
                                                    }


                                                    //Element masthead = tempdoc.select("div#mw-content-text").first();
                                                    Elements paragraphs = tempdoc.select("p:not(:has(#coordinates))");
                                                    //Elements metaOgImage = tempdoc.select("meta[property=og:image]");
                                                    temp = new EventsInterestData();
                                                    temp.setTitle(title);
                                                    temp.setInterest(title);
                                                    temp.setCategories(categories_list);
                                                    String upToNCharacters = paragraphs.text().substring(0, Math.min(paragraphs.text().length(), 100));
                                                    temp.setDetails(paragraphs == null ? "" : upToNCharacters);
                                                    list.add(temp);
                                                    Log.d(TAG, "==========" + list.size() + "==========");


                                                    Bitmap myBitmap = null;
                                                    int i = 0;
                                                    while (myBitmap == null) {
                                                        try {
                                                            URL url1 = new URL(resultUrls.get(i++));
                                                            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
                                                            connection.setDoInput(true);
                                                            connection.connect();
                                                            InputStream input = connection.getInputStream();
                                                            myBitmap = BitmapFactory.decodeStream(input);
                                                        } catch (Exception e) {
                                                            Log.d(TAG, e.getMessage());
                                                        }
                                                    }

                                                    //temp.setImage(metaOgImage == null ? "" : metaOgImage.attr("content"));
                                                    // Bitmap bitmap = new DownloadBitmapTask().execute(resultUrls.get(1)).get();

                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                                    byte[] data = baos.toByteArray();
                                                    //final String pic_id = UUID.randomUUID().toString() + ".jpg";

                                                    final String pic_id = title.replaceAll(" ", "_").toLowerCase() + ".jpg";


                                                    StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("interests_images").child(pic_id);
                                                    fileRef.putBytes(data)
                                                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    //mProgressDialogFragment.dismiss();

                                                                    Log.i(TAG, "Uri: " + taskSnapshot.getDownloadUrl());
                                                                    Log.i(TAG, "Name: " + taskSnapshot.getMetadata().getName());
                                                                    images.add(taskSnapshot.getDownloadUrl().toString());

                                                                    //temp.setImage(taskSnapshot.getDownloadUrl().toString());
                                                                    Log.d(TAG, "========File Uploade=d =========");
                                                                    //nextsib = nextsib.nextElementSibling();

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception exception) {
                                                                    Log.d(TAG, exception.getMessage());
                                                                    //nextsib = nextsib.nextElementSibling();

                                                                }
                                                            })
                                                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    // progress percentage
                                                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                                                    Log.d(TAG, "addOnProgressListener " + progress + "");

                                                                }
                                                            })
                                                            .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    System.out.println("Upload is paused!");
                                                                }
                                                            });

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

            while (images.size() != list.size()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < images.size(); i++) {
                list.get(i).setImage(images.get(i));
            }

            FirebaseDatabase.getInstance().getReference("interests_data").setValue(list);
            Log.d("Finsish", "==========finish===============");
            return null;
        }
    }


    private boolean isListContainsInterest(List<EventsInterestData> list, String interest) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getInterest().toLowerCase().equals(interest.toLowerCase())) return true;
        }
        return false;
    }


}

