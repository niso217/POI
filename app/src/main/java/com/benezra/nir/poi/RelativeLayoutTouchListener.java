package com.benezra.nir.poi;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by nirb on 22/10/2017.
 */

public class RelativeLayoutTouchListener implements View.OnTouchListener {

    static final String logTag = "ActivitySwipeDetector";
    private Context context;
    static final int MIN_DISTANCE = 10;// TODO change this runtime based on screen resolution. for 1920x1080 is to small the 100 distance
    private float downX, downY, upX, upY;
    private LayoutTouchListenerCallback listener;

    // private MainActivity mMainActivity;

    public interface LayoutTouchListenerCallback{
        void onSwipe();
    }

    public RelativeLayoutTouchListener(Context context) {
        this.context = context;
        if (context instanceof LayoutTouchListenerCallback) {
            listener = (LayoutTouchListenerCallback) context;
        }
    }

    public void onRightToLeftSwipe() {
        Log.i(logTag, "RightToLeftSwipe!");
        listener.onSwipe();
    }

    public void onLeftToRightSwipe() {
        Log.i(logTag, "LeftToRightSwipe!");
        listener.onSwipe();
    }

    public void onTopToBottomSwipe() {
        Log.i(logTag, "onTopToBottomSwipe!");
        listener.onSwipe();
    }

    public void onBottomToTopSwipe() {
        Log.i(logTag, "onBottomToTopSwipe!");
        listener.onSwipe();
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(logTag, "ACTION_DOWN!");
                //TOUCH STARTED
                return true;
            case MotionEvent.ACTION_MOVE:
                listener.onSwipe();
                Log.i(logTag, "ACTION_MOVE!");
                //FINGER IS MOVING
                //Do your calculations here, using the x and y positions relative to the starting values you get in ACTION_DOWN
                return true;
            case MotionEvent.ACTION_CANCEL:
                Log.i(logTag, "ACTION_CANCEL!");

            case MotionEvent.ACTION_UP:
                Log.i(logTag, "ACTION_UP");

                //TOUCH COMPLETED
                return true;
        }
        return false;
    }

}
