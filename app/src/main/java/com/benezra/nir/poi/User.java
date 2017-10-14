package com.benezra.nir.poi;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@IgnoreExtraProperties
public class User implements Parcelable {

    private String name;
    private String avatar;
    private String email;
    private String id;
    private ArrayList<String> interests;
    private ArrayList<String> events;


    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(ArrayList<String> interests) {
        this.interests = interests;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<String> events) {
        this.events = events;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(email);
        dest.writeString(id);
        dest.writeSerializable(interests);
        dest.writeSerializable(events);
    }

    private User(Parcel in){
        this.name = in.readString();
        this.avatar = in.readString();
        this.email = in.readString();
        this.id = in.readString();
        this.interests = (ArrayList<String>) in.readSerializable();
        this.events = (ArrayList<String>) in.readSerializable();

    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
