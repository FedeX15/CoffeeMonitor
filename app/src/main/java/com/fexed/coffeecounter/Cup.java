package com.fexed.coffeecounter;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by fedex on 05/12/2017.
 */

@Entity(foreignKeys = @ForeignKey(entity = Coffeetype.class, parentColumns = "key", childColumns = "typekey"))
public class Cup {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private int key;

    @NonNull
    private int typekey;

    private String date;

    private String day;

    public Cup(int typekey) {
        this.typekey = typekey;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss:SSS", Locale.getDefault());
        this.date = sdf.format(Calendar.getInstance().getTime());
        sdf = new SimpleDateFormat("dd/MMM/yyy", Locale.getDefault());
        this.day = sdf.format(Calendar.getInstance().getTime());
    }

    @NonNull
    public int getKey() {
        return key;
    }

    public void setKey(@NonNull int key) {
        this.key = key;
    }

    public int getTypekey() {
        return typekey;
    }

    public void setTypekey(@NonNull int typekey) {
        this.typekey = typekey;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String toString() {

        return date.substring(0, date.length()-7);
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
