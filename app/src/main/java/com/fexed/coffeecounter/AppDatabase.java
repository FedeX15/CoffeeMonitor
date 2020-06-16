package com.fexed.coffeecounter;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Created by fexed on 04/12/2017.
 */

@Database(entities = {Coffeetype.class, Cup.class}, version = 23)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CoffeetypeDAO coffetypeDao();
    public abstract CupDAO cupDAO();
}
