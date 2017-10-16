package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.User;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ParticipateAdapter extends RecyclerView.Adapter<ParticipateAdapter.MyViewHolder> {

    private List<User> userList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView image;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.tv_par_name);
            image = (ImageView) view.findViewById(R.id.iv_par_image);
        }
    }


    public ParticipateAdapter(Context context , List<User> userList) {
        this.userList = userList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        User user = userList.get(position);
        holder.name.setText(user.getName());
        Picasso.with(context).load(user.getAvatar()).into(holder.image);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
