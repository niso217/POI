package com.benezra.nir.poi.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.benezra.nir.poi.Activity.EventsActivity;
import com.benezra.nir.poi.Adapter.ViewHolders;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainEventFragment extends Fragment implements RecyclerTouchListener.ClickListener{

    private FirebaseRecyclerAdapter<String, ViewHolders.ParticipatesViewHolder> mInterestAdapter;
    private FirebaseDatabase mFirebaseInstance;
    private RecyclerView mInteresRecyclerView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseInstance = FirebaseDatabase.getInstance();


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_event, container, false);


        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        mInteresRecyclerView = (RecyclerView) rootView.findViewById(R.id.main_event_list);
        mInteresRecyclerView.setLayoutManager(layoutManager);
        mInteresRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mInteresRecyclerView, this));

        participatesChangeListener();
        return rootView;
    }

    private void participatesChangeListener() {
        Query query = mFirebaseInstance.getReference("interests");

        mInterestAdapter = new FirebaseRecyclerAdapter<String, ViewHolders.ParticipatesViewHolder>(
                String.class, R.layout.participate_list_row, ViewHolders.ParticipatesViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewHolders.ParticipatesViewHolder participatesViewHolder, String model, int position) {
                if (position==0) return;
                participatesViewHolder.name.setText(model);
                participatesViewHolder.image.setImageResource(getResources().getIdentifier(model.toLowerCase(), "drawable", getActivity().getPackageName()));
            }

        };

        mInteresRecyclerView.setAdapter(mInterestAdapter);

    }


    @Override
    public void onClick(View view, int position) {
        Intent galleryIntent = new Intent(getContext(), EventsActivity.class);
        getActivity().startActivity(galleryIntent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
    //;
