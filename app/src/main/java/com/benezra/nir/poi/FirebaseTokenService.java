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
        saveUserToFireBase();
    }


    public void saveUserToFireBase() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            String niftyToken = FirebaseInstanceId.getInstance().getToken().toString();

                            Log.d(TAG, ID_TOKEN + idToken);
                            Log.d(TAG, NOTIFY_TOKEN + niftyToken);

                            SharePref.getInstance(FirebaseTokenService.this).putString(ID_TOKEN, idToken);
                            SharePref.getInstance(FirebaseTokenService.this).putString(NOTIFY_TOKEN, niftyToken);

                            // Send token to your backend via HTTPS
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("notify_token").setValue(FirebaseInstanceId.getInstance().getToken().toString());
                            FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("user_token").setValue(task.getResult().getToken());

                            // ...
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });

    }

}
