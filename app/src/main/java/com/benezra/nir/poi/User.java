package com.benezra.nir.poi;


import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by Ravi Tamada on 07/10/16.
 * www.androidhive.info
 */

@IgnoreExtraProperties
public class User {

    private String name;
    private String avatar;
    private String email;
    private String id;
    private List<String> interests;
    private List<String> events;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }


    public User(String name, String avatar, String email, List<String> interests, List<String> events) {
        this.name = name;
        this.avatar = avatar;
        this.email = email;
        this.interests = interests;
        this.events = events;
    }

    public User(String id, String avatar){
        this.id = id;
        this.avatar = avatar;
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

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }
}
