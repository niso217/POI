package com.benezra.nir.poi;

/**
 * Created by nirb on 29/11/2017.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.benezra.nir.poi.Activity.BaseActivity;
import com.benezra.nir.poi.Activity.MainActivity;
import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Interface.Constants;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Utils.NotificationUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.benezra.nir.poi.Interface.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ID;
import static com.benezra.nir.poi.Interface.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Interface.Constants.EVENT_START;
import static com.benezra.nir.poi.Interface.Constants.EVENT_TITLE;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "FPN";


    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                //JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(remoteMessage);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Constants.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(RemoteMessage remoteMessage) {
        Log.e(TAG, "push json: " + remoteMessage.toString());

        try {
            //JSONObject data = json.getJSONObject("data");

            final String title = remoteMessage.getData().get("title");
            final String body = remoteMessage.getData().get("body");
            final String id = remoteMessage.getData().get("id");
            Log.d(TAG, "Title: " + title);
            Log.d(TAG, "Body: " + body);
            Log.d(TAG, "id: " + id);


            Query query = FirebaseDatabase.getInstance().getReference("events").child(id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        Event event = dataSnapshot.getValue(Event.class);

//                        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
//                            // app is in foreground, broadcast the push message
//                            Intent pushNotification = new Intent(Constants.PUSH_NOTIFICATION);
//                            pushNotification.putExtra("message", body);
//                            LocalBroadcastManager.getInstance(MessagingService.this).sendBroadcast(pushNotification);
//
//                            // play notification sound
//                            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
//                            notificationUtils.playNotificationSound();
//                        } else {
                            // app is in background, show the notification in notification tray
                            Intent resultIntent = new Intent(getApplicationContext(), ViewEventActivity.class);
                            //resultIntent.putExtra("message", body);
                            resultIntent.putExtra(EVENT_ID, event.getId());
                            resultIntent.putExtra(EVENT_TITLE, event.getTitle());
                            resultIntent.putExtra(EVENT_OWNER, event.getOwner());
                            resultIntent.putExtra(EVENT_IMAGE, event.getImage());
                            resultIntent.putExtra(EVENT_DETAILS, event.getDetails());
                            resultIntent.putExtra(EVENT_LATITUDE, event.getLatitude());
                            resultIntent.putExtra(EVENT_LONGITUDE, event.getLongitude());
                            resultIntent.putExtra(EVENT_INTEREST, event.getInterest());
                            resultIntent.putExtra(EVENT_START, event.getStart());
                            resultIntent.putExtra(EVENT_ADDRESS, event.getAddress());
                            showNotificationMessage(getApplicationContext(), title, body, "", resultIntent);

                       // }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
         catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}