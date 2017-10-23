package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.benezra.nir.poi.R;

import java.util.ArrayList;

public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.ViewHolder> {

    ArrayList<Integer> alImage;
    Context context;

    public PicturesAdapter(Context context,ArrayList<Integer> alImage) {
        super();
        this.context = context;
        this.alImage = alImage;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item_event_pic, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.imgThumbnail.setImageResource(alImage.get(i));

    }

    @Override
    public int getItemCount() {
        return alImage.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder  {

        public ImageView imgThumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
        }


    }

}

