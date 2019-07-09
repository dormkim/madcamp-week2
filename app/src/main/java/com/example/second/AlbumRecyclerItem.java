package com.example.second;

public class AlbumRecyclerItem {
    private String itemPath;
    private String photo_id;
    private String itemName;

    public AlbumRecyclerItem(String itemPath, String photo_id) {
        this.itemPath = itemPath;
        this.photo_id = photo_id;
    }
    public void setPhoto_id(String photo_id){
        this.photo_id = photo_id;
    }
    public void setItemPath(String itemPath) {
        this.itemPath = itemPath;
    }
    public void setName(String itemName){
        this.itemName = itemName;
    }
    public String getName(){
        return itemName;
    }
    public String getitemPath(){
        return this.itemPath;
    }
    public String getPhoto_id(){
        return this.photo_id;
    }
}
