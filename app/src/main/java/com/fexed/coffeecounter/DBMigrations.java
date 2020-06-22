package com.fexed.coffeecounter;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Created by Federico Matteoni on 22/06/2020
 */
public class DBMigrations {
    static final Migration MIGRATION_19_20 = new Migration(19, 20) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("CREATE TABLE IF NOT EXISTS `coffeetypenew` (`qnt` INTEGER NOT NULL, `liters` INTEGER NOT NULL, `name` TEXT, `desc` TEXT, `key` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `liquido` INTEGER NOT NULL, `sostanza` TEXT, `fav` INTEGER NOT NULL, `price` REAL NOT NULL)");
            database.execSQL("INSERT INTO coffeetypenew('qnt', 'liters', 'name', 'desc', 'key', 'liquido', 'sostanza', 'fav', 'price') SELECT * FROM coffeetype");
            database.execSQL("DROP TABLE coffeetype");
            database.execSQL("ALTER TABLE coffeetypenew RENAME TO coffeetype");
            database.execSQL("COMMIT");
        }
    };
    static final Migration MIGRATION_20_21 = new Migration(20, 21) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
        }
    };
    static final Migration MIGRATION_21_22 = new Migration(21, 22) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("ALTER TABLE coffeetype ADD COLUMN img TEXT");
            database.execSQL("COMMIT");
        }
    };
    static final Migration MIGRATION_22_23 = new Migration(22, 23) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("ALTER TABLE cup ADD COLUMN latitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE cup ADD COLUMN longitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("COMMIT");
        }
    };
    static final Migration MIGRATION_23_24 = new Migration(23, 24) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("BEGIN TRANSACTION");
            database.execSQL("ALTER TABLE coffeetype ADD COLUMN defaulttype INTEGER NOT NULL DEFAULT 0");
            database.execSQL("COMMIT");
        }
    };
}
