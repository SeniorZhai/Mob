package com.firebase.client.core;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QuerySpec;

public class ValueEventRegistration implements EventRegistration {
    private final ValueEventListener eventListener;
    private final Repo repo;

    public ValueEventRegistration(Repo repo, ValueEventListener eventListener) {
        this.repo = repo;
        this.eventListener = eventListener;
    }

    public boolean respondsTo(EventType eventType) {
        return eventType == EventType.VALUE;
    }

    public boolean equals(Object other) {
        return (other instanceof ValueEventRegistration) && ((ValueEventRegistration) other).eventListener.equals(this.eventListener);
    }

    public int hashCode() {
        return this.eventListener.hashCode();
    }

    public DataEvent createEvent(Change change, QuerySpec query) {
        return new DataEvent(EventType.VALUE, this, new DataSnapshot(new Firebase(this.repo, query.getPath()), change.getIndexedNode()), null);
    }

    public void fireEvent(DataEvent eventData) {
        this.eventListener.onDataChange(eventData.getSnapshot());
    }

    public void fireCancelEvent(FirebaseError error) {
        this.eventListener.onCancelled(error);
    }

    public String toString() {
        return "ValueEventRegistration";
    }
}
