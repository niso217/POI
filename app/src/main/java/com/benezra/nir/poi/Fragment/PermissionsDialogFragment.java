package com.benezra.nir.poi.Fragment;

/**
 * Created by nir on 07/10/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.benezra.nir.poi.Helper.Constants;
import com.benezra.nir.poi.R;

import java.util.Arrays;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static com.benezra.nir.poi.Helper.Constants.LOCATION;

/**
 * Created by tylerjroach on 8/31/16.
 */

public class PermissionsDialogFragment extends DialogFragment {
    private final int PERMISSION_REQUEST_CODE = 11;

    private Context context;
    private PermissionsGrantedCallback listener;
    final static String TAG = PermissionsDialogFragment.class.getSimpleName();
    private boolean shouldResolve;
    private boolean shouldRetry;
    private boolean externalGrantNeeded;
    private String[] permissions;
    private boolean inProgress = false;
    private AlertDialog settings, retry;

    public static PermissionsDialogFragment newInstance() {
        return new PermissionsDialogFragment();
    }

    public PermissionsDialogFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof PermissionsGrantedCallback) {
            listener = (PermissionsGrantedCallback) context;
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("permissions", permissions);
        outState.putBoolean("inProgress", inProgress);
        outState.putBoolean("shouldResolve", shouldResolve);
        outState.putBoolean("externalGrantNeeded", externalGrantNeeded);
        outState.putBoolean("shouldRetry", shouldRetry);

    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            permissions = savedInstanceState.getStringArray("permissions");
            inProgress = savedInstanceState.getBoolean("inProgress");
            shouldResolve = savedInstanceState.getBoolean("shouldResolve");
            externalGrantNeeded = savedInstanceState.getBoolean("externalGrantNeeded");
            shouldRetry = savedInstanceState.getBoolean("shouldRetry");

        }
        setStyle(STYLE_NO_TITLE, R.style.PermissionsDialogFragmentStyle);
        if (!shouldResolve)
            requestNecessaryPermissions();
        setCancelable(false);


    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss");

    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldResolve) {
            if (externalGrantNeeded) {
                showAppSettingsDialog();
            } else if (shouldRetry) {
                showRetryDialog();
            } else {
                //permissions have been accepted
                if (listener != null && isPermissionGranted()) {
                    listener.navigateToCaptureFragment(permissions);
                    dismiss();
                }
            }
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
        listener = null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        shouldResolve = true;
        shouldRetry = false;

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int grantResult = grantResults[i];

            if (!shouldShowRequestPermissionRationale(permission) && grantResult != PackageManager.PERMISSION_GRANTED) {
                externalGrantNeeded = true;
                return;
            } else if (grantResult != PackageManager.PERMISSION_GRANTED) {
                shouldRetry = true;
                return;
            }
        }
    }

    private void requestNecessaryPermissions() {
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    private void showAppSettingsDialog() {
        settings = new AlertDialog.Builder(context)
                .setTitle("Permissions Required")
                .setMessage("In order to record videos, access to the camera, microphone, and storage is needed. Please enable these permissions from the app settings.")
                .setPositiveButton("App Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", context.getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        context.startActivity(intent);
                        dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                }).setCancelable(false).create();
        if (!settings.isShowing())
            settings.show();
    }

    private void showRetryDialog() {
        retry = new AlertDialog.Builder(context)
                .setTitle("Permissions Declined")
                .setMessage("In order to record videos, the app needs access to the camera, microphone, and storage.")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestNecessaryPermissions();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                }).setCancelable(false).create();
        if (!retry.isShowing())
            retry.show();
    }

    public interface PermissionsGrantedCallback {
        void navigateToCaptureFragment(String[] permissions);
    }

    private boolean isPermissionGranted() {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(context, permissions[i]) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }
}
