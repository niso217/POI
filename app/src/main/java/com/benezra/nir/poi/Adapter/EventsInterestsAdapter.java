package com.benezra.nir.poi.Adapter;

/**
 * Created by nirb on 15/11/2017.
 */


import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.benezra.nir.poi.Objects.EventsInterestData;
import com.benezra.nir.poi.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventsInterestsAdapter extends RecyclerView.Adapter<EventsInterestsAdapter.MyCustomViewHolder> {

    private List<EventsInterestData> listInterestData, filterList;
    private Context mContext;

    public EventsInterestsAdapter(Context context, List<EventsInterestData> listItems) {
        this.listInterestData = listItems;
        this.mContext = context;
        this.filterList = new ArrayList<EventsInterestData>();
        // we copy the original list to the filter list and use it for setting row values
        this.filterList.addAll(this.listInterestData);
    }

    public List<EventsInterestData> getFilterList() {
        return filterList;
    }

    @Override
    public MyCustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.participate_list_row, null);
        MyCustomViewHolder viewHolder = new MyCustomViewHolder(view);
        return viewHolder;

    }

    public class MyCustomViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView Image;
        protected TextView tvName;

        public MyCustomViewHolder(View view) {
            super(view);
            this.Image = (CircleImageView) view.findViewById(R.id.iv_par_image);
            this.tvName = (TextView) view.findViewById(R.id.tv_par_name);
        }

    }

    @Override
    public void onBindViewHolder(MyCustomViewHolder customViewHolder, int position) {

        EventsInterestData listItem = filterList.get(position);
        if (!listItem.getImage().equals(""))
        Picasso.with(mContext).load(listItem.getImage()).resize(100,100).into(customViewHolder.Image);
        else
            Picasso.with(mContext).load(R.drawable.cooking_back).resize(100,100).into(customViewHolder.Image);

        customViewHolder.tvName.setText(listItem.getInterest());

    }

    @Override
    public int getItemCount() {
        return (null != filterList ? filterList.size() : 0);
    }

    // Do Search...
    public void filter(final String text) {

        // Searching could be complex..so we will dispatch it to a different thread...
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Clear the filter list
                filterList.clear();

                // If there is no search value, then add all original list items to filter list
                if (TextUtils.isEmpty(text)) {

                    filterList.addAll(listInterestData);

                } else {
                    // Iterate in the original List and add it to filter list...
                    for (EventsInterestData item : listInterestData) {
                        if (item.getInterest().toLowerCase().contains(text.toLowerCase()) ||
                                item.getCategories().contains(text.toLowerCase())) {
                            // Adding Matched items
                            filterList.add(item);
                        }
                    }
                }

                // Set on UI Thread
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Notify the List that the DataSet has changed...
                        notifyDataSetChanged();
                    }
                });

            }
        }).start();

    }

}
