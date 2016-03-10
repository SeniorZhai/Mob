package com.firebase.client.core.view;

import com.firebase.client.FirebaseError;
import com.firebase.client.core.EventRegistration;
import com.firebase.client.core.Path;

public class CancelEvent implements Event {
    private final FirebaseError error;
    private final EventRegistration eventRegistration;
    private final Path path;

    public CancelEvent(EventRegistration eventRegistration, FirebaseError error, Path path) {
        this.eventRegistration = eventRegistration;
        this.path = path;
        this.error = error;
    }

    public Path getPath() {
        return this.path;
    }

    public void fire() {
        this.eventRegistration.fireCancelEvent(this.error);
    }

    public String toString() {
        return getPath() + ":" + "CANCEL";
    }
}
