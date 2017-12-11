package com.benezra.nir.poi.Utils;

/**
 * Created by nirb on 27/11/2017.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.benezra.nir.poi.Helper.SharePref;
import com.benezra.nir.poi.R;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NotificationUtil extends ContextWrapper {

    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "com.benezra.nir.poi.Utils.ANDROID";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
    private NotificationChannel androidChannel;

    public NotificationUtil(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {

        // create android channel
         androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true);
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(true);
        // Sets the notification light color for notifications posted to this channel
        androidChannel.setLightColor(Color.GREEN);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(androidChannel);

    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Notification.Builder getAndroidChannelNotification(String title, String body, PendingIntent notificationPendingIntent,Context context) {

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.addLine(body);

        final Uri alarmSound = Uri.parse(SharePref.getInstance(context).getNotificationSound());



        return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(alarmSound)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
    }


}