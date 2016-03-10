package com.mobcrush.mobcrush.logic;

public enum RoleType {
    user(0),
    admin(1),
    moderator(2),
    broadcaster(3);
    
    private int value;

    private RoleType(int value) {
        this.value = value;
    }
}
