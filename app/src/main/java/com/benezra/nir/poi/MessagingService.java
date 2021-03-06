package com.benezra.nir.poi;

/**
 * Created by nirb on 29/11/2017.
 */

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.benezra.nir.poi.Activity.ViewEventActivity;
import com.benezra.nir.poi.Geofencing.GeofencingActivity;
import com.benezra.nir.poi.Helper.SharePref;
import com.benezra.nir.poi.Interface.Constants;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Utils.NotificationUtil;
import com.benezra.nir.poi.Utils.NotificationUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.benezra.nir.poi.Interface.Constants.EVENT_ADDRESS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_DETAILS;
import static com.benezra.nir.poi.Interface.Constants.EVENT_END;
import static com.benezra.nir.poi.Interface.Constants.EVENT_ID;
import static com.benezra.nir.poi.Interface.Constants.EVENT_IMAGE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_INTEREST;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LATITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_LONGITUDE;
import static com.benezra.nir.poi.Interface.Constants.EVENT_OWNER;
import static com.benezra.nir.poi.Interface.Constants.EVENT_START;
import static com.benezra.nir.poi.Interface.Constants.EVENT_TITLE;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = MessagingService.class.getSimpleName();


    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        Log.e(TAG, "From: " + remoteMessage.getFrom());


        if (remoteMessage == null || !SharePref.getInstance(this).isNotificationOn())
            return;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            try {
                handleDataMessage(remoteMessage);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
        else
            Log.e(TAG, "remoteMessage.getData() is empty");

    }


    private void handleDataMessage(RemoteMessage remoteMessage) {
        try {
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
                        resultIntent.putExtra(EVENT_END, event.getEnd());
                        resultIntent.putExtra(EVENT_ADDRESS, event.getAddress());
                        showNotificationMessage(getApplicationContext(), title, body, "", resultIntent);

                        // }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotification(title,message,context,intent);
        } else {
            notificationUtils = new NotificationUtils(context);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
        }

    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void sendNotification(String title, String message, Context context, Intent notificationIntent) {
        // Create an explicit content Intent that starts the main Activity.

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(GeofencingActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        //Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationUtil mNotificationUtils = new NotificationUtil(this);

        Notification.Builder nb = mNotificationUtils.
                getAndroidChannelNotification(title, message, notificationPendingIntent, context);


        mNotificationManager.notify(0, nb.build());


    }

}