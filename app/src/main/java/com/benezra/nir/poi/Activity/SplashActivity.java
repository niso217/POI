

package com.benezra.nir.poi.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.benezra.nir.poi.Helper.SharePref;
import com.benezra.nir.poi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import static com.benezra.nir.poi.Interface.Constants.ID_TOKEN;
import static com.benezra.nir.poi.Interface.Constants.NOTIFY_TOKEN;


public class SplashActivity extends Activity {

    private FirebaseAuth mAuth;
    private static final String TAG = SignInActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }

        //Get Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();

        if (!isNetworkAvailable())
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int choice) {
                    switch (choice) {
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            break;

                    }
                }
            };


            AlertDialog.Builder builder = new AlertDialog.Builder(
                    this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

            builder.setMessage("No internet connection")
                    .setPositiveButton("LEAVE", dialogClickListener).setCancelable(false);
            builder.show();

            return;
        }

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent mainIntent = new Intent(SplashActivity.this, SignInActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }


    }

    public  boolean isNetworkAvailable() {
        int[] networkTypes = {ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI};
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null &&
                        activeNetworkInfo.getType() == networkType)
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}
