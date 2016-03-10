package com.firebase.client;

import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.core.Path;
import com.firebase.client.core.Repo;
import com.firebase.client.core.ValidationPath;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.snapshot.PriorityUtilities;
import com.firebase.client.utilities.Validation;
import com.firebase.client.utilities.encoding.JsonHelpers;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class OnDisconnect {
    private Path path;
    private Repo repo;

    OnDisconnect(Repo repo, Path path) {
        this.repo = repo;
        this.path = path;
    }

    public void setValue(Object value) {
        onDisconnectSetInternal(value, PriorityUtilities.NullPriority(), null);
    }

    public void setValue(Object value, String priority) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(priority), null);
    }

    public void setValue(Object value, double priority) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(Double.valueOf(priority)), null);
    }

    public void setValue(Object value, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.NullPriority(), listener);
    }

    public void setValue(Object value, String priority, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(priority), listener);
    }

    public void setValue(Object value, double priority, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(Double.valueOf(priority)), listener);
    }

    public void setValue(Object value, Map priority, CompletionListener listener) {
        onDisconnectSetInternal(value, PriorityUtilities.parsePriority(priority), listener);
    }

    private void onDisconnectSetInternal(Object value, Node priority, final CompletionListener onComplete) {
        Validation.validateWritablePath(this.path);
        ValidationPath.validateWithObject(this.path, value);
        try {
            Object bouncedValue = JsonHelpers.getMapper().convertValue(value, Object.class);
            Validation.validateWritableObject(bouncedValue);
            final Node node = NodeUtilities.NodeFromJSON(bouncedValue, priority);
            this.repo.scheduleNow(new Runnable() {
                public void run() {
                    OnDisconnect.this.repo.onDisconnectSetValue(OnDisconnect.this.path, node, onComplete);
                }
            });
        } catch (IllegalArgumentException e) {
            throw new FirebaseException("Failed to parse to snapshot", e);
        }
    }

    public void updateChildren(Map<String, Object> children) {
        updateChildren(children, null);
    }

    public void updateChildren(final Map<String, Object> children, final CompletionListener listener) {
        ValidationPath.validateWithObject(this.path, children);
        final Map<ChildKey, Node> parsedUpdate = new HashMap();
        for (Entry<String, Object> entry : children.entrySet()) {
            Validation.validateWritableObject(entry.getValue());
            parsedUpdate.put(ChildKey.fromString((String) entry.getKey()), NodeUtilities.NodeFromJSON(entry.getValue()));
        }
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                OnDisconnect.this.repo.onDisconnectUpdate(OnDisconnect.this.path, parsedUpdate, listener, children);
            }
        });
    }

    public void removeValue() {
        setValue(null);
    }

    public void removeValue(CompletionListener listener) {
        setValue(null, listener);
    }

    public void cancel() {
        cancel(null);
    }

    public void cancel(final CompletionListener listener) {
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                OnDisconnect.this.repo.onDisconnectCancel(OnDisconnect.this.path, listener);
            }
        });
    }
}
