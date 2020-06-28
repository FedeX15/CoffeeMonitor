package com.fexed.coffeecounter.db;

import android.app.Application;
import android.os.AsyncTask;

import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;

import java.util.List;

/**
 * Created by Federico Matteoni on 28/06/2020
 */
public class DBAccess {
    private static AppDatabase database;
    private getCupsPerDayTask getCupsPerDayTask;
    private getDaysTask getDaysTask;
    private getPerDayTask getPerDayTask;
    private getFavTypesTask getFavTypesTask;
    private getTypesTask getTypesTask;

    public DBAccess(Application application) {
        if (database == null) {
            database = Room.databaseBuilder(application, AppDatabase.class, "typedb")
                    .addMigrations(DBMigrations.MIGRATION_19_20,
                            DBMigrations.MIGRATION_20_21,
                            DBMigrations.MIGRATION_21_22,
                            DBMigrations.MIGRATION_22_23,
                            DBMigrations.MIGRATION_23_24,
                            DBMigrations.MIGRATION_24_25,
                            DBMigrations.MIGRATION_25_26)
                    .build();
        }
    }

    //Cups access
    public void insertCup(final Cup cup) {
        getCupsPerDayTask = null;
        getDaysTask = null;
        getPerDayTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().insert(cup);
            }
        }).start();
    }

    public void insertCups(final Cup... cups) {
        getCupsPerDayTask = null;
        getDaysTask = null;
        getPerDayTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().insertAll(cups);
            }
        }).start();
    }

    public void deleteCup(final Cup cup) {
        getCupsPerDayTask = null;
        getDaysTask = null;
        getPerDayTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().delete(cup);
            }
        }).start();
    }

    public void deleteCups(final Cup... cups) {
        getCupsPerDayTask = null;
        getDaysTask = null;
        getPerDayTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().deleteAll(cups);
            }
        }).start();
    }

    public void deleteCups(final int key) {
        getCupsPerDayTask = null;
        getDaysTask = null;
        getPerDayTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().deleteAll(key);
            }
        }).start();
    }

    public void updateCups(final Cup... cups) {
        getCupsPerDayTask = null;
        getDaysTask = null;
        getPerDayTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().update(cups);
            }
        }).start();
    }

    public void nukeCups() {
        getCupsPerDayTask = null;
        getDaysTask = null;
        getPerDayTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().nuke();
            }
        }).start();
    }

    public AsyncTask<Integer, Void, List<Cup>> getCups(int key) {
        return new getCupsPerKeyTask().execute(key);
    }

    static class getCupsPerKeyTask extends AsyncTask<Integer, Void, List<Cup>> {
        @Override
        protected List<Cup> doInBackground(Integer... integers) {
            return database.cupDAO().getAll(integers[0]);
        }
    }

    public AsyncTask<Void, Void, List<String>> getDays() {
        if (getDaysTask != null) return getDaysTask;
        else {
            getDaysTask = new getDaysTask();
            return getDaysTask.execute();
        }
    }

    static class getDaysTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            return database.cupDAO().getDays();
        }
    }

    public AsyncTask<Void, Void, List<Integer>> perDay() {
        if (getPerDayTask != null) return getPerDayTask;
        else {
            getPerDayTask = new getPerDayTask();
            return getPerDayTask.execute();
        }
    }

    static class getPerDayTask extends AsyncTask<Void, Void, List<Integer>> {
        @Override
        protected List<Integer> doInBackground(Void... voids) {
            return database.cupDAO().perDay();
        }
    }

    public AsyncTask<String, Void, List<Cup>> getCups(String day) {
        if (getCupsPerDayTask != null) return getCupsPerDayTask;
        else {
            getCupsPerDayTask = new getCupsPerDayTask();
            return getCupsPerDayTask.execute(day);
        }
    }

    static class getCupsPerDayTask extends AsyncTask<String, Void, List<Cup>> {
        @Override
        protected List<Cup> doInBackground(String... strings) {
            return database.cupDAO().getAll(strings[0]);
        }
    }

    public AsyncTask<String, Void, List<Cup>> getCups(int key, String day) {
        return new getCupsPerDayAndKeyTask().execute(String.valueOf(key), day);
    }

    static class getCupsPerDayAndKeyTask extends AsyncTask<String, Void, List<Cup>> {
        @Override
        protected List<Cup> doInBackground(String... strings) {
            return database.cupDAO().getAll(Integer.parseInt(strings[0]), strings[1]);
        }
    }

    //Coffeetype access
    public void insertType(final Coffeetype type) {
        getFavTypesTask = null;
        getTypesTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().insert(type);
            }
        }).start();
    }

    public void insertTypes(final Coffeetype... types) {
        getFavTypesTask = null;
        getTypesTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().insertAll(types);
            }
        }).start();
    }

    public void deleteType(final Coffeetype type) {
        getFavTypesTask = null;
        getTypesTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().delete(type);
            }
        }).start();
    }

    public void deleteTypes(final Coffeetype... types) {
        getFavTypesTask = null;
        getTypesTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().deleteAll(types);
            }
        }).start();
    }

    public void updateTypes(final Coffeetype... types) {
        getFavTypesTask = null;
        getTypesTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().update(types);
            }
        }).start();
    }

    public void nukeTypes() {
        getFavTypesTask = null;
        getTypesTask = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().nuke();
            }
        }).start();
    }

    public AsyncTask<Void, Void, List<Coffeetype>> getTypes() {
        if (getTypesTask != null) return getTypesTask;
        else {
            getTypesTask = new getTypesTask();
            return getTypesTask.execute();
        }
    }

    static class getTypesTask extends AsyncTask<Void, Void, List<Coffeetype>> {
        @Override
        protected List<Coffeetype> doInBackground(Void... voids) {
            return database.coffetypeDao().getAll();
        }
    }

    public AsyncTask<Void, Void, List<Coffeetype>> getFavs() {
        if (getFavTypesTask != null) return getFavTypesTask;
        else {
            getFavTypesTask = new getFavTypesTask();
            return getFavTypesTask.execute();
        }
    }

    static class getFavTypesTask extends AsyncTask<Void, Void, List<Coffeetype>> {
        @Override
        protected List<Coffeetype> doInBackground(Void... voids) {
            return database.coffetypeDao().getFavs();
        }
    }

    public AsyncTask<Integer, Void, Coffeetype> getType(int key) {
        return new getTypeTask().execute(key);
    }

    static class getTypeTask extends AsyncTask<Integer, Void, Coffeetype> {
        @Override
        protected Coffeetype doInBackground(Integer... integers) {
            return database.coffetypeDao().get(integers[0]);
        }
    }

    public void checkpoint() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
                database.cupDAO().checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
            }
        }).start();
    }

    public void close() {
        database.close();
        database = null;
    }
}
