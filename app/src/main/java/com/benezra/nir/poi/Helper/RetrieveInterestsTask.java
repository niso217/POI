package com.benezra.nir.poi.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.Utils.DataFaker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Random;

import static com.benezra.nir.poi.Interface.Constants.WIKI_INTERESTS;
import static com.google.android.gms.internal.zzahg.runOnUiThread;

/**
 * Created by nirb on 14/12/2017.
 */

public class RetrieveInterestsTask extends AsyncTask<String,Integer,Boolean> {

    private Context mContext;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private List<EventsInterestData> mEventsInterestDataList;
    private EventsInterestData mEventsInterestData;
    private List<String> mFireBaseImagesURL;
    public AsyncResponse mDelegate;


    final static String TAG = RetrieveInterestsTask.class.getSimpleName();
    private Element mNextElement;


    protected void onPreExecute(){
        mEventsInterestDataList = new ArrayList<>();
        mFireBaseImagesURL = new ArrayList<>();
    }


    public RetrieveInterestsTask() {
    }


    public RetrieveInterestsTask(Context mContext, ImageView mImageView, ProgressBar mProgressBar,TextView textView,AsyncResponse delegate) {
        this.mContext = mContext;
        this.mImageView = mImageView;
        this.mProgressBar = mProgressBar;
        this.mTextView = textView;
        this.mDelegate = delegate;
    }

    protected Boolean doInBackground(String... urls) {


        try {
            Document doc = Jsoup.connect(WIKI_INTERESTS).timeout(10000).get();
            
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
                        mNextElement = h2.nextElementSibling();
                        while (mNextElement != null) {
                            if (mNextElement.tagName().equals("div") || mNextElement.tagName().equals("ul")) {
                                //here you will get an Elements object which you
                                //can iterate through to get the links in the
                                //geography section
                                Elements elements = mNextElement.select("a[href][title]");
                                if (elements.size() > 3) {
                                    for (Element el : elements) {

                                        if(isCancelled()) {
                                            return null;
                                        }
                                        String title = el.text();


                                        if (!title.equals("") && !isListContainsInterest(mEventsInterestDataList, title)) {

                                            int presentage = (int)(100 * ((double)mEventsInterestDataList.size() / 287));
                                            setInterestText(title + System.getProperty ("line.separator") + presentage + " %");

                                            publishProgress(mEventsInterestDataList.size());

                                            Document mEventsInterestDatadoc = null;
                                            try {
                                                mEventsInterestDatadoc = Jsoup.connect(el.attr("abs:href")).timeout(20000).get();

                                            } catch (NullPointerException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            } catch (HttpStatusException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }

                                            if (mEventsInterestDatadoc != null) {
                                                Elements categories = mEventsInterestDatadoc.select("div#mw-normal-catlinks");
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

                                                Elements paragraphs = mEventsInterestDatadoc.select("p:not(:has(#coordinates))");
                                                mEventsInterestData = new EventsInterestData();
                                                mEventsInterestData.setTitle(title);
                                                mEventsInterestData.setInterest(title);
                                                mEventsInterestData.setCategories(categories_list);
                                                String upToNCharacters = paragraphs.text().substring(0, Math.min(paragraphs.text().length(), 100));
                                                mEventsInterestData.setDetails(paragraphs == null ? "" : upToNCharacters);
                                                mEventsInterestDataList.add(mEventsInterestData);

                                            }

                                        }

                                    }
                                }

                                mNextElement = mNextElement.nextElementSibling();

                            } else if (mNextElement.tagName().equals("h2")) {
                                mNextElement = null;
                            } else {
                                mNextElement = mNextElement.nextElementSibling();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }


        return true;
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        mTextView.setText("");
        mProgressBar.setProgress(0);
    }

    private boolean isListContainsInterest(List<EventsInterestData> list, String interest) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getInterest().toLowerCase().equals(interest.toLowerCase())) return true;
        }
        return false;
    }



    protected void onCancelled(){

    }

    protected void onProgressUpdate(Integer... progress){
        mProgressBar.setProgress(progress[0]);

    }


    private void setInterestText(final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(title);

            }
        });
    }


    protected void onPostExecute(Boolean  result){
        super.onPostExecute(result);

        mDelegate.processFinish(result,mEventsInterestDataList);

    }

    public interface AsyncResponse {
        void processFinish(boolean output,List<EventsInterestData> list);
    }
}

