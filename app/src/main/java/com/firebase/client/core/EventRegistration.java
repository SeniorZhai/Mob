package com.firebase.client.core;

import com.firebase.client.FirebaseError;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QuerySpec;

public interface EventRegistration {
    DataEvent createEvent(Change change, QuerySpec querySpec);

    boolean equals(Object obj);

    void fireCancelEvent(FirebaseError firebaseError);

    void fireEvent(DataEvent dataEvent);

    boolean respondsTo(EventType eventType);
}
