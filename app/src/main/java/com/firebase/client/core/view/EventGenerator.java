package com.firebase.client.core.view;

import com.firebase.client.core.EventRegistration;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.snapshot.Index;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventGenerator {
    private final Index index;
    private final QuerySpec query;

    public EventGenerator(QuerySpec query) {
        this.query = query;
        this.index = query.getIndex();
    }

    private void generateEventsForType(List<DataEvent> events, EventType type, List<Change> changes, List<EventRegistration> eventRegistrations, IndexedNode eventCache) {
        List<Change> filteredChanges = new ArrayList();
        for (Change change : changes) {
            if (change.getEventType().equals(type)) {
                filteredChanges.add(change);
            }
        }
        Collections.sort(filteredChanges, changeComparator());
        for (Change change2 : filteredChanges) {
            for (EventRegistration registration : eventRegistrations) {
                if (registration.respondsTo(type)) {
                    events.add(generateEvent(change2, registration, eventCache));
                }
            }
        }
    }

    private DataEvent generateEvent(Change change, EventRegistration registration, IndexedNode eventCache) {
        Change newChange;
        if (change.getEventType().equals(EventType.VALUE) || change.getEventType().equals(EventType.CHILD_REMOVED)) {
            newChange = change;
        } else {
            newChange = change.changeWithPrevName(eventCache.getPredecessorChildName(change.getChildKey(), change.getIndexedNode().getNode(), this.index));
        }
        return registration.createEvent(newChange, this.query);
    }

    public List<DataEvent> generateEventsForChanges(List<Change> changes, IndexedNode eventCache, List<EventRegistration> eventRegistrations) {
        List<DataEvent> events = new ArrayList();
        List<Change> moves = new ArrayList();
        for (Change change : changes) {
            if (change.getEventType().equals(EventType.CHILD_CHANGED) && this.index.indexedValueChanged(change.getOldIndexedNode().getNode(), change.getIndexedNode().getNode())) {
                moves.add(Change.childMovedChange(change.getChildKey(), change.getIndexedNode()));
            }
        }
        generateEventsForType(events, EventType.CHILD_REMOVED, changes, eventRegistrations, eventCache);
        generateEventsForType(events, EventType.CHILD_ADDED, changes, eventRegistrations, eventCache);
        generateEventsForType(events, EventType.CHILD_MOVED, moves, eventRegistrations, eventCache);
        generateEventsForType(events, EventType.CHILD_CHANGED, changes, eventRegistrations, eventCache);
        generateEventsForType(events, EventType.VALUE, changes, eventRegistrations, eventCache);
        return events;
    }

    private Comparator<Change> changeComparator() {
        return new Comparator<Change>() {
            static final /* synthetic */ boolean $assertionsDisabled = (!EventGenerator.class.desiredAssertionStatus());

            public int compare(Change a, Change b) {
                if ($assertionsDisabled || !(a.getChildKey() == null || b.getChildKey() == null)) {
                    return EventGenerator.this.index.compare(new NamedNode(a.getChildKey(), a.getIndexedNode().getNode()), new NamedNode(b.getChildKey(), b.getIndexedNode().getNode()));
                }
                throw new AssertionError();
            }
        };
    }
}
