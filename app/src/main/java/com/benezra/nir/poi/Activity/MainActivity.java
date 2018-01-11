package com.benezra.nir.poi.Activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benezra.nir.poi.Fragment.AboutFragment;
import com.benezra.nir.poi.Fragment.AlertDialogFragment;
import com.benezra.nir.poi.Fragment.BrowserFragment;
import com.benezra.nir.poi.Fragment.EventByInterestListFragment;
import com.benezra.nir.poi.Fragment.MainFragment;
import com.benezra.nir.poi.Fragment.PermissionsDialogFragment;
import com.benezra.nir.poi.Fragment.PreferenceFragment;
import com.benezra.nir.poi.Helper.SharePref;
import com.benezra.nir.poi.Objects.LocationHistory;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.Utils.DataFaker;
import com.benezra.nir.poi.Utils.DateUtil;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static com.benezra.nir.poi.Interface.Constants.ACTION_FINISH;
import static com.benezra.nir.poi.Interface.Constants.ACTION_REMOVE;
import static com.benezra.nir.poi.Interface.Constants.APP_BAR_SIZE;
import static com.benezra.nir.poi.Interface.Constants.CURRENT_FRAGMENT;
import static com.benezra.nir.poi.Interface.Constants.ID_TOKEN;
import static com.benezra.nir.poi.Interface.Constants.NOTIFY_TOKEN;
import static com.benezra.nir.poi.Interface.Constants.USER_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnSuccessListener<Location>,
        OnFailureListener,
        PermissionsDialogFragment.PermissionsGrantedCallback,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,AlertDialogFragment.DialogListenerCallback {

    private Toolbar mToolbar;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private DrawerLayout mDrawerLayout;
    private Location mUserLocation;
    private LocationChangedListener mOnLocationChangedListener;
    private FABClickedListener mFABClickedListener;
    protected SharedPreferences mSharedPreferences;
    private FirebaseUser mFirebaseUser;
    private NavigationView mNavigationView;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final int REQUEST_INVITE = 0;
    // delay to launch nav drawer item, to allow close animation to play
    static final int NAVDRAWER_LAUNCH_DELAY = 250;
    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    static final int MAIN_CONTENT_FADEIN_DURATION = 250;
    public static final int REQ_START_SHARE = 2;
    private FloatingActionButton mFloatingActionButton;
    private String mCurrentFragment;


    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //setTheme(R.style.transparent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);


        mAuth = FirebaseAuth.getInstance();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHandler = new Handler();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        overridePendingTransition(0, 0);

        if (mAuth != null) {
            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            saveUserToFireBase(mFirebaseUser);
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    saveUserToFireBase(user);

                }
            }
        };

        setUpDeepLinking();

        if (savedInstanceState == null) {
            askForLocation();
            //inflateFragment(new MainFragment(), false);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.d(TAG, "Count: " + getSupportFragmentManager().getBackStackEntryCount() + "");
                setFloatingActionImage();

            }
        });


        mToolbar = findViewById(R.id.toolbar);

        setAppBarHeight(APP_BAR_SIZE);

        mFloatingActionButton = findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(this);


    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


        if (getSupportActionBar() == null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        mDrawerLayout = findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null)
            goToNavigationItem(R.id.nav_main);
        View headerView = mNavigationView.inflateHeaderView(R.layout.nav_header_main);

        TextView name = headerView.findViewById(R.id.tv_name);
        ImageView image = headerView.findViewById(R.id.iv_profile);

        name.setText(getString(R.string.hello) + " " + mFirebaseUser.getDisplayName());
        if (mFirebaseUser.getPhotoUrl() != null)
            Picasso.with(this).load(mFirebaseUser.getPhotoUrl().toString()).into(image);
        else
            Picasso.with(this).load(R.drawable.profile).into(image);


        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUserLocation = savedInstanceState.getParcelable(USER_LOCATION);
        mCurrentFragment = savedInstanceState.getString(CURRENT_FRAGMENT);


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(USER_LOCATION, mUserLocation);
        outState.putString(CURRENT_FRAGMENT, mCurrentFragment);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setFloatingActionImage();
    }

    public Location getUserLocation() {
        return mUserLocation;
    }

    public void setLocationCallBack(LocationChangedListener listener) {
        if (listener instanceof LocationChangedListener) {
            mOnLocationChangedListener = listener;
        }
    }

    public void setFABCallBack(FABClickedListener listener) {
        if (listener instanceof FABClickedListener) {
            mFABClickedListener = listener;
        }
    }

    private void broadcastLocation(Location location) {
        if (mOnLocationChangedListener != null)
            mOnLocationChangedListener.onLocationChanged(location);
    }


    private int calculateDeviceHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public void setToolbarBackgroundColor() {

    }

    public void setToolbarBackground(String image) {

        ImageView background = findViewById(R.id.main_backdrop);
        final AVLoadingIndicatorView progress = findViewById(R.id.pb_loading);
        progress.setVisibility(View.VISIBLE);
        if (image != null) {
            Picasso.with(this)
                    .load(image)
                    .placeholder(R.color.colorPrimary)
                    .into(background, new Callback() {
                        @Override
                        public void onSuccess() {
                            progress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progress.setVisibility(View.GONE);

                        }
                    });
        } else {
            progress.setVisibility(View.GONE);
            Picasso.with(this)
                    .cancelRequest(background);
           // Picasso.with(this).load(R.drawable.nir3).into(background);
            background.setImageResource(R.drawable.nir8);
        }


    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (isMainVisible())
                BuildReturnDialogFragment();
            else {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();

                } else {
                    inflateFragment(new MainFragment(), false);
                    selectNavigationItem(R.id.nav_main);
                }
            }

        }
    }

    private boolean isMainVisible() {
        Fragment hm = getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        if (hm != null)
            if (hm.isVisible())
                return true;


        return false;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        final int itemId = item.getItemId();

        return goToNavigationItem(itemId);

    }

    protected boolean goToNavigationItem(final int itemId) {

        if (mNavigationView.getMenu().findItem(itemId).isChecked()) {
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


    public void callDrawerItem(int itemId) {
        Intent intent;
        switch (itemId) {

            case R.id.nav_main:
                inflateFragment(new MainFragment(), false);
                break;
            case R.id.nav_add_event:
                intent = new Intent(this, CreateEventActivity.class);
                intent.setAction(CreateEventActivity.ACTION_SHOW_ANYWAYS);
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
                inflateFragment(new AboutFragment(), false);

                break;
            case R.id.nav_share:
                //shareApp();
                /// intent = new Intent(this, HelpActivity.class);
                // createBackStack(intent);
                onInviteClicked();
                break;
            case R.id.nav_add_interest:
//                intent = new Intent(this, BrowserFragment.class);
//                intent.setAction(BrowserFragment.ACTION_SHOW_ANYWAYS);
//                createBackStack(intent);
                inflateFragment(new BrowserFragment(), false);
                break;
            case R.id.nav_settings:
//                intent = new Intent(this, SettingsActivity.class);
//                createBackStack(intent);
                inflateFragment(new PreferenceFragment(), false);
                break;
            case R.id.fake_data:
                intent = new Intent(this, DataFaker.class);
                intent.setAction(TutorialActivity.ACTION_SHOW_ANYWAYS);
                createBackStack(intent);
                //intent = new Intent(this, GeofencingActivity.class);
                //createBackStack(intent);
                //intent = new Intent(this, BlaBlaActivity.class);
                //createBackStack(intent);
                //sendExample();
                break;
            default:
                finish();
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
            builder.addParentStack(MainActivity.class);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    // set active navigation item
    private void selectNavigationItem(int itemId) {
        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            boolean b = itemId == mNavigationView.getMenu().getItem(i).getItemId();
            mNavigationView.getMenu().getItem(i).setChecked(b);
        }
    }

    public void inflateFragment(Fragment fragment, boolean addToBackStack) {
        mCurrentFragment = fragment.getClass().getSimpleName();
        if (addToBackStack)
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, mCurrentFragment).addToBackStack(mCurrentFragment).commitAllowingStateLoss();
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, mCurrentFragment).commitAllowingStateLoss();

        }
        setFloatingActionImage();
        setToolbarBackground(null);

    }


    public void setmCurrentFragment(String tag) {
        mCurrentFragment = tag;

    }

    public void setFloatingActionImage() {

        if (mCurrentFragment != null) {
            if (mCurrentFragment.equals(MainFragment.class.getSimpleName())) {
                setFloatingActionVisibility(true);
                mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_white_24dp));

            } else {
                setFloatingActionVisibility(false);
                mFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_map_white_36dp));
            }
        }


    }

    public void setFloatingActionVisibility(boolean visibility) {
        if (visibility)
            mFloatingActionButton.setVisibility(View.VISIBLE);
        else
            mFloatingActionButton.setVisibility(View.GONE);
    }

    @SuppressLint("MissingPermission")
    private void initFusedLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, this)
                .addOnFailureListener(this, this);
    }

    public void askForLocation() {
        navigateToCaptureFragment(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

    }

    @Override
    public void navigateToCaptureFragment(String[] permissions) {

        if (isPermissionGranted(permissions)) {
            if (Arrays.asList(permissions).contains(ACCESS_FINE_LOCATION))
                initFusedLocation();
        } else {
            PermissionsDialogFragment dialogFragment = (PermissionsDialogFragment) getSupportFragmentManager().findFragmentByTag(PermissionsDialogFragment.class.getName());
            if (dialogFragment == null) {
                Log.d(TAG, "opening dialog");
                PermissionsDialogFragment permissionsDialogFragment = PermissionsDialogFragment.newInstance();
                permissionsDialogFragment.setPermissions(permissions);
                permissionsDialogFragment.show(getSupportFragmentManager(), PermissionsDialogFragment.class.getName());

            }
        }
    }


    @Override
    public void UserIgnoredPermissionDialog() {
        showSnackBar(getString(R.string.no_location_determined));
        inflateFragment(new MainFragment(), false);

    }

    public boolean isPermissionGranted(String[] permissions) {
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    @Override
    public void onSuccess(Location location) {
        // Got last known location. In some rare situations this can be null.
        if (location != null) {
            broadcastLocation(location);
            mUserLocation = location;
            GeoHash geoHash = new GeoHash(new GeoLocation(location.getLatitude(), location.getLongitude()));
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                    .child("/g").setValue(geoHash.getGeoHashString());
            List<Double> loc = Arrays.asList(location.getLatitude(), location.getLongitude());
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                    .child("/l").setValue(loc);


            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid())
                    .child("location_history").push().setValue(
                    new LocationHistory(loc, DateUtil.getCurrentDateTimeInMilliseconds()));

        }


    }

    @Override
    public void onFailure(@NonNull Exception e) {
        Log.d(TAG, e.getMessage().toString());
    }

    public void showSnackBarWithAction(String message) {
        Snackbar snackbar = Snackbar
                .make(mDrawerLayout, message, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar snackbar1 = Snackbar.make(mDrawerLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });

        snackbar.show();


    }

    public void setAppBarHeight(int divider) {
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (divider == 0) lp.height = 0;
        else
            lp.height = dpToPx(calculateDeviceHeight() / divider);
    }

    public void setAppBarExpended() {
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.setExpanded(true, true);
    }


    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(mDrawerLayout, message, Snackbar.LENGTH_LONG);
        snackbar.show();

    }

    public void saveUserToFireBase(final FirebaseUser user) {
        user.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();

                            Log.d(TAG, ID_TOKEN + idToken);

                            SharePref.getInstance(MainActivity.this).putString(ID_TOKEN, idToken);

                            String notificationToken = SharePref.getInstance(MainActivity.this).getString(NOTIFY_TOKEN, "");

                            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
                            // Send token to your backend via HTTPS
                            mFirebaseInstance.getReference("users").child(user.getUid()).child("name").setValue(user.getDisplayName());
                            mFirebaseInstance.getReference("users").child(user.getUid()).child("email").setValue(user.getEmail());
                            if (user.getPhotoUrl() != null)
                                mFirebaseInstance.getReference("users").child(user.getUid()).child("avatar").setValue(user.getPhotoUrl().toString());
                            mFirebaseInstance.getReference("users").child(user.getUid()).child("notify_radius").setValue(SharePref.getInstance(MainActivity.this).getDefaultRadiusgetDefaultRadius());
                            mFirebaseInstance.getReference().child("users").child(user.getUid()).child("notify_token").setValue(notificationToken);
                            mFirebaseInstance.getReference().child("users").child(user.getUid()).child("user_token").setValue(idToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "done");
                                }
                            });


                        } else {
                            Log.d(TAG, "Fail update user " + task.getException());

                        }
                    }
                });

    }

    private void setUpDeepLinking() {
        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            Log.d(TAG, "getInvitation: no data");
                            return;
                        }

                        // Get the deep link
                        Uri deepLink = data.getLink();

                        // Extract invite
                        FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                        if (invite != null) {
                            String invitationId = invite.getInvitationId();
                        }

                        // Handle the deep link
                        // [START_EXCLUDE]
                        Log.d(TAG, "deepLink:" + deepLink);
                        if (deepLink != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage(getPackageName());
                            intent.setData(deepLink);

                            startActivity(intent);
                        }
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        showSnackBar(getString(R.string.google_play_services_error));
    }


    /**
     * User has clicked the 'Invite' button, launch the invitation UI with the proper
     * title, message, and deep link
     */
    // [START on_invite_clicked]
    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
               // .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(getUriToResource(R.mipmap.ic_launcher))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    // [START on_activity_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // [START_EXCLUDE]
                showSnackBar(getString(R.string.send_failed));

                // [END_EXCLUDE]
            }
        }
        inflateFragment(new MainFragment(),false);
    }


    public final Uri getUriToResource(@AnyRes int resId)
            throws Resources.NotFoundException {
        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(resId)
                + '/' + getResources().getResourceTypeName(resId)
                + '/' + getResources().getResourceEntryName(resId));
        return resUri;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (mCurrentFragment.equals(MainFragment.class.getSimpleName()))
                    callDrawerItem(R.id.nav_add_event);
                if (mCurrentFragment.equals(EventByInterestListFragment.class.getSimpleName()))
                    mFABClickedListener.onFABClicked();
                break;
        }
    }


    private void BuildReturnDialogFragment() {
        AlertDialogFragment alertDialog = (AlertDialogFragment) getSupportFragmentManager().findFragmentByTag(AlertDialogFragment.class.getName());
        if (alertDialog == null) {
            Log.d(TAG, "opening alert dialog");
            HashMap<Integer, String> map = new HashMap<>();
            map.put(BUTTON_POSITIVE, getString(R.string.sure));
            map.put(BUTTON_NEUTRAL, getString(R.string.return_to_event));
            alertDialog = AlertDialogFragment.newInstance(
                    getString(R.string.exit_app_title), getString(R.string.exit_app_body), map, ACTION_FINISH);
            alertDialog.show(getSupportFragmentManager(), AlertDialogFragment.class.getName());

        }
    }

    @Override
    public void onFinishDialog(int state, int action) {

        switch (state) {
            case BUTTON_POSITIVE:
                    finish();
                break;
        }
    }


    // [END on_activity_result]
    public interface LocationChangedListener {
        public void onLocationChanged(Location location);
    }

    public interface FABClickedListener {
        public void onFABClicked();
    }


}
