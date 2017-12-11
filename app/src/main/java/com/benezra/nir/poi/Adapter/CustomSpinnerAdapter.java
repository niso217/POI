package com.benezra.nir.poi.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.benezra.nir.poi.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by nir on 10/10/2017.
 */

public class CustomSpinnerAdapter extends ArrayAdapter<String> implements SpinnerAdapter {

    private final Context activity;
    private ArrayList<String> asr;

    public CustomSpinnerAdapter(@NonNull Context context,ArrayList<String> asr) {
        super(context,0, asr);
        this.activity = context;
        this.asr = asr;
        sortList();
    }

//    public CustomSpinnerAdapter(Context context, ArrayList<String> asr) {
//        this.asr=asr;
//        sortList();
//        activity = context;
//    }


    public void updateInterestList(List<String> list) {
        asr.clear();
        asr.addAll(list);
        sortList();
        this.notifyDataSetChanged();
    }


    private void sortList(){
        Collections.sort(asr, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
    }

    public int getPosition(String interest){
       return  asr.indexOf(interest);
    }


    public int getCount()
    {
        return asr.size();
    }

//    public Object getItem(int i)
//    {
//        return asr.get(i);
//    }

    public long getItemId(int i)
    {
        return (long)i;
    }



    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView txt = new TextView(activity);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(18);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(asr.get(position));
        txt.setTextColor(Color.parseColor("#000000"));
        return  txt;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {

        View listItemView = view;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(activity).inflate(
                    R.layout.spinner_item, viewgroup, false);
        }

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.tv_spinner);
        titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0);
        titleTextView.setText(asr.get(i));

        return  listItemView;

    }

}
