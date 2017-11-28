/*
 This file is part of Privacy Friendly App Example.

 Privacy Friendly App Example is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */

package com.benezra.nir.poi.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Fragment.ProgressDialogFragment;
import com.benezra.nir.poi.Geofencing.GeofencingActivity;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.benezra.nir.poi.Interface.Constants;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Settings.AboutActivity;
import com.benezra.nir.poi.Settings.SettingsActivity;
import com.benezra.nir.poi.Utils.NotificationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.google.android.gms.plus.PlusShare;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.benezra.nir.poi.Fragment.MapFragment.EVENT_LOC_TAB;


/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171017
 *          This class is a parent class of all activities that can be accessed from the
 *          Navigation Drawer (example see GeofencingActivity.java)
 */
public abstract class BaseActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener,
        PermissionsDialogFragment.PermissionsGrantedCallback ,
        Response.Listener,
        Response.ErrorListener{

    // delay to launch nav drawer item, to allow close animation to play
    static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    public static final int REQ_START_SHARE = 2;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    // Navigation drawer:
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private static final String TAG = BaseActivity.class.getSimpleName();
    private FirebaseUser mFirebaseUser;

    // Helper
    private Handler mHandler;
    protected SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHandler = new Handler();


        overridePendingTransition(0, 0);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Constants.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Constants.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Constants.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);

        //if (!TextUtils.isEmpty(regId))
        //txtRegId.setText("Firebase Reg Id: " + regId);
        // else
        //txtRegId.setText("Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    protected abstract int getNavigationDrawerID();

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final int itemId = item.getItemId();

        return goToNavigationItem(itemId);
    }

    protected boolean goToNavigationItem(final int itemId) {

        if (itemId == getNavigationDrawerID()) {
            // just close drawer because we are already in this activity
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // delay transition so the drawer can close
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);

        mDrawerLayout.closeDrawer(GravityCompat.START);

        selectNavigationItem(itemId);

        // fade out the active activity
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }
        return true;
    }

    // set active navigation item
    private void selectNavigationItem(int itemId) {
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            boolean b = itemId == mNavigationView.getMenu().getItem(i).getItemId();
            mNavigationView.getMenu().getItem(i).setChecked(b);
        }
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * {@code AndroidManifest.xml} to find out the parent activity names for each activity.
     *
     * @param intent
     */
    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    /**
     * This method manages the behaviour of the navigation drawer
     * Add your menu items (ids) to res/menu/activity_main_drawer.xml
     *
     * @param itemId Item that has been clicked by the user
     */
    private void callDrawerItem(final int itemId) {

        Intent intent;

        switch (itemId) {
            case R.id.nav_add_event:
                intent = new Intent(this, CreateEventActivity.class);
                intent.setAction(TutorialActivity.ACTION_SHOW_ANYWAYS);
                createBackStack(intent);

                break;
            case R.id.nav_log_out:
                intent = new Intent(this, SignInActivity.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(intent);
                finish();
                break;
            case R.id.nav_help:
                intent = new Intent(this, TutorialActivity.class);
                intent.setAction(TutorialActivity.ACTION_SHOW_ANYWAYS);
                createBackStack(intent);
                break;
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                createBackStack(intent);
                break;
            case R.id.nav_share:
                shareApp();
                /// intent = new Intent(this, HelpActivity.class);
                // createBackStack(intent);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                createBackStack(intent);
                break;
            case R.id.fake_data:
                //intent = new Intent(this, GeofencingActivity.class);
                //createBackStack(intent);
                //intent = new Intent(this, BlaBlaActivity.class);
                //createBackStack(intent);
                sendExample();
                break;
            default:
                finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        View headerView = mNavigationView.inflateHeaderView(R.layout.nav_header_main);

        TextView name = (TextView) headerView.findViewById(R.id.tv_name);
        ImageView image = (ImageView) headerView.findViewById(R.id.iv_profile);

        name.setText(getString(R.string.hello) + " " + mFirebaseUser.getDisplayName());
        Picasso.with(this).load(mFirebaseUser.getPhotoUrl().toString()).into(image);

        selectNavigationItem(getNavigationDrawerID());

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    public void showProgress(String title, String message) {
        ProgressDialogFragment mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment == null) {
            Log.d(TAG, "opening origress dialog");
            mProgressDialogFragment = ProgressDialogFragment.newInstance(
                    title, message, ProgressDialog.STYLE_SPINNER);
            mProgressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getName());
        }
    }

    public void hideProgressMessage() {
        ProgressDialogFragment mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment != null)
            mProgressDialogFragment.dismissAllowingStateLoss();

    }


    /**
     * @param subject - the subject of the post
     * @param body    - the body of the post
     * @param url     - link inside the post
     */
    public void PostOnGoogle(String subject, String body, String url) {
        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText(subject + "" + body)
                .setContentUrl(Uri.parse(url))
                .getIntent();
        startActivityForResult(shareIntent, 0);
    }


    public void GooglePostPhoto(Intent intent) {
        try {
            Uri selectedImage = intent.getData();
            ContentResolver cr = getContentResolver();
            String mime = cr.getType(selectedImage);

            PlusShare.Builder share = new PlusShare.Builder(this);
            share.setText(getResources().getString(R.string.app_name));
            share.addStream(selectedImage);
            share.setType(mime);
            startActivityForResult(share.getIntent(), REQ_START_SHARE);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }


    }

    private void shareApp() {
        String textToShare = "Visit <a href=\"http://www.google.com\">google</a> for more info.";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/html");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(textToShare));
        startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
    }

    final String URL = "https://poi-project.herokuapp.com";
    // Post params to be sent to the server
    HashMap<String, String> params = new HashMap<String, String>();


    private void sendExample() {



        //VolleyHelper.getInstance(this).post(URL,object,BaseActivity.this,BaseActivity.this);

        StringRequest postRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                params.put("regId","cVzkd8aod30:APA91bGlqs_XnlPmI5SOxSRY5PAQozSXb0ANR5eNN5eNLajODBPMrbAk5MW4k3q8WootIlqWj2YPshwXJ4xvuJxhDNckCTd4zFVi-vVaOYJ4e2_QnuRsYTIDcJ-_UI38VBElDWP_C7YN");
                params.put("title","login_id_value");
                params.put("message","username");
                params.put("push_type","individual");

                return params;
            }
        };
        VolleyHelper.getInstance(this).addToRequestQueue(postRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }


    @Override
    public void onResponse(Object response) {
        Log.d(TAG,response.toString());
    }



}
