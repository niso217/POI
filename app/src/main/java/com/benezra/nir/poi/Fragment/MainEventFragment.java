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
import com.benezra.nir.poi.Objects.InterestData;
import com.benezra.nir.poi.R;
import com.benezra.nir.poi.RecyclerTouchListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainEventFragment extends Fragment implements RecyclerTouchListener.ClickListener{

    private FirebaseRecyclerAdapter<InterestData, ViewHolders.ParticipatesViewHolder> mInterestAdapter;
    private FirebaseDatabase mFirebaseInstance;
    private RecyclerView mInteresRecyclerView;
    private List<String> mInterestList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mInterestList = new ArrayList<>();


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
        Query query = mFirebaseInstance.getReference("interests_data");

        mInterestAdapter = new FirebaseRecyclerAdapter<InterestData, ViewHolders.ParticipatesViewHolder>(
                InterestData.class, R.layout.participate_list_row, ViewHolders.ParticipatesViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ViewHolders.ParticipatesViewHolder participatesViewHolder, InterestData model, int position) {
                participatesViewHolder.name.setText(model.getInterest());
                participatesViewHolder.image.setImageResource(getResources().getIdentifier(model.getInterest().toLowerCase(), "drawable", getActivity().getPackageName()));
                mInterestList.add(model.getInterest());
            }

        };

        mInteresRecyclerView.setAdapter(mInterestAdapter);

    }


    @Override
    public void onClick(View view, int position) {
        Intent eventActivity = new Intent(getContext(), EventsActivity.class);
        eventActivity.putExtra("interest",mInterestList.get(position));
        getActivity().startActivity(eventActivity);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
    //;
