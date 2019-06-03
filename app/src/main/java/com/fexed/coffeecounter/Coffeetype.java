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
    private int liters;

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

    private boolean fav;

    private float price;

    public String img;

    public Coffeetype(String name, int liters, String desc, boolean liquido, String sostanza, float price, String img) {
        this.liters = liters;
        this.name = name;
        this.desc = desc;
        this.liquido = liquido;
        this.sostanza = sostanza;
        this.qnt = 0;
        this.price = price;
        this.fav = false;
        this.img = img;
    }

    public int getQnt() {
        return qnt;
    }

    public void setQnt(int qnt) {
        this.qnt = qnt;
    }

    public int getLiters() {
        return liters;
    }

    public void setLiters(int liters) {
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
        //return getName() + "\n" + getDesc() + "\n" + getPrice() + "€\n" + "\nSostanza: " + getSostanza().toLowerCase() + "\nQuantità: " + getLiters() + ((isLiquido()) ? " ml" : " mg");
        return getName();
    }

    public String toBigString() {
        return getDesc() + "\n" + getLiters() + ((isLiquido()) ? " ml" : " mg") + "   " + getPrice() + "€   " + getSostanza();
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }


    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public boolean isFav() {
        return fav;
    }

    public void setFav(boolean fav) {
        this.fav = fav;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return getKey() == ((Coffeetype) obj).getKey();
        } catch (ClassCastException ex) {
            return false;
        }
    }
}
