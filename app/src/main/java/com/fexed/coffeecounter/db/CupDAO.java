package com.fexed.coffeecounter.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fexed.coffeecounter.data.Cup;

import java.util.List;

/**
 * Created by Federico Matteoni on 04/12/2017.
 */
@Dao
public interface CupDAO {
    @Query("SELECT * FROM cup WHERE typekey is :key")
    List<Cup> getAll(int key);

    @Query("SELECT day FROM cup GROUP BY day")
    List<String> getDays();

    @Query("DELETE FROM cup WHERE typekey IS :key")
    void deleteAll(int key);

    @Query("DELETE FROM cup WHERE date = (SELECT max(date) FROM cup c2 WHERE c2.typekey IS :key)")
    void deleteMostRecent(int key);

    @Query("SELECT * FROM cup WHERE day is :day")
    List<Cup> getAll(String day);

    @Query("SELECT COUNT(*) FROM cup GROUP BY day")
    List<Integer> perDay();

    @Query("SELECT COUNT(*) FROM cup GROUP BY `key`")
    List<Integer> perType();

    @Query("SELECT * FROM cup WHERE typekey is :key AND day is :day")
    List<Cup> getAll(int key, String day);

    @Insert
    void insertAll(Cup... cups);

    @Insert
    void insert(Cup cup);

    @Delete
    void delete(Cup cup);

    @Delete
    void deleteAll(Cup... cup);

    @Update
    void update(Cup... cups);

    @Query("DELETE FROM cup")
    void nuke();
}
