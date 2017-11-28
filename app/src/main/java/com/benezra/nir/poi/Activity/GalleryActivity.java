package com.benezra.nir.poi.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.benezra.nir.poi.Adapter.ViewHolders;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import static com.benezra.nir.poi.Interface.Constants.ID;

/**
 * Created by Chike on 2/12/2017.
 */

public class GalleryActivity extends AppCompatActivity implements RecyclerTouchListener.ClickListener {

    private FirebaseDatabase mFirebaseInstance;
    private List<String> mImageSet;
    private RecyclerView mPicturesRecyclerView;
    private FirebaseRecyclerAdapter<EventPhotos, ViewHolders.PicturesActivityViewHolder> mPicturesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mImageSet = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        mPicturesRecyclerView = (RecyclerView) findViewById(R.id.rv_images);
        mPicturesRecyclerView.setLayoutManager(layoutManager);
        mPicturesRecyclerView.setNestedScrollingEnabled(false);
        mPicturesRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mPicturesRecyclerView, this));

        Intent intent = getIntent();
        String eventId = intent.getStringExtra(ID);
        addImagesChangeListener(eventId);

    }


    private void addImagesChangeListener(String eventId) {
        Query query = mFirebaseInstance.getReference("events").child(eventId).child("pictures");
        mImageSet.clear();
        mPicturesAdapter = new FirebaseRecyclerAdapter<EventPhotos, ViewHolders.PicturesActivityViewHolder>(
                EventPhotos.class, R.layout.gallery_item_photo, ViewHolders.PicturesActivityViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewHolders.PicturesActivityViewHolder picturesViewHolder, EventPhotos model, int position) {
                Glide.with(GalleryActivity.this)
                        .load(model.getUrl())
                        .placeholder(R.drawable.ic_cloud_off_red)
                        .into(picturesViewHolder.mPhotoImageView);

                mImageSet.add(model.getUrl());
            }

        };

        mPicturesRecyclerView.setAdapter(mPicturesAdapter);

    }

    @Override
    public void onClick(View view, int position) {
        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra(PhotoActivity.EXTRA_SPACE_PHOTO, mImageSet.get(position));
        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }

}
