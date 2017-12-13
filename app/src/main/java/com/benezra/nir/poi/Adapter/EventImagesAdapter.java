package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.benezra.nir.poi.R;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

public class EventImagesAdapter extends RecyclerView.Adapter<EventImagesAdapter.EventsViewHolder>  {

    private List<String> eventsImagesList;
    private Context context;


    public class EventsViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public AVLoadingIndicatorView mProgressCircle;

        public EventsViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.event_image);
            mProgressCircle = view.findViewById(R.id.pb_loading);

        }
    }


    public EventImagesAdapter(Context context, List<String> events) {
        this.context = context;
        this.eventsImagesList = events;

    }


    public void setItems(List<String> events) {
        this.eventsImagesList = events;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_image_item, parent, false);



        return new EventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EventsViewHolder holder, int position) {
        String image = eventsImagesList.get(position);

        Picasso.with(context)
                .load(image)
                .into(holder.image, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        holder.mProgressCircle.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        holder.mProgressCircle.setVisibility(View.GONE);

                    }
                });

    }


    @Override
    public int getItemCount() {
        return eventsImagesList.size();
    }
}
