package com.mobcrush.mobcrush.datamodel;

public class GroupChannel extends DataModel {
    public String channelLogo;
    public ChatRoom chatRoom;
    public Integer memberCount;
    public String name;
    public String posterImage;
    public String type;

    public GroupChannel(String name) {
        this.name = name;
    }
}
