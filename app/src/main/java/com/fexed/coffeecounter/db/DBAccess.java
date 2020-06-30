package com.fexed.coffeecounter.db;

import android.app.Application;
import android.os.AsyncTask;

import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.fexed.coffeecounter.data.Coffeetype;
import com.fexed.coffeecounter.data.Cup;

import java.util.List;

/**
 * Implements the communication with the AppDatabase
 * Created by Federico Matteoni on 28/06/2020
 */
public class DBAccess {
    /**
     * The AppDatabase
     */
    private static AppDatabase database;

    /**
     * Opens the AppDatabase if it's not already opened
     * @param application the {@link Application} context for opening the database
     */
    public DBAccess(Application application) {
        if (database == null) {
            //Opening the AppDatabase
            database = Room.databaseBuilder(application, AppDatabase.class, "typedb")
                    .addMigrations(DBMigrations.MIGRATION_19_20,
                            DBMigrations.MIGRATION_20_21,
                            DBMigrations.MIGRATION_21_22,
                            DBMigrations.MIGRATION_22_23,
                            DBMigrations.MIGRATION_23_24,
                            DBMigrations.MIGRATION_24_25,
                            DBMigrations.MIGRATION_25_26)
                    .build();

            //Old bug fixed by recalculating the number of cups per type
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Coffeetype coffeetype : database.coffetypeDao().getAll()) {
                       coffeetype.setQnt(database.cupDAO().getAll(coffeetype.getKey()).size());
                       updateTypes(coffeetype);
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (Exception ignored){}
        }
    }

    //Cups access

    /**
     * Adds a new {@link Cup} into the database, on a separate Thread
     * @param cup the {@link Cup} to be added
     */
    public void insertCup(final Cup cup) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().insert(cup);
                Coffeetype type = database.coffetypeDao().get(cup.getTypekey());
                type.setQnt(type.getQnt() + 1);
                database.coffetypeDao().update(type);
            }
        }).start();
    }

    /**
     * Adds a new list of {@link Cup} into the database, on a separate Thread
     * @param cups the {@link Cup}s to be added
     */
    public void insertCups(final Cup... cups) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().insertAll(cups);
                for (Cup cup : cups) {
                    Coffeetype type = database.coffetypeDao().get(cup.getTypekey());
                    type.setQnt(type.getQnt() + 1);
                    database.coffetypeDao().update(type);
                }
            }
        }).start();
    }

    /**
     * Deletes a {@link Cup} from the database, on a separate Thread
     * @param cup the {@link Cup} to be removed
     */
    public void deleteCup(final Cup cup) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().delete(cup);
                Coffeetype type = database.coffetypeDao().get(cup.getTypekey());
                type.setQnt(type.getQnt() - 1);
                database.coffetypeDao().update(type);
            }
        }).start();
    }

    /**
     * Removes a list of {@link Cup} into the database, on a separate Thread
     * @param cups the {@link Cup}s to be removed
     */
    public void deleteCups(final Cup... cups) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().deleteAll(cups);
                for (Cup cup : cups) {
                    Coffeetype type = database.coffetypeDao().get(cup.getTypekey());
                    type.setQnt(type.getQnt() - 1);
                    database.coffetypeDao().update(type);
                }
            }
        }).start();
    }

    /**
     * Deletes all the {@link Cup}s of a certain type from the database, on a separate Thread
     * @param key the {@link Coffeetype} key
     */
    public void deleteCups(final int key) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().deleteAll(key);
                Coffeetype type = database.coffetypeDao().get(key);
                if (type != null) {
                    type.setQnt(0);
                    database.coffetypeDao().update(type);
                }
            }
        }).start();
    }

    /**
     * Updates a list of {@link Cup} of the database, on a separate Thread
     * @param cups the {@link Cup}s to be updated
     */
    public void updateCups(final Cup... cups) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().update(cups);
            }
        }).start();
    }

    /**
     * Cleans the {@link Cup} database
     */
    public void nukeCups() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().nuke();
                for (Coffeetype type : database.coffetypeDao().getAll()) {
                    type.setQnt(0);
                    database.coffetypeDao().update(type);
                }
            }
        }).start();
    }

    /**
     * An AsyncTask that retrieves all the {@link Cup}s of a certain {@link Coffeetype}
     * @param key the {@link Coffeetype} key
     * @return the executing AsyncTask, which will return a {@code List<Cup>} object
     */
    public AsyncTask<Integer, Void, List<Cup>> getCups(int key) {
        return new getCupsPerKeyTask().execute(key);
    }

    static class getCupsPerKeyTask extends AsyncTask<Integer, Void, List<Cup>> {
        @Override
        protected List<Cup> doInBackground(Integer... integers) {
            return database.cupDAO().getAll(integers[0]);
        }
    }

    /**
     * An AsyncTask that retrieves all the days in the database
     * @return the executing AsyncTask, which will return a {@code List<String>} object containing
     * the days in the database
     */
    public AsyncTask<Void, Void, List<String>> getDays() {
        return new getDaysTask().execute();
    }

    static class getDaysTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            return database.cupDAO().getDays();
        }
    }

    /**
     * An AsyncTask that retrieves the number of the {@link Cup}s registered per day
     * @return the executing AsyncTask, which will return a {@code List<Integer>} object
     */
    public AsyncTask<Void, Void, List<Integer>> perDay() {
        return new getPerDayTask().execute();
    }

    static class getPerDayTask extends AsyncTask<Void, Void, List<Integer>> {
        @Override
        protected List<Integer> doInBackground(Void... voids) {
            return database.cupDAO().perDay();
        }
    }

    /**
     * An AsyncTask that retrieves all the {@link Cup}s of a certain day
     * @param day the day
     * @return the executing AsyncTask, which will return a {@code List<Cup>} object
     */
    public AsyncTask<String, Void, List<Cup>> getCups(String day) {
            return new getCupsPerDayTask().execute(day);
    }

    static class getCupsPerDayTask extends AsyncTask<String, Void, List<Cup>> {
        @Override
        protected List<Cup> doInBackground(String... strings) {
            return database.cupDAO().getAll(strings[0]);
        }
    }

    /**
     * An AsyncTask that retrieves all the {@link Cup}s of a certain {@link Coffeetype} in a certain
     * day
     * @param key the {@link Coffeetype} key
     * @param day the day
     * @return the executing AsyncTask, which will return a {@code List<Cup>} object
     */
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

    /**
     * Inserts a {@link Coffeetype} into the database, on a separate Thread
     * @param type the {@link Coffeetype} to be added
     */
    public void insertType(final Coffeetype type) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().insert(type);
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Inserts a list of {@link Coffeetype}s into the database, on a separate Thread
     * @param types the {@link Coffeetype}s to be added
     */
    public void insertTypes(final Coffeetype... types) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().insertAll(types);
            }
        }).start();
    }

    /**
     * Removes a {@link Coffeetype} from the database, on a separate Thread
     * @param type the {@link Coffeetype} to be removed
     */
    public void deleteType(final Coffeetype type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.cupDAO().deleteAll(type.getKey());
                database.coffetypeDao().delete(type);
            }
        }).start();
    }

    /**
     * Removes a list of {@link Coffeetype}s from the database, on a separate Thread
     * @param types the {@link Coffeetype}s to be removed
     */
    public void deleteTypes(final Coffeetype... types) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().deleteAll(types);
            }
        }).start();
    }

    /**
     * Updates a list of {@link Coffeetype}s in the database, on a separate Thread
     * @param types the {@link Coffeetype}s to be updated
     */
    public void updateTypes(final Coffeetype... types) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().update(types);
            }
        }).start();
    }

    /**
     * Cleans the {@link Coffeetype} database, on a separate Thread
     */
    public void nukeTypes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().nuke();
            }
        }).start();
    }

    /**
     * An AsyncTask that retrieves all the {@link Coffeetype}s currently in the database
     * @return the executing AsyncTask, which will return a {@code List<Coffeetype>} object
     */
    public AsyncTask<Void, Void, List<Coffeetype>> getTypes() {
        return new getTypesTask().execute();
    }

    static class getTypesTask extends AsyncTask<Void, Void, List<Coffeetype>> {
        @Override
        protected List<Coffeetype> doInBackground(Void... voids) {
            return database.coffetypeDao().getAll();
        }
    }

    /**
     * An AsyncTask that retrieves all the favourites {@link Coffeetype}s currently in the database
     * @return the executing AsyncTask, which will return a {@code List<Coffeetype>} object
     */
    public AsyncTask<Void, Void, List<Coffeetype>> getFavs() {
        return new getFavTypesTask().execute();
    }

    static class getFavTypesTask extends AsyncTask<Void, Void, List<Coffeetype>> {
        @Override
        protected List<Coffeetype> doInBackground(Void... voids) {
            return database.coffetypeDao().getFavs();
        }
    }

    /**
     * An AsyncTask that retrieves a {@link Coffeetype} currently in the database
     * @param key the key of the {@link Coffeetype} to be retrieved
     * @return the executing AsyncTask, which will return a {@link Coffeetype} object
     */
    public AsyncTask<Integer, Void, Coffeetype> getType(int key) {
        return new getTypeTask().execute(key);
    }

    static class getTypeTask extends AsyncTask<Integer, Void, Coffeetype> {
        @Override
        protected Coffeetype doInBackground(Integer... integers) {
            return database.coffetypeDao().get(integers[0]);
        }
    }

    //Utility
    /**
     * Executes a checkpoint on the database, on a separate Thread
     */
    public void checkpoint() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                database.coffetypeDao().checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
                database.cupDAO().checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
            }
        }).start();
    }

    /**
     * Closes the database and frees the reference
     */
    public void close() {
        database.close();
        database = null;
    }
}
