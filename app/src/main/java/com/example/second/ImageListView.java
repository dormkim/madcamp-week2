package com.example.second;

import android.graphics.drawable.Drawable;

public class ImageListView {
    private Drawable icon;
    private String name;


    public Drawable getIcon(){return icon;}
    public String getName(){return name;}


    public void setIcon(Drawable icon){this.icon = icon;}
    public void setName(String name){this.name = name;}

    public ImageListView(Drawable icon, String name){
        this.icon=icon;
        this.name=name;
    }
}
