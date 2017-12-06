package com.fexed.coffeecounter;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by fedex on 04/12/2017.
 */

@Dao
public interface CoffeetypeDAO {
    @Query("SELECT * FROM coffeetype")
    List<Coffeetype> getAll();

    @Insert
    void insertAll(Coffeetype... types);

    @Insert
    void insert(Coffeetype type);

    @Delete
    void delete(Coffeetype type);

    @Delete
    void deleteAll(Coffeetype... type);

    @Update
    void update(Coffeetype... types);

    @Query("DELETE FROM coffeetype")
    void nuke();
}
