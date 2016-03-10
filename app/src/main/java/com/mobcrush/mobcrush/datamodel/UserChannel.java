package com.mobcrush.mobcrush.datamodel;

public class UserChannel extends DataModel {
    public Channel channel;
    public String streamKey;

    public UserChannel(String name) {
        this.channel = new Channel(name);
    }

    public UserChannel(Channel channel) {
        this.channel = channel;
    }
}
