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
import com.squareup.picasso.Picasso;

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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.benezra.nir.poi.Interface.Constants.WIKI_INTERESTS;
import static com.google.android.gms.internal.zzahg.runOnUiThread;

/**
 * Created by nirb on 14/12/2017.
 */

public class RetrieveInterestsImagesTask extends AsyncTask<List<EventsInterestData>, Integer, Boolean> {

    private Context mContext;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private List<EventsInterestData> mEventsInterestDataList;
    private List<String> mFireBaseImages;

    private int mIndex;

    final static String TAG = RetrieveInterestsImagesTask.class.getSimpleName();


    protected void onPreExecute() {
        mEventsInterestDataList = new ArrayList<>();
        mFireBaseImages = new ArrayList<>();
        mIndex = 0;
    }


    public RetrieveInterestsImagesTask() {
    }


    public RetrieveInterestsImagesTask(Context mContext, ImageView mImageView, ProgressBar mProgressBar, TextView textView) {
        this.mContext = mContext;
        this.mImageView = mImageView;
        this.mProgressBar = mProgressBar;
        this.mTextView = textView;


    }

    protected Boolean doInBackground(List<EventsInterestData>[] lists) {

        mEventsInterestDataList = lists[0];


        for (mIndex = 0; mIndex < mEventsInterestDataList.size(); mIndex++) {

            if(isCancelled()) {
                return null;
            }

            String interest = mEventsInterestDataList.get(mIndex).getInterest();

            int presentage = (int) (100 * ((double) mIndex / mEventsInterestDataList.size()));

            String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
            String url = "https://www.google.com/search?site=imghp&tbm=isch&source=lnt&q=" + interest + "&tbs=isz:m";
//            String url = "https://www.google.co.il/search?q=" + interest + "&safe=active&tbm=isch&source=lnt&tbs=isz:m";
            List<String> resultUrls = new ArrayList<>();

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


                updtaeView(googleImageBitmap, interest + System.getProperty("line.separator") + presentage + " %");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                googleImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] data = baos.toByteArray();
                //final String pic_id = UUID.randomUUID().toString() + ".jpg";

                final String pic_id = interest.replaceAll(" ", "_").toLowerCase() + ".jpg";

                StorageReference fileRef = FirebaseStorage.getInstance().getReference().child("interests_images").child(pic_id);
                fileRef.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                mFireBaseImages.add(taskSnapshot.getDownloadUrl().toString());
                                Log.d(TAG, "========File Uploaded =========");
                                publishProgress(mIndex);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d(TAG, exception.getMessage());

                            }
                        });
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            } catch (org.json.simple.parser.ParseException e) {
                Log.e(TAG, e.getMessage());
                return false;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }

        }

        while (mEventsInterestDataList.size() != mFireBaseImages.size()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        return true;
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        mTextView.setText("");
        mProgressBar.setProgress(0);
    }


    protected void onProgressUpdate(Integer... progress) {
        Log.d(TAG, "Progress: " + progress[0]);
        mProgressBar.setProgress(progress[0]);

    }


    private void updtaeView(final Bitmap image, final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(image);
                mTextView.setText(title);

            }
        });
    }

    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (result) {

            if (addFireBaseUrl()) {
                Toast.makeText(mContext, "Task completed uploading to firebase", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("interests_data").setValue(mEventsInterestDataList).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(mContext, "Finished uploading to firebase", Toast.LENGTH_SHORT).show();
                        mTextView.setText("Finished uploading to firebase");
                        Log.d("Finsish", "==========finish===============");

                    }
                });
            } else
                mTextView.setText("Sizes are incorrect");


        } else {
            mTextView.setText("Task failed");
            Toast.makeText(mContext, "Task failed, network issue", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean addFireBaseUrl() {
        if (mEventsInterestDataList.size() != mFireBaseImages.size()) return false;
        for (int i = 0; i < mEventsInterestDataList.size(); i++) {
            mEventsInterestDataList.get(i).setImage(mFireBaseImages.get(i));
        }
        return true;
    }
}

