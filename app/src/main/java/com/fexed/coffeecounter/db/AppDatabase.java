package com.fexed.coffeecounter.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;

/**
 * Created by Federico Matteoni on 04/12/2017.
 */
@Database(entities = {Coffeetype.class, Cup.class}, version = 26)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CoffeetypeDAO coffetypeDao();

    public abstract CupDAO cupDAO();
}
