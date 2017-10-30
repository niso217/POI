package com.benezra.nir.poi.Objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Chike on 2/11/2017.
 */

public class EventPhotos implements Parcelable {

    private String mUrl;
    private String mTitle;

    public EventPhotos(String url, String title) {
        mUrl = url;
        mTitle = title;
    }

    protected EventPhotos(Parcel in) {
        mUrl = in.readString();
        mTitle = in.readString();
    }

    public static final Creator<EventPhotos> CREATOR = new Creator<EventPhotos>() {
        @Override
        public EventPhotos createFromParcel(Parcel in) {
            return new EventPhotos(in);
        }

        @Override
        public EventPhotos[] newArray(int size) {
            return new EventPhotos[size];
        }
    };

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mUrl);
        parcel.writeString(mTitle);
    }
}
