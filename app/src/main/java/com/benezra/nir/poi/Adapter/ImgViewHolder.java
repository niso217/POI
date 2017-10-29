package com.benezra.nir.poi.Adapter;

/**
 * Created by nirb on 29/10/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.benezra.nir.poi.R;


public class ImgViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;

    public ImgViewHolder(View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.img_view);
    }
}
