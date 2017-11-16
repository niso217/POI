package com.benezra.nir.poi.Activity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.benezra.nir.poi.Interface.FragmentDataCallBackInterface;
import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.SimpleFragmentPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements
        FragmentDataCallBackInterface {

    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    TabLayout mTabLayout;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // new RetrieveFeedTask().execute();

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        mToolbar.setTitle("");

        mAuth = FirebaseAuth.getInstance();


        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);
        // Create an adapter that knows which fragment should be shown on each page
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
//                startActivity(intent);
//
//            }
//        });

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

