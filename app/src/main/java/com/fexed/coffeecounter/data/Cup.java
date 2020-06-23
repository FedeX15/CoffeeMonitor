package com.fexed.coffeecounter.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by fexed on 05/12/2017.
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

    private double latitude;

    private double longitude;

    public Cup(int typekey) {
        this.typekey = typekey;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyy HH:mm:ss:SSS", Locale.getDefault());
        this.date = sdf.format(Calendar.getInstance().getTime());
        sdf = new SimpleDateFormat("yyy/MM/dd", Locale.getDefault());
        this.day = sdf.format(Calendar.getInstance().getTime());
    }

    public Cup(int typekey, String Date, String Day) {
        this.typekey = typekey;
        this.date = Date;
        this.day = Day;
    }

    @NonNull
    public int getKey() { return key; }

    public void setKey(@NonNull int key) { this.key = key; }

    public int getTypekey() { return typekey; }

    public void setTypekey(@NonNull int typekey) { this.typekey = typekey; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    @NonNull
    public String toString() { return day; }

    public String getDay() { return day; }

    public void setDay(String day) { this.day = day; }

    public double getLatitude() { return this.latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return this.longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }
}
