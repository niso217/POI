package com.benezra.nir.poi;

/**
 * Created by nirb on 29/11/2017.
 */


import android.util.Log;

import com.benezra.nir.poi.Helper.SharePref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import static com.benezra.nir.poi.Interface.Constants.NOTIFY_TOKEN;

public class FirebaseTokenService extends FirebaseInstanceIdService {
    private static final String TAG = FirebaseTokenService.class.getSimpleName();
    @Override public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken().toString();
        Log.d(TAG, NOTIFY_TOKEN + refreshedToken);
        SharePref.getInstance(FirebaseTokenService.this).putString(NOTIFY_TOKEN, refreshedToken);
        if (FirebaseAuth.getInstance()!=null && FirebaseAuth.getInstance().getCurrentUser()!=null)
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("notify_token").setValue(refreshedToken);

    }




}
