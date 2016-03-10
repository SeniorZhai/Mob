package com.firebase.client.core.view;

import com.firebase.client.core.Path;

public interface Event {

    public enum EventType {
        CHILD_REMOVED,
        CHILD_ADDED,
        CHILD_MOVED,
        CHILD_CHANGED,
        VALUE
    }

    void fire();

    Path getPath();

    String toString();
}
