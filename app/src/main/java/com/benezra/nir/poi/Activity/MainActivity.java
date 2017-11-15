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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements
        FragmentDataCallBackInterface  {

    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    TabLayout mTabLayout;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //new RetrieveFeedTask().execute();

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);



        mToolbar.setTitle("");

        mAuth = FirebaseAuth.getInstance();


        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

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

    protected ArrayList<String> getBlogStats() throws Exception {
        // get html document structure
        Document document = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_hobbies").get();
        // selector query
        Elements nodeBlogStats = document.select("div-col columns column-width");
        // check results
        ArrayList<String> list = new ArrayList<String>();
        for (Element e : nodeBlogStats) {
            list.add(e.text());
        }
        return list;
    }



    class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

        private Exception exception;
        List<EventsInterestData> list = new ArrayList<>();


        protected Void doInBackground(String... urls) {
            try {
                Document doc = Jsoup.connect("https://en.wikipedia.org/wiki/List_of_hobbies").timeout(5000).get();


                Element intro = doc.body().select("p").first();
                while (intro.tagName().equals("p")) {
                    //here you will get an Elements object which you can
                    //iterate through to get the links in the intro
                    intro = intro.nextElementSibling();
                }

                for (Element h2 : doc.body().select("h2")) {
                    if (h2.select("span").size() == 4) {
                        if (h2.select("span").get(0).text().equals("Outdoor hobbies")) {
                            Element nextsib = h2.nextElementSibling();
                            while (nextsib != null) {
                                if (nextsib.tagName().equals("div")) {
                                    //here you will get an Elements object which you
                                    //can iterate through to get the links in the
                                    //geography section
                                    Elements elements = nextsib.select("a[href][title]");
                                    if (elements.size() > 3) {
                                        for (Element el : elements) {
                                            String title = el.text();
                                            if (!title.equals("")) {
                                                String replaceText = title.replace(' ', '_');
                                                Document tempdoc = Jsoup.connect("https://en.wikipedia.org/wiki/" + replaceText).timeout(5000).get();

                                                Elements categories = tempdoc.select("div#mw-normal-catlinks");
                                                Elements cat_list = new Elements();
                                                cat_list = categories.select("a[href][title]");

                                                String categories_list = "";
                                                for (int i = 1; i < cat_list.size(); i++) {
                                                    String cat = cat_list.get(i).text();
                                                    if (!cat.equals("")) {
                                                        if (i!=cat_list.size()-1)
                                                        categories_list = categories_list + cat.toLowerCase() +",";
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

                return null;
            }
             FirebaseDatabase.getInstance().getReference("interests_data").setValue(list);
            Log.d("Finsish", "==========finish===============");
            return null;
        }
    }

    protected void onPostExecute(ArrayList<String> feed) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}

