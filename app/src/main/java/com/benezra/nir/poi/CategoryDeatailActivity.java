package com.benezra.nir.poi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;


public class CategoryDeatailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_deatail);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseInstance = FirebaseDatabase.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Using the ToolBar as ActionBar
        //Find the toolbar view inside the activity layout
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Sets the Toolbar to act as the ActionBar for this Activity window.
        //Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Getting the intent with the category title, image resource ID and first paragraph text for this category
        Intent categoryDetail = getIntent();
        String title = categoryDetail.getStringExtra("categoryTitle");
        //Bitmap image = categoryDetail.getParcelableExtra("imageResourceId");
        String firstParagraph = categoryDetail.getStringExtra("firstParagraphText");
        final String eventId = categoryDetail.getStringExtra("eventId");
        final double longitude = categoryDetail.getDoubleExtra("longitude", 0);
        final double latitude = categoryDetail.getDoubleExtra("latitude", 0);

        Query query = mFirebaseInstance.getReference("events").child(eventId).child("image");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String image = snapshot.getValue(String.class);
                if (image!=null){
                    ImageView imageView = (ImageView) findViewById(R.id.backdrop);
                    imageView.setImageBitmap(encodeBitmapAndSaveToFirebase(image));

                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("The read failed: ", firebaseError.getMessage());
            }
        });

        //Setting the category title onto collapsing toolbar
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("","");
            }
        });
        collapsingToolbar.setTitle(title);

        //Setting the styles to expanded and collapsed toolbar
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        Button ButtonJoin = (Button) findViewById(R.id.btn_join);
        ButtonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("events").child(eventId).child("participates").child(mFirebaseUser.getUid()).setValue(true);

            }
        });

        Button ButtonRemove = (Button) findViewById(R.id.btn_remove);
        ButtonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("events").child(eventId).child("participates").child(mFirebaseUser.getUid()).removeValue();

            }
        });

        //Setting the category image onto collapsing toolbar
        //ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        //imageView.setImageBitmap(image);
//                VolleyHelper.getInstance(this).getImageLoader().get(image, ImageLoader.getImageListener(imageView,
//                R.mipmap.ic_launcher, android.R.drawable
//                        .ic_dialog_alert));

        //Setting the paragraph text onto TextView
        TextView textView = (TextView) findViewById(R.id.first_paragraph);
        textView.setText(firstParagraph);

        // Find the Navigate Now button
        TextView navigateNow = (TextView) findViewById(R.id.navigate_now);

        // Set a click listener on the button
        navigateNow.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Navigate Now button is clicked on.
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        Log.v("my_tag", "category passed title is " + title);
        //Log.v("my_tag", "category passed image is " + image);
        Log.v("my_tag", "category passed paragraph text is " + firstParagraph);


        // Finding the facebook share button
        final ShareButton shareButton = (ShareButton) findViewById(R.id.fb_share_button);

        // Sharing the content to facebook
        ShareLinkContent content = new ShareLinkContent.Builder()
                // Setting the title that will be shared
                .setContentTitle("Planning a trip to Dubai?")
                // Setting the description that will be shared
                .setContentDescription("Make sure you visit unique attractions recommended by the local people!")
                // Setting the URL that will be shared
                .setContentUrl(Uri.parse("https://justa128.github.io/dubai-tour-guide/landingpage/"))
                // Setting the image that will be shared
                .setImageUrl(Uri.parse("https://cdn-images-1.medium.com/fit/t/800/240/1*jZ3a6rYqrslI83KJFhdvFg.jpeg"))
                .build();
        shareButton.setShareContent(content);

        // Finding the custom facebook share button
        TextView shareFB = (TextView) findViewById(R.id.fb_share_button_custom);

        // Set a click listener on the custom facebook share button
        shareFB.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Share on Facebook custom button is clicked on
            @Override
            public void onClick(View view) {
                shareButton.performClick();

            }
        });

    }

    public Bitmap encodeBitmapAndSaveToFirebase(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Getting the intent with the attraction latitude and longitude
        Intent categoryDetail = getIntent();
        double longitude = categoryDetail.getDoubleExtra("longitude", 0);
        double latitude = categoryDetail.getDoubleExtra("latitude", 0);
        String locationTitle = categoryDetail.getStringExtra("locationTitle");

        Log.v("my_tag", "category passed longitude is " + longitude);
        Log.v("my_tag", "category passed latitude is " + latitude);
        Log.v("my_tag", "location map title is " + locationTitle);

        // Add a marker in the respective location and move the camera and set the zoom level to 15
        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(locationTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));

    }
}
