package com.apps.adrcotfas.goodtime.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface ProfileDao {

    @Insert(onConflict = REPLACE)
    void addProfile(Profile profile);

    @Query("select * from Profile")
    LiveData<List<Profile>> getProfiles();

    @Query("delete from Profile where name = :name")
    void deleteProfile(String name);
}