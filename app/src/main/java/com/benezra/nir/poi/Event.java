package com.benezra.nir.poi;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;
import java.util.Map;

/**
 * Created by nir on 29/09/2017.
 */

@IgnoreExtraProperties
public class Event implements Parcelable {

    private String interest;
    private double latitude;
    private double longitude;
    private String start;
    private String end;
    private String id;
    private String owner;
    private String details;
    @Exclude
    private double distance;
    private String title;
    private String image;
    private Map<String,Boolean> participates;


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Event() {
    }

    public Event(String uuid) {
        this.id = uuid;
    }


    public Map<String, Boolean> getParticipates() {
        return participates;
    }

    public void setParticipates(Map<String, Boolean> participates) {
        this.participates = participates;
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

    public double getDistance() {
        return distance;
    }

    @Exclude
    public void setDistance(Location current) {
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        this.distance =  Math.round((Math.ceil(current.distanceTo(loc)*4) / 4.0d) / 1000);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(interest);


        dest.writeString(start);
        dest.writeString(end);
        dest.writeString(id);
        dest.writeString(owner);
        dest.writeString(details);
        dest.writeString(title);
        dest.writeString(image);

        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(distance);
    }

    private Event(Parcel in){
        this.interest = in.readString();
        this.start = in.readString();
        this.end = in.readString();
        this.id = in.readString();
        this.owner = in.readString();
        this.details = in.readString();
        this.title = in.readString();
        this.image = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.distance = in.readDouble();

    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {

        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
