package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.benezra.nir.poi.Objects.User;
import com.benezra.nir.poi.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventParticipatesAdapter extends RecyclerView.Adapter<EventParticipatesAdapter.EventsViewHolder>  {

    private List<User> participatesList;
    private Context context;


    public class EventsViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView image;
        public TextView name;

        public EventsViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.iv_par_image);
            name = view.findViewById(R.id.tv_par_name);
        }
    }


    public EventParticipatesAdapter(Context context, List<User> users) {
        this.context = context;
        this.participatesList = users;

    }


    public void setItems(List<User> events) {
        this.participatesList = events;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.participate_list_row, parent, false);



        return new EventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final EventsViewHolder holder, int position) {
        User user = participatesList.get(position);

        Picasso.with(context)
                .load(user.getAvatar())
                .placeholder(R.drawable.com_facebook_profile_picture_blank_portrait)
                .into(holder.image, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
        holder.name.setText(user.getName());

    }


    @Override
    public int getItemCount() {
        return participatesList.size();
    }
}
