package com.benezra.nir.poi.Fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Helper.RetrieveInterestsTask;
import com.benezra.nir.poi.Helper.RetrieveWikiPageTask;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Map;

public class BrowserFragment extends Fragment implements
        RetrieveInterestsTask.AsyncResponse,
View.OnClickListener{

    // private String TAG = BrowserFragment.class.getSimpleName();
    private String url;
    private WebView webView;
    private ProgressBar progressBar;
    private float m_downX;
    CoordinatorLayout coordinatorLayout;
    final static String TAG = BrowserFragment.class.getSimpleName();
    public static final String ACTION_SHOW_ANYWAYS = TAG + ".ACTION_SHOW_ANYWAYS";
    private AVLoadingIndicatorView mProgressBar;
    private MainActivity mActivity;
    private ImageButton upload,forward,back;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_browser, container, false);

        mProgressBar = rootView.findViewById(R.id.pb_loading);
        upload =  rootView.findViewById(R.id.btn_upload);
        forward =  rootView.findViewById(R.id.btn_forward);
        back =  rootView.findViewById(R.id.btn_back);


        upload.setOnClickListener(this);
        forward.setOnClickListener(this);
        back.setOnClickListener(this);


        url = "https://www.wikipedia.org";

        // if no url is passed, close the activity
        if (TextUtils.isEmpty(url)) {
            getActivity().finish();
        }

        webView = (WebView) rootView.findViewById(R.id.webView);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        coordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.main_content);

        initWebView();

        webView.loadUrl(url);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
    }


    private void initWebView() {
        webView.setWebChromeClient(new MyWebChromeClient(getContext()));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                invalidateButtons();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                invalidateButtons();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
                invalidateButtons();
            }
        });
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    //Multi touch detected
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        // save the x
                        m_downX = event.getX();
                    }
                    break;

                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        // set x so that it doesn't move
                        event.setLocation(m_downX, event.getY());
                    }
                    break;
                }

                return false;
            }
        });
    }


    public void invalidateButtons() {

        if (!webView.canGoBack()) {
            back.setEnabled(false);
            back.setAlpha(130);
        } else {
            back.setEnabled(true);
            back.setAlpha(255);
        }

        if (!webView.canGoForward()) {
            forward.setEnabled(false);
            forward.setAlpha(130);
        } else {
            forward.setEnabled(true);
            forward.setAlpha(255);
        }

    }



    // backward the browser navigation
    private void back() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    // forward the browser navigation
    private void forward() {
        if (webView.canGoForward()) {
            webView.goForward();
        }
    }

    @Override
    public void processFinish(boolean output, Map<String, Object> list) {
        if (output) {
            if (list != null && list.size() > 0)
                getAllInterests(list);
        } else
        {
            mActivity.showSnackBar("Nothing found");
            setProgress(false);

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_upload:
                setProgress(true);
                Utils.bookmarkUrl(getContext(), webView.getUrl());
                new RetrieveWikiPageTask(this).execute(webView.getUrl());
                break;
            case R.id.btn_forward:
                forward();
                break;
            case R.id.btn_back:
                back();
                break;
        }
        invalidateButtons();

    }

    private class MyWebChromeClient extends WebChromeClient {
        Context context;

        public MyWebChromeClient(Context context) {
            super();
            this.context = context;
        }
    }

    private void getAllInterests(final Map<String, Object> list) { //flag=false - first run flag=true - update database
        Map.Entry<String, Object> entry = list.entrySet().iterator().next();
        String key = entry.getKey();
        Query query = FirebaseDatabase.getInstance().getReference("interests_data").orderByKey().equalTo(key);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mActivity.showSnackBar("Interest already exist");
                    setProgress(false);

                } else {
                    FirebaseDatabase.getInstance().getReference("interests_data").updateChildren(list).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //Toast.makeText(BrowserFragment.this, "Finished uploading", Toast.LENGTH_SHORT).show();
                            mActivity.showSnackBar("Thanks for expanding our knowledge");
                            Log.d(TAG, "==========finish===============");
                            setProgress(false);



                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mActivity.showSnackBar("Something went wrong please try again later");
                setProgress(false);


            }
        });

    }

    private void setProgress(boolean state){
        if (state){
            //coordinatorLayout.setEnabled(false);
            mProgressBar.smoothToShow();
            //webView.setAlpha(0.6f);
        }
            else{
           // coordinatorLayout.setEnabled(true);
            mProgressBar.smoothToHide();
            //webView.setAlpha(1f);
        }

    }

}

