package com.fexed.coffeecounter;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class Coffeetype {
    @ColumnInfo(name = "qnt")
    private int qnt;

    @ColumnInfo(name = "liters")
    private double liters;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "desc")
    private String desc;

    @NonNull
    @PrimaryKey (autoGenerate = true)
    private int key;

    @ColumnInfo(name = "liquido")
    private boolean liquido;

    @ColumnInfo(name = "sostanza")
    private String sostanza;

    private int price;

    public Coffeetype(String name, double liters, String desc, boolean liquido, String sostanza, int price) {
        this.liters = liters;
        this.name = name;
        this.desc = desc;
        this.liquido = liquido;
        this.sostanza = sostanza;
        this.qnt = 0;
        this.price = price;
    }

    public int getQnt() {
        return qnt;
    }

    public void setQnt(int qnt) {
        this.qnt = qnt;
    }

    public double getLiters() {
        return liters;
    }

    public void setLiters(double liters) {
        this.liters = liters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isLiquido() {
        return liquido;
    }

    public void setLiquido(boolean liquido) {
        this.liquido = liquido;
    }

    public String getSostanza() { return sostanza; }

    public void setSostanza(String sostanza) { this.sostanza = sostanza; }

    @Override
    public String toString() {
        return getName() + "\n" + getDesc() + "\nSostanza: " + getSostanza().toLowerCase() + "\n" + getLiters() + ((isLiquido()) ? " l" : " mg");
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
