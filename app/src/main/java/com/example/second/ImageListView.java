package com.example.second;

import android.graphics.drawable.Drawable;

public class ImageListView {
    private Drawable icon;
    private String name;
    boolean selected = false;


    public Drawable getIcon(){return icon;}
    public String getName(){return name;}
    public boolean getSelected() {
        return selected;
    }


    public void setIcon(Drawable icon){this.icon = icon;}
    public void setName(String name){this.name = name;}
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public ImageListView(Drawable icon, String name, boolean selected){
        this.icon = icon;
        this.name = name;
        this.selected = selected;
    }
}
