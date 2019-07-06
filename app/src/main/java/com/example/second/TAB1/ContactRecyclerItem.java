package com.example.second.TAB1;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class ContactRecyclerItem implements Serializable {
    private Drawable iconDrawable;
    private String nameStr;
    private String phoneStr;
    private String contactId;
    private long iconID, personID;

    public ContactRecyclerItem() {
    }

    public void setContactId(String contactId){
        this.contactId = contactId;
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

    public String getContactId(){
        return this.contactId;
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