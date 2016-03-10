package com.mobcrush.mobcrush.datamodel;

public class Channel extends DataModel {
    public String _id;
    public String channelLogo;
    public ChatRoom chatRoom;
    public Integer memberCount;
    public String name;
    public String posterImage;
    public String type;

    public Channel(String name) {
        this(null, name, null, null, null, null);
    }

    public Channel(String _id, String name, String channelLogo, String posterImage, String type, Integer memberCount) {
        this._id = _id;
        this.name = name;
        this.channelLogo = channelLogo;
        this.posterImage = posterImage;
        this.type = type;
        this.memberCount = memberCount;
    }
}
