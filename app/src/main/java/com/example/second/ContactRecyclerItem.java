package com.example.second;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class ContactRecyclerItem implements Serializable {
    private Drawable iconDrawable;
    private String nameStr;
    private String phoneStr;
    private long iconID, personID;

    public ContactRecyclerItem() {
    }

    public void setIcon(Drawable icon) {
        iconDrawable = icon;
    }

    public void setIconID(long id){iconID = id;}

    public void setName(String name) {
        nameStr = name;
    }

    public void setPersonID(long id){personID = id;}

    public void setPhone(String desc) {
        phoneStr = desc;
    }

    public Drawable getIcon() {
        return this.iconDrawable;
    }

    public String getName() {
        return this.nameStr;
    }

    public String getPhone() {
        return this.phoneStr;
    }
    public long getIconID(){
        return this.iconID;
    }

    public long getPersonID(){
        return this.personID;
    }

    @Override
    public String toString(){
        return this.phoneStr;
    }
}