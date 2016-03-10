package com.firebase.client.core.view;

import com.firebase.client.DataSnapshot;
import com.firebase.client.core.EventRegistration;
import com.firebase.client.core.Path;
import com.firebase.client.core.view.Event.EventType;

public class DataEvent implements Event {
    private final EventRegistration eventRegistration;
    private final EventType eventType;
    private final String prevName;
    private final DataSnapshot snapshot;

    public DataEvent(EventType eventType, EventRegistration eventRegistration, DataSnapshot snapshot, String prevName) {
        this.eventType = eventType;
        this.eventRegistration = eventRegistration;
        this.snapshot = snapshot;
        this.prevName = prevName;
    }

    public Path getPath() {
        Path path = this.snapshot.getRef().getPath();
        return this.eventType == EventType.VALUE ? path : path.getParent();
    }

    public DataSnapshot getSnapshot() {
        return this.snapshot;
    }

    public String getPreviousName() {
        return this.prevName;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public void fire() {
        this.eventRegistration.fireEvent(this);
    }

    public String toString() {
        if (this.eventType == EventType.VALUE) {
            return getPath() + ": " + this.eventType + ": " + this.snapshot.getValue(true);
        }
        return getPath() + ": " + this.eventType + ": { " + this.snapshot.getKey() + ": " + this.snapshot.getValue(true) + " }";
    }
}
