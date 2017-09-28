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
    public List<String> intrests;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String name, String email, List<String> intrests) {
        this.name = name;
        this.email = email;
        this.intrests = intrests;
    }


}
