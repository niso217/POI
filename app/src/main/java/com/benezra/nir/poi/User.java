package com.benezra.nir.poi;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

/**
 * Created by Ravi Tamada on 07/10/16.
 * www.androidhive.info
 */

@IgnoreExtraProperties
public class User {

    public String name;
    public String email;
    public List<String> interests;
    public List<String> events;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String name, String email, List<String> interests, List<String> events) {
        this.name = name;
        this.email = email;
        this.interests = interests;
        this.events = events;
    }


}
