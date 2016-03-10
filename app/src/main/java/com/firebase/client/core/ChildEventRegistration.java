package com.firebase.client.core;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.core.view.Change;
import com.firebase.client.core.view.DataEvent;
import com.firebase.client.core.view.Event.EventType;
import com.firebase.client.core.view.QuerySpec;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class ChildEventRegistration implements EventRegistration {
    private final ChildEventListener eventListener;
    private final Repo repo;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$firebase$client$core$view$Event$EventType = new int[EventType.values().length];

        static {
            try {
                $SwitchMap$com$firebase$client$core$view$Event$EventType[EventType.CHILD_ADDED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$firebase$client$core$view$Event$EventType[EventType.CHILD_CHANGED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$firebase$client$core$view$Event$EventType[EventType.CHILD_MOVED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$firebase$client$core$view$Event$EventType[EventType.CHILD_REMOVED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public ChildEventRegistration(Repo repo, ChildEventListener eventListener) {
        this.repo = repo;
        this.eventListener = eventListener;
    }

    public boolean respondsTo(EventType eventType) {
        return eventType != EventType.VALUE;
    }

    public boolean equals(Object other) {
        return (other instanceof ChildEventRegistration) && ((ChildEventRegistration) other).eventListener.equals(this.eventListener);
    }

    public int hashCode() {
        return this.eventListener.hashCode();
    }

    public DataEvent createEvent(Change change, QuerySpec query) {
        return new DataEvent(change.getEventType(), this, new DataSnapshot(new Firebase(this.repo, query.getPath().child(change.getChildKey())), change.getIndexedNode()), change.getPrevName() != null ? change.getPrevName().asString() : null);
    }

    public void fireEvent(DataEvent eventData) {
        switch (AnonymousClass1.$SwitchMap$com$firebase$client$core$view$Event$EventType[eventData.getEventType().ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                this.eventListener.onChildAdded(eventData.getSnapshot(), eventData.getPreviousName());
                return;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                this.eventListener.onChildChanged(eventData.getSnapshot(), eventData.getPreviousName());
                return;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                this.eventListener.onChildMoved(eventData.getSnapshot(), eventData.getPreviousName());
                return;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                this.eventListener.onChildRemoved(eventData.getSnapshot());
                return;
            default:
                return;
        }
    }

    public void fireCancelEvent(FirebaseError error) {
        this.eventListener.onCancelled(error);
    }

    public String toString() {
        return "ChildEventRegistration";
    }
}
