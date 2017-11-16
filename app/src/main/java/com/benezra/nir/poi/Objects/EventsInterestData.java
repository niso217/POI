package com.benezra.nir.poi.Objects;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


import java.util.List;

/**
 * Created by nirb on 07/11/2017.
 */

@Entity(tableName = "interest_data")
public class EventsInterestData implements Comparable,Parcelable {

    @PrimaryKey()
    @NonNull
    private String interest;
    @ColumnInfo(name = "image")
    private String image;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "details")
    private String details;
    @ColumnInfo(name = "categories")
    private String categories;


    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public EventsInterestData() {
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public int compareTo(Object another) {
        int result = ((EventsInterestData)another).getInterest().compareTo(interest);
        if(result > 0){
            return 1;
        }if(result < 0){
            return 0;
        }else{
            return -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(interest);
        dest.writeString(image);
        dest.writeString(title);
        dest.writeString(details);
        dest.writeString(categories);


    }

    private EventsInterestData(Parcel in) {
        this.interest = in.readString();
        this.image = in.readString();
        this.title = in.readString();
        this.details = in.readString();
        this.categories = in.readString();

    }


    public static final Parcelable.Creator<EventsInterestData> CREATOR = new Parcelable.Creator<EventsInterestData>() {

        @Override
        public EventsInterestData createFromParcel(Parcel source) {
            return new EventsInterestData(source);
        }

        @Override
        public EventsInterestData[] newArray(int size) {
            return new EventsInterestData[size];
        }
    };

}
