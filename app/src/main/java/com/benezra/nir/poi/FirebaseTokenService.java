package com.benezra.nir.poi;

/**
 * Created by nirb on 29/11/2017.
 */

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.benezra.nir.poi.Activity.SignInActivity;
import com.benezra.nir.poi.Activity.TutorialActivity;
import com.benezra.nir.poi.Helper.SharePref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import static com.benezra.nir.poi.Interface.Constants.ID_TOKEN;
import static com.benezra.nir.poi.Interface.Constants.NOTIFY_TOKEN;

public class FirebaseTokenService extends FirebaseInstanceIdService {
    private static final String TAG = FirebaseTokenService.class.getSimpleName();
    @Override public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh");
        String refreshedToken = FirebaseInstanceId.getInstance().getToken().toString();
        Log.d(TAG, NOTIFY_TOKEN + refreshedToken);
        SharePref.getInstance(FirebaseTokenService.this).putString(NOTIFY_TOKEN, refreshedToken);
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("notify_token").setValue(refreshedToken);

    }




}
