package com.benezra.nir.poi.DataBase;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.benezra.nir.poi.Objects.EventsInterestData;

import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {

    public static final String TAG = DatabaseInitializer.class.getName();

    public static void populateAsync(@NonNull final AppDatabase db) {
        PopulateDbAsync task = new PopulateDbAsync(db);
        task.execute();
    }

    public static List<EventsInterestData> populateSync(@NonNull final AppDatabase db) {
        return db.interestsDao().getAll();
    }

    public static void addInterest(final AppDatabase db, EventsInterestData interestData) {
        db.interestsDao().insertAll(interestData);
    }

    public static void updateInterests(final AppDatabase db, List<EventsInterestData> interestData) {
        UpdateDbAsync task = new UpdateDbAsync(db,interestData);
        task.execute();
    }



    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;

        PopulateDbAsync(AppDatabase db) {
            mDb = db;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            populateSync(mDb);
            return null;
        }

    }

    private static class UpdateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        private final List<EventsInterestData> mInterestData ;

        UpdateDbAsync(AppDatabase db,List<EventsInterestData> interestData) {
            mDb = db;
            mInterestData = interestData;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDb.interestsDao().updateAll(mInterestData);
            return null;
        }

    }
}
