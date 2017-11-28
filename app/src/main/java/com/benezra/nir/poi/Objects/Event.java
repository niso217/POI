package com.benezra.nir.poi.Objects;

import android.location.Location;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by nir on 29/09/2017.
 */

@IgnoreExtraProperties
public class Event implements Parcelable , Cloneable,Comparable{

    private String interest;
    private double latitude;
    private double longitude;
    private long start;
    private long end;
    private String id;
    private String owner;
    private String details;
    @Exclude
    private double distance;
    @Exclude
    private Uri uri;
    private String address;

    private String title;
    private String image;
    private Map<String,User> participates;

    private Marker marker;


    @Exclude
    public Marker getMarker() {
        return marker;
    }

    @Exclude
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Exclude
    private Location location = new Location("");;


    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    @Exclude
    public Location getLocation() {
        return location;
    }

    @Exclude
    public Uri getUri() {
        return uri;
    }
    @Exclude
    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Event() {
    }

    public Object clone() throws
            CloneNotSupportedException
    {
        return super.clone();
    }


    public Event(String uuid,String userid) {
        this.id = uuid;
        this.owner = userid;
        this.start = Calendar.getInstance().getTimeInMillis();
    }

    public Event(String uuid,LatLng location) {
        this.id = uuid;
        setLatLang(location);
    }

    public Event(String uuid,GeoLocation latLng,Marker marker) {
        this.id = uuid;
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
        this.marker = marker;

    }
    @Exclude
    public Map<String, User> getParticipates() {
        return participates;
    }

    @Exclude
    public void setParticipates(Map<String, User> participates) {
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
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        this.distance =  Math.round((Math.ceil(current.distanceTo(location)*4) / 4.0d) / 1000);
    }

    public void setLatLang(LatLng latLang){
        setLongitude(latLang.longitude);
        setLatitude(latLang.latitude);
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
        this.location.setLatitude(latitude);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        this.location.setLongitude(longitude);

    }

    @Exclude
    public LatLng getLatlng(){
        return new LatLng(latitude,longitude);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(interest);
        dest.writeLong(start);
        dest.writeLong(end);
        dest.writeString(id);
        dest.writeString(owner);
        dest.writeString(details);
        dest.writeString(title);
        dest.writeString(image);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(distance);
        dest.writeString(address);

    }

    private Event(Parcel in){
        this.interest = in.readString();
        this.start = in.readLong();
        this.end = in.readLong();
        this.id = in.readString();
        this.owner = in.readString();
        this.details = in.readString();
        this.title = in.readString();
        this.image = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.distance = in.readDouble();
        this.address = in.readString();

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

    @Override
    public int compareTo(@NonNull Object o) {
        if(((Event)o).getDistance() > distance){
            return -1;
        }if(((Event)o).getDistance() == distance){
            return 0;
        }else{
            return 1;
        }
    }
}
