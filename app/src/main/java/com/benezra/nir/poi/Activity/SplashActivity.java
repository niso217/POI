

package com.benezra.nir.poi.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.benezra.nir.poi.Helper.SharePref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.benezra.nir.poi.Interface.Constants.ID_TOKEN;
import static com.benezra.nir.poi.Interface.Constants.NOTIFY_TOKEN;



public class SplashActivity extends Activity implements OnCompleteListener<GetTokenResult> {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseInstance;
    private static final String TAG = SignInActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance();

        if (mAuth.getCurrentUser() != null)
        {
            mAuth.getCurrentUser().getToken(true).addOnCompleteListener(this);
        }

        else{
            Intent mainIntent = new Intent(SplashActivity.this, SignInActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }



    }


    @Override
    public void onComplete(@NonNull Task<GetTokenResult> task) {

        if (task.isSuccessful()) {
            String idToken = task.getResult().getToken();
            String niftyToken = FirebaseInstanceId.getInstance().getToken().toString();

            Log.d(TAG, ID_TOKEN + idToken);
            Log.d(TAG, NOTIFY_TOKEN + niftyToken);

            SharePref.getInstance(SplashActivity.this).putString(ID_TOKEN, idToken);
            SharePref.getInstance(SplashActivity.this).putString(NOTIFY_TOKEN, niftyToken);

            // Send token to your backend via HTTPS
            mFirebaseInstance.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("notify_token").setValue(FirebaseInstanceId.getInstance().getToken().toString());
            mFirebaseInstance.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("user_token").setValue(task.getResult().getToken());

            Intent intent = new Intent(SplashActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();

            // ...
        } else {
            // Handle error -> task.getException();
        }
    }
}
