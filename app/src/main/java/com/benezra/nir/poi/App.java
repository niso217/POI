package com.benezra.nir.poi;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by nirb on 26/11/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        super.onCreate();

    }
}
