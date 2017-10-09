package com.benezra.nir.poi;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by nir on 09/10/2017.
 */

public class BaseActivity extends AppCompatActivity implements UserEventFragment.UserEventFragmentCallback {

    final static String TAG = AlertDialogFragment.class.getSimpleName();

    public void showDialog() {
        AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.class.getName());
        if (alertDialogFragment == null) {
            Log.d(TAG, "opening dialog");
            alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.loading_data_title), "", null);
            alertDialogFragment.show(getSupportFragmentManager(), AlertDialogFragment.class.getName());
        }
    }
    public void hideDialog() {
        AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.class.getName());
        if (alertDialogFragment != null) {
            Log.d(TAG, "closing dialog");
            alertDialogFragment.dismiss();
        }
    }


}
