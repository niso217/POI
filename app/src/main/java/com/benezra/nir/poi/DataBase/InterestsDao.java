package com.benezra.nir.poi.DataBase;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.benezra.nir.poi.Objects.EventsInterestData;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface InterestsDao {

    @Query("SELECT * FROM interest_data")
    List<EventsInterestData> getAll();

    @Query("SELECT * FROM interest_data where interest LIKE  :interest")
    EventsInterestData findByName(String interest);


    @Query("SELECT COUNT(*) from interest_data")
    int countInterests();

    @Insert
    void insertAll(EventsInterestData... interestData);

    @Delete
    void delete(EventsInterestData interestData);

    @Update
    void updateAll(List<EventsInterestData> interestData);

    @Insert
    void insert(EventsInterestData... interestData);


}
