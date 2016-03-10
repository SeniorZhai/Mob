package com.mobcrush.mobcrush.datamodel;

public class Game extends DataModel {
    public String _id;
    public Integer broadcastCount;
    public ChatRoom chatRoom;
    public boolean enabled;
    public String icon;
    public String name;
    public String posterImage;

    public Game(String _id, String name, String icon, boolean enabled, Integer broadcastCount, String posterImage) {
        this._id = _id;
        this.name = name;
        this.icon = icon;
        this.enabled = enabled;
        this.broadcastCount = broadcastCount;
        this.posterImage = posterImage;
    }
}
