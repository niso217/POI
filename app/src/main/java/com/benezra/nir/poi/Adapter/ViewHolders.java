package com.benezra.nir.poi.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benezra.nir.poi.R;

public class ViewHolders {


    public static class PicturesViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgThumbnail;


        public PicturesViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.event_image);
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

    public static class PicturesActivityViewHolder extends RecyclerView.ViewHolder {

        public ImageView mPhotoImageView;

        public PicturesActivityViewHolder(View itemView) {

            super(itemView);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.iv_photo);
        }

    }


    public static class EventDetailsViewHolder extends RecyclerView.ViewHolder {
        public TextView title, start, distance;

        public EventDetailsViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.tv_title);
            start = (TextView) view.findViewById(R.id.tv_start);
            distance = (TextView) view.findViewById(R.id.tv_distance);
        }
    }


}

