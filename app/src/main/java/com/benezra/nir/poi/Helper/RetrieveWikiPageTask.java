package com.benezra.nir.poi.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.benezra.nir.poi.Objects.EventsInterestData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.benezra.nir.poi.Interface.Constants.WIKI_INTERESTS;

/**
 * Created by nirb on 14/12/2017.
 */

public class RetrieveWikiPageTask extends AsyncTask<String, Integer, Boolean> {


    final static String TAG = RetrieveWikiPageTask.class.getSimpleName();
    public RetrieveInterestsTask.AsyncResponse mDelegate;
    Map<String, Object> mEventsInterestDataMap;
    private Context mContext;
    private int result = -1;


    protected void onPreExecute() {
        mEventsInterestDataMap = new HashMap<>();
    }


    public RetrieveWikiPageTask(RetrieveInterestsTask.AsyncResponse delegate) {
        this.mDelegate = delegate;
        this.mDelegate.asyncStatus(true);
    }


    protected Boolean doInBackground(String... urls) {

        String html = urls[0];

        if (!URLUtil.isHttpsUrl(html)) {
            return false;
        }

        Document mEventsInterestDatadoc = null;
        try {
            mEventsInterestDatadoc = Jsoup.connect(html).timeout(5000).get();


            if (mEventsInterestDatadoc != null) {


                int index = html.lastIndexOf('/');

                if (index == html.length() - 1) {
                    return false;
                }


                final String title = html.substring(index + 1, html.length()).toLowerCase().replaceAll("_", " ");


                Pattern p = Pattern.compile("^([a-zA-Z_ ])[a-zA-Z_-]*[\\w_-]*[\\S]$|^([a-zA-Z_ ])[0-9_-]*[\\S]$|^[a-zA-Z_ ]*[\\S]$");
                boolean hasSpecialChar = p.matcher(title).find();

                if (!hasSpecialChar)
                    return false;


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
                EventsInterestData mEventsInterestData = new EventsInterestData();
                mEventsInterestData.setTitle(title);
                mEventsInterestData.setInterest(title);
                mEventsInterestData.setCategories(categories_list);
                String upToNCharacters = paragraphs.text().substring(0, Math.min(paragraphs.text().length(), 100));
                mEventsInterestData.setDetails(paragraphs == null ? "" : upToNCharacters);
                mEventsInterestDataMap.put(title, mEventsInterestData);


                String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
                String url = "https://www.google.com/search?site=imghp&tbm=isch&source=lnt&q=" + title + "&tbs=isz:m";
//            String url = "https://www.google.co.il/search?q=" + interest + "&safe=active&tbm=isch&source=lnt&tbs=isz:m";
                List<String> resultUrls = new ArrayList<>();


                Document google_doc = Jsoup.connect(url).userAgent(userAgent).referrer("https://www.google.com/").get();

                Elements google_elements = google_doc.select("div.rg_meta");

                JSONObject jsonObject;
                for (Element element : google_elements) {
                    if (element.childNodeSize() > 0) {
                        jsonObject = (JSONObject) new JSONParser().parse(element.childNode(0).toString());
                        resultUrls.add((String) jsonObject.get("ou"));
                    }
                }


                Bitmap googleImageBitmap = null;
                int i = 0;

                while (googleImageBitmap == null) {

                    URL url1 = new URL(resultUrls.get(i++));
                    HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    int responseCode = connection.getResponseCode();

                    if (responseCode == 200) {
                        // response code is OK
                        InputStream input = connection.getInputStream();
                        googleImageBitmap = BitmapFactory.decodeStream(input);
                    } else {
                        Thread.sleep(500);
                    }

                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                googleImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] data = baos.toByteArray();
                //final String pic_id = UUID.randomUUID().toString() + ".jpg";

                final String pic_id = title + ".jpg";

                StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("interests_images").child(pic_id);
                fileRef.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                EventsInterestData eventsInterestData = (EventsInterestData) mEventsInterestDataMap.get(title);
                                if (eventsInterestData != null) {
                                    eventsInterestData.setImage(taskSnapshot.getDownloadUrl().toString());
                                    mEventsInterestDataMap.put(title, eventsInterestData);
                                    Log.d(TAG, "========File Uploaded =========");
                                    result = 0;

                                } else {
                                    Log.d(TAG, "null");
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d(TAG, exception.getMessage());
                                result = 1;
                            }
                        });

            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        } catch (org.json.simple.parser.ParseException e) {
            Log.e(TAG, e.getMessage());
            return false;
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        while (result < 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (result > 0)
            return false;
        else
            return true;


    }

    @Override
    protected void onPostExecute(Boolean state) {
        super.onPostExecute(state);
        mDelegate.processFinish(state, mEventsInterestDataMap);
        this.mDelegate.asyncStatus(false);


    }

}



