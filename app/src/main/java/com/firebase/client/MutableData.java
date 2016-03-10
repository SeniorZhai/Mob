package com.firebase.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.firebase.client.core.Path;
import com.firebase.client.core.SnapshotHolder;
import com.firebase.client.core.ValidationPath;
import com.firebase.client.snapshot.ChildKey;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.client.snapshot.NamedNode;
import com.firebase.client.snapshot.Node;
import com.firebase.client.snapshot.NodeUtilities;
import com.firebase.client.snapshot.PriorityUtilities;
import com.firebase.client.utilities.Validation;
import com.firebase.client.utilities.encoding.JsonHelpers;
import io.fabric.sdk.android.BuildConfig;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MutableData {
    private final SnapshotHolder holder;
    private final Path prefixPath;

    public MutableData(Node node) {
        this(new SnapshotHolder(node), new Path(BuildConfig.FLAVOR));
    }

    private MutableData(SnapshotHolder holder, Path path) {
        this.holder = holder;
        this.prefixPath = path;
        ValidationPath.validateWithObject(this.prefixPath, getValue());
    }

    Node getNode() {
        return this.holder.getNode(this.prefixPath);
    }

    public boolean hasChildren() {
        Node node = getNode();
        return (node.isLeafNode() || node.isEmpty()) ? false : true;
    }

    public boolean hasChild(String path) {
        return !getNode().getChild(new Path(path)).isEmpty();
    }

    public MutableData child(String path) {
        Validation.validatePathString(path);
        return new MutableData(this.holder, this.prefixPath.child(new Path(path)));
    }

    public long getChildrenCount() {
        return (long) getNode().getChildCount();
    }

    public Iterable<MutableData> getChildren() {
        Node node = getNode();
        if (node.isEmpty() || node.isLeafNode()) {
            return new Iterable<MutableData>() {
                public Iterator<MutableData> iterator() {
                    return new Iterator<MutableData>() {
                        public boolean hasNext() {
                            return false;
                        }

                        public MutableData next() {
                            throw new NoSuchElementException();
                        }

                        public void remove() {
                            throw new UnsupportedOperationException("remove called on immutable collection");
                        }
                    };
                }
            };
        }
        final Iterator<NamedNode> iter = IndexedNode.from(node).iterator();
        return new Iterable<MutableData>() {
            public Iterator<MutableData> iterator() {
                return new Iterator<MutableData>() {
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    public MutableData next() {
                        return new MutableData(MutableData.this.holder, MutableData.this.prefixPath.child(((NamedNode) iter.next()).getName()));
                    }

                    public void remove() {
                        throw new UnsupportedOperationException("remove called on immutable collection");
                    }
                };
            }
        };
    }

    @Deprecated
    public MutableData getParent() {
        Path path = this.prefixPath.getParent();
        if (path != null) {
            return new MutableData(this.holder, path);
        }
        return null;
    }

    public String getKey() {
        return this.prefixPath.getBack() != null ? this.prefixPath.getBack().asString() : null;
    }

    public Object getValue() {
        return getNode().getValue();
    }

    public <T> T getValue(Class<T> valueType) {
        try {
            return JsonHelpers.getMapper().convertValue(getNode().getValue(), (Class) valueType);
        } catch (IllegalArgumentException e) {
            throw new FirebaseException("Failed to bounce to type", e);
        }
    }

    public <T> T getValue(GenericTypeIndicator<T> t) {
        try {
            return JsonHelpers.getMapper().convertValue(getNode().getValue(), (TypeReference) t);
        } catch (IllegalArgumentException e) {
            throw new FirebaseException("Failed to bounce to type", e);
        }
    }

    public void setValue(Object value) throws FirebaseException {
        try {
            ValidationPath.validateWithObject(this.prefixPath, value);
            Object bouncedValue = JsonHelpers.getMapper().convertValue(value, Object.class);
            Validation.validateWritableObject(bouncedValue);
            this.holder.update(this.prefixPath, NodeUtilities.NodeFromJSON(bouncedValue));
        } catch (IllegalArgumentException e) {
            throw new FirebaseException("Failed to parse to snapshot", e);
        }
    }

    public void setPriority(Object priority) {
        this.holder.update(this.prefixPath, getNode().updatePriority(PriorityUtilities.parsePriority(priority)));
    }

    public Object getPriority() {
        return getNode().getPriority().getValue();
    }

    public boolean equals(Object o) {
        return (o instanceof MutableData) && this.holder.equals(((MutableData) o).holder) && this.prefixPath.equals(((MutableData) o).prefixPath);
    }

    public String toString() {
        ChildKey front = this.prefixPath.getFront();
        return "MutableData { key = " + (front != null ? front.asString() : "<none>") + ", value = " + this.holder.getRootNode().getValue(true) + " }";
    }
}
