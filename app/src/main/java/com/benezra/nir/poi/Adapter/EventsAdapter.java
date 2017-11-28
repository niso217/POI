package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.benezra.nir.poi.Utils.DateUtil;
import com.benezra.nir.poi.Objects.Event;
import com.benezra.nir.poi.Interface.ItemTouchHelperAdapter;
import com.benezra.nir.poi.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> implements ItemTouchHelperAdapter {

    private List<Event> eventsList;
    private Context context;

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(eventsList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        //eventsList.remove(position);
        //notifyItemRemoved(position);
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder {
        public TextView title, theme, address,start,distance;
        public ImageView image;

        public EventsViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.tv_title);
            theme = (TextView) view.findViewById(R.id.tv_theme);
            address = (TextView) view.findViewById(R.id.tv_address);
            start = (TextView) view.findViewById(R.id.tv_start);
            distance = (TextView) view.findViewById(R.id.tv_distance);
            image = (ImageView) view.findViewById(R.id.iv_image);

        }
    }


    public EventsAdapter(Context context,List<Event> events) {
        this.context = context;
        this.eventsList = events;

    }
    public void setItems(List<Event> events) {
        this.eventsList = events;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false);



        return new EventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(EventsViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.title.setText(event.getTitle());
        holder.theme.setText(event.getInterest());
        holder.address.setText(event.getAddress());
        holder.start.setText(setStart(event.getStart()));
        holder.distance.setText(event.getDistance()+"");
        if (event.getImage()!=null && !event.getImage().equals(""))
        Picasso.with(context)
                .load(event.getImage())
                .placeholder(R.drawable.ic_cloud_off_red)
                .into(holder.image);


    }

    private String setStart(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return DateUtil.CalendartoDate(calendar.getTime()) +" "+ DateUtil.CalendartoTime(calendar.getTime());
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }
}
