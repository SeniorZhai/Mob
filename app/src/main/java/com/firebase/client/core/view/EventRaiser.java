package com.firebase.client.core.view;

import com.firebase.client.EventTarget;
import com.firebase.client.core.Context;
import com.firebase.client.utilities.LogWrapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventRaiser {
    private final EventTarget eventTarget;
    private final LogWrapper logger;

    public EventRaiser(Context ctx) {
        this.eventTarget = ctx.getEventTarget();
        this.logger = ctx.getLogger("EventRaiser");
    }

    public void raiseEvents(List<? extends Event> events) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Raising " + events.size() + " event(s)");
        }
        final ArrayList<Event> eventsClone = new ArrayList(events);
        this.eventTarget.postEvent(new Runnable() {
            public void run() {
                Iterator i$ = eventsClone.iterator();
                while (i$.hasNext()) {
                    Event event = (Event) i$.next();
                    if (EventRaiser.this.logger.logsDebug()) {
                        EventRaiser.this.logger.debug("Raising " + event.toString());
                    }
                    event.fire();
                }
            }
        });
    }
}
