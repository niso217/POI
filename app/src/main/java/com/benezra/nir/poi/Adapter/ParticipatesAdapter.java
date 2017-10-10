package com.benezra.nir.poi.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.benezra.nir.poi.Helper.VolleyHelper;
import com.benezra.nir.poi.User;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.User;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

/**
 * Created by justynagolawska on 12/03/2017.
 */


/*
* {@link CategoryAdapter} is an {@link ArrayAdapter} that can provide the layout for each list
* based on a data source, which is a list of {@link Category} objects.
* */
public class ParticipatesAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<User> mUsers;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the list is the data we want
     * to populate into the lists.
     *
     * @param context The current context. Used to inflate the layout file.
     * @param users A List of Users objects to display in a list
     */
    public ParticipatesAdapter(Activity context, ArrayList<User> users) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for one TextView and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        this.mContext = context;
        this.mUsers = users;
    }

    public void setItems(ArrayList<User> users) {
        this.mUsers.clear();
        this.mUsers = users;
        this.notifyDataSetChanged();

    }


    @Override
    public int getCount() {
        return mUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position The position in the list of data that should be displayed in the
     *                 list item view.
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(
                    R.layout.participate_list_item, parent, false);
        }

        // Get the {@link User} object located at this position in the list
        User currentUser = (User) getItem(position);

        // Find the TextView in the category_list_item.xml layout with the ID category_name
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.tv_par_name);
        // Get the category name from the current Category object and
        // set this text on the nameTextView
        nameTextView.setText("NIR");


        // Find the ImageView in the category_list_item.xml layout with the ID category_image
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.iv_par_image);
//        // Get the image resource ID from the current Category object and
//        // set the image to imageView
        VolleyHelper.getInstance(mContext).getImageLoader().get(currentUser.getAvatar(), ImageLoader.getImageListener(imageView,
                R.mipmap.ic_launcher, android.R.drawable
                        .ic_dialog_alert));

        // Return the whole list item layout (containing 2 TextViews and an ImageView)
        // so that it can be shown in the ListView
        return listItemView;

    }


}
