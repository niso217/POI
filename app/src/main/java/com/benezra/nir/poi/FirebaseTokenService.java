package com.benezra.nir.poi;

/**
 * Created by nirb on 29/11/2017.
 */

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
public class FirebaseTokenService extends FirebaseInstanceIdService {
    private static final String TAG = "FPN";
    @Override public void onTokenRefresh() {
        String deviceId = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "DeviceId: " + deviceId);
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/TopicName");
    }
}
