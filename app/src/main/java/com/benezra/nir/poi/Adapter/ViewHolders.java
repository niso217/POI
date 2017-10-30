package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.benezra.nir.poi.Activity.SpacePhotoActivity;
import com.benezra.nir.poi.Objects.EventPhotos;
import com.benezra.nir.poi.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewHolders {


    public static class PicturesViewHolder extends RecyclerView.ViewHolder  {

        public ImageView imgThumbnail;


        public PicturesViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
        }


    }

    public static class ParticipatesViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView image;

        public ParticipatesViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.tv_par_name);
            image = (ImageView) view.findViewById(R.id.iv_par_image);
        }
    }

    public static class PicturesActivityViewHolder extends RecyclerView.ViewHolder  {

        public ImageView mPhotoImageView;

        public PicturesActivityViewHolder(View itemView) {

            super(itemView);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.iv_photo);
        }

    }



}

