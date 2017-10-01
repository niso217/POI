package com.benezra.nir.poi.Helper;

/**
 * Created by nirb on 19/09/2017.
 */


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


public class VolleyHelper {
    private static VolleyHelper mInstance;
    private RequestQueue mRequestQueue;
    private static Context mContext;
    private final ImageLoader mImageLoader;

    private VolleyHelper(Context context){
        // Specify the application context
        mContext = context;
        // Get the request queue
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache());

    }

    public static synchronized VolleyHelper getInstance(Context context){
        // If Instance is null then initialize new Instance
        if(mInstance == null){
            mInstance = new VolleyHelper(context);
        }
        // Return VolleyHelper new Instance
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        // If RequestQueue is null the initialize new RequestQueue
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        // Return RequestQueue
        return mRequestQueue;
    }

    public<T> void addToRequestQueue(Request<T> request){
        // Add the specified request to the request queue
        getRequestQueue().add(request);
    }


    public ImageLoader getImageLoader(){
        return mImageLoader;
    }

    public void get(String method, JSONObject jsonRequest,
                    Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){

        JsonObjectRequest objRequest = new JsonObjectRequest(Request.Method.GET, method, jsonRequest, listener, errorListener);
        addToRequestQueue(objRequest);
    }

    public void put(String method, JSONObject jsonRequest,
                    Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){

        JsonObjectRequest objRequest = new JsonObjectRequest(Request.Method.PUT, method, jsonRequest, listener, errorListener);
        addToRequestQueue(objRequest);
    }

    public void post(String method, JSONObject jsonRequest,
                     Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){

        JsonObjectRequest objRequest = new JsonObjectRequest(Request.Method.POST, method, jsonRequest, listener, errorListener);
        addToRequestQueue(objRequest);
    }

    public void delete(String method, JSONObject jsonRequest,
                       Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){

        JsonObjectRequest objRequest = new JsonObjectRequest(Request.Method.DELETE, method, jsonRequest, listener, errorListener);
        addToRequestQueue(objRequest);
    }
}